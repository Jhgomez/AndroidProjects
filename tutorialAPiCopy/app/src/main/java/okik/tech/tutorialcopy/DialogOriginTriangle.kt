package okik.tech.tutorialcopy

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.graphics.Path
import android.view.View

class DialogOriginTriangle @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : View(context, attrs) {
    // This works in combination with TOP or BOTTOM orientation it won't work with LEFT nor RIGHT
    // orientation
    private var xOriginOffsetPercent: Float
    // This works in combination with LEFT or RIGHT orientation, it won't work with TOP nor BOTTOM
    private var yOriginOffsetPercent: Float

    // this component is intended to be the origin of a dialog, so if set to "Buttom", it will be a triangle upside down
    // "Top" is a normal triangle, "right" the triangle is rotated to the right(growing from left right to left), "left"
    // triangle is rotated to left(it grows from left to right)
    private var orientation: String

    private val path: Path
    private var paint: Paint

    init {
        val passAttrs = context.obtainStyledAttributes(attrs, R.styleable.DialogOriginTriangle)

        try {
            yOriginOffsetPercent =
                passAttrs.getFloat(R.styleable.DialogOriginTriangle_YOriginOffsetPercent, 0.5f)

            xOriginOffsetPercent =
                passAttrs.getFloat(R.styleable.DialogOriginTriangle_XOriginOffsetPercent, 0.5f)

            orientation =
                passAttrs.getString(R.styleable.DialogOriginTriangle_Orientation) ?: "bottom"

       } finally {
            passAttrs.recycle()
        }

        path = Path()

        paint = Paint()
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        paint.alpha = 100
        paint.isAntiAlias = true // to render smooth edges
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        path.reset()

        val triangleHeight = height.toFloat()
        val triangleWidth = width.toFloat()

        var xOrigin: Float
        var yOrigin: Float

        var xFirstVertex: Float
        var yFirstVertex: Float

        var xSecondVertex: Float
        var ySecondVertex: Float

        when(orientation.lowercase()) {
            "bottom" -> {
                xOrigin = triangleWidth * xOriginOffsetPercent
                yOrigin = triangleHeight
                xFirstVertex = triangleWidth
                yFirstVertex = 0f
                xSecondVertex = 0f
                ySecondVertex = 0f
            }
            "top" -> {
                xOrigin = triangleWidth * xOriginOffsetPercent
                yOrigin = 0f
                xFirstVertex = triangleWidth
                yFirstVertex = triangleHeight
                xSecondVertex = 0f
                ySecondVertex = triangleHeight
            }
            "left" -> {
                xOrigin = 0f
                yOrigin = triangleHeight * yOriginOffsetPercent
                xFirstVertex = triangleWidth
                yFirstVertex = 0f
                xSecondVertex = triangleWidth
                ySecondVertex = triangleHeight
            }
            "right" -> {
                xOrigin = triangleWidth
                yOrigin = triangleHeight * yOriginOffsetPercent
                xFirstVertex = 0f
                yFirstVertex = 0f
                xSecondVertex = 0f
                ySecondVertex = triangleHeight
            }
            else -> throw IllegalArgumentException("bad orientation")
        }

        path.moveTo(xOrigin, yOrigin)

        path.lineTo(xFirstVertex, yFirstVertex)
        path.lineTo(xSecondVertex, ySecondVertex)
        path.close()

        canvas.drawPath(path, paint)
    }

    fun MoveToPosition(position: IntArray, xOffset: Int, yOffset: Int) {
        translationX = position[0].toFloat() + xOffset
        translationY = position[1].toFloat() + yOffset
    }

    fun setDivideByOriginInitX(value: Float) {
        xOriginOffsetPercent = value
        invalidate()
    }

    fun setDivideByOriginInitY(value: Float) {
        yOriginOffsetPercent = value
        invalidate()
    }

    fun setTrianglePosition(value: String) {
        orientation = value
        invalidate()
    }

    fun setPaint(paint: Paint) {
        this.paint = paint
        invalidate()
    }
}