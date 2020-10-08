package de.rki.coronawarnapp.worker

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import be.sciensano.coronalert.service.DummyService
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.notification.NotificationHelper
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.TimeAndDateExtensions
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler.stop
import timber.log.Timber
import be.sciensano.coronalert.service.submission.SubmissionService as BeSubmissionService

/**
 * Diagnosis Test Result Periodic retrieval
 *
 * @see BackgroundWorkScheduler
 */
class DiagnosisTestResultRetrievalPeriodicWorker(
    val context: Context,
    workerParams: WorkerParameters
) :
    CoroutineWorker(context, workerParams) {

    companion object {
        private val TAG: String? = DiagnosisTestResultRetrievalPeriodicWorker::class.simpleName
    }

    /**
     * Work execution
     *
     *
     * @return Result
     *
     * @see LocalData.isTestResultNotificationSent
     * @see LocalData.initialPollingForTestResultTimeStamp
     */
    override suspend fun doWork(): Result {

        Timber.d("Background job started. Run attempt: $runAttemptCount")
        BackgroundWorkHelper.sendDebugNotification(
            "TestResult Executing: Start", "TestResult started. Run attempt: $runAttemptCount "
        )

        if (runAttemptCount > BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD) {
            Timber.d("Background job failed after $runAttemptCount attempts. Rescheduling")

            BackgroundWorkHelper.sendDebugNotification(
                "TestResult Executing: Failure", "TestResult failed with $runAttemptCount attempts"
            )

            BackgroundWorkScheduler.scheduleDiagnosisKeyPeriodicWork()
            return Result.failure()
        }
        var result = Result.success()
        try {
            if (LocalData.registrationToken() != null && !LocalData.isTestResultNotificationSent()) {
                if (TimeAndDateExtensions.calculateDays(
                        LocalData.initialPollingForTestResultTimeStamp(),
                        System.currentTimeMillis()
                    ) < BackgroundConstants.POLLING_VALIDITY_MAX_DAYS
                ) {
                    val testResult = BeSubmissionService.asyncRequestTestResult()
                    DummyService.fakeAckRequest()
                    initiateNotification(TestResult.fromInt(testResult.result))
                } else {
                    BeSubmissionService.deleteRegistrationToken()
                }
            } else {
                DummyService.sendDummyRequestsIfNeeded()
            }
        } catch (e: Exception) {
            Timber.e(e)
            result = Result.retry()
        }

        BackgroundWorkHelper.sendDebugNotification(
            "TestResult Executing: End", "TestResult result: $result "
        )

        return result
    }

    /**
     * Notification Initiation
     *
     * If the returned Test Result is Negative, Positive or Invalid
     * The Background polling  will be stopped
     * and a notification is shown, but only if the App is not in foreground
     *
     * @see LocalData.isTestResultNotificationSent
     * @see LocalData.initialPollingForTestResultTimeStamp
     * @see TestResult
     */
    private fun initiateNotification(testResult: TestResult) {
        if (testResult == TestResult.NEGATIVE || testResult == TestResult.POSITIVE ||
            testResult == TestResult.INVALID
        ) {
            if (!CoronaWarnApplication.isAppInForeground) {
                NotificationHelper.sendNotification(
                    CoronaWarnApplication.getAppContext()
                        .getString(R.string.notification_name),
                    CoronaWarnApplication.getAppContext()
                        .getString(R.string.notification_body),
                    NotificationCompat.PRIORITY_HIGH
                )
            }
            LocalData.isTestResultNotificationSent(true)
        }
    }

    /**
     * Stops the Background Polling worker
     *
     * @see LocalData.initialPollingForTestResultTimeStamp
     * @see BackgroundWorkScheduler.stop
     */
    private fun stopWorker() {
        LocalData.initialPollingForTestResultTimeStamp(0L)
        BackgroundWorkScheduler.WorkType.DIAGNOSIS_TEST_RESULT_PERIODIC_WORKER.stop()

        BackgroundWorkHelper.sendDebugNotification(
            "TestResult Stopped", "TestResult Stopped"
        )
    }
}
