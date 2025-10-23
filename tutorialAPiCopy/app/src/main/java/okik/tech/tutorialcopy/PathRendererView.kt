package okik.tech.tutorialcopy

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.graphics.Path
import android.view.View

class PathRendererView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val path: Path
    private var paint: Paint

    init {
        path = Path()

        paint = Paint()
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        paint.alpha = 100
        paint.isAntiAlias = true // to render smooth edges
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawPath(path, paint)
    }

    fun moveTo(x: Float, y: Float) {
        path.moveTo(x, y)
//        invalidate() // requests a redraw
    }

    fun lineTo(x: Float, y: Float) {
        path.lineTo(x, y)
//        invalidate() // requests a redraw
    }

    fun cloze() {
        path.close()
        invalidate() // requests a redraw
    }

    fun setPaint(paint: Paint) {
        this.paint = paint
        invalidate()
    }
}