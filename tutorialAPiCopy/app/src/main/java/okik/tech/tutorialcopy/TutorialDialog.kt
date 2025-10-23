package okik.tech.tutorialcopy

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RenderEffect
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.RequiresApi


/**
 * This custom layout is expected to only have one child, which is usually a Linear or Constraint
 * Layout or similar.
 */
@RequiresApi(Build.VERSION_CODES.S)
class TutorialDialog @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    var selectedView: View? = null
    var focusArea: FocusArea? = null

    val TRIANGLE_SPACING_PX = 150
    val DIALOG_PADDING_PX = 30

    val paint: Paint = Paint()
    val path: Path = Path()

    init {
        paint.color = Color.WHITE
        paint.alpha = 100
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 8f
        paint.isAntiAlias = true
        
        id = generateViewId()
        
        setBackgroundColor(Color.TRANSPARENT)
    }

    fun renderTutorialDialog(
        focusArea: FocusArea,
        dialogBackgroundPadding: Byte,
        dialogBackgroundPaint: Paint?,
        dialogBackgroundRadius: Float?,
        dialogBackgroundEffect: RenderEffect?,
        dialogGravity: Int,
        dialogXOffsetDp: Float,
        dialogYOffsetDp: Float,
        originOffsetPercent: Float,
        destinationOffsetPercent: Float,
        shouldCenterOnMainAxis: Boolean,
        dialogContent: View
    ) {
        this.focusArea = focusArea

        val background = TutorialDialogBackground(context)
        background.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        
        background.renderRoundedDialog(
            focusArea,
            30,
            focusArea.roundedCornerSurrounding?.paint,
            focusArea.roundedCornerSurrounding?.cornerRadius?.toFloat(),
            focusArea.surroundingThicknessEffect,
            Gravity.BOTTOM,
            -30f,
            30f,
            0.5f,
            0.5f,
            true,
            dialogContent
        )
        
        addView(background)

        background.setRenderEffect(focusArea.surroundingThicknessEffect)
        
        layoutForeground(
            focusArea,
            dialogGravity,
            dialogBackgroundPadding,
            dialogXOffsetDp,
            dialogYOffsetDp,
            originOffsetPercent,
            destinationOffsetPercent,
            shouldCenterOnMainAxis,
            dialogContent
        )

        // this will trigger the drawChild method, in which we make the copies we need
        invalidate()
    }

    private fun layoutForeground(
        fa: FocusArea,
        gravity: Int,
        dialogBackgroundPadding: Byte,
        dialogXOffsetDp: Float,
        dialogYOffsetDp: Float,
        originOffsetPercent: Float,
        destinationOffsetPercent: Float,
        shouldCenterOnMainAxis: Boolean,
        dialogContent: View
    ) {
        // the passed view always has to be drawn on screen already
//        cloneViewLocationAndSize(viewToClipTo)

        val viewHeight = fa.view.height
        val viewWidth = fa.view.width

        val dialogXOffsetPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dialogXOffsetDp,
            resources.displayMetrics
        )

        val dialogYOffsetPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dialogYOffsetDp,
            resources.displayMetrics
        )



        when (gravity) {
            Gravity.START, Gravity.END -> {
                val xMargin = dialogXOffsetPx
                val yMargin = fa.viewLocation[1] + dialogYOffsetPx

                if (gravity == Gravity.START) {
                    val startX = fa.viewLocation[0]
                    val startY = fa.viewLocation[1] + viewHeight * originOffsetPercent
//
                    path.moveTo(startX.toFloat(), startY)
//
                    if (shouldCenterOnMainAxis) {

                        val verticalCenter = resources.displayMetrics.heightPixels/2
                        var difference = verticalCenter - dialogContent.layoutParams.height/2 - DIALOG_PADDING_PX

                        difference = if (difference < 0) 0 else difference

                        val firstVertexX = startX - xMargin
                        val firstVertexY = difference + dialogContent.layoutParams.height * destinationOffsetPercent

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX
                        val secondVertexY = firstVertexY + TRIANGLE_SPACING_PX

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        visibility = VISIBLE


//                        invalidate()
                    } else {


                        val firstVertexX = startX - xMargin
                        val firstVertexY = yMargin + dialogContent.layoutParams.height * destinationOffsetPercent

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX
                        val secondVertexY = firstVertexY + TRIANGLE_SPACING_PX

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()


                        visibility = VISIBLE

//                        invalidate()
                    }
                } else {
                    val startX = fa.viewLocation[0] + viewWidth
                    val startY = fa.viewLocation[1] + viewHeight * originOffsetPercent

                    path.moveTo(startX.toFloat(), startY)

                    if (shouldCenterOnMainAxis) {

                        val verticalCenter = resources.displayMetrics.heightPixels/2
                        var difference = verticalCenter - dialogContent.layoutParams.height/2 - DIALOG_PADDING_PX

                        difference = if (difference < 0) 0 else difference

                        val firstVertexX = startX + xMargin
                        val firstVertexY = difference + dialogContent.layoutParams.height * destinationOffsetPercent

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX
                        val secondVertexY = firstVertexY + TRIANGLE_SPACING_PX

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()


                        visibility = VISIBLE

//                        invalidate()
                    } else {

                        val firstVertexX = startX + xMargin
                        val firstVertexY = yMargin + dialogContent.layoutParams.height * destinationOffsetPercent

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX
                        val secondVertexY = firstVertexY + TRIANGLE_SPACING_PX

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()



                        visibility = VISIBLE

//                        invalidate()
                    }
                }
            }
            Gravity.TOP, Gravity.BOTTOM -> {
                val xMargin = fa.viewLocation[0] + dialogXOffsetPx
                val yMargin = dialogYOffsetPx

                if (gravity == Gravity.TOP) {
                    val startX = fa.viewLocation[0] + viewWidth * originOffsetPercent
                    val startY = fa.viewLocation[1]

                    path.moveTo(startX, startY.toFloat())

                    if (shouldCenterOnMainAxis) {


                        val horizontalCenter = resources.displayMetrics.widthPixels/2

                        var difference = horizontalCenter - dialogContent.layoutParams.width/2 - DIALOG_PADDING_PX

                        difference = if (difference < 0) 0 else difference

                        val firstVertexX = difference + dialogContent.layoutParams.width * destinationOffsetPercent
                        val firstVertexY = startY - yMargin

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX + TRIANGLE_SPACING_PX
                        val secondVertexY = firstVertexY

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()



                        visibility = VISIBLE

//                        invalidate()
                    } else {

                        val firstVertexX = xMargin + dialogContent.layoutParams.width * destinationOffsetPercent
                        val firstVertexY = startY - yMargin

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX + TRIANGLE_SPACING_PX
                        val secondVertexY = firstVertexY

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()


                        visibility = VISIBLE

//                        invalidate()
                    }
                } else {
                    val startX = fa.viewLocation[0] + viewWidth * originOffsetPercent
                    val startY = fa.viewLocation[1] + viewHeight

                    path.moveTo(startX, startY.toFloat())

                    if (shouldCenterOnMainAxis) {

                        val horizontalCenter = resources.displayMetrics.widthPixels/2

                        var difference = horizontalCenter - dialogContent.layoutParams.width/2 - DIALOG_PADDING_PX

                        difference = if (difference < 0) 0 else difference

                        val firstVertexX = difference + dialogContent.layoutParams.width * destinationOffsetPercent
                        val firstVertexY = startY + yMargin

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX + TRIANGLE_SPACING_PX
                        val secondVertexY = firstVertexY

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()


                        visibility = VISIBLE

//                        invalidate()
                    } else {

                        val firstVertexX = xMargin + dialogContent.layoutParams.width * destinationOffsetPercent
                        val firstVertexY = startY + yMargin

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX + TRIANGLE_SPACING_PX
                        val secondVertexY = firstVertexY

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()



                        visibility = VISIBLE

//                        invalidate()
                    }
                }
            }
            else -> throw IllegalArgumentException("Gravity can only be, top, bottom, start or end")
        }
    }
    
    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        canvas.drawPath(path, paint)
    }
}

