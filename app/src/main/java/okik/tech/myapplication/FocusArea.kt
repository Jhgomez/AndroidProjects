package okik.tech.myapplication

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RenderEffect
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import androidx.annotation.RequiresApi


/**
 * This method makes a copy in a "Recording Canvas" of the specified view but it also copies its
 * surroundings(the surrounding area is determined by surroundingThickness param), note if the background
 * of the layout containing your views has been set to "transparent" only the nodes within the
 * "window"(view + surrounding thickness) with a non transparent background will have the "focus"
 * feel(will not be applied any effect that are applied to the outer area), giving visual focus to the content
 * within the window. Nonetheless, you can add a different effect to the surrounding thickness area.
 * A limitation of this method is that the window area can't have rounded corners but in turn its content will
 * be an exact copy of the underlying view(if no effect applied to thickness area) as opposed with
 * the other method in which the surrounding area can have rounded corners but the surrounding area
 * will apply its effects(or paint color), if any, on top of any effect applied to the original
 * content(outer area, meaning, outside thickness area), like blur or a simple color painted
 * on top the screen
 *
 * @param view the view you want visual focus on
 * @param viewLocation in case you need to do some custom calculation to move the copy of the view
 * to the right location on screen, if null we will calculate it only from top to bottom(means we
 * remove the top inset) in an portrait orientation, no support for landscape orientation yet
 * @param surroundingThickness density pixels(dp) amount between the end of view and the outer area,
 * valid values are numbers >= 0
 * @param surroundingThicknessEffect if null, the copy of the surrounding is the same surrounding
 * as in original outer area
 * @param outerAreaEffect any render effect applied to surrounding area
 * @param overlayParams the params to be applied to overlay, if null it will match parent automatically
 * @param overlayPaint if null no overlay will be painted, otherwise an overlay on the full screen
 * will be painted using this paint object
 */

@RequiresApi(Build.VERSION_CODES.S)
class FocusArea private constructor(
    view: View,
    viewLocation: IntArray,
    surroundingThickness: Float,
    surroundingThicknessEffect: RenderEffect?,
    outerAreaEffect: RenderEffect?,
    overlayParams: LayoutParams,
    overlayPaint: Paint
){

    class Builder {
        private var view: View? = null
        private var viewLocation: IntArray? = null
        private var surroundingThickness: Byte = 0
        private var surroundingThicknessEffect: RenderEffect? = null
        private var outerAreaEffect: RenderEffect? = null
        private var overlayParams: LayoutParams? = null
        private var overlayPaint: Paint? = null

        fun setView(view: View) {
            this.view = view
        }

        fun setViewLocation(viewLocation: IntArray) {
            this.viewLocation = viewLocation
        }

        fun setSurroundingThickness(surroundingThickness: Byte) {
            this.surroundingThickness = surroundingThickness
        }

        fun setSurroundingThicknessEffect(surroundingThicknessEffect: RenderEffect) {
            this.surroundingThicknessEffect = surroundingThicknessEffect
        }

        fun setOuterAreaEffect(outerAreaEffect: RenderEffect?) {
            this.outerAreaEffect = outerAreaEffect
        }

        fun setOverlayParams(overlayParams: LayoutParams) {
            this.overlayParams = overlayParams
        }

        fun setOverlayPaint(overlayPaint: Paint) {
            this.overlayPaint= overlayPaint
        }


        fun build(): FocusArea {
            if (view == null) throw IllegalStateException("view can't be null")

            if (viewLocation == null) {
                viewLocation = intArrayOf(0, 0)

                view!!.getLocationOnScreen(viewLocation)

                var topBarHeight: Int

                topBarHeight = view!!.rootWindowInsets?.getInsetsIgnoringVisibility(
                    WindowInsets.Type.statusBars()
                )?.top ?: 0

                viewLocation!![1] = viewLocation!![1] - topBarHeight
            } else if (viewLocation!!.size != 2) {
                throw IllegalStateException("Location can only contain two values, x and y")
            }

            if (overlayParams == null) {
                overlayParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }

            if (overlayPaint == null) {
                overlayPaint = Paint()

                overlayPaint!!.color = Color.WHITE
                overlayPaint!!.alpha = 100
            }

            return FocusArea(
                view!!,
                viewLocation!!,
                if (surroundingThickness > 0) {
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        surroundingThickness.toFloat(),
                        view!!.resources.displayMetrics
                    )
                } else {
                    0f
                },
                surroundingThicknessEffect,
                outerAreaEffect,
                overlayParams!!,
                overlayPaint!!,
            )
        }
    }
}