package be.sciensano.coronalert.ui.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.rki.coronawarnapp.databinding.FragmentInformationDataTransferBinding
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.util.IGNORE_CHANGE_TAG

class InformationDataTransferFragment : Fragment() {
    companion object {
        private val TAG: String? = InformationDataTransferFragment::class.simpleName
    }

    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private var _binding: FragmentInformationDataTransferBinding? = null
    private val binding: FragmentInformationDataTransferBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInformationDataTransferBinding.inflate(inflater)
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

        binding.informationSwitchRowDataTransfer.settingsSwitchRowSwitch.setOnCheckedChangeListener { _, _ ->
            if (binding.informationSwitchRowDataTransfer.settingsSwitchRowSwitch.tag != IGNORE_CHANGE_TAG) {
                settingsViewModel.toggleDataTranfer()
            }
        }

        binding.informationSwitchRowDataTransfer.settingsSwitchRow.setOnClickListener {
            settingsViewModel.toggleDataTranfer()
        }

        binding.informationDataTransferHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.informationDataTransferContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

}
