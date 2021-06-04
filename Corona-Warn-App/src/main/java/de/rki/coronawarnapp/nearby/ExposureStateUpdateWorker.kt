package de.rki.coronawarnapp.nearby

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.common.api.ApiException
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.transaction.RiskLevelTransaction
import timber.log.Timber

class ExposureStateUpdateWorker(val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    companion object {
        private val TAG = ExposureStateUpdateWorker::class.simpleName
    }

    override suspend fun doWork(): Result {
        try {
            RiskLevelTransaction.start()
            Timber.v("risk level calculation triggered")
        } catch (e: ApiException) {
            e.report(ExceptionCategory.EXPOSURENOTIFICATION)
        } catch (e: TransactionException) {
            e.report(ExceptionCategory.INTERNAL)
        }

        return Result.success()
    }
}
