package okik.tech.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet


/**
 * This custom layout is expected to only have one child, which is usually a Linear or Constraint
 * Layout or similar.
 */
@RequiresApi(Build.VERSION_CODES.S)
class TutorialDisplayLayoutCopy @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private var currentLocation: IntArray? = null
    var selectedView: View? = null
    var focusArea: FocusArea? = null

    private val contentCopy = RenderNode("ContentCopy")
    private val contentWithEffect = RenderNode("BlurredContent")
    private val focusedContent = RenderNode("FocusContent")
    private val focusedContentCopy = RenderNode("FocusContentCopy")

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

//        invalidate()
    }



//    view: View,
//    viewLocation: IntArray?,
//    surroundingThickness: Byte,
//    surroundingThicknessEffect: RenderEffect?,
//    outerAreaEffect: RenderEffect?,
//    overlayParams: LayoutParams?,
//    overlayPaint: Paint?

    fun renderFocusArea(focusArea: FocusArea) {
        if (this.focusArea == null) {
            initDialogCL()
        }

        this.focusArea = focusArea

        // this will trigger the drawChild method, in which we make the copies we need
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

        // if dialog holder is null this also means we need to add layer to obscure the background
        if (dialogHolder == null) {
            initDialogCL()
        }

//        setUpDialog(
//            view,
//            dialogGravity,
//            dialogBackgroundPadding,
//            dialogBackgroundPaint,
//            dialogBackgroundRadius,
//            dialogBackgroundEffect,
//            dialogXOffsetDp,
//            dialogYOffsetDp,
//            originOffsetPercent,
//            destinationOffsetPercent,
//            shouldCenterOnMainAxis,
//            dialogContent
//        )

        focusView(
            view,
            viewBackgroundPadding,
            viewBackgroundPaint,
            viewBackgroundRadius,
            viewBackgroundEffect
        )

        invalidate()
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

        val colorBackgroundView = View(context)
        colorBackgroundView.id = generateViewId()
        colorBackgroundView.setBackgroundColor(Color.BLACK)
        colorBackgroundView.alpha = .3f
        colorBackgroundView.visibility = GONE

        addView(colorBackgroundView)

        colorBackgroundView.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )

        val focusSurrounding = RoundedShape(context)
        focusSurrounding.id = generateViewId()

        addView(focusSurrounding)

//        constraintSet.connect(colorBackgroundView.id, ConstraintSet.TOP, dialogHolder!!.id, ConstraintSet.TOP)
//        constraintSet.connect(colorBackgroundView.id, ConstraintSet.LEFT, dialogHolder!!.id, ConstraintSet.LEFT)

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

        topBarHeight = rootWindowInsets?.getInsetsIgnoringVisibility(
            WindowInsets.Type.statusBars()
        )?.top ?: 0

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

//                        invalidate()
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

//                        invalidate()
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

//                        invalidate()
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

//                        invalidate()
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

//                        invalidate()
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

//                        invalidate()
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

//                        invalidate()
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
        // child at 0 should always be the only child added, by the user, to this custom view
        if (getChildAt(0).id == child?.id) {
            focusArea?.also { fa ->
                // this custom layout should only have one child and we record its content with no effect here
                // so we can make a copy of the area we want to focus on
                contentCopy.setPosition(0, 0, width, height)

                val contentCopyRecordingCanvas = contentCopy.beginRecording()
                val isInvalidatedIssued =
                    super.drawChild(contentCopyRecordingCanvas, child, drawingTime)
                contentCopy.endRecording()

                // create a version of the original view that lives in "contentCopy" and apply the
                // passed effect
                contentWithEffect.setRenderEffect(fa.outerAreaEffect)

                contentWithEffect.setPosition(0, 0, width, height)
                val contentWithEffectRecordingCanvas = contentWithEffect.beginRecording()
                contentWithEffectRecordingCanvas.drawRenderNode(contentCopy)
                contentWithEffect.endRecording()

                // draw the applied effect to content to canvas of custom layout
                canvas.drawRenderNode(contentWithEffect)

                // make a copy of original content but only of the specified view and the requested
                // surrounding area
                if (fa.viewLocation.size == 2) {
                    var focusWidth = fa.view.width
                    var focusHeight = fa.view.height

                    var translationX = fa.viewLocation[0].toFloat()
                    var translationY = fa.viewLocation[1].toFloat()

                    var canvasTranslationX = -fa.viewLocation[0].toFloat()
                    var canvasTranslationY = -fa.viewLocation[1].toFloat()

                    // here is where we add the surrounding area thickness as a square which is a
                    // exact copy of the actual surrounding but, again, note that its limitation is
                    // that the surrounding area shape can only be a square
                    if (fa.roundedCornerSurrounding == null) {
                        // if user wants to apply an effect to this surrounding area we need to make
                        // a copy of it and then another copy just or the view user wants to add focus on
                        // but if no effect added to this focus area there is no need
                        if (fa.surroundingThicknessEffect != null) {
                            if (focusArea!!.surroundingThickness.top > 0
                                || focusArea!!.surroundingThickness.bottom > 0
                                || focusArea!!.surroundingThickness.start > 0
                                || focusArea!!.surroundingThickness.end > 0
                            ) {
                                focusedContentCopy.setRenderEffect(fa.surroundingThicknessEffect)

                                val squareSurroundingWidth = focusWidth +
                                        fa.surroundingThickness.start +
                                        fa.surroundingThickness.end

                                val squareSurroundingHeight = focusWidth +
                                        fa.surroundingThickness.top +
                                        fa.surroundingThickness.bottom

                                focusedContentCopy.setPosition(
                                    0,
                                    0,
                                    squareSurroundingWidth.toInt(),
                                    squareSurroundingHeight.toInt()
                                )

                                focusedContentCopy.translationX =
                                    translationX - fa.surroundingThickness.start
                                focusedContentCopy.translationY =
                                    translationY - fa.surroundingThickness.top

                                val focusAreaCopyRecordingCanvas =
                                    focusedContentCopy.beginRecording()

                                focusAreaCopyRecordingCanvas.translate(
                                    canvasTranslationX + fa.surroundingThickness.start,
                                    canvasTranslationY + fa.surroundingThickness.bottom
                                )

                                focusAreaCopyRecordingCanvas.drawRenderNode(contentCopy)

                                focusedContentCopy.endRecording()
                            }
                        } else {
                            // if no effect and no rounded corner surrounding then make the copy
                            // the correct size
                            focusWidth += fa.surroundingThickness.start.toInt()   +
                            focusHeight += fa.surroundingThickness * 2

                            translationX -= fa.surroundingThickness
                            translationY -= fa.surroundingThickness

                            canvasTranslationX += fa.surroundingThickness
                            canvasTranslationY += fa.surroundingThickness
                        }
                    }

                    focusedContent.setPosition(
                        0,
                        0,
                        focusWidth,
                        focusHeight
                    )

                    focusedContent.translationX = translationX
                    focusedContent.translationY = translationY

                    val focusAreaRecordingCanvas = focusedContent.beginRecording()

                    focusAreaRecordingCanvas.translate(
                        canvasTranslationX,
                        canvasTranslationY
                    )

                    focusAreaRecordingCanvas.drawRenderNode(contentCopy)

                    focusedContent.endRecording()

                    // note the focus area was not draw here yet, that is because it will be draw above its
                    // specified surrounding area which at the same time has to be rendered above
                    // the background overlay

                    // child at 1 is always the overlay
                    getChildAt(1).apply {
                        alpha = fa.overlayAlpha
                        setBackgroundColor(fa.overlayColor)
                        visibility = VISIBLE // this triggers a call to drawChild
                    }

                    return isInvalidatedIssued
                }

                return isInvalidatedIssued
            }
        }

        // child at 1 is added "lazily" so we have to make sure its been added already
        if (childCount > 1) {
            // this custom layout will always have a view at index 1 that has a background color with some
            // alpha, and it is draw on top of the copy of the original content that was applied
            // the effect
            if (getChildAt(1).id == child?.id) {
                // first just draw overlay to screen(view's canvas)
                val isInvalidatedIssued = super.drawChild(canvas, child, drawingTime)

                // this code can't execute without focus area being not null,
                // so we just check whatever the user set for surrounding area,
                // remember rounded corner surrounding is draw on top of any effect applied
                // to the outer area
                if (focusArea!!.roundedCornerSurrounding != null) {
                    // again, if a rounder corner surrounding was passed, we will draw a rounded
                    // view on top of the view that was applied an effect(if any), which at the same
                    // time has an overlay on top, and then the below rounded surrounding so we can
                    // then draw the plain copy on top, this is what creates the visual focus, child
                    // at 2 is always a rounded view we set up as requested
                    if (childCount > 2) {
                        (getChildAt(2) as RoundedShape).apply {
                            setPaint(focusArea!!.roundedCornerSurrounding!!.paint)
                            setRadius(focusArea!!.roundedCornerSurrounding!!.cornerRadius.toFloat())

                            val xLocation = focusArea!!.viewLocation[0] - focusArea!!.surroundingThickness
                            translationX = translationX - (translationX - xLocation)

                            val yLocation = focusArea!!.viewLocation[1] - focusArea!!.surroundingThickness
                            translationY = translationY - (translationY - yLocation)

                            visibility = VISIBLE

                            // we differ rendering focus area again as we need it render on top of the
                            // surrounding rounded area, so it will be render after the rounder area is rendered
                        }
                    }
                } else {
                    if (focusArea!!.surroundingThicknessEffect != null) {
                        if (focusArea!!.surroundingThickness > 0) {
                            canvas.drawRenderNode(focusedContentCopy)
                            canvas.drawRenderNode(focusedContent)
                        } else {
                            canvas.drawRenderNode(focusedContent)
                        }
                    } else {
                        canvas.drawRenderNode(focusedContent)
                    }
                }

                return isInvalidatedIssued
            }
        }

        // rounded surrounding area is always at index 2
        if (childCount > 2) {
            // this will be true only if user passed a rounded surrounding object, so we need to
            // render on canvas rounded background and then the view to focus
            if (getChildAt(2).id == child?.id) {
                val isInvalidatedIssued = super.drawChild(canvas, child, drawingTime)

                canvas.drawRenderNode(focusedContent)

                return isInvalidatedIssued
            }
        }

        // if no focus area has been specified just render the node
        val invalidated = super.drawChild(canvas, child, drawingTime)
        return invalidated
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        canvas.drawPath(path, paint)
    }
}

