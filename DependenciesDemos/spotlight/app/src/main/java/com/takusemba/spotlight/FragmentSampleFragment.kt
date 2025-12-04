package com.takusemba.spotlight

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.takusemba.spotlight.shape.Circle
import com.takusemba.spotlight.sample.R
import com.takusemba.spotlight.sample.databinding.FragmentFragmentSampleBinding
import com.takusemba.spotlight.sample.databinding.LayoutTargetBinding

class FragmentSampleFragment : Fragment() {

    private lateinit var binding: FragmentFragmentSampleBinding
    private var currentToast: Toast? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.start.setOnClickListener { startButton ->
            val targets = ArrayList<Target>()

            // first target
//            val firstRoot = FrameLayout(requireContext())
            val first = LayoutTargetBinding.inflate(layoutInflater)
            val firstTarget = Target.Builder()
                .setAnchor(binding.one)
                .setShape(Circle(100f))
                .setOverlay(first.root)
                .setOnTargetListener(object : OnTargetListener {
                    override fun onStarted() {
                        currentToast?.cancel()
                        currentToast = Toast.makeText(
                            requireContext(),
                            "first target is started",
                            Toast.LENGTH_SHORT
                        )
                        currentToast?.show()
                    }

                    override fun onEnded() {
                        currentToast?.cancel()
                        currentToast = Toast.makeText(
                            requireContext(),
                            "first target is ended",
                            Toast.LENGTH_SHORT
                        )
                        currentToast?.show()
                    }
                })
                .build()

            targets.add(firstTarget)

            // second target
//            val secondRoot = FrameLayout(requireActivity())
//            val second = layoutInflater.inflate(R.layout.layout_target, secondRoot)
            val secondTarget = Target.Builder()
                .setAnchor(binding.two)
                .setShape(Circle(150f))
                .setOverlay(first.root)
                .setOnTargetListener(object : OnTargetListener {
                    override fun onStarted() {
                        currentToast?.cancel()
                        currentToast = Toast.makeText(
                            requireContext(),
                            "second target is started",
                            Toast.LENGTH_SHORT
                        )
                        currentToast?.show()
                    }

                    override fun onEnded() {
                        currentToast?.cancel()
                        currentToast = Toast.makeText(
                            requireContext(),
                            "second target is ended",
                            Toast.LENGTH_SHORT
                        )
                        currentToast?.show()
                    }
                })
                .build()

            targets.add(secondTarget)

            // third target
//            val thirdRoot = FrameLayout(requireContext())
//            val third = layoutInflater.inflate(R.layout.layout_target, thirdRoot)
            val thirdTarget = Target.Builder()
                .setAnchor(binding.three)
                .setShape(Circle(200f))
                .setOverlay(first.root)
                .setOnTargetListener(object : OnTargetListener {
                    override fun onStarted() {
                        currentToast?.cancel()
                        currentToast = Toast.makeText(
                            requireContext(),
                            "third target is started",
                            Toast.LENGTH_SHORT
                        )
                        currentToast?.show()
                    }

                    override fun onEnded() {
                        currentToast?.cancel()
                        currentToast = Toast.makeText(
                            requireContext(),
                            "third target is ended",
                            Toast.LENGTH_SHORT
                        )
                        currentToast?.show()
                    }
                })
                .build()

            targets.add(thirdTarget)

            // create spotlight
            val spotlight = Spotlight.Builder(requireActivity())
                .setTargets(targets)
                .setBackgroundColorRes(R.color.spotlightBackground)
                .setDuration(1000L)
                .setAnimation(DecelerateInterpolator(2f))
                .setOnSpotlightListener(object : OnSpotlightListener {
                    override fun onStarted() {
                        currentToast?.cancel()
                        currentToast = Toast.makeText(
                            requireContext(),
                            "spotlight is started",
                            Toast.LENGTH_SHORT
                        )
                        currentToast?.show()
//                        startButton.isEnabled = false
                    }

                    override fun onEnded() {
                        currentToast?.cancel()
                        currentToast = Toast.makeText(
                            requireContext(),
                            "spotlight is ended",
                            Toast.LENGTH_SHORT
                        )
                        currentToast?.show()
                        startButton.isEnabled = true
                    }
                })
                .build()

            spotlight.start()

            val nextTarget = View.OnClickListener { spotlight.next() }

            val closeSpotlight = View.OnClickListener { spotlight.finish() }

            first.closeTarget.setOnClickListener(nextTarget)
//            first.findViewById<View>(R.id.close_target).setOnClickListener(nextTarget)
//            second.findViewById<View>(R.id.close_target).setOnClickListener(nextTarget)
//            third.findViewById<View>(R.id.close_target).setOnClickListener(nextTarget)

            first.closeSpotlight.setOnClickListener(closeSpotlight)
//            first.findViewById<View>(R.id.close_spotlight).setOnClickListener(closeSpotlight)
//            second.findViewById<View>(R.id.close_spotlight).setOnClickListener(closeSpotlight)
//            third.findViewById<View>(R.id.close_spotlight).setOnClickListener(closeSpotlight)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFragmentSampleBinding.inflate(layoutInflater)

        return binding.root
    }
}