package be.sciensano.coronalert.ui.covicode

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentCovicodeBinding
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.http.ForbiddenException
import de.rki.coronawarnapp.nearby.InternalExposureNotificationPermissionHelper
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.observeEvent

private const val COVICODE_DIGITS_NUMBER = 12

class CovicodeFragment : Fragment(), InternalExposureNotificationPermissionHelper.Callback {

    private var _binding: FragmentCovicodeBinding? = null
    private val binding: FragmentCovicodeBinding get() = _binding!!

    private val viewModel: CovicodeViewModel by activityViewModels()
    private val tracingViewModel: TracingViewModel by activityViewModels()

    private lateinit var internalExposureNotificationPermissionHelper:
            InternalExposureNotificationPermissionHelper

    override fun onFailure(exception: Exception?) {
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        internalExposureNotificationPermissionHelper =
            InternalExposureNotificationPermissionHelper(this, this)

        // get the binding reference by inflating it with the current layout
        _binding = FragmentCovicodeBinding.inflate(inflater)
        binding.submissionViewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.submissionError.observeEvent(viewLifecycleOwner) {
            DialogHelper.showDialog(buildErrorDialog(it))
        }

        viewModel.submissionState.observe(viewLifecycleOwner, Observer {
            if (it == ApiRequestState.SUCCESS) {
                navigateToSubmissionDoneFragment()
            }
        })

        setButtonOnClickListener()

    }

    private fun buildErrorDialog(exception: CwaWebException): DialogHelper.DialogInstance {
        return when (exception) {
            is BadRequestException -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_paring_invalid_title,
                R.string.submission_error_dialog_web_paring_invalid_body,
                R.string.submission_error_dialog_web_paring_invalid_button_positive,
                null,
                true,
            )
            is ForbiddenException -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_tan_invalid_title,
                R.string.submission_error_dialog_web_tan_invalid_body,
                R.string.submission_error_dialog_web_tan_invalid_button_positive,
                null,
                true,
            )
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
            )
            else -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                R.string.submission_error_dialog_web_generic_error_body,
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
            )
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        this.internalExposureNotificationPermissionHelper.onResolutionComplete(
            requestCode,
            resultCode
        )
    }


    override fun onResume() {
        super.onResume()
        tracingViewModel.refreshIsTracingEnabled()
        binding.covicodeContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.covicodeHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }
        binding.covicodeButtonNext.setOnClickListener {

            DialogHelper.showDialog(
                DialogHelper.DialogInstance(
                    requireActivity(),
                    R.string.submission_intro_symptoms_dialog_title,
                    R.string.submission_intro_symptoms_dialog_body,
                    R.string.submission_intro_symptoms_dialog_positive,
                    R.string.submission_intro_symptoms_dialog_negative,
                    true,
                    {
                        findNavController().doNavigate(
                            CovicodeFragmentDirections.actionCovicodeFragmentToCovicodeSymptomsFragment()
                        )
                    },
                    {
                        val alertDialog: AlertDialog = requireContext().let {
                            val viewInflated: View = LayoutInflater.from(context)
                                .inflate(R.layout.view_covicode_dialog, getView() as ViewGroup?, false)

                            val input = viewInflated.findViewById<View>(R.id.covicode_input) as EditText

                            val builder = AlertDialog.Builder(it)
                            builder.apply {
                                setTitle(R.string.covicode_dialog_title)
                                setView(viewInflated)
                                setCancelable(true)
                                setPositiveButton(R.string.covicode_dialog_positive) { _, _ ->
                                    checkCovicodeAndSend(input.text.toString())
                                }
                                setNegativeButton(R.string.covicode_dialog_negative) { dialog, _ ->
                                    dialog.cancel()
                                }

                            }
                            builder.create()
                        }
                        alertDialog.show()
                    }
                ))
        }
    }

    private fun checkCovicodeAndSend(code: String) = run {
        if(code.length == COVICODE_DIGITS_NUMBER && code.isDigitsOnly()) {
            viewModel.setCovicode(code)
            continueIfTracingEnabled()
        } else {
            DialogHelper.showDialog(DialogHelper.DialogInstance(
                requireActivity(),
                R.string.covicode_error,
                R.string.covicode_dialog_text,
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true
            ))
        }
    }


    private fun navigateToSubmissionDoneFragment() =
        findNavController().doNavigate(
            CovicodeFragmentDirections.actionCovicodeFragmentToSubmissionDoneFragment()
        )

    override fun onKeySharePermissionGranted(keys: List<TemporaryExposureKey>) {
        super.onKeySharePermissionGranted(keys)
        if (keys.isNotEmpty()) {
            viewModel.beSubmitDiagnosisKeysForCovicode(keys)
        } else {
            viewModel.submitWithNoDiagnosisKeys()
            navigateToSubmissionDoneFragment()
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

        internalExposureNotificationPermissionHelper.requestPermissionToShareKeys()
    }
}
