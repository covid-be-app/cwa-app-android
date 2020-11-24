package be.sciensano.coronalert.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestRequestBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.main.MainActivity
import java.util.Calendar
import java.util.Date

private const val MAXIMUM_KEYS = 14

class SubmissionTestRequestFragment : Fragment() {

    private val viewModel: SubmissionTestRequestViewModel by activityViewModels()
    private var _binding: FragmentSubmissionTestRequestBinding? = null
    private val binding: FragmentSubmissionTestRequestBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSubmissionTestRequestBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calendar = Calendar.getInstance()
        viewModel.setSymptomsDate(calendar.time)

        binding.submissionDatePicker.minDate = let {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DATE, -MAXIMUM_KEYS)
            cal.time.time
        }
        // no date in future
        binding.submissionDatePicker.maxDate = Date().time

        binding.submissionDatePicker.init(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ) { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            viewModel.setSymptomsDate(calendar.time)
        }

        binding.submissionTestRequestHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }

        binding.submissionTestRequestButtonNext.setOnClickListener {
            if (SubmissionTestRequestFragmentArgs.fromBundle(requireArguments()).skipCodeInformation) {
                viewModel.generateTestId()
                findNavController().doNavigate(
                    SubmissionTestRequestFragmentDirections
                        .actionSubmissionTestRequestFragmentToMainFragment()
                )
            } else {
                findNavController().doNavigate(
                    SubmissionTestRequestFragmentDirections
                        .actionSubmissionTestRequestFragmentToSubmissionTestRequestSaveFragment()
                )
            }
        }
    }
}
