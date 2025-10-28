package okik.tech.tutorialcopy

import android.graphics.Color
import android.graphics.Paint
import android.graphics.RecordingCanvas
import android.graphics.RenderEffect
import android.view.Gravity
import android.view.View

class FocusDialog private constructor(
    val originBackgroundPaint: Paint,
    val referenceViewLocation: IntArray,
    val referenceViewWidth: Int,
    val referenceViewHeight: Int,
    val shouldClipToBackground: Boolean,
    val dialogGravity: Int,
    val dialogXMarginDp: Float,
    val dialogYMarginDp: Float,
    val originOffsetPercent: Double,
    val destinationOffsetPercent: Double,
    val centerDialogOnMainAxis: Boolean,
    val dialogView: View,
    val backgroundRenderEffect: RenderEffect?,
    val originRenderCanvasPositionCommand: (RecordingCanvas, View) -> Unit
){
    class Builder {
        private var dialogBackgroundPaint: Paint? = null
        private var referenceView: View? = null
        private var referenceViewWidth: Int? = null
        private var referenceViewHeight: Int? = null
        private var referenceViewLocation: IntArray? = null
        private var shouldClipToBackground: Boolean = true
        private var gravity: Int = Gravity.BOTTOM
        private var dialogXMarginDp: Short = 0
        private var dialogYMarginDp: Short = 0
        private var originOffsetPercent: Double = 0.5
        private var destinationOffsetPercent: Double = 0.5
        private var centerDialogOnMainAxis: Boolean = false
        private var view: View? = null
        private var backgroundRenderEffect: RenderEffect? = null
        private var originRenderCanvasPositionCommand: (RecordingCanvas, View) -> Unit = { _,_ -> }

        fun setOriginRenderCanvasPositionCommand(renderCanvasPositionCommand: (RecordingCanvas, View) -> Unit): Builder {
            this.originRenderCanvasPositionCommand = renderCanvasPositionCommand
            return this
        }

        fun setOriginBackgroundPaint(dialogBackgroundPaint: Paint): Builder{
            this.dialogBackgroundPaint = dialogBackgroundPaint
            return this
        }
        fun setReferenceViewLocation(referenceViewLocation: IntArray): Builder{
            this.referenceViewLocation = referenceViewLocation
            return this
        }
        fun setReferenceViewSize(width: Int, height: Int): Builder {
            this.referenceViewWidth = width
            this.referenceViewHeight = height

            return this
        }
        fun setShouldClipToBackground(shouldClipToBackground: Boolean): Builder{
            this.shouldClipToBackground = shouldClipToBackground
            return this
        }
        fun setDialogGravity(gravity: Int): Builder{
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
        fun setCenterDialogOnMainAxis(centerDialogOnMainAxis: Boolean): Builder{
            this.centerDialogOnMainAxis  = centerDialogOnMainAxis
            return this
        }

        fun setDialogView(view: View): Builder {
            this.view = view
            return this
        }

        /**
         * This is just a convenience method as it helps to automatically set location, width and height
         * properties, however if location, width and height values are explicitly set they those
         * values will have precedence and the value inferred from this parameter will be ignored
         */
        fun setReferenceView(view: View): Builder {
            this.referenceView = view
            return this
        }

        fun setBackgroundRenderEffect(backgroundRenderEffect: RenderEffect?): Builder  {
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

                if (referenceViewWidth == null && referenceViewHeight == null) {
                    this.referenceViewWidth = referenceView!!.width
                    this.referenceViewHeight = referenceView!!.height
                }
            }

            if (referenceViewLocation == null || referenceViewWidth == null || referenceViewHeight == null ) {
                throw IllegalStateException("You have to pass either a view reference or a view location and view width and height explicitly")
            }

            return FocusDialog(
                dialogBackgroundPaint!!,
                referenceViewLocation!!,
                referenceViewWidth!!,
                referenceViewHeight!!,
                shouldClipToBackground,
                gravity,
                dpToPx(dialogXMarginDp, this.view!!.context),
                dpToPx(dialogYMarginDp, this.view!!.context),
                originOffsetPercent,
                destinationOffsetPercent,
                centerDialogOnMainAxis,
                view!!,
                backgroundRenderEffect,
                originRenderCanvasPositionCommand
            )
        }
    }
}