package okik.tech.myapplication.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import okik.tech.myapplication.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome

        val roundContainer = RoundContainer(requireContext())
        roundContainer.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            200
        )
        roundContainer.id = View.generateViewId()

        val constraintSet = ConstraintSet()
        constraintSet.clone(root as ConstraintLayout)

        constraintSet.connect(roundContainer.id, ConstraintSet.TOP, root.id, ConstraintSet.TOP)
        constraintSet.connect(roundContainer.id, ConstraintSet.START, root.id, ConstraintSet.START)

        constraintSet.applyTo(root)

        root.addView(roundContainer)

        val text = TextView(requireContext())
        text.text = "ivannita bebe"
        text.textSize = 32f
        text.setTextColor(Color.RED)

        roundContainer.addView(text)

//        roundContainer.setEffectHolderBackgroundPadding(10)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}