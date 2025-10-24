package okik.tech.tutorialcopy

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import okik.tech.tutorialcopy.databinding.DialogContentBinding
import okik.tech.tutorialcopy.databinding.FragmentFirstBinding
import okik.tech.tutorialcopy.databinding.RecyclerItemBinding

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

        binding.buttonFirst.setOnClickListener {
            val aView = manager.findViewByPosition(1)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (aView != null) {
                    val paint = Paint()
                    paint.color = Color.GREEN
                    paint.alpha = 0
                    paint.isAntiAlias = true
                    paint.style = Paint.Style.FILL
                    paint.strokeWidth = 8f


                    val n = 16f

//                    binding.clTtdl.setBackgroundColor(Color.TRANSPARENT)
                    val roundShape = OvalShape()

                    val shapeDrawable = ShapeDrawable(roundShape)

                    val focusArea = FocusArea.Builder()
                        .setView(aView)
                        .setOuterAreaEffect(
                            RenderEffect.createBlurEffect(100f, 100f, Shader.TileMode.CLAMP)
                        )
                        .setOuterAreaOverlayColor(Color.BLACK)
                        .setOuterAreaOverlayAlpha(30)
//                            .setSurroundingThicknessEffect(
//                                RenderEffect.createBlurEffect(
//                                    1f,
//                                    1f,
//                                    Shader.TileMode.CLAMP
//                                )
//                            )
//                            .setSurroundingAreaPadding(
//                                FocusArea.InnerPadding(10f, 10f, 10f, 10f)
//                            )
                        .setSurroundingAreaBackgroundDrawable(shapeDrawable)
                        .setSurroundingAreaPaint(paint)
                        .setShouldClipToBackground(true)
                        .setSurroundingThickness(
                            FocusArea.SurroundingThickness(50f, 50f, 50f, 50f)
                        ).build()


                    (binding.root as TutorialDisplayLayout).renderFocusArea(focusArea)
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}