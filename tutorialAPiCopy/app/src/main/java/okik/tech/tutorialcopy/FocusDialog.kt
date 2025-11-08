package okik.tech.tutorialcopy

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RecordingCanvas
import android.graphics.RenderEffect
import android.view.View

/**
 * @property pathViewRenderCanvasPositionCommand used to change the position of the RecordingCanvas
 * that paints the views behind the path view, most likely you won't ever need to set this value when using a
 * "TutorialDisplayLayout" as the path view always fill the whole screen
 * @property pathViewPathGeneratorCommand This call back is triggered when dialogView has been measured,
 * positioned and is about to be draw on screen, and it will pass a reference to the path object that can
 * be used to create a connection between the view that is created as a copy of the view that the
 * dialog is visually "attached" to and the dialogView, you should use things like the reference view
 * and dialog view location on screen to draw the path, you could use convenience methods in "RenderNodeBehindPathView"
 * to draw a connection easily. This callback is executed just before dialog is draw on screen, this
 * to get the actual position and measures of the dialog in cases like when either width or height is not
 * explicitly defined(MATCH_PARENT or 0dp with constraints on both sides of an edge)
 * @property dialogConstraintsCommand This call back gives you full control to set constraints and
 * margins(all position) of the dialog view that is added to a "DialogWrapperLayout" instance when
 * an instance of FocusDialog is passed to an instance of "TutorialDisplayLayout". DialogWrapperLayout
 * is a constraint layout, it only has three children, first, a view that serves as a clone of the
 * measures and location on screen of original view, is used to conceptually clone the view our dialog
 * will be visually attached to(you shouldn't change its constraints nor margins at all in this call back).
 * Second, a view to draw a path, you most likely want to use it to draw a path/bridge/connection
 * between the view that the dialog is visually attached to and the dialog, you don't need to constraint
 * this one(no reference to it is passed here). Third, the dialog view we intend to constraint and
 * define margins for. This callback is executed before dialog is added and after the reference view
 * is already added to the constraint layout hierarchy
 */
class FocusDialog private constructor(
    val originBackgroundPaint: Paint,
    val referenceViewLocation: IntArray,
    val referenceViewWidth: Int,
    val referenceViewHeight: Int,
    val shouldClipToBackground: Boolean,
    val dialogView: View,
    val backgroundRenderEffect: RenderEffect?,
    val pathViewRenderCanvasPositionCommand: (RecordingCanvas, View) -> Unit,
    val pathViewPathGeneratorCommand: ((refView: View, dialog: View) -> Path)?,
    val dialogConstraintsCommand: (constraintLayout: DialogWrapperLayout, refView: View, dialog: View) -> Unit
){
    class Builder {
        private var dialogBackgroundPaint: Paint? = null
        private var referenceView: View? = null
        private var referenceViewWidth: Int? = null
        private var referenceViewHeight: Int? = null
        private var referenceViewLocation: IntArray? = null
        private var shouldClipToBackground: Boolean = true
        private var view: View? = null
        private var backgroundRenderEffect: RenderEffect? = null
        private var pathViewRenderCanvasPositionCommand: (RecordingCanvas, View) -> Unit = { _, _ -> }
        private var pathViewPathGeneratorCommand: ((refView: View, dialog: View) -> Path)? = null
        private var dialogConstraintsCommand: ((constraintLayout: DialogWrapperLayout, refView: View, dialog: View) -> Unit)? = null

        fun setPathViewRenderCanvasPositionCommand(pathViewRenderCanvasPositionCommand: (RecordingCanvas, View) -> Unit): Builder {
            this.pathViewRenderCanvasPositionCommand = pathViewRenderCanvasPositionCommand
            return this
        }

        fun setPathViewPathGeneratorCommand(
            pathViewPathGeneratorCommand: ((refView: View, dialog: View) -> Path)?
        ): Builder {
            this.pathViewPathGeneratorCommand = pathViewPathGeneratorCommand
            return this
        }

        fun setDialogConstraintsCommand(
            dialogConstraintsCommand: (constraintLayout: DialogWrapperLayout, refView: View, dialog: View) -> Unit
        ): Builder {
            this.dialogConstraintsCommand = dialogConstraintsCommand
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

            if (dialogConstraintsCommand == null) {
                throw IllegalStateException("You have to explicitly set constraints for you dialog, set dialogConstraintsCommand value in FocusDialogBuilder, use methods like constraintDialogToBottom in DialogWrapperLayout")
            }

            return FocusDialog(
                dialogBackgroundPaint!!,
                referenceViewLocation!!,
                referenceViewWidth!!,
                referenceViewHeight!!,
                shouldClipToBackground,
                view!!,
                backgroundRenderEffect,
                pathViewRenderCanvasPositionCommand,
                pathViewPathGeneratorCommand,
                dialogConstraintsCommand!!
            )
        }
    }
}