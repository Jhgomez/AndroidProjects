package com.takusemba.spotlight

import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import androidx.appcompat.app.AppCompatActivity
import com.takusemba.spotlight.shape.Circle
import com.takusemba.spotlight.sample.R
import com.takusemba.spotlight.sample.databinding.ActivityActivitySampleBinding
import com.takusemba.spotlight.sample.databinding.LayoutTargetBinding

class ActivitySampleActivity : AppCompatActivity() {
    private var currentToast: Toast? = null
    private lateinit var binding: ActivityActivitySampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityActivitySampleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.start.setOnClickListener { startButton ->
            val targets = ArrayList<Target>()

            // first target
            val first = LayoutTargetBinding.inflate(layoutInflater)
            val firstTarget = Target.Builder()
                .setAnchor(binding.one)
                .setShape(Circle(100f))
                .setOverlay(first.root)
                .setOnTargetListener(object : OnTargetListener {
                    override fun onStarted() {
                        currentToast?.cancel()
                        currentToast = makeText(
                            this@ActivitySampleActivity,
                            "first target is started",
                            LENGTH_SHORT
                        )
                        currentToast?.show()
                    }

                    override fun onEnded() {
                        currentToast?.cancel()
                        currentToast = makeText(
                            this@ActivitySampleActivity,
                            "first target is ended",
                            LENGTH_SHORT
                        )
                        currentToast?.show()
                    }
                })
                .build()

            targets.add(firstTarget)

            // second target
//            val secondRoot = FrameLayout(this)
//            val second = layoutInflater.inflate(R.layout.layout_target, secondRoot)
            val secondTarget = Target.Builder()
                .setAnchor(binding.two)
                .setShape(Circle(150f))
                .setOverlay(first.root)
                .setOnTargetListener(object : OnTargetListener {
                    override fun onStarted() {
                        currentToast?.cancel()
                        currentToast = makeText(
                            this@ActivitySampleActivity,
                            "second target is started",
                            LENGTH_SHORT
                        )
                        currentToast?.show()
                    }

                    override fun onEnded() {
                        currentToast?.cancel()
                        currentToast = makeText(
                            this@ActivitySampleActivity,
                            "second target is ended",
                            LENGTH_SHORT
                        )
                        currentToast?.show()
                    }
                })
                .build()

            targets.add(secondTarget)

            // third target
//            val thirdRoot = FrameLayout(this)
//            val third = layoutInflater.inflate(R.layout.layout_target, thirdRoot)
            val thirdTarget = Target.Builder()
                .setAnchor(binding.three)
                .setShape(Circle(200f))
                .setOverlay(first.root)
                .setOnTargetListener(object : OnTargetListener {
                    override fun onStarted() {
                        currentToast?.cancel()
                        currentToast = makeText(
                            this@ActivitySampleActivity,
                            "third target is started",
                            LENGTH_SHORT
                        )
                        currentToast?.show()
                    }

                    override fun onEnded() {
                        currentToast?.cancel()
                        currentToast = makeText(
                            this@ActivitySampleActivity,
                            "third target is ended",
                            LENGTH_SHORT
                        )
                        currentToast?.show()
                    }
                })
                .build()

            targets.add(thirdTarget)

            // create spotlight
            val spotlight = Spotlight.Builder(this@ActivitySampleActivity)
                .setTargets(targets)
                .setBackgroundColorRes(R.color.spotlightBackground)
                .setDuration(1000L)
                .setAnimation(DecelerateInterpolator(2f))
                .setOnSpotlightListener(object : OnSpotlightListener {
                    override fun onStarted() {
                        currentToast?.cancel()
                        currentToast = makeText(
                            this@ActivitySampleActivity,
                            "spotlight is started",
                            LENGTH_SHORT
                        )
                        currentToast?.show()
//                        startButton.isEnabled = false
                    }

                    override fun onEnded() {
                        currentToast?.cancel()
                        currentToast = makeText(
                            this@ActivitySampleActivity,
                            "spotlight is ended",
                            LENGTH_SHORT
                        )
                        currentToast?.show()
                        startButton.isEnabled = true
                    }
                })
                .build()

            spotlight.start()

            val nextTarget = View.OnClickListener { spotlight.next() }

            val closeSpotlight = View.OnClickListener { spotlight.finish() }

//            first.findViewById<View>(R.id.close_target).setOnClickListener(nextTarget)
            first.closeTarget.setOnClickListener(nextTarget)
//            second.findViewById<View>(R.id.close_target).setOnClickListener(nextTarget)
//            third.findViewById<View>(R.id.close_target).setOnClickListener(nextTarget)

//            first.findViewById<View>(R.id.close_spotlight).setOnClickListener(closeSpotlight)
            first.closeSpotlight.setOnClickListener(closeSpotlight)
//            second.findViewById<View>(R.id.close_spotlight).setOnClickListener(closeSpotlight)
//            third.findViewById<View>(R.id.close_spotlight).setOnClickListener(closeSpotlight)
        }
    }
}
