package be.sciensano.coronalert.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import be.sciensano.coronalert.service.DummyService
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler.stop
import timber.log.Timber

/**
 * Periodic background noise worker
 *
 * @see BackgroundWorkScheduler
 */
class BackgroundNoisePeriodicWorker(
    val context: Context,
    workerParams: WorkerParameters
) :
    CoroutineWorker(context, workerParams) {

    companion object {
        private val TAG: String? = BackgroundNoisePeriodicWorker::class.simpleName
    }

    /**
     * Work execution
     *
     * @return Result
     *
     */
    override suspend fun doWork(): Result {
        Timber.d("Background job started. Run attempt: $runAttemptCount")

        val result = Result.success()
        DummyService(WebRequestBuilder.getInstance()).initScenario()

        return result
    }

    private fun stopWorker() {
        BackgroundWorkScheduler.WorkType.BACKGROUND_NOISE_PERIODIC_WORK.stop()
    }
}
