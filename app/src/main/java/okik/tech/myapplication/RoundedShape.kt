package okik.tech.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class RoundedShape @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : View(context, attrs) {
    private var radius: Float
    private var paint: Paint

    init {
        val passAttrs = context.obtainStyledAttributes(attrs, R.styleable.RoundedShape)

        try {
            radius =
                passAttrs.getFloat(R.styleable.RoundedShape_shapeCornerRadius, 8f)

       } finally {
            passAttrs.recycle()
        }

        paint = Paint()
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        paint.alpha = 100
        paint.isAntiAlias = true // to render smooth edges
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), radius, radius, paint)
    }

    fun MoveToPosition(position: IntArray, xOffset: Int, yOffset: Int) {
        translationX = position[0].toFloat() + xOffset
        translationY = position[1].toFloat() + yOffset
    }

    fun setRadius(value: Float) {
        radius = value
        invalidate()
    }

    fun setPaint(paint: Paint) {
        this.paint = paint
        invalidate()
    }
}