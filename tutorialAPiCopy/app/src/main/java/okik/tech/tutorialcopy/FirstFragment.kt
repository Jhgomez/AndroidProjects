package okik.tech.tutorialcopy

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.PopupWindow
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


        var smoothScroller = object : LinearSmoothScroller(requireContext()){

            override fun getHorizontalSnapPreference(): Int {
                return LinearSmoothScroller.SNAP_TO_START
            }

            override fun onTargetFound(
                targetView: View,
                state: RecyclerView.State,
                action: Action
            ) {
                super.onTargetFound(targetView, state, action)
            }

            override fun calculateDxToMakeVisible(view: View, snapPreference: Int): Int {
                val layoutManager = getLayoutManager()
                if (layoutManager == null || !layoutManager.canScrollHorizontally()) {
                    return 0
                }
                val params = view.getLayoutParams() as RecyclerView.LayoutParams
                val left = layoutManager.getDecoratedLeft(view) - params.leftMargin
                val right = layoutManager.getDecoratedRight(view) + params.rightMargin
                val start = layoutManager.getPaddingLeft()
                val end = layoutManager.getWidth() - layoutManager.getPaddingRight()

                val ret = calculateDtToFit(left, right, start, end, snapPreference)

                return ret
            }
        }

        var scrollProcessed = false

        val listen = object : RecyclerView.OnScrollListener() {
            var scrolledToRigth = false
            var automaticScrollRunning = false
            var lastDx = 0
            var index = 0

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                when(newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        if (!scrollProcessed) {
                            if (scrolledToRigth && lastDx >= 5) {
                                if (index < 5) index = index + 1
                            } else if (!scrolledToRigth && lastDx <= -5) {
                                if (index > 0) index = index - 1
                            }

                            smoothScroller.targetPosition = index

                            Log.d("FirstFrag", "target $index")

                            automaticScrollRunning = true
                            manager.startSmoothScroll(smoothScroller)

                            scrollProcessed = true

                            automaticScrollRunning = false
                        }

                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        scrollProcessed = false
                    }
                    RecyclerView.SCROLL_STATE_SETTLING -> {

                    }
                }
            }



            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!scrollProcessed) {

                    scrolledToRigth = dx > 0

                    if (scrolledToRigth && dx >= 5 || !scrolledToRigth && dx <= -5) {
                        lastDx = dx
                    }
                }
            }

        }

//        binding.recycler.addOnScrollListener(listen)

//        smoothScroller.targetPosition = 3
//        manager.startSmoothScroll(smoothScroller)
//        binding.recycler.smoothScrollToPosition(3)

//        val replica = MaterialButton(requireContext())
//        replica.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
//        var pixelsToDp = { pixels:Float  ->
//            TypedValue.applyDimension(
//                TypedValue.COMPLEX_UNIT_DIP,
//                pixels,
//                resources.displayMetrics
//            ).toInt()
//        }
//        (replica.layoutParams as ViewGroup.MarginLayoutParams).setMargins(pixelsToDp(80f), pixelsToDp(16f), 0, 0)
//        replica.text = "a test"
//        binding.root.addView(replica)

        binding.buttonFirst.setOnClickListener {
//            requireActivity().window.decorView.setRenderEffect(RenderEffect.createBlurEffect(10f, 10f, Shader.TileMode.DECAL))
            val aView = manager.findViewByPosition(1)
            val clone = RecyclerItemBinding.inflate(layoutInflater).root

//            val aView = binding.buttonFirst
//            val clone = MaterialButton(requireContext())
//            clone.text = "Next"

//            showPopup(binding.root, aView, clone)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
//                val triangle = DialogOriginTriangle(requireContext())
//                binding.root.setBackgroundColor(Color.BLUE)
//                triangle.layoutParams = FrameLayout.LayoutParams(100, 100)
//                triangle.setRenderEffect(
//                                                    RenderEffect.createBlurEffect(
//                                    30f,
//                                    30f,
//                                    Shader.TileMode.CLAMP
//                                )
//                )
//                (binding.root as FrameLayout).addView(triangle)



                if (aView != null) {
                        val content = DialogContentBinding.inflate(layoutInflater)

                        val widthInPixels = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            400f,
                            resources.displayMetrics
                        )

                        // always specify the width and height of the content of the dialog like this
                        content.root.layoutParams = ConstraintLayout.LayoutParams(
                            widthInPixels.toInt(),
                            widthInPixels.toInt()/2
                        )

//                        (binding.root as TutorialDisplayLayout).focusViewWithDialog(
//                            aView,
//                            null,
//                            null,
//                            null,
//                            null,
//                            null,
//                            null,
//                            null,
//                            null,
//                            Gravity.BOTTOM,
//                            -30f,
//                            30f,
//                            0.5f,
//                            0.5f,
//                            true,
//                            content.root
//                        )

                        val paint = Paint()
                    paint.color = Color.GREEN
                    paint.alpha = 50
                        paint.isAntiAlias = true
                        paint.style = Paint.Style.FILL
                    paint.strokeWidth = 8f


                        val focusArea = FocusArea.Builder()
                            .setView(aView)
                            .setOuterAreaEffect(RenderEffect.createBlurEffect(10f, 10f, Shader.TileMode.CLAMP))
                            .setOverlayColor(Color.BLACK)
                            .setOverlayAlpha(30f)
                            .setSurroundingThicknessEffect(
                                RenderEffect.createBlurEffect(
                                    80f,
                                    80f,
                                    Shader.TileMode.CLAMP
                                )
                            )
                            .setRoundedCornerSurrounding(
                                FocusArea.RoundedCornerSurrounding(
                                    paint,
                                    10,
                                    FocusArea.InnerPadding(0f, 0f, 0f, 0f)
                                )
                            )
                            .setSurroundingThickness(
                                FocusArea.SurroundingThickness(40f, 40f, 40f, 40f)
                            ).build()


                        (binding.root as TutorialDisplayLayout).renderFocusArea(focusArea)

//                    val ft: FragmentTransaction = childFragmentManager.beginTransaction()
//            ft.show(HomeDialog())

//                    val dial = TutorialFragmentDialog()
//                    dial.setFocusAreas(focusArea)
//                        dial.show(ft, "")

//                    binding.root.focusView(aView, null, null, null, null)
                    } else {
//                    binding.root.focusView(null, null, null, null, null)
                    }
            } else {
                binding.viewOverlay!!.visibility = View.VISIBLE
//            binding.root.setRenderEffect(RenderEffect.createBlurEffect(10f, 10f, Shader.TileMode.DECAL))

                showPopup(binding.root, aView, clone)
            }
        }
    }

    private fun highlightView(view: View) {
//        val tutorialLayout = TutorialDisplayLayout(requireContext(), view)
//        tutorialLayout.highlightView(view)

//        val popi = PopupWindow(
//            tutorialLayout,
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            false // closes on outside touche if true
//        )
//
//        popi.showAtLocation(binding.root, Gravity.NO_GRAVITY, 0, 0)
    }

    private fun showPopup(button: View, aView: View?, viewClone: View) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}