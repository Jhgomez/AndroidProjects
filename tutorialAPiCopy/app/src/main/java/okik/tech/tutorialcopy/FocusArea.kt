package okik.tech.tutorialcopy

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat


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
 * @param overlayColor if null no overlay will be painted, otherwise an overlay on the full screen
 * will be painted using this paint object
 */

@RequiresApi(Build.VERSION_CODES.S)
class FocusArea private constructor(
    val view: View,
    val viewLocation: IntArray,
    val surroundingThickness: SurroundingThickness,
    val surroundingThicknessEffect: RenderEffect?,
    val surroundingAreaPaint: Paint,
    val surroundingAreaPadding: InnerPadding,
    val surroundingAreaBackgroundDrawable: Drawable,
    val shouldClipToBackground: Boolean,
    val outerAreaEffect: RenderEffect?,
    val overlayColor: Int,
    val overlayAlpha: Short,
) {
    /**
     * All params are in DP and will be converter to PX automatically
     */
    data class SurroundingThickness(
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
        private var surroundingAreaPaint: Paint? = null
        private var surroundingAreaPadding: InnerPadding? = null
        private var surroundingAreaBackgroundDrawable: Drawable? = null
        private var shouldClipToBackground: Boolean = true
        private var outerAreaEffect: RenderEffect? = null
        private var overlayColor: Int = Color.TRANSPARENT
        private var overlayAlpha: Short = 125


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

        fun setOuterAreaEffect(outerAreaEffect: RenderEffect?): Builder {
            this.outerAreaEffect = outerAreaEffect
            return this
        }

        fun setOuterAreaOverlayColor(overlayColor: Int): Builder {
            this.overlayColor = overlayColor
            return this
        }

        /**
         * @param overlayAlpha values from 0 to 255 where 0 is fully transparent and 255 is fully opaque
         */
        fun setOuterAreaOverlayAlpha(overlayAlpha: Short): Builder {
            this.overlayAlpha = overlayAlpha
            return this
        }

        fun setSurroundingAreaPaint(surroundingAreaPaint: Paint): Builder {
            this.surroundingAreaPaint = surroundingAreaPaint
            return this
        }

        /**
         * Using paddings when clip to background is true may be a little buggy at the moment, but these
         * work as expected when clip to background is false, when clip to padding is true you should use
         * different tick values for the edge you need to be a different size
         */
        fun setSurroundingAreaPadding(surroundingAreaPadding: InnerPadding): Builder {
            this.surroundingAreaPadding = surroundingAreaPadding
            return this
        }

        /**
         * Consider creating drawables at runtime, drawables defined in XML could be complex to configure
         * correctly when used in this library APIs. Drawables created at runtime doesn't have much automatic
         * configs, making it easy to configure its format with a simple Paint instance. You should
         * prefer shape drawables as these are the only drawables tested with at the moment
         */
        fun setSurroundingAreaBackgroundDrawable(surroundingAreaBackgroundDrawable: Drawable): Builder {
            this.surroundingAreaBackgroundDrawable = surroundingAreaBackgroundDrawable
            return this
        }

        /**
         * This property in combination with others renders very specific behaviours on screen, if
         * set to false then a copy of outer area(with the specified outer area overlay color,
         * alpha and effect) is draw in the surroundings of the view you want to focus on, so if you
         * pass a render effect to the surrounding area it will apply that copy of the surrounding area
         * the surrounding area is always a rectangle defined by the start, top, end, bottom thickness, this
         * means you'd see a sharp edged rectangle/square with another effect applied in addition to the one already
         * added by the outer area properties and effect. If you pass a background drawable like a shape
         * drawable, it will be drawn on top of the surrounding area with the characteristics previously
         * mentioned and the drawable will have the specified paint applied to it(note that for now this only
         * works with shape drawables with other type of drawables you have to modify "manually"), so if you
         * add an effect to the surrounding area in this set up you will also see it applied to the drawable
         * so a very useful use case would be to pass the same type of render effect and with same exact
         * values and define clip to background as false and define a background drawable, and add inner padding
         * if applying effects like blur, to give space to the effect to be fully visible, if used with blur
         * it will create soft edges background shapes. If you need sharp edges that can have rounded corners
         * and different shapes then set this to true, if you set it to true, and render a completely transparent
         * color/paint you will have a feel of a hollow/hole which is an exact copy of the surrounding views of the
         * original content(below the render effect and overlay of the outer area) and to this copy of
         * the surrounding area you can apply effects, and a paint in combination of a drawable(at this
         * time we assume you'd like to use shape drawables). Be aware the rendering process is always the
         * same to the surrounding area, before drawing anything to the view that holds the focus area
         * we draw the copy of either the below content with effect or the original content(without any effect)
         * as explained above and then we draw the background which serves us as an overlay as we apply the
         * paint to this drawable that in a normal/common use case it is the background and not an overlay
         * but in our use case the actual background is the copy of the views below. In short
         * if set to false render effect is applied to the surrounding underlying views as well as
         * the surrounding area overlay. If set to true(default), render effect is applied to the copy of the
         * original views only and no the drawable attached as background which is the overlay of the
         * surrounding area
         */
        fun setShouldClipToBackground(shouldClipToBackground: Boolean): Builder {
            this.shouldClipToBackground = shouldClipToBackground
            return this
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
                    dpToPx(surroundingThickness!!.top, this.view!!.context),
                    dpToPx(surroundingThickness!!.bottom, this.view!!.context),
                    dpToPx(surroundingThickness!!.start, this.view!!.context),
                    dpToPx(surroundingThickness!!.end, this.view!!.context)
                )
            }


            if (surroundingAreaPadding == null) {
                surroundingAreaPadding = InnerPadding(0f, 0f, 0f, 0f)
            } else {
                surroundingAreaPadding = InnerPadding(
                    dpToPx(surroundingAreaPadding!!.top, this.view!!.context),
                    dpToPx(surroundingAreaPadding!!.bottom, this.view!!.context),
                    dpToPx(surroundingAreaPadding!!.start, this.view!!.context),
                    dpToPx(surroundingAreaPadding!!.end, this.view!!.context)
                )
            }

            if (surroundingAreaBackgroundDrawable == null) {
                surroundingAreaBackgroundDrawable = dispatchDefaultDrawable(this.view!!.context)
            }

            if (surroundingAreaPaint == null) {
                val paint = Paint()
                paint.color = Color.WHITE
                paint.alpha = 170
                paint.isAntiAlias = true
                paint.style = Paint.Style.FILL

                surroundingAreaPaint = paint
            }

            return FocusArea(
                view!!,
                viewLocation!!,
                surroundingThickness!!,
                surroundingThicknessEffect,
                surroundingAreaPaint!!,
                surroundingAreaPadding!!,
                surroundingAreaBackgroundDrawable!!,
                shouldClipToBackground,
                outerAreaEffect,
                overlayColor,
                overlayAlpha
            )
        }
    }
}