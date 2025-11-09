package okik.tech.tutorialcopy

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RecordingCanvas
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.View

/**
 * This component draws a render node and draws a path on top of it
 */
class RenderNodeBehindPathView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs){
    private var paint: Paint = Paint()
    private var renderCanvasPositionCommand: (RecordingCanvas, View) -> Unit = { _, _ -> }
    private var renderEffect: RenderEffect? = null

    private var setEffectOnBackgroundOnly = true
    private var shouldClipPath: Boolean = true

    private val blurNode: RenderNode?
    private var backgroundViewRenderNode: RenderNode? = null
    private var fallBackDrawable: Drawable? = null

    private var path: Path? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            blurNode = RenderNode("UnderlyingView")
        } else {
            blurNode = null
        }
    }

    /**
     * If no background to the effect holder was set then this won't change the view in any way
     */
    fun setPathPaint(paint: Paint) {
        this.paint = paint
    }

    fun setBackgroundViewRenderNode(renderNode: RenderNode) {
        this.backgroundViewRenderNode = renderNode
    }

    fun getPath(): Path = this.path ?: Path()

    fun setBackgroundConfigs(
        backgroundViewRenderNode: RenderNode?,
        path: Path,
        pathPaint: Paint,
        shouldClipPath: Boolean,
        setEffectOnBackgroundOnly: Boolean,
        renderEffect: RenderEffect?,
        renderCanvasPositionCommand: (RecordingCanvas, View) -> Unit
    ) {
        this.renderEffect = renderEffect
        this.setEffectOnBackgroundOnly = setEffectOnBackgroundOnly
        this.backgroundViewRenderNode = backgroundViewRenderNode
        this.path = path
        this.shouldClipPath = shouldClipPath
        this.renderCanvasPositionCommand = renderCanvasPositionCommand
        setWillNotDraw(false)

        this.paint = pathPaint

//        // if should not clip to background the effect is applied to
//        // the drawing
//        if (!backgroundSettings.shouldClipToBackground) {
//            setRenderEffect(backgroundSettings.renderEffect)
//        }
        if (!setEffectOnBackgroundOnly && backgroundViewRenderNode != null) {
            setRenderEffect(renderEffect)
        }
    }

    override fun draw(canvas: Canvas) {
        if (path != null) {
            if (shouldClipPath) {
                canvas.clipPath(path!!)
            }

            if (blurNode != null) {
                drawBackgroundRenderNode(canvas)
            }

            canvas.drawPath(path!!, paint)
        }

//        super.draw(canvas)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
    }

    private fun drawBackgroundRenderNode(canvas: Canvas) {
        blurNode!!.setPosition(0, 0, width, height)

        recordBackgroundViews()

        // Draw on the system canvas
        canvas.drawRenderNode(blurNode)
    }

    private fun recordBackgroundViews() {
        val recordingCanvas = blurNode!!.beginRecording()

        if (fallBackDrawable != null) {
            fallBackDrawable!!.draw(recordingCanvas)
        }

        if (setEffectOnBackgroundOnly) {
            blurNode.setRenderEffect(renderEffect)
        }

        renderCanvasPositionCommand.invoke(recordingCanvas, this)

        recordingCanvas.drawRenderNode(backgroundViewRenderNode!!)

        blurNode.endRecording()
    }

    fun setFallbackBackground(frameClearDrawable: Drawable?) {
        this.fallBackDrawable = frameClearDrawable
    }
}