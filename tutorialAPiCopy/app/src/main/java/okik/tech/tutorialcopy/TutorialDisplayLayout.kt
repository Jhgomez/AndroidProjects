package okik.tech.tutorialcopy

import android.content.Context
import android.graphics.Canvas
import android.graphics.RenderNode
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.core.view.size

/**
 * This custom layout is expected to only have one child, which is usually a Linear or Constraint
 * Layout or similar.
 */
class TutorialDisplayLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private var dialogWrapperLayout: DialogWrapperLayout? = null

    private val contentCopy: RenderNode?

    private var popup: PopupWindow? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            contentCopy = RenderNode("ContentCopy")
        } else {
            contentCopy = null
        }
    }

    fun renderFocusAreaWithDialog(focusArea: FocusArea, focusDialog: FocusDialog) {
//        this.renderFocusArea(focusArea)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // this custom layout should only have one child and we record its content with no effect here
            // so we can make a copy of the area we want to focus on
            contentCopy!!.setPosition(0, 0, width, height)

            val contentCopyRecordingCanvas = contentCopy.beginRecording()

            getChildAt(0).draw(contentCopyRecordingCanvas)

            contentCopy.endRecording()
        }

        dialogWrapperLayout = DialogWrapperLayout(context)

        dialogWrapperLayout!!.configuredDialog(
            focusArea,
            focusDialog,
            contentCopy
        )

        popup = PopupWindow(
            dialogWrapperLayout,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            true // closes on outside touche if true
        )

        this.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}
            override fun onViewDetachedFromWindow(v: View) { hideTutorialComponents() }
        })

        popup!!.setOnDismissListener(PopupWindow.OnDismissListener { popup = null })

        popup!!.showAtLocation(this, Gravity.NO_GRAVITY, 0, 0)
    }

    fun hideTutorialComponents() {
        if (popup != null && popup!!.isShowing) popup!!.dismiss()

        popup = null
    }

    override fun drawChild(canvas: Canvas, child: View?, drawingTime: Long): Boolean {
        // child at 0 should always be the only child added, by the user, to this custom view
        if (getChildAt(0).id == child?.id) {

            // this could have been executed in an onPreDrawListener, however it is triggered
            // too often there while here is only triggered when something has actually change
            // in this child, and this helps us update the background in the dialog and in path view
            if (dialogWrapperLayout != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // this custom layout should only have one child and we record its content with no effect here
                    // so we can make a copy of the area we want to focus on
                    contentCopy!!.setPosition(0, 0, width, height)

                    val contentCopyRecordingCanvas = contentCopy.beginRecording()

                    val isInvalidatedIssued =
                        super.drawChild(contentCopyRecordingCanvas, child, drawingTime)
                    contentCopy.endRecording()

                    canvas.drawRenderNode(contentCopy)

                    dialogWrapperLayout!!.updateBackground(contentCopy)

                    return isInvalidatedIssued
                }
            }
        }

        // if no focus area has been specified just render the node
        val invalidated = super.drawChild(canvas, child, drawingTime)
        return invalidated
    }
}

