package okik.tech.tutorialcopy

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.FrameLayout

// this component is just a frame layout wrapped in a rounded corner shape, the difference
// with a material card is that this one doesn't have "card elevation" and is intended to be passed
// any view programmatically. It wraps its content automatically. Be aware you have to add padding to it
// everytime you need
class RoundContainer @JvmOverloads constructor(
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

        setBackgroundColor(Color.TRANSPARENT) // custom viewgroups need this call otherwise they wont be visible
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

        canvas.drawLine(0f, 0f, 350f, 2000f, paint)
        // Draw the rounded rectangle
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), cornerRadius, cornerRadius, paint)
    }
}