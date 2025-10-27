package okik.tech.tutorialcopy

import android.app.Dialog
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RenderEffect
import android.view.Gravity
import android.view.View
import kotlin.contracts.Effect

class FocusDialog private constructor(
    val dialogBackgroundPaint: Paint,
    val referenceViewLocation: IntArray,
    val referenceViewWidth: Int,
    val referenceViewHeight: Int,
    val shouldClipToBackground: Boolean,
    val gravity: Int,
    val dialogXMarginDp: Float,
    val dialogYMarginDp: Float,
    val originOffsetPercent: Double,
    val destinationOffsetPercent: Double,
    val shouldCenterOnMainAxis: Boolean,
    val view: View,
    val backgroundRenderEffect: RenderEffect?
){
    class Builder {
        var dialogBackgroundPaint: Paint? = null
        var referenceView: View? = null
        var referenceViewWidth: Int? = null
        var referenceViewHeight: Int? = null
        var referenceViewLocation: IntArray? = null
        var shouldClipToBackground: Boolean = true
        var gravity: Int = Gravity.BOTTOM
        var dialogXMarginDp: Short = 0
        var dialogYMarginDp: Short = 0
        var originOffsetPercent: Double = 0.5
        var destinationOffsetPercent: Double = 0.5
        var shouldCenterOnMainAxis: Boolean = false
        var view: View? = null
        var backgroundRenderEffect: RenderEffect? = null

        fun setDialogBackgroundPaint(dialogBackgroundPaint: Paint): Builder{
            this.dialogBackgroundPaint = dialogBackgroundPaint
            return this
        }
        fun setReferenceViewLocation(referenceViewLocation: IntArray): Builder{
            this.referenceViewLocation = referenceViewLocation
            return this
        }
        fun setShouldClipToBackground(shouldClipToBackground: Boolean): Builder{
            this.shouldClipToBackground = shouldClipToBackground
            return this
        }
        fun setGravity(gravity: Int): Builder{
            this.gravity = gravity
            return this
        }
        fun setDialogXMarginDp(dialogXMarginDp: Short): Builder{
            this.dialogXMarginDp = dialogXMarginDp
            return this
        }
        fun setDialogYMarginDp(dialogYMarginDp: Short): Builder{
            this.dialogYMarginDp = dialogYMarginDp
            return this
        }
        fun setOriginOffsetPercent(originOffsetPercent: Double): Builder{
            this.originOffsetPercent = originOffsetPercent
            return this
        }
        fun setDestinationOffsetPercent(destinationOffsetPercent: Double): Builder{
            this.destinationOffsetPercent = destinationOffsetPercent
            return this
        }
        fun setShouldCenterOnMainAxis(shouldCenterOnMainAxis: Boolean): Builder{
            this.shouldCenterOnMainAxis  = shouldCenterOnMainAxis
            return this
        }

        fun setView(view: View): Builder {
            this.view = view
            return this
        }

        fun setReferenceView(view: View): Builder {
            this.referenceView = view
            return this
        }

        fun setBackgroundRenderEffect(backgroundRenderEffect: RenderEffect): Builder  {
            this.backgroundRenderEffect = backgroundRenderEffect
            return this
        }

        fun build(): FocusDialog {
            if (view == null) throw IllegalStateException("A view has to be defined")

            if (dialogBackgroundPaint == null) {
                val paint = Paint()
                paint.color = Color.WHITE
                paint.alpha = 170
                paint.isAntiAlias = true
                paint.style = Paint.Style.FILL

                dialogBackgroundPaint = paint
            }

            if (referenceView != null) {
                if(referenceViewLocation == null) {
                    referenceViewLocation = IntArray(2)
                    this.referenceView!!.getLocationInWindow(this.referenceViewLocation)
                }

                this.referenceViewWidth = referenceView!!.width
                this.referenceViewHeight = referenceView!!.height
            } else {
                throw IllegalStateException("You have to pass a reference to the view to attach the dialog to")
            }

            return FocusDialog(
                dialogBackgroundPaint!!,
                referenceViewLocation!!,
                referenceViewWidth!!,
                referenceViewHeight!!,
                shouldClipToBackground,
                gravity,
                dpToPx(dialogXMarginDp, this.referenceView!!.context),
                dpToPx(dialogYMarginDp, this.referenceView!!.context),
                originOffsetPercent,
                destinationOffsetPercent,
                shouldCenterOnMainAxis,
                view!!,
                backgroundRenderEffect
            )
        }
    }
}