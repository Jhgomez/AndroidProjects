package okik.tech.tutorialcopy

import android.app.Activity
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowInsets
import android.widget.FrameLayout.LayoutParams
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
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

        binding.block.setBackgroundColor(Color.GREEN)

        var count = 0
        binding.buttonFirst.setOnClickListener {
//            if (count == 0) {
//                count += 1
                val aView = binding.thete
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {

                }

                if (aView != null) {
                    val paint = Paint()
                    paint.color = Color.WHITE
                    paint.alpha = 200
                    paint.isAntiAlias = true
                    paint.style = Paint.Style.FILL
                    paint.strokeWidth = 16f

                    val focusArea = FocusArea.Builder()
                        .setView(aView) // binding.thete ?:
//                        .setOuterAreaEffect(
//                            RenderEffect.createBlurEffect(10f, 10f, Shader.TileMode.CLAMP)
//                        )
                        .setOuterAreaOverlayColor(Color.BLACK)
                        .setOuterAreaOverlayAlpha(110)
//                    .setSurroundingAreaPadding(10, 10, 10, 10)
//                    .setSurroundingAreaBackgroundDrawableFactory(
//                        {
//                            val roundShape = OvalShape()
//
//                            return@setSurroundingAreaBackgroundDrawableFactory ShapeDrawable(roundShape)
//                        }
//                    )
//                        .setSurroundingThicknessEffect(
//                            RenderEffect.createBlurEffect(10f, 10f, Shader.TileMode.CLAMP)
//                        )
                        .setSurroundingAreaPaint(paint)
                        .setShouldClipToBackground(true)
                        .setSurroundingThickness(50, 50, 50, 50)
                        .build()

                    val dialog = getDialog(focusArea)

                    val focusDialog = focusArea
                        .generateMatchingFocusDialog()
                        .setDialogView(dialog)
                        .setDialogConstraintsCommand { cl, focusView, dialog ->
                            DialogWrapperLayout.constraintDialogToTop(
                                cl,
                                focusView,
                                dialog,
                                0.0,
                                dpToPx(400, focusView.context).toDouble(),
                                false
                            )
                        }
                        .setPathViewPathGeneratorCommand { focusVIew, dialog ->
                            DialogWrapperLayout.drawPathToTopDialog(
                                focusVIew,
                                dialog,
                                0.1,
                                0.1,
                                dpToPx(120, focusVIew.context).toDouble()
                            )
                        }
                        .build()


//                    binding.root.renderFocusArea(focusArea)
                    binding.root.renderFocusAreaWithDialog(focusArea, focusDialog)

//                val bsb = BottomSheetBehavior.from(binding.bottomchit)
//                bsb.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
//                    override fun onStateChanged(
//                        bottomSheet: View,
//                        newState: Int
//                    ) {
//
//                    }
//
//                    override fun onSlide(
//                        bottomSheet: View,
//                        slideOffset: Float
//                    ) {
//
//                    }
//                })
//
//                bsb.state = BottomSheetBehavior.STATE_COLLAPSED
                }
//            } else {
//                count -= 1
//                binding.root.hideTutorialComponents()
//            }

            val colors = arrayOf(Color.MAGENTA, Color.WHITE, Color.RED, Color.GREEN, Color.YELLOW,
                Color.BLUE)

            val thread = Thread({
                Thread.sleep(2000)

                val dim = dpToPx(120, requireContext()).toInt()

                val view = View(requireContext())
                view.id = View.generateViewId()
                view.layoutParams = ConstraintLayout.LayoutParams(dim, dim)

                binding.clTtdl.post {
                    binding.clTtdl.addView(view)

                    val cs = ConstraintSet()
                    cs.clone(binding.clTtdl)

                    cs.connect(view.id, ConstraintSet.BOTTOM, binding.clTtdl.id, ConstraintSet.BOTTOM)
                    cs.connect(view.id, ConstraintSet.LEFT, binding.clTtdl.id, ConstraintSet.LEFT)

                    cs.applyTo(binding.clTtdl)

                    (view.layoutParams as MarginLayoutParams).marginStart = dpToPx(16, requireContext()).toInt()
                    (view.layoutParams as MarginLayoutParams).bottomMargin = dpToPx(32, requireContext()).toInt()

                    view.setBackgroundColor(colors.random())
                }
            })

            thread.start()
        }
    }

    private fun getDialog(
        focusArea: FocusArea
    ): BackgroundEffectRendererLayout {
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

        val backgroundSettings = focusArea.generateBackgroundSettings()

        dialog.setBackgroundConfigs(backgroundSettings)


//        val insets =
//            ViewCompat.getRootWindowInsets(referenceView)
//
//        val topInset: Int
//
//        if (insets != null) {
//            topInset = insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.statusBars()).top
//        } else {
//            topInset = 0
//        }

       content.tb.setOnClickListener {
           binding.root.hideTutorialComponents()
        }

        return dialog
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}