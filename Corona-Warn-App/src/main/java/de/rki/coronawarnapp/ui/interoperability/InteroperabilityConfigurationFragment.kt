package de.rki.coronawarnapp.ui.interoperability

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import be.sciensano.coronalert.http.responses.DynamicTexts
import be.sciensano.coronalert.http.responses.countryIconToResMap
import be.sciensano.coronalert.ui.DynamicTextsViewModel
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInteroperabilityConfigurationBinding
import de.rki.coronawarnapp.ui.Country
import de.rki.coronawarnapp.ui.main.MainActivity
import java.util.Locale

class InteroperabilityConfigurationFragment :
    Fragment(R.layout.fragment_interoperability_configuration) {


    private val viewModel: DynamicTextsViewModel by activityViewModels()
    private var _binding: FragmentInteroperabilityConfigurationBinding? = null
    private val binding: FragmentInteroperabilityConfigurationBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInteroperabilityConfigurationBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getDynamicTexts(requireContext())

        viewModel.dynamicTexts.observe(viewLifecycleOwner, Observer {

            val texts = it.texts

            val countryData = it.structure.participatingCountries.list.map { country ->
                val label = DynamicTexts.getText(country.text, texts, Locale.getDefault().language)
                Country(countryIconToResMap[country.icon] ?: R.drawable.ic_country_eu, label)
            }

            binding.countryData = countryData
        })

        binding.interoperabilityConfigurationHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }
    }
}
