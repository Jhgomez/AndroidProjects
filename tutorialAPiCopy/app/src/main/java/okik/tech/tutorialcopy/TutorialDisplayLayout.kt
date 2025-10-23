package okik.tech.tutorialcopy

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
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
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.annotation.RequiresApi
import androidx.core.graphics.ColorUtils
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
    private lateinit var focusAreaCopyRecordingCanvas: RecordingCanvas

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
//        invalidate()

        val tutorialLayout = TutorialDialogBackground(context)

        val content = DialogContentBinding.inflate(LayoutInflater.from(context))

        val widthInPixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            300f,
            resources.displayMetrics
        )

        val hInPixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            300f,
            resources.displayMetrics
        )

        // always specify the width and height of the content of the dialog like this
        content.root.layoutParams = LayoutParams(
            widthInPixels.toInt(),
            LayoutParams.MATCH_PARENT
        )

        (content.root.layoutParams as MarginLayoutParams).setMargins(0, 0, 0, 0)

        tutorialLayout.renderRoundedDialog(
            focusArea,
            focusArea.roundedCornerSurrounding?.innerPadding?.end?.toInt()?.toByte() ?: 0,
            focusArea.roundedCornerSurrounding?.paint,
            focusArea.roundedCornerSurrounding?.cornerRadius?.toFloat(),
            focusArea.surroundingThicknessEffect,
            Gravity.BOTTOM,
            -30f,
            30f,
            0.5f,
            0.5f,
            true,
            content.root
        )

        val popi = PopupWindow(
            tutorialLayout,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            false // closes on outside touche if true
        )

        popi

        popi.showAtLocation(this, Gravity.NO_GRAVITY, 0, 0)

        content.tb.setOnClickListener {
            popi.dismiss()
            this.focusArea = null

            for (i in 1 .. childCount -1) {
                getChildAt(i).visibility = GONE
            }

//            invalidate()
//            binding.viewOverlay?.visibility = View.INVISIBLE
//            binding.root.setRenderEffect(null)
//            requireActivity().window.decorView.setRenderEffect(null)

        }
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
        val focusSurrounding = RoundContainerTwo(context)
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

                val overlayColor = ColorUtils.setAlphaComponent(fa.overlayColor, 100)
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

                                focusAreaCopyRecordingCanvas =
                                    focusedContentCopy.beginRecording()

                                (fa.view.context as Activity).window.decorView.background.draw(focusAreaCopyRecordingCanvas)

                                focusAreaCopyRecordingCanvas.translate(
                                    canvasTranslationX + fa.surroundingThickness.start,
                                    canvasTranslationY + fa.surroundingThickness.top
                                )

                                focusAreaCopyRecordingCanvas.drawRenderNode(contentCopy)

//                                focusedContentCopy.endRecording()
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

                    // child at 1 is always the overlay
                    getChildAt(1).apply {
                        alpha = 0f
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
                focusArea?.also { fa ->
                    // first just draw overlay to screen(view's canvas)
                    val isInvalidatedIssued = super.drawChild(canvas, child, drawingTime)

                    // this code can't execute without focus area being not null,
                    // so we just check whatever the user set for surrounding area,
                    // remember rounded corner surrounding is draw on top of any effect applied
                    // to the outer area
                    if (fa.roundedCornerSurrounding != null) {
                        // again, if a rounder corner surrounding was passed, we will draw a rounded
                        // view on top of the view that was applied an effect(if any), which at the same
                        // time has an overlay on top, and then the below rounded surrounding so we can
                        // then draw the plain copy on top, this is what creates the visual focus, child
                        // at 2 is always a rounded view we set up as requested
                        if (childCount > 2) {
                            val surrounding = getChildAt(2)
                            (surrounding as RoundContainerTwo).apply {
                                val shapeWidth = fa.view.width +
                                        fa.surroundingThickness.start +
                                        fa.surroundingThickness.end

                                val shapeHeight = fa.view.height +
                                        fa.surroundingThickness.top +
                                        fa.surroundingThickness.bottom

                                layoutParams = LayoutParams(shapeWidth.toInt(), shapeHeight.toInt())

//                            (layoutParams as MarginLayoutParams).setMargins(focusArea!!.roundedCornerSurrounding!!.marginEnd.toInt())
//                            this.invalidate()

                                if (fa.roundedCornerSurrounding.cornerRadius > 0) {
                                    val n = fa.roundedCornerSurrounding.cornerRadius.toFloat()

                                    val roundShape = RoundRectShape(
                                        floatArrayOf(n, n, n, n, n, n, n ,n),
//            RectF(0f, 0f, 100f, 100f),
                                        null,
                                        null
                                    )

                                    val shapeDrawable = ShapeDrawable(roundShape)

                                    surrounding.setEffectHolderBackgroundDrawable(shapeDrawable)

                                    // TODO change "setEffectHolderBackgroundPadding" to accept four padding values
                                    surrounding.setEffectHolderBackgroundPadding(fa.roundedCornerSurrounding.innerPadding.end.toInt())

                                    val aPaint = Paint()
                                    aPaint.color = Color.BLUE
                                    aPaint.alpha = 30
                                    aPaint.style = Paint.Style.FILL
                                    surrounding.setEffectHolderBackgroundPaint(aPaint)
                                }

//                                this.setPadding(
//                                    fa.roundedCornerSurrounding!!.innerPadding.start.toInt(),
//                                    fa.roundedCornerSurrounding!!.innerPadding.top.toInt(),
//                                    fa.roundedCornerSurrounding!!.innerPadding.end.toInt(),
//                                    fa.roundedCornerSurrounding!!.innerPadding.bottom.toInt()
//                                )

//                                not using the roundedContainer view anymore
//                                setPaint(fa.roundedCornerSurrounding!!.paint)
//                                setRadius(fa.roundedCornerSurrounding!!.cornerRadius.toFloat())

                                // """the effect is not applied to focus area view itself"""
                                // this effect actually may be useless here, consider passing a null value
                                // as when applied to this view it doesn't affect the views below(that is just
                                // android rendering works)
//                                setRenderEffect(fa.surroundingThicknessEffect)

                                val xLocation = fa.viewLocation[0] -
                                        fa.surroundingThickness.start

                                translationX = translationX - (translationX - xLocation)
//
                                val yLocation = fa.viewLocation[1] -
                                        fa.surroundingThickness.top

                                translationY = translationY - (translationY - yLocation)

                                visibility = VISIBLE

                                // we differ rendering focus area again as we need it render on top of the
                                // surrounding rounded area, so it will be render after the rounder area is rendered
                            }
                        }
                    } else {
                        if (fa.surroundingThicknessEffect != null) {
                            if (fa.surroundingThickness.top > 0
                                || fa.surroundingThickness.bottom > 0
                                || fa.surroundingThickness.start > 0
                                || fa.surroundingThickness.end > 0
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
        }

        // rounded surrounding area is always at index 2
        if (childCount > 2) {
            // this will be true only if user passed a rounded surrounding object, so we need to
            // render on canvas rounded background and then the view to focus
            if (getChildAt(2).id == child?.id) {
                if (focusArea != null) {

                    val isInvalidatedIssued =
                        super.drawChild(focusAreaCopyRecordingCanvas, child, drawingTime)

//                    focusAreaCopyRecordingCanvas.drawColor(ColorUtils.setAlphaComponent(Color.RED, 175))

                    focusedContentCopy.endRecording()

                    canvas.drawRenderNode(focusedContentCopy)

                    canvas.drawRenderNode(focusedContent)

                    return isInvalidatedIssued
                }
            }
        }

        // if no focus area has been specified just render the node
        val invalidated = super.drawChild(canvas, child, drawingTime)
        return invalidated
    }
}

