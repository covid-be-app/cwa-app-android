package de.rki.coronawarnapp.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import be.sciensano.coronalert.http.responses.DynamicNews
import be.sciensano.coronalert.http.responses.DynamicTexts
import be.sciensano.coronalert.http.responses.PositiveTestResultCardExplanation
import be.sciensano.coronalert.http.responses.iconToResMap
import be.sciensano.coronalert.ui.DynamicTextsViewModel
import be.sciensano.coronalert.ui.LinkTestActivity
import be.sciensano.coronalert.ui.StatisticsViewModel
import be.sciensano.coronalert.ui.submission.SubmissionTestRequestViewModel
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentMainBinding
import de.rki.coronawarnapp.databinding.IncludeRiskDetailsBehaviorRowBinding
import de.rki.coronawarnapp.risk.RiskLevelConstants
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.timer.TimerHelper
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.ExternalActionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.DecimalFormat
import java.util.Date
import java.util.Locale

/**
 * After the user has finished the onboarding this fragment will be the heart of the application.
 * Three ViewModels are needed that this fragment shows all relevant information to the user.
 * Also the Menu is set here.
 *
 * @see tracingViewModel
 * @see settingsViewModel
 * @see submissionViewModel
 * @see PopupMenu
 */
class MainFragment : Fragment() {

    companion object {
        private val TAG: String? = MainFragment::class.simpleName
    }

    private val tracingViewModel: TracingViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val submissionViewModel: SubmissionViewModel by activityViewModels()
    private val statisticsViewModel: StatisticsViewModel by activityViewModels()
    private val testRequestViewModel: SubmissionTestRequestViewModel by activityViewModels()
    private val dynamicTextsViewModel: DynamicTextsViewModel by activityViewModels()

    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater)
        binding.tracingViewModel = tracingViewModel
        binding.settingsViewModel = settingsViewModel
        binding.submissionViewModel = submissionViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
        setContentDescription()

        setEnvironmentDebugText()

//        showOneTimeTracingExplanationDialog()
        observeStatistics()

        val url = activity?.intent?.extras?.getString(MainActivity.URL_ARGUMENT)
        if (url != null) {
            addTestForConfirmation(url)
        }

        dynamicTextsViewModel.getDynamicTexts(requireContext())
        dynamicTextsViewModel.dynamicTexts.observe(viewLifecycleOwner, Observer {
            addPositiveTestExplanation(it.structure.positiveTestResultCard.explanation, it.texts)
        })

        dynamicTextsViewModel.getDynamicNews(requireContext())
        dynamicTextsViewModel.dynamicNews.observe(viewLifecycleOwner, Observer {
            it?.let { news ->
                it.structure.news.explanation.getOrNull(0)?.let {
                    binding.news.newsCard.visibility = View.VISIBLE
                    binding.news.newsCardTextTitle.text = DynamicNews.getText(it.title, news.texts, Locale.getDefault().language)
                    binding.news.newsCardTextBody.text = DynamicNews.getText(it.text, news.texts, Locale.getDefault().language)
                }
            }

        })
    }

    private fun addPositiveTestExplanation(
        sections: List<PositiveTestResultCardExplanation>,
        texts: Map<String, Map<String, String>>
    ) {
        binding.submissionStatusCardPositiveResultExplanationDynamic.removeAllViewsInLayout()

        val inflater =
            LayoutInflater.from(binding.submissionStatusCardPositiveResultExplanationDynamic.context)
        sections.forEach { section ->
            val newViewBinding = IncludeRiskDetailsBehaviorRowBinding.inflate(
                inflater,
                binding.submissionStatusCardPositiveResultExplanationDynamic,
                true
            )

            newViewBinding.riskLevel = RiskLevelConstants.INCREASED_RISK
            newViewBinding.body =
                DynamicTexts.getText(section.text, texts, Locale.getDefault().language)
            newViewBinding.icon =
                ContextCompat.getDrawable(
                    requireContext(),
                    iconToResMap[section.icon] ?: R.drawable.circle
                )
        }
    }

    private fun addTestForConfirmation(url: String) {
        if (submissionViewModel.getMobileTestIduiCode() == null) {
            submissionViewModel.newTestForConfirmation = true
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
                            MainFragmentDirections.actionMainFragmentToSubmissionTestRequestFragment(
                                true
                            )
                        )
                    },
                    {
                        testRequestViewModel.setSymptomsDate(null)
                        testRequestViewModel.generateTestId()
                        navigateToLinkTestActivity(url)
                    }
                ))
        } else {
            navigateToLinkTestActivity(url)
        }

    }

    private fun navigateToLinkTestActivity(url: String) {
        val r1 = submissionViewModel.getMobileTestIduiCode()
        val t0 = submissionViewModel.getMobileTestIdt0()
        if (r1 != null && t0 != null) {
            LinkTestActivity.start(requireActivity(), url, r1, t0)
            activity?.intent?.removeExtra(MainActivity.URL_ARGUMENT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LinkTestActivity.ACTIVATION_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
                DialogHelper.showDialog(
                    DialogHelper.DialogInstance(
                        requireActivity(),
                        R.string.test_linked_dialog_title,
                        R.string.test_linked_dialog_body,
                        R.string.test_linked_dialog_button_positive,
                        null,
                        true,
                        {},
                        {}
                    ))
                submissionViewModel.newTestForConfirmation = false
            } else {
                if (submissionViewModel.newTestForConfirmation) {
                    submissionViewModel.deregisterTestFromDevice()
                    submissionViewModel.newTestForConfirmation = false
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setEnvironmentDebugText() {
        val urls = listOf(
            BuildConfig.DOWNLOAD_CDN_URL,
            BuildConfig.STATISTICS_CDN_URL,
            BuildConfig.SUBMISSION_CDN_URL,
            BuildConfig.VERIFICATION_CDN_URL
        )

        if (urls.any { url -> !url.contains("prd") }) {
            binding.textEnvironment.visibility = View.VISIBLE
            if (urls.all { url -> url.contains("tst") }) {
                binding.textEnvironment.text = "TST ENVIRONMENT"
            }
            if (urls.all { url -> url.contains("stg") }) {
                binding.textEnvironment.text = "STG ENVIRONMENT"
            }
        } else {
            binding.textEnvironment.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        // refresh required data
        tracingViewModel.refreshRiskLevel()
        tracingViewModel.refreshExposureSummary()
        tracingViewModel.refreshLastTimeDiagnosisKeysFetchedDate()
        tracingViewModel.refreshIsTracingEnabled()
        tracingViewModel.refreshActiveTracingDaysInRetentionPeriod()
        TimerHelper.checkManualKeyRetrievalTimer()
        submissionViewModel.refreshDeviceUIState()
        tracingViewModel.refreshLastSuccessfullyCalculatedScore()
        statisticsViewModel.refreshStatistics(requireContext())
        binding.mainScrollview.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun observeStatistics() {
        statisticsViewModel.statistics.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                val lastUpdatedDate =
                    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                        .format(Date(it.lastUpdatedDate))

                val startDate =
                    android.text.format.DateFormat.format("dd MMM", Date(it.startDate))
                val endDate = android.text.format.DateFormat.format("dd MMM", Date(it.endDate))

                binding.statistics.statisticsCard.visibility = View.VISIBLE
                binding.vaccinationInfo.vaccinationCard.visibility = View.VISIBLE

                binding.statistics.statisticsTextSubtitle.text =
                    getString(R.string.statistics_date_range, startDate, endDate)
                binding.statistics.bulletInfectionsText.text = getString(
                    R.string.statistics_infected,
                    it.averageInfected,
                    it.averageInfectedChangePercentage
                )
                binding.statistics.bulletPointHospitalisationsText.text = getString(
                    R.string.statistics_hospitalised,
                    it.averageHospitalised,
                    it.averageHospitalisedChangePercentage
                )
                binding.statistics.bulletPointDeceasedText.text = getString(
                    R.string.statistics_deceased,
                    it.averageDeceased,
                    it.averageDeceasedChangePercentage
                )
                binding.statistics.statisticsTextFooter.text =
                    getString(R.string.statistics_last_updated, lastUpdatedDate)

                binding.vaccinationInfo.vaccinationUpdateText.text =
                    getString(R.string.statistics_last_updated, lastUpdatedDate)
                binding.vaccinationInfo.vaccination1Dose.text = DecimalFormat("###,###").format(it.atLeastPartiallyVaccinated).replace(',', ' ')
                binding.vaccinationInfo.vaccinationVaccinated.text = DecimalFormat("###,###").format(it.fullyVaccinated).replace(',', ' ')
            } else {
                binding.statistics.statisticsCard.visibility = View.GONE
                binding.vaccinationInfo.vaccinationCard.visibility = View.GONE
            }

        })
    }

    private fun setContentDescription() {
        val shareButtonString: String = getString(R.string.button_share)
        val menuButtonString: String = getString(R.string.button_menu)
        val mainCardString: String = getString(R.string.hint_external_webpage)
        binding.mainHeaderShare.buttonIcon.contentDescription = shareButtonString
        binding.mainHeaderOptionsMenu.buttonIcon.contentDescription = menuButtonString
        binding.mainAbout.mainCard.contentDescription = mainCardString
    }

    private fun setButtonOnClickListener() {
        binding.mainTestUnregistered.submissionStatusCardUnregistered.setOnClickListener {
            toSubmissionIntro()
        }
        binding.mainTestUnregistered.submissionStatusCardUnregisteredButton.setOnClickListener {
            toSubmissionIntro()
        }
        binding.mainTestDone.submissionStatusCardDone.setOnClickListener {
            findNavController().doNavigate(
                MainFragmentDirections.actionMainFragmentToSubmissionDoneFragment()
            )
        }
        binding.mainTestResult.submissionStatusCardContent.setOnClickListener {
            toSubmissionResult()
        }
        binding.mainTestResult.submissionStatusCardContentButton.setOnClickListener {
            toSubmissionResult()
        }
        binding.mainTestPositive.setOnClickListener {
            toSubmissionResult()
        }
        binding.submissionStatusCardPositiveButton.setOnClickListener {
            toSubmissionResult()
        }
        binding.mainTracing.setOnClickListener {
            findNavController().doNavigate(MainFragmentDirections.actionMainFragmentToSettingsTracingFragment())
        }
        binding.mainRisk.riskCard.setOnClickListener {
            findNavController().doNavigate(MainFragmentDirections.actionMainFragmentToRiskDetailsFragment())
        }
        binding.mainRisk.riskCardButtonUpdate.setOnClickListener {
            tracingViewModel.refreshDiagnosisKeys()
            settingsViewModel.updateManualKeyRetrievalEnabled(false)
        }
        binding.mainRisk.riskCardButtonEnableTracing.setOnClickListener {
            findNavController().doNavigate(MainFragmentDirections.actionMainFragmentToSettingsTracingFragment())
        }
        binding.mainAbout.mainCard.setOnClickListener {
            ExternalActionHelper.openUrl(
                this,
                requireContext().getString(R.string.main_about_link)
            )
        }
        binding.mainHeaderShare.buttonIcon.setOnClickListener {
            findNavController().doNavigate(MainFragmentDirections.actionMainFragmentToMainSharingFragment())
        }
        binding.mainHeaderOptionsMenu.buttonIcon.setOnClickListener {
            showPopup(it)
        }

        binding.tools.toolsCard.setOnClickListener {
            findNavController().doNavigate(MainFragmentDirections.actionMainFragmentToToolsFragment())
        }

        binding.mainTestUnregistered.submissionStatusCardRegisteredButton.setOnClickListener {
            findNavController().doNavigate(MainFragmentDirections.actionMainFragmentToLinkTestInfoFragment())
        }

        binding.covicode.covicodeCard.setOnClickListener {
            findNavController().doNavigate(MainFragmentDirections.actionMainFragmentToCovicodeFragment())
        }
    }

    private fun toSubmissionResult() {
        findNavController().doNavigate(
            MainFragmentDirections.actionMainFragmentToSubmissionResultFragment()
        )
    }

    private fun toSubmissionIntro() {
        findNavController().doNavigate(
            MainFragmentDirections.actionMainFragmentToSubmissionIntroFragment()
        )
    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.inflate(R.menu.menu_main)
        popup.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener when (it.itemId) {
                R.id.menu_help -> {
                    findNavController().doNavigate(MainFragmentDirections.actionMainFragmentToMainOverviewFragment())
                    true
                }
                R.id.menu_information -> {
                    findNavController().doNavigate(MainFragmentDirections.actionMainFragmentToInformationFragment())
                    true
                }
                R.id.menu_settings -> {
                    findNavController().doNavigate(MainFragmentDirections.actionMainFragmentToSettingsFragment())
                    true
                }
                else -> super.onOptionsItemSelected(it)
            }
        }
        popup.show()
    }

    private fun showOneTimeTracingExplanationDialog() {

        // check if the dialog explaining the tracing time was already shown
        if (!LocalData.tracingExplanationDialogWasShown()) {

            val activity = this.requireActivity()

            lifecycleScope.launch {

                // get all text strings and the current active tracing time
                val infoPeriodLogged =
                    getString(R.string.risk_details_information_body_period_logged)
                val infoPeriodLoggedAssessment =
                    getString(
                        R.string.risk_details_information_body_period_logged_assessment,
                        (TimeVariables.getActiveTracingDaysInRetentionPeriod()).toString()
                    )
                val infoFAQ = getString(R.string.risk_details_explanation_dialog_faq_body)

                withContext(Dispatchers.Main) {

                    // display the dialog
                    DialogHelper.showDialog(
                        DialogHelper.DialogInstance(
                            activity,
                            getString(R.string.risk_details_explanation_dialog_title),
                            "$infoPeriodLogged\n\n$infoPeriodLoggedAssessment\n\n$infoFAQ",
                            getString(R.string.errors_generic_button_positive),
                            null,
                            null,
                            {
                                LocalData.tracingExplanationDialogWasShown(true)
                            },
                            {}
                        ))
                }
            }
        }
    }
}
