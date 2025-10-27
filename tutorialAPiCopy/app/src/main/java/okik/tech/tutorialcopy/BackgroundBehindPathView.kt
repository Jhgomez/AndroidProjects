package okik.tech.tutorialcopy

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RenderNode
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View

/**
 * This component draws a render node and draws a path on top of it
 */
class BackgroundInPathView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs){
    private var paint: Paint = Paint()
    private var backgroundSettings: BackgroundSettings? = null
    private var shouldClipPath: Boolean = true

    private val blurNode = RenderNode("UnderlyingView")
    var backgroundViewRenderNode: RenderNode? = null
    private var fallBackDrawable: Drawable? = null

    private var path: Path? = null

    /**
     * If no background to the effect holder was set then this won't change the view in any way
     */
    fun setPathPaint(paint: Paint) {
        this.paint = paint
    }

    fun setBackgroundConfigs(
        backgroundSettings: BackgroundSettings,
        backgroundViewRenderNode: RenderNode,
        path: Path,
        shouldClipPath: Boolean
    ) {
        this.backgroundSettings = backgroundSettings
        this.backgroundViewRenderNode = backgroundViewRenderNode
        this.path = path
        this.shouldClipPath = shouldClipPath
        setWillNotDraw(false)

        this.paint = backgroundSettings.backgroundOverlayPaint

        // if should not clip to background the effect is applied to
        // the drawing
        if (!backgroundSettings.shouldClipToBackground) {
            setRenderEffect(backgroundSettings.renderEffect)
        }
    }

    override fun draw(canvas: Canvas) {
        if (path != null && backgroundSettings != null) {
            if (shouldClipPath) {
                canvas.clipPath(path!!)
            }

            drawBackgroundRenderNode(canvas)

            canvas.drawPath(path!!, paint)
        }

//        super.draw(canvas)
    }

    private fun drawBackgroundRenderNode(canvas: Canvas) {
        blurNode.setPosition(0, 0, width, height)

        recordBackgroundViews()

        // Draw on the system canvas
        canvas.drawRenderNode(blurNode)
    }

    private fun recordBackgroundViews() {
        val recordingCanvas = blurNode.beginRecording()

        if (fallBackDrawable != null) {
            fallBackDrawable!!.draw(recordingCanvas)
        }

        if (backgroundSettings!!.shouldClipToBackground) {
            blurNode.setRenderEffect(backgroundSettings!!.renderEffect)
        }

        backgroundSettings!!.renderCanvasPositionCommand.invoke(recordingCanvas, this)

        recordingCanvas.drawRenderNode(backgroundViewRenderNode!!)

        blurNode.endRecording()
    }

    fun setFallbackBackground(frameClearDrawable: Drawable?) {
        this.fallBackDrawable = frameClearDrawable
    }
}