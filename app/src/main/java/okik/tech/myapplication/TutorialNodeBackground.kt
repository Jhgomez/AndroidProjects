package okik.tech.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class TutorialNodeBackground @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs){
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