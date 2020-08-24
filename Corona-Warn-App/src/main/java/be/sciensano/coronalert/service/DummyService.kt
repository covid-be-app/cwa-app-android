package be.sciensano.coronalert.service

import be.sciensano.coronalert.storage.dummyTestRequestsToSend
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.storage.LocalData
import kotlinx.coroutines.delay
import timber.log.Timber
import java.util.concurrent.TimeUnit

@Suppress("MagicNumber")
class DummyService(
    private val webRequestBuilder: WebRequestBuilder
) {
    private val dummyToken = "000000000000000|2020-01-01"

    fun initScenario() {
        if ((0..100).random() < 20 && LocalData.dummyTestRequestsToSend() == -1) {
            val random = (2..5).random()
            LocalData.dummyTestRequestsToSend(random)
        }
    }

    suspend fun sendDummyRequestsIfNeeded() {
        if (LocalData.dummyTestRequestsToSend() > 0) {
            fakeTestRequest()
            if (LocalData.dummyTestRequestsToSend() == 0) {
                fakeKeysUpload()
            }
        }
    }

    private suspend fun fakeTestRequest() {
        webRequestBuilder.beAsyncGetTestResult(dummyToken)
        LocalData.dummyTestRequestsToSend(LocalData.dummyTestRequestsToSend() - 1)
    }

    private suspend fun fakeKeysUpload() {
        ignoreExceptions { webRequestBuilder.beAsyncAckTestResult(dummyToken) }

        val delayInSeconds = (5..15).random()

        delay(TimeUnit.SECONDS.toMillis(delayInSeconds.toLong()))

        ignoreExceptions { webRequestBuilder.beAsyncDummySubmitKeysToServer() }
    }


    private suspend fun ignoreExceptions(body: suspend () -> Unit) {
        try {
            body.invoke()
        } catch (e: Exception) {
            Timber.d(e, "Ignoring dummy request exception")
        }
    }
}
