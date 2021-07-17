package be.sciensano.coronalert.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.databinding.FragmentCovidsafeBinding
import de.rki.coronawarnapp.databinding.FragmentLinkTestInfoBinding
import de.rki.coronawarnapp.ui.main.MainActivity

class CovidSafeFragment : Fragment() {
    companion object {
        private val TAG: String? = CovidSafeFragment::class.simpleName
    }

    private var _binding: FragmentCovidsafeBinding? = null
    private val binding: FragmentCovidsafeBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCovidsafeBinding.inflate(inflater)
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

        binding.covidsafeAppButton.setOnClickListener {
            launchCodidSafe()
        }

        binding.covidsafeApp.setOnClickListener {
            launchCodidSafe()
        }
    }

    fun launchCodidSafe() = run {
        startActivity(Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://cert-app.be/launch")
        ))
    }

    override fun onResume() {
        super.onResume()
        binding.container.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

}
