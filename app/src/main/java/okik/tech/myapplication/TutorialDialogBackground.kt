package okik.tech.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RenderEffect
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet


/**
 * This custom layout is expected to only have one child, which is usually a Linear or Constraint
 * Layout or similar.
 */
@RequiresApi(Build.VERSION_CODES.S)
class TutorialDialogBackground @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {
    var focusArea: FocusArea? = null

    val TRIANGLE_SPACING_PX = 150
    val DIALOG_PADDING_PX = 30

    var paint: Paint = Paint()
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

    fun renderRoundedDialog(
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
        if (this.focusArea == null) {
            addReferenceAndDialogContainer()
        }

        this.focusArea = focusArea

        cloneViewLocationAndSize(focusArea)

        setUpDialog(
            focusArea,
            dialogGravity,
            dialogBackgroundPadding,
            dialogBackgroundPaint,
            dialogBackgroundRadius,
            dialogBackgroundEffect,
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

    private fun addReferenceAndDialogContainer() {
        val referenceView = View(context)
        referenceView.id = generateViewId()
        referenceView.layoutParams = LayoutParams(0, 0)
        addView(referenceView)

        val constraintSet = ConstraintSet()
        constraintSet.clone(this)

        // margins will be added later
        constraintSet.connect(referenceView.id, ConstraintSet.TOP, id, ConstraintSet.TOP)
        constraintSet.connect(referenceView.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT)

        constraintSet.applyTo(this)

        // TODO lets see if we can actually pass this from somewhere later, which would give user more control
        // constraints and margins will be set up later in the flow
        val dialogContainer = RoundContainerTwo(context)
        dialogContainer.id = generateViewId()
        
        // TODO pass this params from somewhere later
        dialogContainer.layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )

        addView(dialogContainer)
    }

    private fun setUpDialog(
        fa: FocusArea,
        gravity: Int,
        dialogBackgroundPadding: Byte,
        dialogBackgroundPaint: Paint?,
        dialogBackgroundRadius: Float?,
        dialogBackgroundEffect: RenderEffect?,
        dialogXOffsetDp: Float,
        dialogYOffsetDp: Float,
        originOffsetPercent: Float,
        destinationOffsetPercent: Float,
        shouldCenterOnMainAxis: Boolean,
        dialogContent: View
    ) {
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

        // dialog container is always index 1
        val dialogContainer = getChildAt(1) as RoundContainerTwo

        if (dialogBackgroundRadius != null && dialogBackgroundRadius > 0) {
            val n = dialogBackgroundRadius!!
            val roundShape = RoundRectShape(
                floatArrayOf(n, n, n, n, n, n, n ,n),
//            RectF(0f, 0f, 100f, 100f),
                null,
                null
            )

            val shapeDrawable = ShapeDrawable(roundShape)

            dialogContainer.setEffectHolderBackgroundDrawable(shapeDrawable)

            dialogContainer.setEffectHolderBackgroundEffect(dialogBackgroundEffect)

            dialogContainer.setEffectHolderBackgroundPadding(dialogBackgroundPadding.toInt())

            if (dialogBackgroundPaint != null) {
                dialogContainer.setEffectHolderBackgroundPaint(dialogBackgroundPaint)
            }
        }

        val dialogCs = ConstraintSet()
        dialogCs.clone(this)

        // we're responsible to add padding to our custom view as explained in its definition
        if (dialogBackgroundPadding > 0) {
            val pad = dialogBackgroundPadding.toInt()
            dialogContainer.setEffectHolderBackgroundPadding(pad)

            // this is our responsibility
            dialogContent.setPadding(
                dialogContent.paddingLeft + pad,
                dialogContent.paddingTop + pad,
                dialogContent.paddingRight + pad,
                dialogContent.paddingBottom + pad
            )
        }

        dialogContainer.addView(dialogContent)

        val referenceView = getChildAt(0)

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
                        dialogCs.connect(dialogContainer.id, ConstraintSet.TOP, id, ConstraintSet.TOP)
                        dialogCs.connect(dialogContainer.id, ConstraintSet.BOTTOM, id, ConstraintSet.BOTTOM)
                        dialogCs.connect(dialogContainer.id, ConstraintSet.RIGHT, referenceView.id, ConstraintSet.LEFT, xMargin.toInt())
                        dialogCs.setHorizontalBias(dialogContainer.id, 1f)
                        dialogCs.connect(dialogContainer.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT)
//
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

                        dialogCs.applyTo(this)

//                        invalidate()
                    } else {
                        dialogCs.connect(dialogContainer.id, ConstraintSet.TOP, id, ConstraintSet.TOP, yMargin.toInt())
                        dialogCs.setHorizontalBias(dialogContainer.id, 1f)
                        dialogCs.connect(dialogContainer.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT)
                        dialogCs.connect(
                            dialogContainer.id, ConstraintSet.RIGHT,
                            referenceView.id, ConstraintSet.LEFT,
                            xMargin.toInt()
                        )

                        val firstVertexX = startX - xMargin
                        val firstVertexY = yMargin + dialogContent.layoutParams.height * destinationOffsetPercent

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX
                        val secondVertexY = firstVertexY + TRIANGLE_SPACING_PX

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(this)

                        visibility = VISIBLE

//                        invalidate()
                    }
                } else {
                    val startX = fa.viewLocation[0] + viewWidth
                    val startY = fa.viewLocation[1] + viewHeight * originOffsetPercent

                    path.moveTo(startX.toFloat(), startY)

                    if (shouldCenterOnMainAxis) {
                        dialogCs.connect(dialogContainer.id, ConstraintSet.TOP, id, ConstraintSet.TOP)
                        dialogCs.connect(dialogContainer.id, ConstraintSet.BOTTOM, id, ConstraintSet.BOTTOM)
                        dialogCs.connect(dialogContainer.id, ConstraintSet.LEFT, referenceView.id, ConstraintSet.RIGHT, xMargin.toInt())
                        dialogCs.connect(dialogContainer.id, ConstraintSet.RIGHT, id, ConstraintSet.RIGHT)
                        dialogCs.setHorizontalBias(dialogContainer.id, 0f)

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

                        dialogCs.applyTo(this)

                        visibility = VISIBLE

//                        invalidate()
                    } else {
                        dialogCs.connect(dialogContainer.id, ConstraintSet.TOP, id, ConstraintSet.TOP, yMargin.toInt())
                        dialogCs.connect(dialogContainer.id, ConstraintSet.LEFT, referenceView.id, ConstraintSet.RIGHT, xMargin.toInt())

                        val firstVertexX = startX + xMargin
                        val firstVertexY = yMargin + dialogContent.layoutParams.height * destinationOffsetPercent

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX
                        val secondVertexY = firstVertexY + TRIANGLE_SPACING_PX

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(this)

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
                        dialogCs.connect(dialogContainer.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT)
                        dialogCs.connect(dialogContainer.id, ConstraintSet.RIGHT, id, ConstraintSet.RIGHT)
                        dialogCs.connect(dialogContainer.id, ConstraintSet.BOTTOM, referenceView.id, ConstraintSet.TOP, yMargin.toInt())
                        dialogCs.connect(dialogContainer.id, ConstraintSet.TOP, id, ConstraintSet.TOP)
                        dialogCs.setVerticalBias(dialogContainer.id, 1f)

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

                        dialogCs.applyTo(this)

                        visibility = VISIBLE

//                        invalidate()
                    } else {
                        dialogCs.connect(dialogContainer.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT, xMargin.toInt())
                        dialogCs.connect(dialogContainer.id, ConstraintSet.BOTTOM, referenceView.id, ConstraintSet.TOP, yMargin.toInt())

                        val firstVertexX = xMargin + dialogContent.layoutParams.width * destinationOffsetPercent
                        val firstVertexY = startY - yMargin

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX + TRIANGLE_SPACING_PX
                        val secondVertexY = firstVertexY

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(this)

                        visibility = VISIBLE

//                        invalidate()
                    }
                } else {
                    val startX = fa.viewLocation[0] + viewWidth * originOffsetPercent
                    val startY = fa.viewLocation[1] + viewHeight

                    path.moveTo(startX, startY.toFloat())

                    if (shouldCenterOnMainAxis) {
                        dialogCs.connect(dialogContainer.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT)
                        dialogCs.connect(dialogContainer.id, ConstraintSet.RIGHT, id, ConstraintSet.RIGHT)
                        dialogCs.connect(dialogContainer.id, ConstraintSet.TOP, referenceView.id, ConstraintSet.BOTTOM, yMargin.toInt())
                        dialogCs.connect(dialogContainer.id, ConstraintSet.BOTTOM, id, ConstraintSet.BOTTOM)
                        dialogCs.setVerticalBias(dialogContainer.id, 0f)

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

                        dialogCs.applyTo(this)

                        visibility = VISIBLE

//                        invalidate()
                    } else {
                        dialogCs.connect(dialogContainer.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT, xMargin.toInt())
                        dialogCs.connect(dialogContainer.id, ConstraintSet.TOP, referenceView.id, ConstraintSet.BOTTOM, yMargin.toInt())

                        val firstVertexX = xMargin + dialogContent.layoutParams.width * destinationOffsetPercent
                        val firstVertexY = startY + yMargin

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX + TRIANGLE_SPACING_PX
                        val secondVertexY = firstVertexY

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(this)

                        visibility = VISIBLE

//                        invalidate()
                    }
                }
            }
            else -> throw IllegalArgumentException("Gravity can only be, top, bottom, start or end")
        }
    }

    /**
     * @param view we will use this view to set up a clone that the dialog will use as a reference
     * to define its constraints
     */
    private fun cloneViewLocationAndSize(fa: FocusArea) {
        // view that acts a as a clone of the original view is always at index 0
        val referenceView = getChildAt(0)

        val params = referenceView.layoutParams as LayoutParams

        params.leftMargin = fa.viewLocation[0]
        params.topMargin = fa.viewLocation[1]

        params.width = fa.view.width
        params.height = fa.view.height
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        canvas.drawPath(path, paint)
    }
}

