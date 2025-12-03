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

        binding.one.setOnClickListener { _ ->
            binding.root.invalidateOne()
        }

        binding.two.setOnClickListener { _ ->
            binding.root.invalidateTwo()
        }

        binding.three.setOnClickListener { _ ->
            binding.root.invalidateThree()
        }

//        roundContainer.setEffectHolderBackgroundPadding(10)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}