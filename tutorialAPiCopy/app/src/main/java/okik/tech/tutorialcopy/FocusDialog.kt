package okik.tech.tutorialcopy

import android.app.Dialog
import android.graphics.Color
import android.graphics.Paint
import android.view.Gravity
import android.view.View

class FocusDialog private constructor(
    val dialogBackgroundPaint: Paint,
    val referenceViewLocation: IntArray,
    val referenceViewWidth: Int,
    val referenceViewHeight: Int,
    val shouldClipToBackground: Boolean,
    val gravity: Int,
    val dialogXMarginDp: Float,
    val dialogYMarginDp: Float,
    val originOffsetPercent: Float,
    val destinationOffsetPercent: Float,
    val shouldCenterOnMainAxis: Boolean,
    val view: View
){
    class Builder {
        var dialogBackgroundPaint: Paint? = null
        var referenceView: View? = null
        var referenceViewWidth: Int? = null
        var referenceViewHeight: Int? = null
        var referenceViewLocation: IntArray? = null
        var shouldClipToBackground: Boolean = true
        var gravity: Int = Gravity.BOTTOM
        var dialogXMarginDp: Float = 0f
        var dialogYMarginDp: Float = 0f
        var originOffsetPercent: Float = 0.5f
        var destinationOffsetPercent: Float = 0.5f
        var shouldCenterOnMainAxis: Boolean = false
        var view: View? = null

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
        fun setDialogXMarginDp(dialogXMarginDp: Float): Builder{
            this.dialogXMarginDp = dialogXMarginDp
            return this
        }
        fun setDialogYMarginDp(dialogYMarginDp: Float): Builder{
            this.dialogYMarginDp = dialogYMarginDp
            return this
        }
        fun setOriginOffsetPercent(originOffsetPercent: Float): Builder{
            this.originOffsetPercent = originOffsetPercent
            return this
        }
        fun setDestinationOffsetPercent(destinationOffsetPercent: Float): Builder{
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
                dialogXMarginDp,
                dialogYMarginDp,
                originOffsetPercent,
                destinationOffsetPercent,
                shouldCenterOnMainAxis,
                view!!
            )
        }
    }
}