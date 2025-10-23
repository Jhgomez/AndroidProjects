package okik.tech.tutorialcopy

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
 * @param overlayColor if null no overlay will be painted, otherwise an overlay on the full screen
 * will be painted using this paint object
 */

@RequiresApi(Build.VERSION_CODES.S)
class FocusArea private constructor(
    val view: View,
    val viewLocation: IntArray,
    val surroundingThickness: SurroundingThickness,
    val surroundingThicknessEffect: RenderEffect?,
    val roundedCornerSurrounding: RoundedCornerSurrounding?,
    val outerAreaEffect: RenderEffect?,
    val overlayParams: LayoutParams,
    val overlayColor: Int,
    val overlayAlpha: Float
){

    /**
     * All params are in DP and will be converter to PX automatically
     */
    data class SurroundingThickness(
        val top: Float,
        val bottom: Float,
        val start: Float,
        val end: Float
    )

    /**
     * if this object is passed to a focus area object it will rectangle behind the view the user wants
     * to add visual focus on but surrounding area will not be an exact copy of the actual surrounding,
     * and instead this rounded corner surrounding will be created on top of any effect applied to
     * the outer area(outside the focus view area)
     */
    data class RoundedCornerSurrounding(
        val paint: Paint,
        val cornerRadius: Short,
        val innerPadding: InnerPadding
    )

    /**
     * Lets you add padding to rounded corner surrounding, useful when applying overlay effects
     * to rounded corner surrounding area
     */
    data class InnerPadding(
        val top: Float,
        val bottom: Float,
        val start: Float,
        val end: Float
    )

    class Builder {
        private var view: View? = null
        private var viewLocation: IntArray? = null
        private var surroundingThickness: SurroundingThickness? = null
        private var surroundingThicknessEffect: RenderEffect? = null
        private var roundedCornerSurrounding: RoundedCornerSurrounding? = null
        private var outerAreaEffect: RenderEffect? = null
        private var overlayParams: LayoutParams? = null
        private var overlayColor: Int = Color.TRANSPARENT
        private var overlayAlpha: Float = 0.0f

        fun setView(view: View): Builder {
            this.view = view
            return this
        }

        fun setViewLocation(viewLocation: IntArray): Builder {
            this.viewLocation = viewLocation
            return this
        }

        /**
         * @param surroundingThickness value is considered to be DP(it will be converted to PX automatically)
         */
        fun setSurroundingThickness(surroundingThickness: SurroundingThickness): Builder {
            this.surroundingThickness = surroundingThickness
            return this
        }

        /**
         * @param surroundingThicknessEffect the effect to be applied to the surrounding area. There
         * is a difference in what happens when a rounded corner surrounding is specified and when not,
         * but first you have to be aware that this is related to the effect applied to the outer area,
         * when no rounded corner specified you automatically get an exact copy of the surrounding area
         * which size is determined by the specified thickness, if you also specify the effect to be
         * applied(with this method), then it is applied to that exact part of the original view, while
         * the outer area is applied the other effect(if any effect was applied/passed in the builder
         * with method #setOuterAreaEffect) without affecting surrounding area but the limitation is that
         * it can not have rounded corners, it only supports sharp edges(no rounded). The other scenario
         * is when you specify a rounded surrounding in which case the outer area effect is also applied to the
         * area below rounded surrounding view, that is because the rounded corner surrounding is
         * literally draw on top of the original view, which again, might be affected by the specified outer
         * area effect, but the rounded area at the same time can have any effect applied, note that
         * the rounded area effect is not actually affecting the views below it, it is actually just
         * being draw on top of it
         */
        fun setSurroundingThicknessEffect(surroundingThicknessEffect: RenderEffect): Builder {
            this.surroundingThicknessEffect = surroundingThicknessEffect
            return this
        }

        fun setRoundedCornerSurrounding(roundedCornerSurrounding: RoundedCornerSurrounding): Builder {
            this.roundedCornerSurrounding = roundedCornerSurrounding
            return this
        }

        fun setOuterAreaEffect(outerAreaEffect: RenderEffect?): Builder {
            this.outerAreaEffect = outerAreaEffect
            return this
        }

        fun setOverlayParams(overlayParams: LayoutParams): Builder {
            this.overlayParams = overlayParams
            return this
        }

        fun setOverlayColor(overlayColor: Int): Builder {
            this.overlayColor = overlayColor
            return this
        }

        /**
         * @param overlayAlpha values from 0 to 1 where 0 is fully transparent and 1 is fully opaque
         */
        fun setOverlayAlpha(overlayAlpha: Float): Builder {
            this.overlayAlpha = overlayAlpha
            return this
        }

        // should only be called in the build method
        private fun dpToPx(dp: Float): Float {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                this.view!!.resources.displayMetrics
            )
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

            if (surroundingThickness == null) {
                surroundingThickness = SurroundingThickness(0f, 0f, 0f, 0f)
            } else {
                surroundingThickness = SurroundingThickness(
                    dpToPx(surroundingThickness!!.top),
                    dpToPx(surroundingThickness!!.bottom),
                    dpToPx(surroundingThickness!!.start),
                    dpToPx(surroundingThickness!!.end)
                )
            }

            if (overlayParams == null) {
                overlayParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }

            if (roundedCornerSurrounding != null) {
                roundedCornerSurrounding = RoundedCornerSurrounding(
                    roundedCornerSurrounding!!.paint,
                    roundedCornerSurrounding!!.cornerRadius,
                    InnerPadding(
                        dpToPx(roundedCornerSurrounding!!.innerPadding.top),
                        dpToPx(roundedCornerSurrounding!!.innerPadding.bottom),
                        dpToPx(roundedCornerSurrounding!!.innerPadding.start),
                        dpToPx(roundedCornerSurrounding!!.innerPadding.end),
                    )
                )
            }

            return FocusArea(
                view!!,
                viewLocation!!,
                surroundingThickness!!,
                surroundingThicknessEffect,
                roundedCornerSurrounding,
                outerAreaEffect,
                overlayParams!!,
                overlayColor,
                overlayAlpha
            )
        }
    }
}