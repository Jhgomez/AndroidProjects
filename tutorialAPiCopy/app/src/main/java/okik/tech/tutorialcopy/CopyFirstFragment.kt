package okik.tech.tutorialcopy

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import okik.tech.tutorialcopy.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class CopyFirstFragment : Fragment() {

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



        val adapter = MyAdapter(listOf(1,2,3,4))
        binding.recycler.adapter = adapter

        val listener = Listener(
            manager,
            { firstCompVisible ->
                var smoothScroller = object : LinearSmoothScroller(requireContext()){


                    override fun getHorizontalSnapPreference(): Int {
                        Log.d("FirstFrag", "HSnap")
                        return SNAP_TO_START
                    }

                    // 3
                    override fun onTargetFound(
                        targetView: View,
                        state: RecyclerView.State,
                        action: Action
                    ) {
                        super.onTargetFound(targetView, state, action)
                        Log.d("FirstFrag", "Found")
                    }

                    //1
                    override fun calculateDxToMakeVisible(view: View, snapPreference: Int): Int {
                        Log.d("FirstFrag", "DxToMake")
                        val layoutManager = getLayoutManager()
                        if (layoutManager == null || !layoutManager.canScrollHorizontally()) {
                            return 0
                        }
                        val params = view.getLayoutParams() as RecyclerView.LayoutParams
                        val left = layoutManager.getDecoratedLeft(view) - params.leftMargin
                        val right = layoutManager.getDecoratedRight(view) + params.rightMargin
                        val start = layoutManager.getPaddingLeft()
                        val end = layoutManager.getWidth() - layoutManager.getPaddingRight()
                        Log.d("FirstFrag", "left $left")
                        Log.d("FirstFrag", "right $right")
                        Log.d("FirstFrag", "start $start")
                        Log.d("FirstFrag", "end $end")

                        val ret = calculateDtToFit(left, right, start, end, snapPreference)

                        Log.d("FirstFrag", "ret $ret")
                        return ret
                    }
                }



//                binding.recycler.offsetChildrenHorizontal(0)
                smoothScroller.targetPosition = firstCompVisible
//        manager.startSmoothScroll(smoothScroller)
//                Thread.sleep(1000)


//                manager.offsetChildrenHorizontal(0)
//                manager.scrollToPositionWithOffset(firstCompVisible, 0)
                manager.startSmoothScroll(smoothScroller)

                Log.d("FirstFrag", "Shoulve scrolled")
            }
        )


        var smoothScroller = object : LinearSmoothScroller(requireContext()){

            override fun getHorizontalSnapPreference(): Int {
                Log.d("FirstFrag", "HSnap")
                return SNAP_TO_START
            }

            // 3
            override fun onTargetFound(
                targetView: View,
                state: RecyclerView.State,
                action: Action
            ) {
                super.onTargetFound(targetView, state, action)
                Log.d("FirstFrag", "Found")
            }

            //1
            override fun calculateDxToMakeVisible(view: View, snapPreference: Int): Int {
                Log.d("FirstFrag", "DxToMake")
                val layoutManager = getLayoutManager()
                if (layoutManager == null || !layoutManager.canScrollHorizontally()) {
                    return 0
                }
                val params = view.getLayoutParams() as RecyclerView.LayoutParams
                val left = layoutManager.getDecoratedLeft(view) - params.leftMargin
                val right = layoutManager.getDecoratedRight(view) + params.rightMargin
                val start = layoutManager.getPaddingLeft()
                val end = layoutManager.getWidth() - layoutManager.getPaddingRight()
                Log.d("FirstFrag", "left $left")
                Log.d("FirstFrag", "right $right")
                Log.d("FirstFrag", "start $start")
                Log.d("FirstFrag", "end $end")

                val ret = calculateDtToFit(left, right, start, end, snapPreference)

                Log.d("FirstFrag", "ret $ret")
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
                        var firstVisible = manager.findFirstVisibleItemPosition()
                        var firstCompVisible = manager.findFirstCompletelyVisibleItemPosition()

                        Log.d("FirstFrag", "firstVisible $firstVisible")
                        Log.d("FirstFrag", "firstCompVisible $firstCompVisible")


//                        var aView = recyclerView.get(firstCompVisible)
//
//                        var location = intArrayOf(0, 0)
//
//                        aView.getLocationInWindow(location)
//                        Log.d("FirstFrag", "WindowLoc ${location[0]} ${location[1]}")
//
//                        aView.getLocationOnScreen(location)
//                        Log.d("FirstFrag", "ScreenLoc ${location[0]} ${location[1]}")
//
//                        aView.getLocationInSurface(location)
//                        Log.d("FirstFrag", "SurfLoc ${location[0]} ${location[1]}")

                        if (!scrollProcessed) {


//                                if (scrolledToRigth) firstCompVisible else firstVisible
//                            var flexibleIndex = when {
//                                scrolledToRigth && lastDx >= 10 -> {
//                                    Log.d("FirstFrag", "r>20")
//                                    firstCompVisible
//                                }
//                                scrolledToRigth && lastDx < 10 -> {
//                                    Log.d("FirstFrag", "r<20")
//                                    firstVisible
//                                }
//                                !scrolledToRigth && lastDx <= -10 -> {
//                                    Log.d("FirstFrag", "l<-20")
//                                    firstVisible
//                                }
//                                !scrolledToRigth && lastDx > -10 -> {
//                                    Log.d("FirstFrag", "l>-20")
//                                    firstCompVisible
//                                }
//                                else -> throw IllegalStateException("bad state")
//                            }

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

//                            Thread.sleep(300)
                            automaticScrollRunning = false

                            Log.d("FirstFrag", "Shoulve scrolled")
                        }

                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        scrollProcessed = false

                        Log.d("FirstFragment", "Dragging")
                    }
                    RecyclerView.SCROLL_STATE_SETTLING -> {
//                        recyclerView.stopScroll()
//                        recyclerView.stopNestedScroll()
                        Log.d("FirstFragment", "SETTLING")
                    }
                }
            }



            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                if (!automaticScrollRunning)

                if (!scrollProcessed) {

                    scrolledToRigth = dx > 0

                    if (scrolledToRigth && dx >= 5 || !scrolledToRigth && dx <= -5) {
                        lastDx = dx
                        Log.d("FirstFrag", "dx $dx")
                        Log.d("FirstFrag", "toRight $scrolledToRigth")
                    }
                }

//                recyclerView.stopScroll()
            }
        }









        binding.recycler.addOnScrollListener(listen)

//        smoothScroller.targetPosition = 3
//        manager.startSmoothScroll(smoothScroller)
//        binding.recycler.smoothScrollToPosition(3)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}