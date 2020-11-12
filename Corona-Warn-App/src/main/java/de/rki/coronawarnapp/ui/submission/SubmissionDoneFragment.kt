package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import be.sciensano.coronalert.http.responses.OtherInformation
import be.sciensano.coronalert.http.responses.PleaseNote
import be.sciensano.coronalert.http.responses.iconToResMap
import be.sciensano.coronalert.ui.DynamicTextsViewModel
import com.google.android.gms.common.api.ApiException
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionDoneBinding
import de.rki.coronawarnapp.databinding.IncludeRiskDetailsBehaviorRowBinding
import de.rki.coronawarnapp.databinding.ViewBulletPointEntryBinding
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.risk.RiskLevelConstants
import de.rki.coronawarnapp.ui.onboarding.OnboardingActivity
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.util.DataRetentionHelper
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * The [SubmissionDoneFragment] displays information to a user that submitted his exposure keys
 */
class SubmissionDoneFragment : Fragment() {

    private var _binding: FragmentSubmissionDoneBinding? = null
    private val binding: FragmentSubmissionDoneBinding get() = _binding!!
    private val submissionViewModel: SubmissionViewModel by activityViewModels()
    private val dynamicTextsViewModel: DynamicTextsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // get the binding reference by inflating it with the current layout
        _binding = FragmentSubmissionDoneBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()

        lifecycleScope.launch {
            try {
                val isTracingEnabled = InternalExposureNotificationClient.asyncIsEnabled()
                // only stop tracing if it is currently enabled
                if (isTracingEnabled) {
                    InternalExposureNotificationClient.asyncStop()
                    BackgroundWorkScheduler.stopWorkScheduler()
                }
            } catch (apiException: ApiException) {
                apiException.report(
                    ExceptionCategory.EXPOSURENOTIFICATION,
                    SubmissionDoneFragment::class.simpleName,
                    null
                )
            }
            withContext(Dispatchers.IO) {
                DataRetentionHelper.clearAllLocalData(requireContext())
            }
        }

        dynamicTextsViewModel.dynamicTexts.observe(viewLifecycleOwner, Observer {
            addPleaseNote(it.structure.thankYou.pleaseNote, it.texts)
            addFurtherInfos(it.structure.thankYou.otherInformation, it.texts)
        })
    }

    override fun onResume() {
        super.onResume()
        binding.submissionDoneContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun addPleaseNote(sections: List<PleaseNote>, texts: Map<String, Map<String, String>>) {
        binding.submissionDoneBehaviorDynamic.removeAllViewsInLayout()
        val inflater = LayoutInflater.from(binding.submissionDoneBehaviorDynamic.context)
        sections.forEach { section ->
            val newViewBinding = IncludeRiskDetailsBehaviorRowBinding.inflate(
                inflater,
                binding.submissionDoneBehaviorDynamic,
                true
            )
            newViewBinding.body =
                (texts[Locale.getDefault().language] ?: texts["en"])?.get(section.text) ?: ""
            newViewBinding.riskLevel = RiskLevelConstants.INCREASED_RISK
            newViewBinding.icon =
                ContextCompat.getDrawable(
                    requireContext(),
                    iconToResMap[section.icon] ?: R.drawable.circle
                )
        }
    }

    private fun addFurtherInfos(
        sections: List<OtherInformation>,
        texts: Map<String, Map<String, String>>
    ) {
        binding.submissionDoneFurtherInfoDynamic.removeAllViewsInLayout()
        val inflater = LayoutInflater.from(binding.submissionDoneFurtherInfoDynamic.context)
        sections.flatMap { it.paragraphs }.forEach { paragraph ->
            val newViewBinding = ViewBulletPointEntryBinding.inflate(
                inflater,
                binding.submissionDoneFurtherInfoDynamic,
                true
            )

            newViewBinding.bulletPointContent.text =
                (texts[Locale.getDefault().language] ?: texts["en"])?.get(paragraph) ?: ""

        }
    }

    private fun showResetDialog() {
        DialogHelper.showDialog(
            DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_done_reset_dialog_title,
                R.string.submission_done_reset_dialog_body,
                R.string.submission_done_reset_dialog_button_positive,
                null,
                false,
                {
                    OnboardingActivity.start(requireContext())
                    activity?.finish()
                },
                {}
            ))
    }

    private fun setButtonOnClickListener() {
        binding.submissionDoneHeader.headerButtonBack.buttonIcon.setOnClickListener {
            showResetDialog()

        }
        binding.submissionDoneButtonDone.setOnClickListener {
            showResetDialog()
        }
    }
}
