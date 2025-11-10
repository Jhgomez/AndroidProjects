package okik.tech.tutorialcopy

import android.app.Activity
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
 * This custom layout is expected to only have one child, if you add more children they won't render
 * when displaying a focus view and/or visually attaching a dialog. This layout use a popup window dialog
 * to disable any interaction with the child view, by default it will be in the same exact position/location
 * and with thw same width and height as this layout
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

    /**
     * When you display a dialog using this API you'll be interacting with two components you need to
     * configure correctly(besides the dialog view you pass in the focus dialog object), you will interact
     * with an instance of "BackgroundEffectRendererLayout" which serves as the view that lets you add visual
     * focus on the view you pass in the FocusArea object, on Android 31 and above you have the ability to
     * add RenderEffects(eg. blur), whether or not you pass a RenderEffect, you have to control the background
     * views location that are render behind this focus view, since the PopupWindow instance used to render
     * all this components will have same size and location as the instance of this layout, you have to
     * make sure the location of the view you're passing is relative to this layout, to get the location of
     * the view you use apis las "getTop", "getLeft", "View.getLocationOnScreen" however they have different
     * implications you need to be aware of, nevertheless, your responsibility is to make sure that location
     * is relative to origin of this layout(beware the plane in android starts at the left top corner and
     * grows in X axis to the right and grows in Y axis towards bottom", one way to do it right is to
     * get location on screen and then subtract top status bar insets and top bar(if any) if portrait orientation and
     * bottom navigation bar left inset and top bar(if any) if landscape orientation, as long as you calculate
     * this correctly, the views behind focus area will be render correctly, it really depends on where in the
     * view hierarchy you have added this layout to. Usually you'd want to add it as the root of you app(in
     * your main activity layout)
     */
    fun renderFocusAreaWithDialog(focusArea: FocusArea, focusDialog: FocusDialog) {
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

        val loc = IntArray(2)
        getLocationOnScreen(loc)

        val possibleWidth = resources.displayMetrics.widthPixels - loc[0]
        val possibleHeight = resources.displayMetrics.heightPixels - loc[1]

        popup = PopupWindow(
            dialogWrapperLayout,
            width.coerceAtMost(possibleWidth),
            height.coerceAtMost(possibleHeight),
            true // closes on outside touche if true
        )

        this.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}
            override fun onViewDetachedFromWindow(v: View) { hideTutorialComponents() }
        })

        popup!!.setOnDismissListener(PopupWindow.OnDismissListener { popup = null })

        popup!!.showAtLocation(this, Gravity.NO_GRAVITY, loc[0], loc[1])
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

