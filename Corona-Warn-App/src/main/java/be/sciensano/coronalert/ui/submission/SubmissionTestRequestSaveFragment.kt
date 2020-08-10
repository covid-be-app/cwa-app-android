package be.sciensano.coronalert.ui.submission

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestRequestSaveBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.main.MainActivity
import timber.log.Timber
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
        binding.submissionViewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showQrCode(code: String) {
        val writer = QRCodeWriter()
        try {
            val bitMatrix = writer.encode(code, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            binding.submissionTestRequestSaveQrImage.setImageBitmap(bmp)
        } catch (e: WriterException) {
            Timber.e(e)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val mobileTestId = viewModel.generateTestId()

        viewModel.submissionDate.value?.let {
            binding.submissionTestRequestSaveDate.text =
                DateFormat.getDateInstance(DateFormat.FULL).format(it)

        }

        showQrCode(mobileTestId.toString())
        binding.submissionTestRequestSaveCode.text = mobileTestId.toString()

        binding.submissionTestRequestSaveHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }

        binding.submissionTestRequestSaveButtonSave.setOnClickListener {
            viewModel.saveTestId()
            findNavController().doNavigate(
                SubmissionTestRequestSaveFragmentDirections
                    .actionSubmissionTestRequestSaveFragmentToSubmissionResultFragment()
            )

        }

    }

}
