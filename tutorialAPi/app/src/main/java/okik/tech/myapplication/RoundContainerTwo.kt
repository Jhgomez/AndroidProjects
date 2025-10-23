package okik.tech.myapplication

import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import androidx.annotation.RequiresApi

/**
 * This component is just a frame layout wrapped in a rounded corner shape, the difference
 * with a material card is that this one doesn't have "card elevation" and also you can apply
 * effects to its background without affecting the foreground(its content). The background, which
 * enables to render effects, exclusively, without affecting the content of the container, is actually
 * a view that lives behind the rest of the content, and is added automatically when view is
 * instantiated, it is referred to as "effectHolderBackground"
 */
class RoundContainerTwo @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs){
    private var paint: Paint = Paint()
    private var cornerRadius: Float = 0f

    init {
        val effectHolderBackground = View(context)
        effectHolderBackground.layoutParams =
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        effectHolderBackground.visibility = GONE

//        addView(effectHolderBackground)

        setBackgroundColor(Color.TRANSPARENT) // custom viewgroups need this call otherwise they wont be visible
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
     * This effect is only applied to "helper" view, which was added automatically
     * with the purpose of being able to render effects on the background of this custom view
     * without affecting the foreground views(rest of its children)
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun setEffectHolderBackgroundEffect(effect: RenderEffect?) {
//        getChildAt(0).setRenderEffect(effect)
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
//        getChildAt(0).background = drawable
//
//        getChildAt(0).setClipToOutline(true)
//        getChildAt(0).setOutlineProvider(object : ViewOutlineProvider() {
//            override fun getOutline(view: View?, outline: Outline) {
//                getChildAt(0).getBackground().getOutline(outline)
//                outline.setAlpha(1f)
//            }
//        })
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
//        val backgroundDrawable = getChildAt(0).background
//
//        if (backgroundDrawable is InsetDrawable) {
//            getChildAt(0).background = InsetDrawable(backgroundDrawable.drawable, padding)
//
//        } else if (backgroundDrawable != null) {
//            val drawable = InsetDrawable(backgroundDrawable, padding)
//
//            getChildAt(0).background = drawable
//        }
    }
}