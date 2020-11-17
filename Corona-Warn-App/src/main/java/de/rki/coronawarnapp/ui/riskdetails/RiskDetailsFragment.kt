package de.rki.coronawarnapp.ui.riskdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import be.sciensano.coronalert.http.responses.DynamicTexts
import be.sciensano.coronalert.http.responses.PreventiveMeasure
import be.sciensano.coronalert.http.responses.iconToResMap
import be.sciensano.coronalert.ui.DynamicTextsViewModel
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentRiskDetailsBinding
import de.rki.coronawarnapp.databinding.IncludeRiskDetailsBehaviorRowBinding
import de.rki.coronawarnapp.databinding.ViewBulletPointEntryBinding
import de.rki.coronawarnapp.risk.RiskLevelConstants
import de.rki.coronawarnapp.timer.TimerHelper
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import java.util.Locale

/**
 * This is the detail view of the risk card if additional information for the user.
 *
 * @see TracingViewModel
 * @see SettingsViewModel
 */
class RiskDetailsFragment : Fragment() {

    companion object {
        private val TAG: String? = RiskDetailsFragment::class.simpleName
    }

    private val tracingViewModel: TracingViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val dynamicTextsViewModel: DynamicTextsViewModel by activityViewModels()

    private var _binding: FragmentRiskDetailsBinding? = null
    private val binding: FragmentRiskDetailsBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRiskDetailsBinding.inflate(inflater)
        binding.tracingViewModel = tracingViewModel
        binding.settingsViewModel = settingsViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListeners()

        dynamicTextsViewModel.dynamicTexts.observe(viewLifecycleOwner, Observer {
            if (tracingViewModel.riskLevel.value == RiskLevelConstants.INCREASED_RISK) {
                addPreventiveMeasures(it.structure.highRisk.preventiveMeasures, it.texts)
            } else {
                addPreventiveMeasures(it.structure.standard.preventiveMeasures, it.texts)
            }

        })

        tracingViewModel.riskLevel.observe(viewLifecycleOwner, Observer {
            dynamicTextsViewModel.getDynamicTexts(requireContext())
        })
    }

    override fun onResume() {
        super.onResume()
        // refresh required data
        tracingViewModel.refreshRiskLevel()
        tracingViewModel.refreshExposureSummary()
        tracingViewModel.refreshLastTimeDiagnosisKeysFetchedDate()
        TimerHelper.checkManualKeyRetrievalTimer()
        tracingViewModel.refreshActiveTracingDaysInRetentionPeriod()
        binding.riskDetailsContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun addPreventiveMeasures(
        sections: List<PreventiveMeasure>,
        texts: Map<String, Map<String, String>>
    ) {
        binding.riskDetailsDynamics.removeAllViewsInLayout()
        val inflater = LayoutInflater.from(binding.riskDetailsDynamics.context)
        sections.forEach { section ->
            val newViewBinding = IncludeRiskDetailsBehaviorRowBinding.inflate(
                inflater,
                binding.riskDetailsDynamics,
                true
            )
            newViewBinding.icon =
                ContextCompat.getDrawable(
                    requireContext(),
                    iconToResMap[section.icon] ?: R.drawable.circle
                )
            newViewBinding.body =
                DynamicTexts.getText(section.text, texts, Locale.getDefault().language)

            newViewBinding.riskLevel = tracingViewModel.riskLevel.value


            section.paragraphs?.forEach { paragraph ->
                val paragraphBinding = ViewBulletPointEntryBinding.inflate(
                    inflater,
                    newViewBinding.riskDetailsBehaviorLayout,
                    true
                )
                paragraphBinding.bulletPointContent.text =
                    DynamicTexts.getText(paragraph, texts, Locale.getDefault().language)

            }
        }
    }

    private fun setButtonOnClickListeners() {
        binding.riskDetailsHeaderButtonBack.setOnClickListener {
            (activity as MainActivity).goBack()
        }
        binding.riskDetailsButtonUpdate.setOnClickListener {
            tracingViewModel.refreshDiagnosisKeys()
            settingsViewModel.updateManualKeyRetrievalEnabled(false)
        }
        binding.riskDetailsButtonEnableTracing.setOnClickListener {
            findNavController().doNavigate(
                RiskDetailsFragmentDirections.actionRiskDetailsFragmentToSettingsTracingFragment()
            )
        }
    }
}
