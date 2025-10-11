package okik.tech.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.graphics.Path
import android.widget.FrameLayout

class TutorialDialogContainer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {
    // 0 will start triangle from left top or bottom corner, 1 will start triangle from right top
    // or bottom towards bottom or top edge depending on Y value in vertical orientation
    // TODO: 0 will start triangle from left or right top, 1 will start triangle from left or right
    //  bottom towards right or left opposite side depending on Y value in horizontal orientation
    private var divideByOriginInitX: Float
    // 0 will start triangle from top towards bottom, 1 will start triangle from bottom towards top in vertical orientation
    // TODO: 0 will start from left edge towards right, 1 will start right towards left edge in a horizontal orientation
    private var divideByOriginInitY: Float

    private var trianglePositionOffsetPercentage: Float

    private var trianglePosition: String

    private var orientation: String

    private val path: Path
    private val paint: Paint

    init {
        val passAttrs = context.obtainStyledAttributes(attrs, R.styleable.TutorialDialogContainer)

        try {
            divideByOriginInitY =
                passAttrs.getFloat(R.styleable.TutorialDialogContainer_DivideByOriginInitY, 0f)

            divideByOriginInitX =
                passAttrs.getFloat(R.styleable.TutorialDialogContainer_DivideByOriginInitX, 2f)

            trianglePositionOffsetPercentage =
                passAttrs.getFloat(R.styleable.TutorialDialogContainer_TrianglePositionOffsetPercentage, .5f)

            if (divideByOriginInitY < 0f || divideByOriginInitX < 0f || trianglePositionOffsetPercentage < 0f) {
                throw IllegalArgumentException("attribute can not be negative")
            }

            orientation =
                passAttrs.getString(R.styleable.TutorialDialogContainer_Orientation) ?: "vertical"

            trianglePosition =
                passAttrs.getString(R.styleable.TutorialDialogContainer_TrianglePosition) ?: "top"

//            triangleWidth =
//                passAttrs.getInt(R.styleable.TutorialDialogContainer_TriangleWidth, layoutParams.width/5)
        } finally {
            passAttrs.recycle()
        }

        path = Path()

        paint = Paint()
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true // to render smooth edges
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        path.reset()

        val triangleHeight = height * 0.2f
        val triangleWidth = width * 0.2f

        val triangleXOrigin = when(trianglePosition.lowercase()) {
            "top", "bottom" -> {
                width * trianglePositionOffsetPercentage
            }
            "right" -> {
                0f
            }
            else -> {
                width - triangleWidth
            }
        }

        val triangleYOrigin = when(trianglePosition.lowercase()) {
            "right", "left" -> {
                height * trianglePositionOffsetPercentage
            }
            "top" -> {
                0f
            }
            else -> {
                height - triangleHeight
            }
        }

        val triangleXMaxVal = triangleXOrigin + triangleWidth
        val triangleYMaxVal = triangleYOrigin + triangleHeight

        if (orientation.lowercase().equals("vertical")) {
            if (divideByOriginInitX == 0f) {

                if (divideByOriginInitY == 0f) {
                    path.moveTo(triangleXOrigin, triangleYOrigin)

                    path.lineTo(triangleXMaxVal, triangleYMaxVal)
                    path.lineTo(triangleXOrigin, triangleYMaxVal)
                    path.close()
                } else {
                    path.moveTo(triangleXOrigin,  triangleYOrigin + triangleHeight/divideByOriginInitY)

                    if (divideByOriginInitY == 1f) {
                        path.lineTo(triangleXMaxVal, triangleYOrigin)
                        path.lineTo(triangleXOrigin, triangleYOrigin)
                        path.close()
                    } else {
                        path.lineTo(triangleXMaxVal, triangleYMaxVal)
                        path.lineTo(triangleXOrigin, triangleXMaxVal)
                        path.close()
                    }
                }
            } else {
                if (divideByOriginInitY == 0f) {
                    path.moveTo(triangleXOrigin + triangleWidth/divideByOriginInitX, triangleYOrigin)

                    path.lineTo(triangleXMaxVal, triangleYMaxVal)
                    path.lineTo(triangleXOrigin, triangleYMaxVal)
                    path.close()
                } else {
                    path.moveTo(triangleXOrigin + triangleWidth/divideByOriginInitX, triangleYOrigin + triangleHeight/divideByOriginInitY)

                    if (divideByOriginInitY == 1f) {
                        path.lineTo(triangleXMaxVal, triangleYOrigin)
                        path.lineTo(triangleXOrigin, triangleYOrigin)
                        path.close()
                    } else {
                        path.lineTo(triangleXMaxVal, triangleYMaxVal)
                        path.lineTo(triangleXOrigin, triangleYMaxVal)
                        path.close()
                    }
                }
            }
        } // TODO implement horizontal

        canvas.drawPath(path, paint)
    }

    fun MoveToPosition(position: IntArray, xOffset: Int, yOffset: Int) {
        translationX = position[0].toFloat() + xOffset
        translationY = position[1].toFloat() + yOffset
    }

    fun setDivideByOriginInitX(value: Float) {
        divideByOriginInitX = value
        invalidate()
    }
    fun setDivideByOriginInitY(value: Float) {
        divideByOriginInitY = value
        invalidate()
    }
    fun setTrianglePositionOffsetPercentage(value: Float) {
        trianglePositionOffsetPercentage = value
        invalidate()
    }
    fun setTrianglePosition(value: String) {
        trianglePosition = value
        invalidate()
    }
    fun setOrientation(value: String) {
        orientation = value
        invalidate()
    }

}