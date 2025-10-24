package okik.tech.tutorialcopy

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.RecordingCanvas
import android.graphics.RenderNode
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.annotation.RequiresApi
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColor
import okik.tech.tutorialcopy.databinding.DialogContentBinding


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
            initComponents(focusArea)
        }

        this.focusArea = focusArea
    }


    private fun initComponents(focusArea: FocusArea) {
        // view at index 1
        val focusSurrounding = FocusSurrounding(context)
        focusSurrounding.id = generateViewId()
//        focusSurrounding.visibility = GONE

        val shapeWidth = focusArea.view.width +
                focusArea.surroundingThickness.start +
                focusArea.surroundingThickness.end

        val shapeHeight = focusArea.view.height +
                focusArea.surroundingThickness.top +
                focusArea.surroundingThickness.bottom

        focusSurrounding.layoutParams = LayoutParams(shapeWidth.toInt(), shapeHeight.toInt())

        val xLocation = focusArea.viewLocation[0] -
                focusArea.surroundingThickness.start

        focusSurrounding.translationX = translationX - (translationX - xLocation)
//
        val yLocation = focusArea.viewLocation[1] -
                focusArea.surroundingThickness.top

        focusSurrounding.translationY = translationY - (translationY - yLocation)

        focusArea?.roundedCornerSurrounding?.apply {
            if (cornerRadius > 0) {
                val n = cornerRadius.toFloat()

                val roundShape = RoundRectShape(
                    floatArrayOf(n, n, n, n, n, n, n ,n),
                    null,
                    null
                )

                val shapeDrawable = ShapeDrawable(roundShape)

                focusSurrounding.setEffectHolderBackgroundDrawable(shapeDrawable)
                focusSurrounding.setEffectHolderBackgroundPaint(paint)
                focusSurrounding.setEffectHolderBackgroundPadding(innerPadding.top.toInt())
//                val aPaint = shapeDrawable.paint
//                aPaint.color = paint.color
//                aPaint.alpha = paint.alpha
//                aPaint.style = paint.style
//                aPaint.strokeWidth = paint.strokeWidth
//                aPaint.isAntiAlias = paint.isAntiAlias

            }
        }

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

                val overlayColor = ColorUtils.setAlphaComponent(fa.overlayColor, fa.overlayAlpha.toInt())
                contentWithEffectRecordingCanvas.drawColor(overlayColor)

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
//                    if (fa.roundedCornerSurrounding == null) {
                        // if user wants to apply an effect to this surrounding area we need to make
                        // a copy of it and then another copy just or the view user wants to add focus on
                        // but if no effect added to this focus area there is no need

//                    }

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

                    return isInvalidatedIssued
                }

                return isInvalidatedIssued
            }
        }


            // this will be true only if user passed a rounded surrounding object, so we need to
            // render on canvas rounded background and then the view to focus
            if (child is FocusSurrounding) {
                if (focusArea != null) {

                    if (focusArea!!.roundedCornerSurrounding != null) {
                        if (focusArea!!.surroundingThickness.top > 0
                            || focusArea!!.surroundingThickness.bottom > 0
                            || focusArea!!.surroundingThickness.start > 0
                            || focusArea!!.surroundingThickness.end > 0
                        ) {
                            (child as FocusSurrounding).renderNodeBlurController(
                                getChildAt(0),
                                focusArea!!,
                                contentCopy
                            )

                            return super.drawChild(canvas, child, drawingTime)

//                            val surroundingWidth = focusArea!!.view.width +
//                                    focusArea!!.surroundingThickness.start +
//                                    focusArea!!.surroundingThickness.end
//
//                            val surroundingHeight = focusArea!!.view.height +
//                                    focusArea!!.surroundingThickness.top +
//                                    focusArea!!.surroundingThickness.bottom
//
//                            focusedContentCopy.setPosition(
//                                0,
//                                0,
//                                surroundingWidth.toInt(),
//                                surroundingHeight.toInt()
//                            )
//
//                            focusedContentCopy.translationX = focusArea!!.viewLocation[0].toFloat() -
//                                    focusArea!!.surroundingThickness.start
//
//                            focusedContentCopy.translationY = focusArea!!.viewLocation[1].toFloat() -
//                                    focusArea!!.surroundingThickness.top
//
//                            val focusAreaCopyRecordingCanvas =
//                                focusedContentCopy.beginRecording()
//                            focusedContentCopy.clipToBounds = true
//                            focusedContentCopy.clipToOutline = true
//
//                            (focusArea!!.view.context as Activity).window.decorView.background.draw(
//                                focusAreaCopyRecordingCanvas
//                            )
//
//                            focusAreaCopyRecordingCanvas.translate(
//                                -focusArea!!.viewLocation[0].toFloat() + focusArea!!.surroundingThickness.start,
//                                -focusArea!!.viewLocation[1].toFloat() + focusArea!!.surroundingThickness.top
//                            )
//
//                            child.setClipToOutline(true)
//                            child.setOutlineProvider(object : ViewOutlineProvider() {
//                                override fun getOutline(view: View?, outline: Outline) {
//                                    child.getBackground().getOutline(outline)
//                                    outline.setAlpha(1f)
//                                }
//                            })
//
//                            (focusArea!!.view.context as Activity).window.decorView.background.draw(
//                                focusAreaCopyRecordingCanvas
//                            )
//
//                            val n = 18f
//
//                            val roundShape = RoundRectShape(
//                                floatArrayOf(n, n, n, n, n, n, n ,n),
//                                null,
//                                null
//                            )
//
//                            val shapeDrawable = ShapeDrawable(roundShape)
//
//
//                            focusAreaCopyRecordingCanvas.drawRenderNode(contentCopy)
//                            super.drawChild(focusAreaCopyRecordingCanvas, child, drawingTime)
//
//                            focusedContentCopy.setRenderEffect(
//                                focusArea!!.surroundingThicknessEffect
//                            )
//
//
//                            val overlay = ColorUtils.setAlphaComponent(
//                                focusArea!!.roundedCornerSurrounding!!.paint.color,
//                                170
//                            )
//
////                            focusAreaCopyRecordingCanvas.drawColor(overlay)
//
//                            focusedContentCopy.endRecording()
//
//                            canvas.drawRenderNode(focusedContentCopy)
                        }
                    } else {
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
                    }

//                    val isInvalidatedIssued =
//                        super.drawChild(focusAreaCopyRecordingCanvas, child, drawingTime)
//
//                    focusAreaCopyRecordingCanvas.drawColor(ColorUtils.setAlphaComponent(Color.RED, 175))
//
//                    focusedContentCopy.endRecording()
//
//                    canvas.drawRenderNode(focusedContentCopy)
//
//                    canvas.drawRenderNode(focusedContent)

                    return false
                }
            }

        // if no focus area has been specified just render the node
        val invalidated = super.drawChild(canvas, child, drawingTime)
        return invalidated
    }
}

