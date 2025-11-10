package okik.tech.tutorialcopy

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RenderEffect
import android.view.View

/**
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
    val pathViewPathPaint: Paint,
    val dialogView: View,
    val pathViewBackgroundRenderEffect: RenderEffect?,
    val pathViewPathGeneratorCommand: ((focusView: View, dialog: View) -> Path)?,
    val dialogConstraintsCommand: (constraintLayout: DialogWrapperLayout, focusView: View, dialog: View) -> Unit
){
    class Builder {
        private var pathViewPathPaint: Paint? = null
        private var dialogView: View? = null
        private var pathViewBackgroundRenderEffect: RenderEffect? = null
        private var pathViewPathGeneratorCommand: ((refView: View, dialog: View) -> Path)? = null
        private var dialogConstraintsCommand: ((constraintLayout: DialogWrapperLayout, refView: View, dialog: View) -> Unit)? = null

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
            this.pathViewPathPaint = dialogBackgroundPaint
            return this
        }

        fun setDialogView(view: View): Builder {
            this.dialogView = view
            return this
        }

        fun setPathViewBackgroundRenderEffect(backgroundRenderEffect: RenderEffect?): Builder  {
            this.pathViewBackgroundRenderEffect = backgroundRenderEffect
            return this
        }

        fun build(): FocusDialog {
            if (dialogView == null) throw IllegalStateException("A view has to be defined")

            if (pathViewPathPaint == null) {
                val paint = Paint()
                paint.color = Color.WHITE
                paint.alpha = 170
                paint.isAntiAlias = true
                paint.style = Paint.Style.FILL

                pathViewPathPaint = paint
            }

            if (dialogConstraintsCommand == null) {
                throw IllegalStateException("You have to explicitly set constraints for you dialog, set dialogConstraintsCommand value in FocusDialogBuilder, use methods like constraintDialogToBottom in DialogWrapperLayout")
            }

            return FocusDialog(
                pathViewPathPaint!!,
                dialogView!!,
                pathViewBackgroundRenderEffect,
                pathViewPathGeneratorCommand,
                dialogConstraintsCommand!!
            )
        }
    }
}