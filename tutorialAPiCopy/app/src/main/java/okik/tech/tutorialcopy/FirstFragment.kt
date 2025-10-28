package okik.tech.tutorialcopy

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout.LayoutParams
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import okik.tech.tutorialcopy.databinding.DialogContentBinding
import okik.tech.tutorialcopy.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var manager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recycler.layoutManager = manager

        val adapter = MyAdapter(listOf(1,2,3,4,5,6,7,8,9))
        binding.recycler.adapter = adapter

        binding.block?.setBackgroundColor(Color.BLUE)

        binding.buttonFirst.setOnClickListener {
            val aView = manager.findViewByPosition(3)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {

            }

            if (aView != null) {
                val paint = Paint()
                paint.color = Color.GREEN
                paint.alpha = 180
                paint.isAntiAlias = true
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 16f

                val focusArea = FocusArea.Builder()
                    .setView(aView) // binding.thete ?:
//                        .setOuterAreaEffect(
//                            RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
//                        )
                    .setOuterAreaOverlayColor(Color.BLACK)
                    .setOuterAreaOverlayAlpha(180)
                    .setSurroundingAreaPadding(10, 10, 10, 10)
                    .setSurroundingAreaBackgroundDrawableFactory(
                        {
                            val roundShape = OvalShape()

                            return@setSurroundingAreaBackgroundDrawableFactory ShapeDrawable(roundShape)
                        }
                    )
//                        .setSurroundingThicknessEffect(
//                            RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
//                        )
                    .setSurroundingAreaPaint(paint)
                    .setShouldClipToBackground(false)
                    .setSurroundingThickness(50, 50, 50, 50)
                    .build()

                val dialog = getDialog(focusArea)

                val focusDialog = focusArea.generateMatchingFocusDialog(
                    Gravity.BOTTOM,
                    0,
                    80,
                    0.5,
                    0.5,
                    false,
                    dialog
                )

                (binding.root as TutorialDisplayLayout).renderFocusAreaWithDialog(focusArea, focusDialog)
            }
        }
    }

    private fun getDialog(
        focusArea: FocusArea
    ): View {
        val dialog = BackgroundEffectRendererLayout(requireContext())

        dialog.layoutParams = ConstraintLayout.LayoutParams(700, 530)

        val content = DialogContentBinding.inflate(LayoutInflater.from(requireContext()))

        content.root.layoutParams = LayoutParams(700, 530)

        (content.root.layoutParams as MarginLayoutParams).setMargins(0, 0, 0, 0)
//
//        content.root.setPadding(
//            focusArea.surroundingAreaPadding.start.toInt(),
//            focusArea.surroundingAreaPadding.top.toInt(),
//            focusArea.surroundingAreaPadding.end.toInt(),
//            focusArea.surroundingAreaPadding.bottom.toInt()
//        )

        dialog.setFallbackBackground(requireActivity().window.decorView.background)

        dialog.addView(content.root)

        content.tb.setOnClickListener {
//            popi.dismiss()
//            this.focusArea = null
//
//            for (i in 1 .. childCount -1) {
//                getChildAt(i).visibility = GONE
//            }
        }

        return dialog
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}