package okik.tech.tutorialcopy

import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.RenderNode
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.ShapeDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout

/**
 * This component is just a frame layout wrapped in a rounded corner shape, the difference
 * with a material card is that this one doesn't have "card elevation" and also you can apply
 * effects to its background without affecting the foreground(its content). The background, which
 * enables to render effects, exclusively, without affecting the content of the container, is actually
 * a view that lives behind the rest of the content, and is added automatically when view is
 * instantiated, it is referred to as "effectHolderBackground"
 */
class BackgroundEffectRendererLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs){
    private var paint: Paint = Paint()
    private var backgroundSettings: BackgroundSettings? = null

    private val effectApplierRenderNode: RenderNode?
    var backgroundViewRenderNode: RenderNode? = null
    private var fallBackDrawable: Drawable? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            effectApplierRenderNode = RenderNode("Effect node")
        } else {
            effectApplierRenderNode = null
        }
    }

    /**
     * If no background to the effect holder was set then this won't change the view in any way
     */
    fun clonePaintToBackgroundDrawable(paint: Paint) {
        this.paint.alpha = paint.alpha
        this.paint.style = paint.style
        this.paint.strokeWidth = paint.strokeWidth
        this.paint.isAntiAlias = true
        this.paint.color = paint.color
    }

    /**
     * This custom view has a "helper view" to hold any drawable(as background) and RenderEffect(its applied
     * to "helper view"), the helper view makes easy to render an effect and different shapes like when using
     * ShapeDrawables, etc. It also enables us to render a drawable on this helper view and another in the actual
     * RoundContainer instance(if you ever need to). You can use it in combination with method "setEffectHolderBackgroundEffect"
     * to apply the effect without affecting other views(foreground) in this container and gives the possibility to create
     * views that have effects on its background without affecting the foreground, which is something only available
     * in the current android sdk through "blur windows" in combination with something like "DialogFragment"s and is
     * limited to blur effects only, also try using "setEffectHolderBackgroundPadding" and "setEffectHolderBackgroundPaint".
     * Be aware drawables(when set as backgrounds) always fills full width and height of the the view that contains
     * them, at least for "ShapeDrawables", so there is no need to set LayoutParams and if you want it to add padding
     * to the helper view's background drawable, use "setEffectHolderBackgroundPadding", this is helpful to allow
     * an effect to have the space it needs to render correctly/fully on thew view's canvas
     */
    fun configureDrawableAsBackground(drawable: Drawable) {
        background = drawable

        if (drawable is ShapeDrawable) this.paint = drawable.paint
    }

    fun clipToBackground() {
        setClipToOutline(true)
        setOutlineProvider(object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline) {
                getBackground().getOutline(outline)
                outline.setAlpha(1f)
            }
        })
    }

    /**
     * This convenience method won't do nothing if the user didn't set up a Background Drawable to the
     * effect holder view using the method {@link #updateEffectHolderBackgroundDrawable(Drawable)}, but
     * if a drawable background was set up, it makes easy to add different paddings to the background,
     * however NOTE that this padding is only applied to the background making the USER RESPONSIBLE for
     * adding padding to the the instance of the container itself, this represents a cumbersome responsibility
     * for the user if not aware of the behavior of this custom view but it also enables interesting
     * and more flexible UI possibilities
     */
    fun setPaddingToBackgroundDrawable(
        top: Int,
        bottom: Int,
        start: Int,
        end: Int
    ) {
        val backgroundDrawable = background

        if (backgroundDrawable is InsetDrawable) {
            background = InsetDrawable(backgroundDrawable.drawable, start, top, end, bottom)

        } else if (backgroundDrawable != null) {
            val drawable = InsetDrawable(backgroundDrawable, start, top, end, bottom)

            background = drawable
        }
    }

    fun setBackgroundConfigs(
        backgroundSettings: BackgroundSettings,
        backgroundViewRenderNode: RenderNode?
    ) {
        setBackgroundRenderNode(backgroundViewRenderNode)
        setBackgroundConfigs(backgroundSettings)
    }

    fun setBackgroundConfigs(backgroundSettings: BackgroundSettings) {
        this.backgroundSettings = backgroundSettings
        setWillNotDraw(false)

        configureDrawableAsBackground(backgroundSettings.backgroundDrawable)

        setPaddingToBackgroundDrawable(
            backgroundSettings.padding.top.toInt(),
            backgroundSettings.padding.bottom.toInt(),
            backgroundSettings.padding.start.toInt(),
            backgroundSettings.padding.end.toInt()
        )

        if (backgroundSettings.shouldClipToBackground) {
            clipToBackground()
        }

        clonePaintToBackgroundDrawable(backgroundSettings.backgroundOverlayPaint)

        // if should not clip to background the effect is applied to
        // the drawing
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S && !backgroundSettings.shouldClipToBackground) {
            setRenderEffect(backgroundSettings.renderEffect)
        }
    }

    fun setBackgroundRenderNode(backgroundViewRenderNode: RenderNode?) {
        this.backgroundViewRenderNode = backgroundViewRenderNode
    }

    override fun draw(canvas: Canvas) {
        if (backgroundSettings != null && effectApplierRenderNode != null) {
            drawBackgroundRenderNode(canvas)
        }

        super.draw(canvas)
    }

    private fun drawBackgroundRenderNode(canvas: Canvas) {
        effectApplierRenderNode!!.setPosition(0, 0, width, height)

        recordBackgroundViews()

        if (backgroundSettings!!.shouldClipToBackground) {
            effectApplierRenderNode.setRenderEffect(backgroundSettings!!.renderEffect)
        }

        // Draw on the system canvas
        canvas.drawRenderNode(effectApplierRenderNode)
    }

    private fun recordBackgroundViews() {
        val recordingCanvas = effectApplierRenderNode!!.beginRecording()
        if (fallBackDrawable != null) {
            fallBackDrawable!!.draw(recordingCanvas)
        }

        backgroundSettings!!.renderCanvasPositionCommand.invoke(recordingCanvas, this)

        recordingCanvas.drawRenderNode(backgroundViewRenderNode!!)

        effectApplierRenderNode.endRecording()
    }

    fun setFallbackBackground(frameClearDrawable: Drawable?) {
        this.fallBackDrawable = frameClearDrawable
    }
}