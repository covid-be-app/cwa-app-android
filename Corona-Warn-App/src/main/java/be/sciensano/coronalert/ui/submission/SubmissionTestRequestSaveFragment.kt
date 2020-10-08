package be.sciensano.coronalert.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import be.sciensano.coronalert.util.DateUtil
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestRequestSaveBinding
import de.rki.coronawarnapp.ui.doNavigate
import java.text.DateFormat


class SubmissionTestRequestSaveFragment : Fragment() {

    private val viewModel: SubmissionTestRequestViewModel by activityViewModels()
    private var _binding: FragmentSubmissionTestRequestSaveBinding? = null
    private val binding: FragmentSubmissionTestRequestSaveBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSubmissionTestRequestSaveBinding.inflate(inflater)
        binding.lifecycleOwner = this
        // registers callback when the os level back is pressed
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mobileTestId = viewModel.generateTestId()
        binding.submissionTestRequestSaveDate.text =
            DateFormat.getDateInstance(DateFormat.FULL)
                .format(DateUtil.parseServerDate(mobileTestId.t0).toDate())

        binding.submissionTestRequestSaveCode.text = mobileTestId.toString()

        binding.submissionTestRequestSaveHeader.headerButtonBack.buttonIcon.setOnClickListener {
            findNavController().doNavigate(
                SubmissionTestRequestSaveFragmentDirections
                    .actionSubmissionTestRequestSaveFragmentToMainFragment()
            )
        }

        binding.submissionTestRequestSaveButtonSave.setOnClickListener {
            findNavController().doNavigate(
                SubmissionTestRequestSaveFragmentDirections
                    .actionSubmissionTestRequestSaveFragmentToMainFragment()
            )


        }
    }

    // Overrides default back behaviour
    private val backCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().doNavigate(
                    SubmissionTestRequestSaveFragmentDirections
                        .actionSubmissionTestRequestSaveFragmentToMainFragment()
                )
            }
        }
}
