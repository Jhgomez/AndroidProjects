package okik.tech.myapplication

import android.app.ActionBar
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
import android.widget.Button
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import okik.tech.myapplication.databinding.FragmentFirstBinding
import okik.tech.myapplication.databinding.OverlayTransparentBinding
import okik.tech.myapplication.databinding.RecyclerItemBinding

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

        val listener = Listener(
            manager,
            { firstCompVisible ->
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

                    //1
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

                smoothScroller.targetPosition = firstCompVisible

                manager.startSmoothScroll(smoothScroller)
            }
        )


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
            binding.viewOverlay.visibility = View.VISIBLE
            binding.root.setRenderEffect(RenderEffect.createBlurEffect(10f, 10f, Shader.TileMode.DECAL))

            val aView = manager.findViewByPosition(2)
            val clone = RecyclerItemBinding.inflate(layoutInflater).root

//            val aView = binding.buttonFirst
//            val clone = MaterialButton(requireContext())
//            clone.text = "Next"

            showPopup(binding.root, aView, clone)
        }
    }

    private fun showPopup(button: View, aView: View?, viewClone: View) {
        var location = intArrayOf(0, 0)

        aView?.getLocationOnScreen(location)

        val tutorialLayout = TutorialLayout(requireContext())

        var topBarHeight: Int

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            topBarHeight = binding.root.rootWindowInsets?.getInsetsIgnoringVisibility(
                WindowInsets.Type.statusBars())?.top ?: 0
        } else {
            topBarHeight = binding.root.rootWindowInsets?.systemWindowInsetTop ?: 0
        }

        location[1] = location[1] - topBarHeight

        val content = OverlayTransparentBinding.inflate(layoutInflater)

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

        tutorialLayout.setUpCloneWithBackgroundAndDialog(
            originalView =  aView ?: throw IllegalArgumentException("View not found in recycler"),
            position = location,
            clone = viewClone,
            dialogContent = content.root,
            gravity =  "bottom",
            dialogXOffsetDp = 60f,
            dialogYOffsetDp = 32f,
            originOffsetDp = .5f,
            destinationOffsetDp = .5f,
            shouldCenterOnMainAxis = true
        )

        val popi = PopupWindow(
            tutorialLayout,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            false // closes on outside touche if true
        )

        popi.showAtLocation(binding.root, Gravity.NO_GRAVITY, 0, 0)

        content.tb.setOnClickListener {
            popi.dismiss()
            binding.viewOverlay.visibility = View.INVISIBLE
            binding.root.setRenderEffect(null)

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}