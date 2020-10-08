package be.sciensano.coronalert.ui.submission

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import be.sciensano.coronalert.util.TemporaryExposureKeyExtensions.date
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionKeysListBinding
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.ForbiddenException
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.ui.submission.SubmissionDoneFragment
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUIFormat
import de.rki.coronawarnapp.util.observeEvent
import java.util.Locale

class SubmissionKeysListFragment : Fragment() {

    private val submissionViewModel: SubmissionViewModel by activityViewModels()

    private var _binding: FragmentSubmissionKeysListBinding? = null
    private val binding: FragmentSubmissionKeysListBinding get() = _binding!!

    private lateinit var countries: List<Country>

    private val adapter: KeyAdapter =
        KeyAdapter(getLanguage()) { item: Pair<TemporaryExposureKey, Country>, index: Int ->
            showCountriesListDialog(countries) {
                onCountryClick(it, item, index)
            }

        }

    private fun onCountryClick(
        country: Country,
        item: Pair<TemporaryExposureKey, Country>,
        index: Int
    ) {
        adapter.items[index] = item.copy(item.first, country)
        adapter.notifyItemChanged(index)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSubmissionKeysListBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.submissionKeysListRecycler.adapter = adapter
        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.submissionKeysListRecycler.layoutManager = linearLayoutManager

        binding.submissionKeysListHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }

        countries = submissionViewModel.getCountries(requireContext())

        binding.submissionKeysListButtonSubmit.setOnClickListener {
            submissionViewModel.beSubmitDiagnosisKeys(adapter.items)
        }

        submissionViewModel.keyPairs.observe(viewLifecycleOwner, Observer {
            adapter.items = it.toMutableList()
        })

        submissionViewModel.submissionError.observeEvent(viewLifecycleOwner) {
            DialogHelper.showDialog(buildErrorDialog(it))
        }

        submissionViewModel.submissionState.observe(viewLifecycleOwner, Observer {
            if (it == ApiRequestState.SUCCESS) {
                navigateToSubmissionDoneFragment()
            }
        })
    }

    private fun buildErrorDialog(exception: Exception): DialogHelper.DialogInstance {
        return when (exception) {
            is BadRequestException -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_paring_invalid_title,
                R.string.submission_error_dialog_web_paring_invalid_body,
                R.string.submission_error_dialog_web_paring_invalid_button_positive,
                null,
                true,
                ::navigateToSubmissionResultFragment
            )
            is ForbiddenException -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_tan_invalid_title,
                R.string.submission_error_dialog_web_tan_invalid_body,
                R.string.submission_error_dialog_web_tan_invalid_button_positive,
                null,
                true,
                ::navigateToSubmissionResultFragment
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
                ::navigateToSubmissionResultFragment
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
                ::navigateToSubmissionResultFragment
            )
            else -> DialogHelper.DialogInstance(
                requireActivity(),
                R.string.submission_error_dialog_web_generic_error_title,
                R.string.submission_error_dialog_web_generic_error_body,
                R.string.submission_error_dialog_web_generic_error_button_positive,
                null,
                true,
                ::navigateToSubmissionResultFragment
            )
        }
    }

    private fun navigateToSubmissionResultFragment() =
        findNavController().doNavigate(
            SubmissionKeysListFragmentDirections.actionSubmissionKeysListFragmentToSubmissionResultFragment()
        )

    /**
     * Navigate to submission done Fragment
     * @see SubmissionDoneFragment
     */
    private fun navigateToSubmissionDoneFragment() =
        findNavController().doNavigate(
            SubmissionKeysListFragmentDirections.actionSubmissionKeysListFragmentToSubmissionDoneFragment()
        )


    private fun showCountriesListDialog(
        countries: List<Country>,
        clickListener: (country: Country) -> Any
    ) {

        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.submission_countries_selection_dialog_title)

        val countriesNames = countries.map { it.name[getLanguage()] ?: it.name["en"] }

        builder.setItems(
            countriesNames.toTypedArray()
        ) { _, which ->
            clickListener(countries[which])
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun getLanguage() = Locale.getDefault().language

    internal class KeyAdapter(
        val language: String,
        val clickListener: (item: Pair<TemporaryExposureKey, Country>, index: Int) -> Any
    ) :
        RecyclerView.Adapter<KeyAdapter.KeyHolder>() {

        var items: MutableList<Pair<TemporaryExposureKey, Country>> = mutableListOf()

        inner class KeyHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val date: TextView = view.findViewById(R.id.item_submission_keys_list_date)
            private val country: TextView =
                view.findViewById(R.id.item_submission_keys_list_country)

            fun bind(key: Pair<TemporaryExposureKey, Country>, index: Int) {
                date.text = key.first.date().toDate().toUIFormat(itemView.context)

                country.text = key.second.name[language] ?: key.second.name["en"]
                itemView.setOnClickListener { clickListener(key, index) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeyHolder {
            return KeyHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_submission_keys_list, parent, false)
            )
        }

        override fun getItemCount(): Int {
            return items.size
        }

        override fun onBindViewHolder(holder: KeyHolder, position: Int) {
            holder.bind(items[position], position)
        }
    }
}

