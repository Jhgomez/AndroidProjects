package okik.tech.myapplication

import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSmoothScroller.SNAP_TO_START
import androidx.recyclerview.widget.RecyclerView

class Listener(private val manager: LinearLayoutManager, private val callback: (Int) -> Unit) : RecyclerView.OnScrollListener() {
    var prev = 1

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)

        when(newState) {
            RecyclerView.SCROLL_STATE_IDLE -> {
                var firstVisible = manager.findFirstVisibleItemPosition()
                var firstCompVisible = manager.findFirstCompletelyVisibleItemPosition()

                var lastVisible = manager.findLastVisibleItemPosition()
                var lastCompVisible = manager.findLastCompletelyVisibleItemPosition()

                prev = firstCompVisible

                Log.d("FirstFrag", "firstVisible $firstVisible")
                Log.d("FirstFrag", "firstCompVisible $firstCompVisible")
                Log.d("FirstFrag", "lastVisible $lastVisible")
                Log.d("FirstFrag", "lastCompVisible $lastCompVisible")

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

                callback(firstCompVisible)

            }
            RecyclerView.SCROLL_STATE_DRAGGING -> {
                Log.d("FirstFragment", "Dragging")
            }
            RecyclerView.SCROLL_STATE_SETTLING -> {

                Log.d("FirstFragment", "SETTLING")
            }
        }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
    }
}