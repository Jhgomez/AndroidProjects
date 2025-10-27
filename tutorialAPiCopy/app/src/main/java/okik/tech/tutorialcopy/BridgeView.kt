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
 * This component is just a frame layout wrapped in a rounded corner shape, the difference
 * with a material card is that this one doesn't have "card elevation" and also you can apply
 * effects to its background without affecting the foreground(its content). The background, which
 * enables to render effects, exclusively, without affecting the content of the container, is actually
 * a view that lives behind the rest of the content, and is added automatically when view is
 * instantiated, it is referred to as "effectHolderBackground"
 */
class BridgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs){
    private var paint: Paint = Paint()
    private var backgroundSettings: BlurBackgroundSettings? = null

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
        backgroundSettings: BlurBackgroundSettings,
        backgroundViewRenderNode: RenderNode,
        path: Path
    ) {
        this.backgroundSettings = backgroundSettings
        this.backgroundViewRenderNode = backgroundViewRenderNode
        this.path = path
        setWillNotDraw(false)

        this.paint = backgroundSettings.backgroundOverlayPaint

        // if should not clip to background the effect is applied to
        // the drawing
        if (!backgroundSettings.shouldClipToBackground) {
            setRenderEffect(backgroundSettings.renderEffect)
        }
    }

    override fun draw(canvas: Canvas) {
        if (path != null) {
            canvas.clipPath(path!!)
        }

        if (backgroundSettings != null) {
            drawBackgroundRenderNode(canvas)
        }

        if (path != null) {
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