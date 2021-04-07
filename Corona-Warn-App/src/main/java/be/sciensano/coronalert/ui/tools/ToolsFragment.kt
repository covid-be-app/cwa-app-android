@file:Suppress("MaxLineLength")

package be.sciensano.coronalert.ui.tools

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.databinding.FragmentToolsBinding
import de.rki.coronawarnapp.ui.main.MainActivity

class ToolsFragment : Fragment() {
    companion object {
        private val TAG: String? = ToolsFragment::class.simpleName
    }

    private var _binding: FragmentToolsBinding? = null
    private val binding: FragmentToolsBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentToolsBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.header.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }

        binding.toolsVaccinationInformation.mainRow.setOnClickListener {
            findNavController().navigate(
                ToolsFragmentDirections.actionToolsFragmentToToolsChildrenFragment(
                    VaccinationInformation()
                )
            )

        }

        binding.toolsTestReservation.mainRow.setOnClickListener {
            findNavController().navigate(
                ToolsFragmentDirections.actionToolsFragmentToToolsChildrenFragment(
                    TestReservation()
                )
            )
        }

        binding.toolsQuarantineCertification.mainRow.setOnClickListener {
            findNavController().navigate(
                ToolsFragmentDirections.actionToolsFragmentToToolsChildrenFragment(
                    QuarantineCertificate()
                )
            )
        }

        binding.toolsPassengerLocatorForm.mainRow.setOnClickListener {
            findNavController().navigate(
                ToolsFragmentDirections.actionToolsFragmentToToolsChildrenFragment(
                    PassengerLocatorForm()
                )
            )
        }

        binding.toolsDeclarationOfHonour.mainRow.setOnClickListener {
            findNavController().navigate(
                ToolsFragmentDirections.actionToolsFragmentToToolsChildrenFragment(
                    DeclarationOfHonour()
                )
            )
        }
        binding.toolsTestCenter.mainRow.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/d/u/0/viewer?mid=1GFrpKwhDE97_pplhtikcfDxakEhohXst&ll=50.48641435902384%2C4.395339249999988&z=9")
                )
            )
        }
    }
}
