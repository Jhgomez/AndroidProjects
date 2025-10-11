package okik.tech.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.FrameLayout

// this component is just a frame layout wrapped in a rounded corner shape, the difference
// with a material card is that this one doesn't have "card elevation" and is intended to be passed
// any view programmatically. It wraps its content automatically
class DialogContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs){
    private var paint: Paint
    private var cornerRadius: Float

    init {
        paint = Paint()
        paint.color = Color.WHITE
        paint.alpha = 100
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 8f
        paint.isAntiAlias = true

        cornerRadius = 80f

        setBackgroundColor(Color.TRANSPARENT)
    }

    fun updateBackgroundPaint(paint: Paint) {
        this.paint = paint
        invalidate()
    }

    fun updateCornerRadius(radius: Float) {
        cornerRadius = radius
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        // Draw the rounded rectangle
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), cornerRadius, cornerRadius, paint)
    }
}