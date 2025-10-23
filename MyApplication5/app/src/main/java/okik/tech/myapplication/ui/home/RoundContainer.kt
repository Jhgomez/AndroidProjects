package okik.tech.myapplication.ui.home

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.setPadding

// this component is just a frame layout wrapped in a rounded corner shape, the difference
// with a material card is that this one doesn't have "card elevation" and is intended to be passed
// any view programmatically. It wraps its content automatically. Be aware you have to add padding to it
// everytime you need
class RoundContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs){
    private var paint: Paint = Paint()
    private var cornerRadius: Float = 0f

    init {
        val effectHolderBackground = View(context)
        effectHolderBackground.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        val roundShape = RoundRectShape(
            floatArrayOf(60f, 60f, 60f, 60f, 60f, 60f, 60f, 60f),
//            RectF(0f, 0f, 100f, 100f),
                null,
            null
        )

        val shapeDrawable = ShapeDrawable(roundShape)
        val drawable = InsetDrawable(shapeDrawable, 20)

//        drawable.setPadding(30, 30, 30, 30)
//        drawable.bounds = Rect(10, 10, 290, 100)
//        drawable.

        effectHolderBackground.background = drawable

        paint = shapeDrawable.paint
        paint.color = Color.GREEN
        paint.alpha = 255
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 8f
        paint.isAntiAlias = true
//        cornerRadius = 80f




        effectHolderBackground.setRenderEffect(
            RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
        )

        addView(effectHolderBackground)

        setBackgroundColor(Color.TRANSPARENT) // custom viewgroups need this call otherwise they wont be visible
    }

    /**
     * If no background to the effect holder was set then this won't change the view in any way
     */
    fun updateEffectHolderBackgroundPaint(paint: Paint) {
        this.paint.alpha = paint.alpha
        this.paint.style = paint.style
        this.paint.strokeWidth = paint.strokeWidth
        this.paint.isAntiAlias = true
        this.paint.color = paint.color
        invalidate()
    }

    fun updateEffectHolderBackgroundEffect(effect: RenderEffect) {
        getChildAt(0).setRenderEffect(effect)
    }

    /**
     * This view has a "helper view" to hold any drawable and RenderEffect, the helper view makes
     * easy to render an effect and different shapes like when using ShapeDrawables, etc. It also
     * enables us to have a drawable on this helper view and another in the actual RoundContainer
     * instance
     */
    fun updateEffectHolderBackgroundDrawable(drawable: Drawable) {
        getChildAt(0).background = drawable
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
        val backgroundDrawable = getChildAt(0).background

        if (backgroundDrawable is InsetDrawable) {
            getChildAt(0).background = InsetDrawable(backgroundDrawable.drawable, padding)

        } else if (backgroundDrawable != null) {
            val drawable = InsetDrawable(backgroundDrawable, padding)

            getChildAt(0).background = drawable
        }
    }
}