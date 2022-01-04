package de.rki.coronawarnapp.ui

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ApiException
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.onboarding.OnboardingActivity
import de.rki.coronawarnapp.update.UpdateChecker
import de.rki.coronawarnapp.util.DataRetentionHelper
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LauncherActivity : AppCompatActivity() {
    companion object {
        private val TAG: String? = LauncherActivity::class.simpleName
        private const val PCR_LENGTH: Int = 16
    }

    private lateinit var updateChecker: UpdateChecker

    override fun onResume() {
        super.onResume()

        updateChecker = UpdateChecker(this)
        lifecycleScope.launch {
            updateChecker.checkForUpdate()
        }
    }

    fun navigateToActivities() {
        val uri: Uri? = intent?.data

        if (uri != null && isPcrValid(uri)) {
            startMainActivityWithTestActivivation(uri.toString())
        } else {
            when {
                LocalData.numberOfSuccessfulSubmissions() > 0 -> {
                    lifecycleScope.launch {
                        try {
                            val isTracingEnabled =
                                InternalExposureNotificationClient.asyncIsEnabled()
                            // only stop tracing if it is currently enabled
                            if (isTracingEnabled) {
                                InternalExposureNotificationClient.asyncStop()
                                BackgroundWorkScheduler.stopWorkScheduler()
                            }
                        } catch (apiException: ApiException) {
                            apiException.report(
                                ExceptionCategory.EXPOSURENOTIFICATION,
                                TAG,
                                null
                            )
                        }
                        withContext(Dispatchers.IO) {
                            DataRetentionHelper.clearAllLocalData(this@LauncherActivity)
                        }
                        startOnboardingActivity()
                    }
                }
                LocalData.isOnboarded() -> {
                    startMainActivity()

                }
                else -> {
                    startOnboardingActivity()
                }
            }
        }
    }

    private fun isPcrValid(data: Uri?): Boolean {
        val pcr = data?.getQueryParameter("pcr")
        val authorizedCharacters = (('a'..'z') + ('A'..'Z') + ('0'..'9'))

        return (pcr != null && pcr.length == PCR_LENGTH && pcr.all {
            authorizedCharacters.contains(
                it
            )
        } && data.queryParameterNames.size == 1)
    }

    private fun startOnboardingActivity() {
        OnboardingActivity.start(this)
        this.overridePendingTransition(0, 0)
        finish()
    }

    private fun startMainActivity() {
        MainActivity.start(this)
        this.overridePendingTransition(0, 0)
        finish()
    }

    private fun startMainActivityWithTestActivivation(url: String) {
        MainActivity.startForTestActivation(this, url)
        this.overridePendingTransition(0, 0)
        finish()
    }
}
