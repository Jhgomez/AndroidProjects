package okik.tech.tutorialcopy

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.RenderNode
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.ShapeDrawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import androidx.core.graphics.ColorUtils


/**
 * This component is just a frame layout wrapped in a rounded corner shape, the difference
 * with a material card is that this one doesn't have "card elevation" and also you can apply
 * effects to its background without affecting the foreground(its content). The background, which
 * enables to render effects, exclusively, without affecting the content of the container, is actually
 * a view that lives behind the rest of the content, and is added automatically when view is
 * instantiated, it is referred to as "effectHolderBackground"
 */
class FocusSurrounding @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs){
    private var paint: Paint = Paint()

    private var backgroundView: View? = null
    private var focusArea: FocusArea? = null

    private val backgroundViewLocation = IntArray(2)
    private val ownLocation = IntArray(2)

    private val blurNode = RenderNode("BlurView node")
    var backgroundViewRenderNode: RenderNode? = null
    private var frameClearDrawable: Drawable? = null

    init {
//        setBackgroundColor(Color.TRANSPARENT) // custom viewgroups need this call otherwise they wont be visible
    }

    /**
     * If no background to the effect holder was set then this won't change the view in any way
     */
    fun setEffectHolderBackgroundPaint(paint: Paint) {
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
    fun setEffectHolderBackgroundDrawable(drawable: Drawable) {
        background = drawable

        setClipToOutline(true)
        setOutlineProvider(object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline) {
                getBackground().getOutline(outline)
                outline.setAlpha(1f)
            }
        })

        if (drawable is ShapeDrawable) this.paint = drawable.paint
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
    fun setEffectHolderBackgroundPadding(padding: Int) {
        val backgroundDrawable = background

        if (backgroundDrawable is InsetDrawable) {
            background = InsetDrawable(backgroundDrawable.drawable, padding)

        } else if (backgroundDrawable != null) {
            val drawable = InsetDrawable(backgroundDrawable, padding)

            background = drawable
        }
    }




//    private var backgroundView: View? = null
//    private var focusArea: FocusArea? = null
//    private val blurNode = RenderNode("BlurView node")
//    val backgroundViewRenderNode: RenderNode? = null
//    private val frameClearDrawable: Drawable? = null

    fun renderNodeBlurController(
        backgroundView: View,
        focusArea: FocusArea,
        backgroundViewRenderNode: RenderNode
    ) {
        this.backgroundView = backgroundView
        this.focusArea = focusArea
        this.backgroundViewRenderNode = backgroundViewRenderNode
        setWillNotDraw(false)
    }

    override fun draw(canvas: Canvas) {
        if (backgroundView != null && focusArea != null) {
            saveOnScreenLocation()

            hardwarePath(canvas)
        }

        super.draw(canvas)
    }

    private fun saveOnScreenLocation() {
        getLocationOnScreen(ownLocation)
        backgroundView!!.getLocationOnScreen(backgroundViewLocation)
    }

    private fun hardwarePath(canvas: Canvas) {
        blurNode.setPosition(0, 0, width, height)

//        updateRenderNodeProperties()

        drawSnapshot()

        // Draw on the system canvas
        canvas.drawRenderNode(blurNode)

        if (focusArea!!.roundedCornerSurrounding!!.paint.color != Color.TRANSPARENT) {

            canvas.drawColor(
                ColorUtils.setAlphaComponent(
                    focusArea!!.roundedCornerSurrounding!!.paint.color,
                    focusArea!!.roundedCornerSurrounding!!.paint.alpha
                )
            )
        }
    }

    private fun updateRenderNodeProperties() {
        val layoutTranslationX = -getLeftValue().toFloat()
        val layoutTranslationY = -getTopValue().toFloat()

        // Pivot point for the rotation and scale (in case it's applied)
        blurNode.setPivotX(width / 2f - layoutTranslationX)
        blurNode.setPivotY(height / 2f - layoutTranslationY)
//
//        blurNode.setTranslationX(-width.toFloat())
//        blurNode.setTranslationY()
    }

    private fun drawSnapshot() {
        val recordingCanvas = blurNode.beginRecording()
        if (frameClearDrawable != null) {
            frameClearDrawable!!.draw(recordingCanvas)
        }

        recordingCanvas.translate(
            -focusArea!!.viewLocation[0].toFloat() + focusArea!!.surroundingThickness.start,
            -focusArea!!.viewLocation[1].toFloat() + focusArea!!.surroundingThickness.top,
        )

        recordingCanvas.drawRenderNode(backgroundViewRenderNode!!)
        // Looks like the order of this doesn't matter

        blurNode.setRenderEffect(focusArea!!.surroundingThicknessEffect)

        blurNode.endRecording()
    }

    fun setFallbackBackground(frameClearDrawable: Drawable?) {
        this.frameClearDrawable = frameClearDrawable
    }

    private fun getTopValue(): Int {
        return ownLocation[1] - backgroundViewLocation[1]
    }

    private fun getLeftValue(): Int {
        return ownLocation[0] - backgroundViewLocation[0]
    }

}