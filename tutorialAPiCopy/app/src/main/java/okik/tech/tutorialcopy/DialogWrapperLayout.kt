package okik.tech.tutorialcopy

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.RenderNode
import android.util.AttributeSet
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
    val OPOSITE_LEG = dpToPx(16, context)

    private val path: Path = Path()

    init {
        setBackgroundColor(Color.TRANSPARENT)
    }

    override fun drawChild(canvas: Canvas, child: View?, drawingTime: Long): Boolean {
        return super.drawChild(canvas, child, drawingTime)
    }

    fun configuredDialog(fd: FocusDialog, renderNode: RenderNode) {
        val bridgeView = RenderNodeBehindPathView(context)
        bridgeView.id = generateViewId()

        if (isEmpty()) {
            if (id == -1) id = generateViewId()

            addReferenceView()

            if (fd.view.id == -1) fd.view.id = generateViewId()

            addView(fd.view)
            addView(bridgeView)
        }

        cloneViewLocationAndSize(fd)

        setUpDialogAndBridgePath(fd)

        val constraintSet = ConstraintSet()
        constraintSet.clone(this)

        constraintSet.connect(bridgeView.id, ConstraintSet.TOP, id, ConstraintSet.TOP)
        constraintSet.connect(bridgeView.id, ConstraintSet.START, id, ConstraintSet.START)

        bridgeView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        bridgeView.setBackgroundConfigs(
            renderNode,
            path,
            fd.dialogBackgroundPaint,
            true,
            fd.shouldClipToBackground,
            fd.backgroundRenderEffect,
            { _, _ -> }
        )

        if (context is Activity) {
            bridgeView.setFallbackBackground((context as Activity).window.decorView.background)
        }
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
    }

    /**
     * @param view we will use this view to set up a clone that the dialog will use as a reference
     * to define its constraints
     */
    private fun cloneViewLocationAndSize(fa: FocusDialog) {
        // view that acts a as a clone of the original view is always at index 0
        val referenceView = getChildAt(0)

        val params = referenceView.layoutParams as LayoutParams

        params.leftMargin = fa.referenceViewLocation[0]
        params.topMargin = fa.referenceViewLocation[1]

        params.width = fa.referenceViewWidth
        params.height = fa.referenceViewHeight
    }

    private fun setUpDialogAndBridgePath(fa: FocusDialog) {
        val viewHeight = fa.referenceViewHeight
        val viewWidth = fa.referenceViewWidth

        val dialogXOffsetPx = fa.dialogXMarginDp
        val dialogYOffsetPx = fa.dialogYMarginDp

        val dialogCs = ConstraintSet()
        dialogCs.clone(this)

        val referenceView = getChildAt(0)

        when (fa.gravity) {
            Gravity.START, Gravity.END -> {
                val xMargin = dialogXOffsetPx
                val yMargin = fa.referenceViewLocation[1] + dialogYOffsetPx

                if (fa.gravity == Gravity.START) {
                    val startX = fa.referenceViewLocation[0]
                    val startY = fa.referenceViewLocation[1] + viewHeight * fa.originOffsetPercent
//
                    path.moveTo(startX.toFloat(), startY.toFloat())
//
                    if (fa.shouldCenterOnMainAxis) {
                        dialogCs.connect(fa.view.id, ConstraintSet.TOP, id, ConstraintSet.TOP)
                        dialogCs.connect(fa.view.id, ConstraintSet.BOTTOM, id, ConstraintSet.BOTTOM)
                        dialogCs.setHorizontalBias(fa.view.id, 1f)
                        dialogCs.connect(fa.view.id, ConstraintSet.RIGHT, referenceView.id, ConstraintSet.LEFT, xMargin.toInt())
                        dialogCs.connect(fa.view.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT)
//
                        val verticalCenter = resources.displayMetrics.heightPixels/2
                        var difference = verticalCenter - fa.view.layoutParams.height/2

                        difference = if (difference < 0) 0 else difference

                        val firstVertexX = startX - xMargin
                        val firstVertexY = difference + fa.view.layoutParams.height * fa.destinationOffsetPercent.toFloat()

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX - OPOSITE_LEG
                        val secondVertexY = firstVertexY + TRIANGLE_SPACING_PX

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(this)
                    } else {
                        dialogCs.connect(fa.view.id, ConstraintSet.TOP, id, ConstraintSet.TOP, yMargin.toInt())
                        dialogCs.setVerticalBias(fa.view.id, 1f)
                        dialogCs.connect(fa.view.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT)
                        dialogCs.connect(fa.view.id, ConstraintSet.RIGHT, referenceView.id, ConstraintSet.LEFT, xMargin.toInt())

                        dialogCs.connect(
                            fa.view.id, ConstraintSet.RIGHT,
                            referenceView.id, ConstraintSet.LEFT,
                            xMargin.toInt()
                        )

                        val firstVertexX = startX - xMargin
                        val firstVertexY = yMargin + fa.view.layoutParams.height * fa.destinationOffsetPercent.toFloat()

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX  - OPOSITE_LEG
                        val secondVertexY = firstVertexY + TRIANGLE_SPACING_PX

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(this)
                    }
                } else {
                    val startX = fa.referenceViewLocation[0] + viewWidth
                    val startY = fa.referenceViewLocation[1] + viewHeight * fa.originOffsetPercent

                    path.moveTo(startX.toFloat(), startY.toFloat())

                    if (fa.shouldCenterOnMainAxis) {
                        dialogCs.connect(fa.view.id, ConstraintSet.TOP, id, ConstraintSet.TOP)
                        dialogCs.connect(fa.view.id, ConstraintSet.BOTTOM, id, ConstraintSet.BOTTOM)
                        dialogCs.setHorizontalBias(fa.view.id, 0f)
                        dialogCs.connect(fa.view.id, ConstraintSet.LEFT, referenceView.id, ConstraintSet.RIGHT, xMargin.toInt())
                        dialogCs.connect(fa.view.id, ConstraintSet.RIGHT, id, ConstraintSet.RIGHT)

                        val verticalCenter = resources.displayMetrics.heightPixels/2
                        var difference = verticalCenter - fa.view.layoutParams.height/2

                        difference = if (difference < 0) 0 else difference

                        val firstVertexX = startX + xMargin
                        val firstVertexY = difference + fa.view.layoutParams.height * fa.destinationOffsetPercent.toFloat()

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX + OPOSITE_LEG
                        val secondVertexY = firstVertexY + TRIANGLE_SPACING_PX

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(this)
                    } else {
                        dialogCs.connect(fa.view.id, ConstraintSet.TOP, id, ConstraintSet.TOP, yMargin.toInt())
                        dialogCs.setHorizontalBias(fa.view.id, 0f)
                        dialogCs.connect(fa.view.id, ConstraintSet.LEFT, referenceView.id, ConstraintSet.RIGHT, xMargin.toInt())
                        dialogCs.connect(fa.view.id, ConstraintSet.RIGHT, id, ConstraintSet.RIGHT)

                        val firstVertexX = startX + xMargin
                        val firstVertexY = yMargin + fa.view.layoutParams.height * fa.destinationOffsetPercent.toFloat()

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX + OPOSITE_LEG
                        val secondVertexY = firstVertexY + TRIANGLE_SPACING_PX

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(this)
                    }
                }
            }
            Gravity.TOP, Gravity.BOTTOM -> {
                val xMargin = fa.referenceViewLocation[0] + dialogXOffsetPx
                val yMargin = dialogYOffsetPx

                if (fa.gravity == Gravity.TOP) {
                    val startX = fa.referenceViewLocation[0] + viewWidth * fa.originOffsetPercent
                    val startY = fa.referenceViewLocation[1]

                    path.moveTo(startX.toFloat(), startY.toFloat())

                    if (fa.shouldCenterOnMainAxis) {
                        dialogCs.connect(fa.view.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT)
                        dialogCs.connect(fa.view.id, ConstraintSet.RIGHT, id, ConstraintSet.RIGHT)
                        dialogCs.connect(fa.view.id, ConstraintSet.BOTTOM, referenceView.id, ConstraintSet.TOP, yMargin.toInt())
                        dialogCs.connect(fa.view.id, ConstraintSet.TOP, id, ConstraintSet.TOP)
                        dialogCs.setVerticalBias(fa.view.id, 1f)

                        val horizontalCenter = resources.displayMetrics.widthPixels/2

                        var difference = horizontalCenter - fa.view.layoutParams.width/2

                        difference = if (difference < 0) 0 else difference

                        val firstVertexX = difference + fa.view.layoutParams.width * fa.destinationOffsetPercent.toFloat()
                        val firstVertexY = startY - yMargin

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX + TRIANGLE_SPACING_PX
                        val secondVertexY = firstVertexY - OPOSITE_LEG

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(this)
                    } else {
                        dialogCs.connect(fa.view.id, ConstraintSet.LEFT, referenceView.id, ConstraintSet.LEFT, xMargin.toInt())
                        dialogCs.connect(fa.view.id, ConstraintSet.BOTTOM, referenceView.id, ConstraintSet.TOP, yMargin.toInt())
                        dialogCs.connect(fa.view.id, ConstraintSet.TOP, id, ConstraintSet.TOP)
                        dialogCs.setVerticalBias(fa.view.id, 1f)

                        val firstVertexX = xMargin + fa.view.layoutParams.width * fa.destinationOffsetPercent.toFloat()
                        val firstVertexY = startY - yMargin

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX + TRIANGLE_SPACING_PX
                        val secondVertexY = firstVertexY - OPOSITE_LEG

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(this)
                    }
                } else {
                    val startX = fa.referenceViewLocation[0] + viewWidth * fa.originOffsetPercent
                    val startY = fa.referenceViewLocation[1] + viewHeight

                    path.moveTo(startX.toFloat(), startY.toFloat())

                    if (fa.shouldCenterOnMainAxis) {
                        dialogCs.connect(fa.view.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT)
                        dialogCs.connect(fa.view.id, ConstraintSet.RIGHT, id, ConstraintSet.RIGHT)
                        dialogCs.connect(fa.view.id, ConstraintSet.TOP, referenceView.id, ConstraintSet.BOTTOM, yMargin.toInt())
                        dialogCs.connect(fa.view.id, ConstraintSet.BOTTOM, id, ConstraintSet.BOTTOM)
                        dialogCs.setVerticalBias(fa.view.id, 0f)

                        val horizontalCenter = resources.displayMetrics.widthPixels/2

                        var difference = horizontalCenter - fa.view.layoutParams.width/2

                        difference = if (difference < 0) 0 else difference

                        val firstVertexX = difference + fa.view.layoutParams.width * fa.destinationOffsetPercent.toFloat()
                        val firstVertexY = startY + yMargin

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX + TRIANGLE_SPACING_PX
                        val secondVertexY = firstVertexY + OPOSITE_LEG

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(this)
                    } else {
                        dialogCs.connect(fa.view.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT, xMargin.toInt())
                        dialogCs.connect(fa.view.id, ConstraintSet.TOP, referenceView.id, ConstraintSet.BOTTOM, yMargin.toInt())
                        dialogCs.connect(fa.view.id, ConstraintSet.BOTTOM, id, ConstraintSet.BOTTOM)
                        dialogCs.setVerticalBias(fa.view.id, 0f)

                        val firstVertexX = xMargin + fa.view.layoutParams.width * fa.destinationOffsetPercent.toFloat()
                        val firstVertexY = startY + yMargin

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX + TRIANGLE_SPACING_PX
                        val secondVertexY = firstVertexY + OPOSITE_LEG

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(this)
                    }
                }
            }
            else -> throw IllegalArgumentException("Gravity can only be, top, bottom, start or end")
        }
    }
}