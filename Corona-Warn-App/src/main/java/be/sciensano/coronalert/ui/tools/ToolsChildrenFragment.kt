package be.sciensano.coronalert.ui.tools

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentToolsChildrenBinding
import de.rki.coronawarnapp.ui.main.MainActivity


class ToolsChildrenFragment : Fragment() {
    companion object {
        private val TAG: String? = ToolsChildrenFragment::class.simpleName
    }

    private var _binding: FragmentToolsChildrenBinding? = null
    private val binding: FragmentToolsChildrenBinding get() = _binding!!

    private val args: ToolsChildrenFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentToolsChildrenBinding.inflate(inflater)
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

        binding.header.title = getString(args.tools.titleRes)
        binding.toolsIllustration.setImageResource(args.tools.illustrationRes)

        args.tools.children.forEachIndexed { index, child ->
            val childView = layoutInflater.inflate(R.layout.include_tools_row_sub, null)

            childView.findViewById<TextView>(R.id.main_row_item_subtitle).text =
                getString(child.titleRes)

            child.suffix?.let {
                childView.findViewById<TextView>(R.id.main_row_item_suffix).text = it
            }

            childView.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(child.url)))
            }

            binding.layoutChildren.addView(childView, index)
        }

    }

}
