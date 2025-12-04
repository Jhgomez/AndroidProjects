package com.takusemba.spotlight

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.takusemba.spotlight.sample.R
import com.takusemba.spotlight.sample.databinding.ActivityChooserBinding

class ChooserActivity : AppCompatActivity(R.layout.activity_chooser) {

    private lateinit var binding: ActivityChooserBinding

    private val samples: Array<String> = arrayOf(
        SAMPLE_SPOTLIGHT_ON_ACTIVITY,
        SAMPLE_SPOTLIGHT_ON_FRAGMENT
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChooserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, samples)
        binding.sampleList.adapter = adapter
        binding.sampleList.setOnItemClickListener { _, _, position, _ ->
            when (samples[position]) {
                SAMPLE_SPOTLIGHT_ON_ACTIVITY -> {
                    val intent = Intent(this, ActivitySampleActivity::class.java)
                    startActivity(intent)
                }

                SAMPLE_SPOTLIGHT_ON_FRAGMENT -> {
                    val intent = Intent(this, FragmentSampleActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    companion object {
        private const val SAMPLE_SPOTLIGHT_ON_ACTIVITY = "Spotlight on Activity"
        private const val SAMPLE_SPOTLIGHT_ON_FRAGMENT = "Spotlight on Fragment"
    }
}
