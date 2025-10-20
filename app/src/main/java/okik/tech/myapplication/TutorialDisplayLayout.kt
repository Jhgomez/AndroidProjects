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
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.setMargins
import androidx.core.view.setPadding


/**
 * This custom layout is expected to only have one child, which is usually a Linear or Constraint
 * Layout or similar.
 */
@RequiresApi(Build.VERSION_CODES.S)
class TutorialDisplayLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    var focusArea: FocusArea? = null

    private val contentCopy = RenderNode("ContentCopy")
    private val contentWithEffect = RenderNode("BlurredContent")
    private val focusedContent = RenderNode("FocusContent")
    private val focusedContentCopy = RenderNode("FocusContentCopy")

    val paint: Paint = Paint()

    init {
        paint.color = Color.WHITE
        paint.alpha = 100
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 8f
        paint.isAntiAlias = true
    }

    fun renderFocusArea(focusArea: FocusArea) {
        if (this.focusArea == null) {
            initComponents()
        }

        this.focusArea = focusArea

        // this will trigger the drawChild method, in which we make the copies we need
        invalidate()
    }


    private fun initComponents() {
        // view at index 1
        val colorBackgroundView = View(context)
        colorBackgroundView.id = generateViewId()
        colorBackgroundView.setBackgroundColor(Color.BLUE)
//        colorBackgroundView.alpha = .3f
        colorBackgroundView.visibility = GONE

        addView(colorBackgroundView)

        colorBackgroundView.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )

        // view at index 2
        val focusSurrounding = RoundedShape(context)
        focusSurrounding.id = generateViewId()
        focusSurrounding.visibility = GONE

        addView(focusSurrounding)
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

                                val surroundingWidth = focusWidth + fa.surroundingThickness.start + fa.surroundingThickness.end
                                val surroundingHeight = focusHeight + fa.surroundingThickness.top + fa.surroundingThickness.bottom

                                focusedContentCopy.setPosition(
                                    0,
                                    0,
                                    surroundingWidth.toInt(),
                                    surroundingHeight.toInt()
                                )

                                focusedContentCopy.translationX =
                                    translationX - fa.surroundingThickness.start
                                focusedContentCopy.translationY =
                                    translationY - fa.surroundingThickness.top

                                val focusAreaCopyRecordingCanvas =
                                    focusedContentCopy.beginRecording()

                                focusAreaCopyRecordingCanvas.translate(
                                    canvasTranslationX + fa.surroundingThickness.start,
                                    canvasTranslationY + fa.surroundingThickness.top
                                )

                                focusAreaCopyRecordingCanvas.drawRenderNode(contentCopy)

                                focusedContentCopy.endRecording()
                            }
                        } else {
                            // if no effect and no rounded corner surrounding then make the copy
                            // the correct size
                            focusWidth += fa.surroundingThickness.start.toInt() + fa.surroundingThickness.end.toInt()
                            focusHeight += fa.surroundingThickness.top.toInt() + fa.surroundingThickness.bottom.toInt()

                            translationX -= fa.surroundingThickness.start
                            translationY -= fa.surroundingThickness.top

                            canvasTranslationX += fa.surroundingThickness.start
                            canvasTranslationY += fa.surroundingThickness.top
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
                            val shapeWidth = focusArea!!.view.width +
                                    focusArea!!.surroundingThickness.start +
                                    focusArea!!.surroundingThickness.end

                            val shapeHeight = focusArea!!.view.height +
                                    focusArea!!.surroundingThickness.top +
                                    focusArea!!.surroundingThickness.bottom

                            layoutParams = LayoutParams(shapeWidth.toInt(), shapeHeight.toInt())

//                            (layoutParams as MarginLayoutParams).setMargins(focusArea!!.roundedCornerSurrounding!!.marginEnd.toInt())
//                            this.invalidate()

                            this.setPadding(
                                focusArea!!.roundedCornerSurrounding!!.innerPadding.start.toInt(),
                                focusArea!!.roundedCornerSurrounding!!.innerPadding.top.toInt(),
                                focusArea!!.roundedCornerSurrounding!!.innerPadding.end.toInt(),
                                focusArea!!.roundedCornerSurrounding!!.innerPadding.bottom.toInt()
                            )

                            setPaint(focusArea!!.roundedCornerSurrounding!!.paint)
                            setRadius(focusArea!!.roundedCornerSurrounding!!.cornerRadius.toFloat())
                            // this effect actually may be useless here, consider passing a null value
                            // as when applied to this view it doesn't affect the views below(that is just
                            // android rendering works)
                            setRenderEffect(focusArea!!.surroundingThicknessEffect)

                            val xLocation = focusArea!!.viewLocation[0] -
                                    focusArea!!.surroundingThickness.start

                            translationX = translationX - (translationX - xLocation)
//
                            val yLocation = focusArea!!.viewLocation[1] -
                                    focusArea!!.surroundingThickness.top

                            translationY = translationY - (translationY - yLocation)

                            visibility = VISIBLE

                            // we differ rendering focus area again as we need it render on top of the
                            // surrounding rounded area, so it will be render after the rounder area is rendered
                        }
                    }
                } else {
                    if (focusArea!!.surroundingThicknessEffect != null) {
                        if (focusArea!!.surroundingThickness.top > 0
                            || focusArea!!.surroundingThickness.bottom > 0
                            || focusArea!!.surroundingThickness.start > 0
                            || focusArea!!.surroundingThickness.end > 0
                        ) {
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
}

