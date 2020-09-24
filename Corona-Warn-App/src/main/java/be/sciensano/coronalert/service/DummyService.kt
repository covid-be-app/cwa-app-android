package be.sciensano.coronalert.service

import be.sciensano.coronalert.storage.dummyTestRequestsToSend
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.storage.LocalData
import kotlinx.coroutines.delay
import timber.log.Timber
import java.util.concurrent.TimeUnit

@Suppress("MagicNumber")
object DummyService {
    private const val dummyToken = "000000000000000|2020-01-01"
    private val webRequestBuilder = WebRequestBuilder.getInstance()

    suspend fun sendDummyRequestsIfNeeded() {
        Timber.d("init dummy scenario")
        if (LocalData.dummyTestRequestsToSend() == -1) {
            if ((0..60).random() == 0) {
                val random = (2 until 5).random()
                LocalData.dummyTestRequestsToSend(random)
            }
        } else {
            fakeTestRequest()
            if (LocalData.dummyTestRequestsToSend() == 0) {
                fakeKeysUpload()
            }
        }
    }

    suspend fun fakeAckRequest() {
        Timber.d("fake ack request")
        webRequestBuilder.beAsyncAckTestResult(dummyToken)
    }

    private suspend fun fakeTestRequest() {
        Timber.d("fake test request")
        webRequestBuilder.beAsyncGetTestResult(dummyToken)
        fakeAckRequest()
        LocalData.dummyTestRequestsToSend(LocalData.dummyTestRequestsToSend() - 1)
    }

    private suspend fun fakeKeysUpload() {
        delay(TimeUnit.SECONDS.toMillis((5 until 15).random().toLong()))
        fakeTestRequest()

        delay(TimeUnit.SECONDS.toMillis((5 until 15).random().toLong()))
        ignoreExceptions { webRequestBuilder.beAsyncDummySubmitKeysToServer() }

        LocalData.dummyTestRequestsToSend(-1)
    }


    private suspend fun ignoreExceptions(body: suspend () -> Unit) {
        try {
            body.invoke()
        } catch (e: Exception) {
            Timber.d(e, "Ignoring dummy request exception")
        }
    }
}
