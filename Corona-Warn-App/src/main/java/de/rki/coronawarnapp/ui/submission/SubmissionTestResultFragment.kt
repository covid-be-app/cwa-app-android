package de.rki.coronawarnapp.ui.submission

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import be.sciensano.coronalert.http.responses.DynamicTexts
import be.sciensano.coronalert.http.responses.Explanation
import be.sciensano.coronalert.http.responses.iconToResMap
import be.sciensano.coronalert.ui.DynamicTextsViewModel
import be.sciensano.coronalert.util.DateUtil
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestResultBinding
import de.rki.coronawarnapp.databinding.ViewSimpleStepEntryBinding
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.observeEvent
import kotlinx.android.synthetic.main.include_step_entry_simple_body.view.*
import kotlinx.android.synthetic.main.include_submission_test_result.view.*
import java.text.DateFormat
import java.util.Locale


/**
 * A simple [Fragment] subclass.
 */
class SubmissionTestResultFragment : Fragment() {
    companion object {
        private val TAG: String? = SubmissionTanFragment::class.simpleName
    }

    private val submissionViewModel: SubmissionViewModel by activityViewModels()
    private val tracingViewModel: TracingViewModel by activityViewModels()
    private val dynamicTextsViewModel: DynamicTextsViewModel by activityViewModels()

    private var _binding: FragmentSubmissionTestResultBinding? = null
    private val binding: FragmentSubmissionTestResultBinding get() = _binding!!

    // Overrides default back behaviour
    private val backCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().doNavigate(
                    SubmissionTestResultFragmentDirections.actionSubmissionResultFragmentToMainFragment()
                )
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // get the binding reference by inflating it with the current layout
        _binding = FragmentSubmissionTestResultBinding.inflate(inflater)
        binding.submissionViewModel = submissionViewModel
        binding.lifecycleOwner = this
        // registers callback when the os level back is pressed
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun addExplanation(
        sections: List<Explanation>,
        texts: Map<String, Map<String, String>>
    ) {
        binding.submissionTestResultExplanationDynamic.removeAllViewsInLayout()

        val inflater = LayoutInflater.from(binding.submissionTestResultExplanationDynamic.context)
        sections.forEachIndexed { i, section ->
            val newViewBinding = ViewSimpleStepEntryBinding.inflate(
                inflater,
                binding.submissionTestResultExplanationDynamic,
                true
            )

            val paragraphs = section.paragraphs?.joinToString("\n\n") { paragraph ->
                DynamicTexts.getText(paragraph, texts, Locale.getDefault().language)
            }

            newViewBinding.isFinal = sections.size == i + 1
            newViewBinding.title =
                DynamicTexts.getText(section.title, texts, Locale.getDefault().language)
            newViewBinding.text =
                "${
                    DynamicTexts.getText(
                        section.text,
                        texts,
                        Locale.getDefault().language
                    )
                }${paragraphs ?: ""}"
            newViewBinding.icon =
                ContextCompat.getDrawable(
                    requireContext(),
                    iconToResMap[section.icon] ?: R.drawable.circle
                )
        }
    }

    private fun navigateToMainScreen() =
        findNavController().doNavigate(
            SubmissionTestResultFragmentDirections.actionSubmissionResultFragmentToMainFragment()
        )

    private fun buildErrorDialog(exception: CwaWebException): DialogHelper.DialogInstance {
        return when (exception) {
            is CwaServerError -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                getString(
                    R.string.submission_error_dialog_web_generic_network_error_body,
                    exception.statusCode
                ),
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
                ::navigateToMainScreen
            )
            is CwaClientError -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                getString(
                    R.string.submission_error_dialog_web_generic_network_error_body,
                    exception.statusCode
                ),
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
                ::navigateToMainScreen
            )
            else -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                R.string.submission_error_dialog_web_generic_error_body,
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
                ::navigateToMainScreen
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()

        submissionViewModel.uiStateError.observeEvent(viewLifecycleOwner) {
            DialogHelper.showDialog(buildErrorDialog(it))
        }

        submissionViewModel.deviceUiState.observe(viewLifecycleOwner, Observer { uiState ->
            if (uiState == DeviceUIState.PAIRED_REDEEMED) {
                showRedeemedTokenWarningDialog()
            }
            dynamicTextsViewModel.getDynamicTexts(requireContext())
        })

        dynamicTextsViewModel.dynamicTexts.observe(viewLifecycleOwner, Observer {
            if (submissionViewModel.deviceUiState.value == DeviceUIState.PAIRED_NEGATIVE) {
                addExplanation(it.structure.negativeTestResult.explanation, it.texts)
            }
            if (submissionViewModel.deviceUiState.value == DeviceUIState.PAIRED_POSITIVE) {
                addExplanation(it.structure.positiveTestResult.explanation, it.texts)
            }
        })


        val uiCode = submissionViewModel.getMobileTestIduiCode()
        val t0 = submissionViewModel.getMobileTestIdt0()
        if (uiCode != null && t0 != null) {
            val date =
                DateFormat.getDateInstance(DateFormat.FULL)
                    .format(DateUtil.parseServerDate(t0).toDate())

            binding.submissionTestResultContent.submission_test_result_pending_steps.simple_step_entry_body.text =
                getString(
                    R.string.submission_test_result_steps_added_body_with_code,
                    date,
                    uiCode
                )
        }
    }

    private fun showRedeemedTokenWarningDialog() {
        val dialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.submission_error_dialog_web_tan_redeemed_title,
            R.string.submission_error_dialog_web_tan_redeemed_body,
            R.string.submission_error_dialog_web_tan_redeemed_button_positive
        )

        DialogHelper.showDialog(dialog)
    }

    override fun onResume() {
        super.onResume()
        binding.submissionTestResultContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        submissionViewModel.refreshDeviceUIState()
        tracingViewModel.refreshIsTracingEnabled()
    }

    private fun setButtonOnClickListener() {
        binding.submissionTestResultButtonPendingRefresh.setOnClickListener {
            submissionViewModel.refreshDeviceUIState()
            binding.submissionTestResultCard.testResultCard
                .sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        }

        binding.submissionTestResultButtonPendingRemoveTest.setOnClickListener {
            removeTestAfterConfirmation()
        }

        binding.submissionTestResultButtonNegativeRemoveTest.setOnClickListener {
            removeTestAfterConfirmation()
        }

        binding.submissionTestResultButtonPositiveContinue.setOnClickListener {
            continueIfTracingEnabled()
        }

        binding.submissionTestResultButtonInvalidRemoveTest.setOnClickListener {
            removeTestAfterConfirmation()
        }

        binding.submissionTestResultHeader.headerButtonBack.buttonIcon.setOnClickListener {
            findNavController().doNavigate(
                SubmissionTestResultFragmentDirections.actionSubmissionResultFragmentToMainFragment()
            )
        }
    }

    private fun continueIfTracingEnabled() {
        if (tracingViewModel.isTracingEnabled.value != true) {
            val tracingRequiredDialog = DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_test_result_dialog_tracing_required_title,
                R.string.submission_test_result_dialog_tracing_required_message,
                R.string.submission_test_result_dialog_tracing_required_button
            )
            DialogHelper.showDialog(tracingRequiredDialog)
            return
        }

        findNavController().doNavigate(
            SubmissionTestResultFragmentDirections
                .actionSubmissionResultFragmentToSubmissionResultPositiveOtherWarningFragment()
        )
    }

    private fun removeTestAfterConfirmation() {
        val removeTestDialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.submission_test_result_dialog_remove_test_title,
            R.string.submission_test_result_dialog_remove_test_message,
            R.string.submission_test_result_dialog_remove_test_button_positive,
            R.string.submission_test_result_dialog_remove_test_button_negative,
            positiveButtonFunction = {
                submissionViewModel.deregisterTestFromDevice()
                findNavController().doNavigate(
                    SubmissionTestResultFragmentDirections.actionSubmissionResultFragmentToMainFragment()
                )
            }
        )
        DialogHelper.showDialog(removeTestDialog).apply {
            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getColor(R.color.colorTextSemanticRed))
        }
    }
}
