package okik.tech.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

class TutorialDisplayLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private var currentLocation: IntArray? = null
    var selectedView: View? = null

    private val contentCopy = RenderNode("ContentCopy")
    private val blurredContent = RenderNode("BlurredContent")
    private val focusedContent = RenderNode("FocusContent")

    private var VIEW_PADDING_PX = 120
    val TRIANGLE_SPACING_PX = 150
    val DIALOG_PADDING_PX = 30

    private var dialogHolder: ConstraintLayout? = null

    val paint: Paint = Paint()
    val path: Path = Path()

    init {
        paint.color = Color.WHITE
        paint.alpha = 100
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 8f
        paint.isAntiAlias = true

        setBackgroundColor(Color.TRANSPARENT)
    }

    fun focusView(
        view: View,
        backgroundPadding: Byte?,
        backgroundPaint: Paint?,
        backgroundRadius: Float?,
        viewBackgroundEffect: RenderEffect?
    ) {
        if (currentLocation == null) {
            saveLocationOnScreen(view)
        }

        selectedView = view

        invalidate()
    }

    fun focusViewWithDialog(
        view: View,
        viewBackgroundPadding: Byte?,
        viewBackgroundPaint: Paint?,
        viewBackgroundRadius: Float?,
        viewBackgroundEffect: RenderEffect?,
        dialogBackgroundPadding: Byte?,
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
        saveLocationOnScreen(view)

        if (dialogHolder == null) initDialogCL()

        setUpDialog(
            view,
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

        focusView(
            view,
            viewBackgroundPadding,
            viewBackgroundPaint,
            viewBackgroundRadius,
            viewBackgroundEffect
        )
    }

    private fun initDialogCL() {
        dialogHolder = ConstraintLayout(context)
        dialogHolder!!.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        dialogHolder!!.visibility = GONE
//        dialogHolder!!.setBackgroundColor(Color.BLUE)
        dialogHolder!!.id = generateViewId()

        val referenceView = View(context)
        referenceView.id = generateViewId()
        referenceView.layoutParams = ConstraintLayout.LayoutParams(0, 0)
        dialogHolder!!.addView(referenceView)

        val constraintSet = ConstraintSet()
        constraintSet.clone(dialogHolder)

        constraintSet.connect(referenceView.id, ConstraintSet.TOP, dialogHolder!!.id, ConstraintSet.TOP)
        constraintSet.connect(referenceView.id, ConstraintSet.LEFT, dialogHolder!!.id, ConstraintSet.LEFT)

        constraintSet.applyTo(dialogHolder)

        // constraints and margins will be set up later in the flow
        val dialogContainer = RoundContainer(context)
        dialogContainer.id = generateViewId()
//        dialogContainer.layoutParams = ConstraintLayout.LayoutParams(
//            ConstraintLayout.LayoutParams.WRAP_CONTENT,
//            ConstraintLayout.LayoutParams.WRAP_CONTENT
//        )

        dialogContainer.setPadding(DIALOG_PADDING_PX, DIALOG_PADDING_PX, DIALOG_PADDING_PX, DIALOG_PADDING_PX)

        dialogHolder!!.addView(dialogContainer)

        addView(dialogHolder)
    }

    private fun saveLocationOnScreen(view: View) {
        currentLocation = intArrayOf(0, 0)

        view.getLocationOnScreen(currentLocation)

        var topBarHeight: Int

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            topBarHeight = rootWindowInsets?.getInsetsIgnoringVisibility(
                WindowInsets.Type.statusBars()
            )?.top ?: 0
        } else {
            topBarHeight = rootWindowInsets?.systemWindowInsetTop ?: 0
        }

        currentLocation!![1] = currentLocation!![1] - topBarHeight
    }


    private fun setUpDialog(
        viewToClipTo: View,
        gravity: Int,
        dialogBackgroundPadding: Byte?,
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
        // the passed view always has to be drawn on screen already
        cloneViewLocationAndSize(viewToClipTo)

        val viewHeight = viewToClipTo.height
        val viewWidth = viewToClipTo.width

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
        val dialogContainer = dialogHolder!!.getChildAt(1) as RoundContainer
        dialogContainer.addView(dialogContent)

        val dialogCs = ConstraintSet()
        dialogCs.clone(dialogHolder)

        val referenceView = dialogHolder!!.getChildAt(0)

        when (gravity) {
            Gravity.START, Gravity.END -> {
                val xMargin = dialogXOffsetPx
                val yMargin = currentLocation!![1] + dialogYOffsetPx

                if (gravity == Gravity.START) {
                    val startX = currentLocation!![0]
                    val startY = currentLocation!![1] + viewHeight * originOffsetPercent
//
                    path.moveTo(startX.toFloat(), startY)
//
                    if (shouldCenterOnMainAxis) {
                        dialogCs.connect(dialogContainer.id, ConstraintSet.TOP, dialogHolder!!.id, ConstraintSet.TOP)
                        dialogCs.connect(dialogContainer.id, ConstraintSet.BOTTOM, dialogHolder!!.id, ConstraintSet.BOTTOM)
                        dialogCs.connect(dialogContainer.id, ConstraintSet.RIGHT, referenceView.id, ConstraintSet.LEFT, xMargin.toInt())
                        dialogCs.setHorizontalBias(dialogContainer.id, 1f)
                        dialogCs.connect(dialogContainer.id, ConstraintSet.LEFT, dialogHolder!!.id, ConstraintSet.LEFT)
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

                        dialogHolder!!.visibility = VISIBLE

                        dialogCs.applyTo(dialogHolder)

                        invalidate()
                    } else {
                        dialogCs.connect(dialogContainer.id, ConstraintSet.TOP, dialogHolder!!.id, ConstraintSet.TOP, yMargin.toInt())
                        dialogCs.setHorizontalBias(dialogContainer.id, 1f)
                        dialogCs.connect(dialogContainer.id, ConstraintSet.LEFT, dialogHolder!!.id, ConstraintSet.LEFT)
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

                        dialogCs.applyTo(dialogHolder)

                        dialogHolder!!.visibility = VISIBLE

                        invalidate()
                    }
                } else {
                    val startX = currentLocation!![0] + viewWidth
                    val startY = currentLocation!![1] + viewHeight * originOffsetPercent

                    path.moveTo(startX.toFloat(), startY)

                    if (shouldCenterOnMainAxis) {
                        dialogCs.connect(dialogContainer.id, ConstraintSet.TOP, dialogHolder!!.id, ConstraintSet.TOP)
                        dialogCs.connect(dialogContainer.id, ConstraintSet.BOTTOM, dialogHolder!!.id, ConstraintSet.BOTTOM)
                        dialogCs.connect(dialogContainer.id, ConstraintSet.LEFT, referenceView.id, ConstraintSet.RIGHT, xMargin.toInt())
                        dialogCs.connect(dialogContainer.id, ConstraintSet.RIGHT, dialogHolder!!.id, ConstraintSet.RIGHT)
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

                        dialogCs.applyTo(dialogHolder)

                        dialogHolder!!.visibility = VISIBLE

                        invalidate()
                    } else {
                        dialogCs.connect(dialogContainer.id, ConstraintSet.TOP, dialogHolder!!.id, ConstraintSet.TOP, yMargin.toInt())
                        dialogCs.connect(dialogContainer.id, ConstraintSet.LEFT, referenceView.id, ConstraintSet.RIGHT, xMargin.toInt())

                        val firstVertexX = startX + xMargin
                        val firstVertexY = yMargin + dialogContent.layoutParams.height * destinationOffsetPercent

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX
                        val secondVertexY = firstVertexY + TRIANGLE_SPACING_PX

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(dialogHolder)

                        dialogHolder!!.visibility = VISIBLE

                        invalidate()
                    }
                }
            }
            Gravity.TOP, Gravity.BOTTOM -> {
                val xMargin = currentLocation!![0] + dialogXOffsetPx
                val yMargin = dialogYOffsetPx

                if (gravity == Gravity.TOP) {
                    val startX = currentLocation!![0] + viewWidth * originOffsetPercent
                    val startY = currentLocation!![1]

                    path.moveTo(startX, startY.toFloat())

                    if (shouldCenterOnMainAxis) {
                        dialogCs.connect(dialogContainer.id, ConstraintSet.LEFT, dialogHolder!!.id, ConstraintSet.LEFT)
                        dialogCs.connect(dialogContainer.id, ConstraintSet.RIGHT, dialogHolder!!.id, ConstraintSet.RIGHT)
                        dialogCs.connect(dialogContainer.id, ConstraintSet.BOTTOM, referenceView.id, ConstraintSet.TOP, yMargin.toInt())
                        dialogCs.connect(dialogContainer.id, ConstraintSet.TOP, dialogHolder!!.id, ConstraintSet.TOP)
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

                        dialogCs.applyTo(dialogHolder)

                        dialogHolder!!.visibility = VISIBLE

                        invalidate()
                    } else {
                        dialogCs.connect(dialogContainer.id, ConstraintSet.LEFT, dialogHolder!!.id, ConstraintSet.LEFT, xMargin.toInt())
                        dialogCs.connect(dialogContainer.id, ConstraintSet.BOTTOM, referenceView.id, ConstraintSet.TOP, yMargin.toInt())

                        val firstVertexX = xMargin + dialogContent.layoutParams.width * destinationOffsetPercent
                        val firstVertexY = startY - yMargin

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX + TRIANGLE_SPACING_PX
                        val secondVertexY = firstVertexY

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(dialogHolder)

                        dialogHolder!!.visibility = VISIBLE

                        invalidate()
                    }
                } else {
                    val startX = currentLocation!![0] + viewWidth * originOffsetPercent
                    val startY = currentLocation!![1] + viewHeight

                    path.moveTo(startX, startY.toFloat())

                    if (shouldCenterOnMainAxis) {
                        dialogCs.connect(dialogContainer.id, ConstraintSet.LEFT, dialogHolder!!.id, ConstraintSet.LEFT)
                        dialogCs.connect(dialogContainer.id, ConstraintSet.RIGHT, dialogHolder!!.id, ConstraintSet.RIGHT)
                        dialogCs.connect(dialogContainer.id, ConstraintSet.TOP, referenceView.id, ConstraintSet.BOTTOM, yMargin.toInt())
                        dialogCs.connect(dialogContainer.id, ConstraintSet.BOTTOM, dialogHolder!!.id, ConstraintSet.BOTTOM)
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

                        dialogCs.applyTo(dialogHolder)

                        dialogHolder!!.visibility = VISIBLE

                        invalidate()
                    } else {
                        dialogCs.connect(dialogContainer.id, ConstraintSet.LEFT, dialogHolder!!.id, ConstraintSet.LEFT, xMargin.toInt())
                        dialogCs.connect(dialogContainer.id, ConstraintSet.TOP, referenceView.id, ConstraintSet.BOTTOM, yMargin.toInt())

                        val firstVertexX = xMargin + dialogContent.layoutParams.width * destinationOffsetPercent
                        val firstVertexY = startY + yMargin

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX + TRIANGLE_SPACING_PX
                        val secondVertexY = firstVertexY

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(dialogHolder)

                        dialogHolder!!.visibility = VISIBLE

                        invalidate()
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
    private fun cloneViewLocationAndSize(view: View) {
        // view that acts a as a clone of the original view is always at first index
        val referenceView = dialogHolder!!.getChildAt(0)

        val params = referenceView.layoutParams as ConstraintLayout.LayoutParams

        params.leftMargin = currentLocation!![0]
        params.topMargin = currentLocation!![1]

        params.width = view.width
        params.height = view.height

//        referenceView.setBackgroundColor(Color.GREEN)
    }

    override fun drawChild(canvas: Canvas, child: View?, drawingTime: Long): Boolean {
        if (getChildAt(0).id == child?.id) {
            selectedView?.let { view ->
                // this custom layout should only have one child and we record/copy it with no effect here
                // into the content copy node, but note it doesn't end up being drawn on canvas
                contentCopy.setPosition(0, 0, width, height)
                val contentCopyRecordingCanvas = contentCopy.beginRecording()
                val isInvalidatedIssued =
                    super.drawChild(contentCopyRecordingCanvas, child, drawingTime)

                contentCopy.endRecording()

                // the only child with no effect is in our previous render node, note it was
                // not draw on canvas. The one we will draw on canvas is the below render node, note
                // this copy made from the no effect render node above has the blur effect
                blurredContent.setRenderEffect(
                    RenderEffect.createBlurEffect(
                        20f,
                        20f,
                        Shader.TileMode.CLAMP
                    )
                )

                blurredContent.setPosition(0, 0, width, height)
                val blurredContentRecordingCanvas = blurredContent.beginRecording()
                blurredContentRecordingCanvas.drawRenderNode(contentCopy)
                blurredContent.endRecording()
                // draw the blurred content to this custom layout canvas
                canvas.drawRenderNode(blurredContent)

                if (currentLocation?.size == 2) {
                    focusedContent.setPosition(
                        0,
                        0,
                        view.width,
                        view.height
                    )

                    focusedContent.translationX = currentLocation!![0].toFloat()
                    focusedContent.translationY = currentLocation!![1].toFloat()

                    val focusAreaRecordingCanvas = focusedContent.beginRecording()

                    focusAreaRecordingCanvas.translate(
                        -currentLocation!![0].toFloat(),
                        -currentLocation!![1].toFloat()
                    )

                    focusAreaRecordingCanvas.drawRenderNode(contentCopy)
                    focusedContent.endRecording()

                    canvas.drawRenderNode(focusedContent)

                    return isInvalidatedIssued
                }

                return isInvalidatedIssued
            }
        }

        // if no view is required to be focus just do
        val invalidated = super.drawChild(canvas, child, drawingTime)
        return invalidated
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        canvas.drawPath(path, paint)
    }
}

