package okik.tech.tutorialcopy

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isEmpty

/**
 * This component is just a frame layout wrapped in a rounded corner shape, the difference
 * with a material card is that this one doesn't have "card elevation" and also you can apply
 * effects to its background without affecting the foreground(its content). The background, which
 * enables to render effects, exclusively, without affecting the content of the container, is actually
 * a view that lives behind the rest of the content, and is added automatically when view is
 * instantiated, it is referred to as "effectHolderBackground"
 */
class DialogWrapperLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val TRIANGLE_SPACING_PX = 150
    private val path: Path = Path()

    init {
        id = generateViewId()
        setBackgroundColor(Color.TRANSPARENT)
    }

    fun renderRoundedDialog(
        focusArea: FocusArea,
        dialogGravity: Int,
        dialogXOffsetDp: Float,
        dialogYOffsetDp: Float,
        originOffsetPercent: Float,
        destinationOffsetPercent: Float,
        shouldCenterOnMainAxis: Boolean,
        dialog: View
    ) {
        if (isEmpty()) {
            addReferenceView()

            // add dialog
            dialog.id = generateViewId()
            addView(dialog)
        }

        cloneViewLocationAndSize(focusArea)

        setUpDialog(
            focusArea,
            dialogGravity,
            dialogXOffsetDp,
            dialogYOffsetDp,
            originOffsetPercent,
            destinationOffsetPercent,
            shouldCenterOnMainAxis,
            dialog
        )
    }

    private fun addReferenceView() {
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

//        // TODO lets see if we can actually pass this from somewhere later, which would give user more control
//        // constraints and margins will be set up later in the flow
//        val dialogContainer = RoundContainerTwo(context)
//        dialogContainer.id = generateViewId()
//
//        // TODO pass this params from somewhere later
//        dialogContainer.layoutParams = LayoutParams(
//            LayoutParams.WRAP_CONTENT,
//            LayoutParams.WRAP_CONTENT
//        )
//
//        addView(dialogContainer)
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

    private fun setUpDialog(
        fa: FocusArea,
        gravity: Int,
        dialogXOffsetDp: Float,
        dialogYOffsetDp: Float,
        originOffsetPercent: Float,
        destinationOffsetPercent: Float,
        shouldCenterOnMainAxis: Boolean,
        dialog: View
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

        val dialogCs = ConstraintSet()
        dialogCs.clone(this)

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
                        dialogCs.connect(dialog.id, ConstraintSet.TOP, id, ConstraintSet.TOP)
                        dialogCs.connect(dialog.id, ConstraintSet.BOTTOM, id, ConstraintSet.BOTTOM)
                        dialogCs.connect(dialog.id, ConstraintSet.RIGHT, referenceView.id, ConstraintSet.LEFT, xMargin.toInt())
                        dialogCs.setHorizontalBias(dialog.id, 1f)
                        dialogCs.connect(dialog.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT)
//
                        val verticalCenter = resources.displayMetrics.heightPixels/2
                        var difference = verticalCenter - dialog.layoutParams.height/2

                        difference = if (difference < 0) 0 else difference

                        val firstVertexX = startX - xMargin
                        val firstVertexY = difference + dialog.layoutParams.height * destinationOffsetPercent

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX
                        val secondVertexY = firstVertexY + TRIANGLE_SPACING_PX

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        visibility = VISIBLE

                        dialogCs.applyTo(this)
                    } else {
                        dialogCs.connect(dialog.id, ConstraintSet.TOP, id, ConstraintSet.TOP, yMargin.toInt())
                        dialogCs.setHorizontalBias(dialog.id, 1f)
                        dialogCs.connect(dialog.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT)
                        dialogCs.connect(
                            dialog.id, ConstraintSet.RIGHT,
                            referenceView.id, ConstraintSet.LEFT,
                            xMargin.toInt()
                        )

                        val firstVertexX = startX - xMargin
                        val firstVertexY = yMargin + dialog.layoutParams.height * destinationOffsetPercent

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX
                        val secondVertexY = firstVertexY + TRIANGLE_SPACING_PX

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(this)

                        visibility = VISIBLE
                    }
                } else {
                    val startX = fa.viewLocation[0] + viewWidth
                    val startY = fa.viewLocation[1] + viewHeight * originOffsetPercent

                    path.moveTo(startX.toFloat(), startY)

                    if (shouldCenterOnMainAxis) {
                        dialogCs.connect(dialog.id, ConstraintSet.TOP, id, ConstraintSet.TOP)
                        dialogCs.connect(dialog.id, ConstraintSet.BOTTOM, id, ConstraintSet.BOTTOM)
                        dialogCs.connect(dialog.id, ConstraintSet.LEFT, referenceView.id, ConstraintSet.RIGHT, xMargin.toInt())
                        dialogCs.connect(dialog.id, ConstraintSet.RIGHT, id, ConstraintSet.RIGHT)
                        dialogCs.setHorizontalBias(dialog.id, 0f)

                        val verticalCenter = resources.displayMetrics.heightPixels/2
                        var difference = verticalCenter - dialog.layoutParams.height/2

                        difference = if (difference < 0) 0 else difference

                        val firstVertexX = startX + xMargin
                        val firstVertexY = difference + dialog.layoutParams.height * destinationOffsetPercent

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX
                        val secondVertexY = firstVertexY + TRIANGLE_SPACING_PX

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(this)

                        visibility = VISIBLE
                    } else {
                        dialogCs.connect(dialog.id, ConstraintSet.TOP, id, ConstraintSet.TOP, yMargin.toInt())
                        dialogCs.connect(dialog.id, ConstraintSet.LEFT, referenceView.id, ConstraintSet.RIGHT, xMargin.toInt())

                        val firstVertexX = startX + xMargin
                        val firstVertexY = yMargin + dialog.layoutParams.height * destinationOffsetPercent

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX
                        val secondVertexY = firstVertexY + TRIANGLE_SPACING_PX

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(this)

                        visibility = VISIBLE
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
                        dialogCs.connect(dialog.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT)
                        dialogCs.connect(dialog.id, ConstraintSet.RIGHT, id, ConstraintSet.RIGHT)
                        dialogCs.connect(dialog.id, ConstraintSet.BOTTOM, referenceView.id, ConstraintSet.TOP, yMargin.toInt())
                        dialogCs.connect(dialog.id, ConstraintSet.TOP, id, ConstraintSet.TOP)
                        dialogCs.setVerticalBias(dialog.id, 1f)

                        val horizontalCenter = resources.displayMetrics.widthPixels/2

                        var difference = horizontalCenter - dialog.layoutParams.width/2

                        difference = if (difference < 0) 0 else difference

                        val firstVertexX = difference + dialog.layoutParams.width * destinationOffsetPercent
                        val firstVertexY = startY - yMargin

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX + TRIANGLE_SPACING_PX
                        val secondVertexY = firstVertexY

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(this)

                        visibility = VISIBLE
                    } else {
                        dialogCs.connect(dialog.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT, xMargin.toInt())
                        dialogCs.connect(dialog.id, ConstraintSet.BOTTOM, referenceView.id, ConstraintSet.TOP, yMargin.toInt())

                        val firstVertexX = xMargin + dialog.layoutParams.width * destinationOffsetPercent
                        val firstVertexY = startY - yMargin

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX + TRIANGLE_SPACING_PX
                        val secondVertexY = firstVertexY

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(this)

                        visibility = VISIBLE
                    }
                } else {
                    val startX = fa.viewLocation[0] + viewWidth * originOffsetPercent
                    val startY = fa.viewLocation[1] + viewHeight

                    path.moveTo(startX, startY.toFloat())

                    if (shouldCenterOnMainAxis) {
                        dialogCs.connect(dialog.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT)
                        dialogCs.connect(dialog.id, ConstraintSet.RIGHT, id, ConstraintSet.RIGHT)
                        dialogCs.connect(dialog.id, ConstraintSet.TOP, referenceView.id, ConstraintSet.BOTTOM, yMargin.toInt())
                        dialogCs.connect(dialog.id, ConstraintSet.BOTTOM, id, ConstraintSet.BOTTOM)
                        dialogCs.setVerticalBias(dialog.id, 0f)

                        val horizontalCenter = resources.displayMetrics.widthPixels/2

                        var difference = horizontalCenter - dialog.layoutParams.width/2

                        difference = if (difference < 0) 0 else difference

                        val firstVertexX = difference + dialog.layoutParams.width * destinationOffsetPercent
                        val firstVertexY = startY + yMargin

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX + TRIANGLE_SPACING_PX
                        val secondVertexY = firstVertexY

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(this)

                        visibility = VISIBLE
                    } else {
                        dialogCs.connect(dialog.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT, xMargin.toInt())
                        dialogCs.connect(dialog.id, ConstraintSet.TOP, referenceView.id, ConstraintSet.BOTTOM, yMargin.toInt())

                        val firstVertexX = xMargin + dialog.layoutParams.width * destinationOffsetPercent
                        val firstVertexY = startY + yMargin

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX + TRIANGLE_SPACING_PX
                        val secondVertexY = firstVertexY

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(this)

                        visibility = VISIBLE
                    }
                }
            }
            else -> throw IllegalArgumentException("Gravity can only be, top, bottom, start or end")
        }
    }
}