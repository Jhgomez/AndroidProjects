package okik.tech.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout

class TutorialDisplayLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private var currentLocation: IntArray = intArrayOf()
    var selectedView: View? = null

    private val contentCopy = RenderNode("ContentCopy")
    private val blurredContent = RenderNode("BlurredContent")
    private val focusedContent = RenderNode("FocusContent")

    private var VIEW_PADDING_PX = 120

    fun focusView(view: View?) {
        if (view != null) {
            currentLocation = intArrayOf(0, 0)

            view.getLocationOnScreen(currentLocation)

            var topBarHeight: Int

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                topBarHeight = rootWindowInsets?.getInsetsIgnoringVisibility(
                    WindowInsets.Type.statusBars()
                )?.top ?: 0
            } else {
                topBarHeight = rootWindowInsets?.systemWindowInsetTop ?: 0
            }

            currentLocation[1] = currentLocation[1] - topBarHeight
        }
        selectedView = view

        invalidate()
    }

    override fun drawChild(canvas: Canvas, child: View?, drawingTime: Long): Boolean {
        selectedView?.let { view ->
            // this custom layout should only have one child and we record/copy it with no effect here
            // into the content copy node, but note it doesn't end up being drawn on canvas
            contentCopy.setPosition(0, 0, width, height)
            val contentCopyRecordingCanvas = contentCopy.beginRecording()
            val isInvalidated = super.drawChild(contentCopyRecordingCanvas, child, drawingTime)
            contentCopy.endRecording()

            // the only child with no effect is in our previous render node, note it was
            // not draw on canvas. The one we will draw on canvas is the below render node, note
            // this copy made from the no effect render node above has the blur effect
            blurredContent.setRenderEffect(
                RenderEffect.createBlurEffect(
                    20f,
                    20f,
                    Shader.TileMode.CLAMP
                )
            )

            blurredContent.setPosition(0, 0, width, height)
            val blurredContentRecordingCanvas = blurredContent.beginRecording()
            blurredContentRecordingCanvas.drawRenderNode(contentCopy)
            blurredContent.endRecording()
            // draw the blurred content to this custom layout canvas
            canvas.drawRenderNode(blurredContent)

            if (currentLocation.size == 2) {
                focusedContent.setPosition(
                    0,
                    0,
                    view.width,
                    view.height
                )

                focusedContent.translationX = currentLocation[0].toFloat()
                focusedContent.translationY = currentLocation[1].toFloat()

                val focusAreaRecordingCanvas = focusedContent.beginRecording()

                focusAreaRecordingCanvas.translate(
                    -currentLocation[0].toFloat(),
                    -currentLocation[1].toFloat()
                )

                focusAreaRecordingCanvas.drawRenderNode(contentCopy)
                focusedContent.endRecording()

                canvas.drawRenderNode(focusedContent)

                return isInvalidated
            }

            return isInvalidated
        }

        // if no view is required to be focus just do
        return super.drawChild(canvas, child, drawingTime)
    }
}
