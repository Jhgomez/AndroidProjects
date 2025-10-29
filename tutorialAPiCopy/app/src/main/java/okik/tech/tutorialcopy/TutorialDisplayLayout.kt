package okik.tech.tutorialcopy

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.RenderNode
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.core.graphics.ColorUtils
import androidx.core.view.size

/**
 * This custom layout is expected to only have one child, which is usually a Linear or Constraint
 * Layout or similar.
 */
class TutorialDisplayLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    var focusArea: FocusArea? = null

    private val contentCopy: RenderNode?
    private val contentWithEffect: RenderNode?
    private val focusedContent: RenderNode?

    private var popup: PopupWindow? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            contentCopy = RenderNode("ContentCopy")
            contentWithEffect = RenderNode("BlurredContent")
            focusedContent = RenderNode("FocusContent")
        } else {
            contentCopy = null
            contentWithEffect = null
            focusedContent = null
        }
    }

    fun renderFocusArea(focusArea: FocusArea) {
        if (this.focusArea == null) {
            initComponents(focusArea)
        }

        this.focusArea = focusArea
    }

    fun renderFocusAreaWithDialog(focusArea: FocusArea, focusDialog: FocusDialog) {
        this.renderFocusArea(focusArea)

        val dialogWrapperLayout = DialogWrapperLayout(context)

        dialogWrapperLayout.configuredDialog(
            focusDialog,
            if (focusDialog.shouldClipToBackground) contentCopy else contentWithEffect
        )

        popup = PopupWindow(
            dialogWrapperLayout,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            true // closes on outside touche if true
        )

        this.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}
            override fun onViewDetachedFromWindow(v: View) { dismissDialog() }
        })

        popup!!.setOnDismissListener(PopupWindow.OnDismissListener { popup = null })

        popup!!.showAtLocation(this, Gravity.NO_GRAVITY, 0, 0)
    }

    fun dismissDialog() {
        this.focusArea = null

        if (popup != null && popup!!.isShowing) popup!!.dismiss()

        popup = null

        for (i in 1..size - 1) {
            val child = getChildAt(i)
            if (child is BackgroundEffectRendererLayout) {
                child.visibility = GONE
                break
            }
        }
    }

    private fun initComponents(focusArea: FocusArea) {
        // view at index 1
        val focusSurrounding = BackgroundEffectRendererLayout(context)
        focusSurrounding.id = generateViewId()

        val shapeWidth = focusArea.view.width +
                focusArea.surroundingThickness.start +
                focusArea.surroundingThickness.end

        val shapeHeight = focusArea.view.height +
                focusArea.surroundingThickness.top +
                focusArea.surroundingThickness.bottom

        focusSurrounding.layoutParams = LayoutParams(shapeWidth.toInt(), shapeHeight.toInt())

        val xLocation = focusArea.viewLocation[0] -
                focusArea.surroundingThickness.start

        val yLocation = focusArea.viewLocation[1] -
                focusArea.surroundingThickness.top

        (focusSurrounding.layoutParams as MarginLayoutParams).setMargins(xLocation.toInt(), yLocation.toInt(), 0 , 0)

        val backgroundSettings = focusArea.generateBackgroundSettings (
            { recordingCanvas, _ ->
                recordingCanvas.translate(
                    -focusArea.viewLocation[0].toFloat() + focusArea.surroundingThickness.start,
                    -focusArea.viewLocation[1].toFloat() + focusArea.surroundingThickness.top,
                )
            }
        )

        focusSurrounding.setBackgroundConfigs(
            backgroundSettings,
            if (focusArea.shouldClipToBackground) contentCopy else contentWithEffect
        )

        if (context is Activity) {
            focusSurrounding.setFallbackBackground((context as Activity).window.decorView.background)
        }

        addView(focusSurrounding)
    }

    override fun drawChild(canvas: Canvas, child: View?, drawingTime: Long): Boolean {
        // child at 0 should always be the only child added, by the user, to this custom view
        if (getChildAt(0).id == child?.id) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                return super.drawChild(canvas, child, drawingTime)
            }

            focusArea?.also { fa ->
                // this custom layout should only have one child and we record its content with no effect here
                // so we can make a copy of the area we want to focus on
                contentCopy!!.setPosition(0, 0, width, height)

                val contentCopyRecordingCanvas = contentCopy.beginRecording()

                val isInvalidatedIssued =
                    super.drawChild(contentCopyRecordingCanvas, child, drawingTime)
                contentCopy.endRecording()

                // create a version of the original view that lives in "contentCopy" and apply the
                // passed effect
                contentWithEffect!!.setRenderEffect(fa.outerAreaEffect)

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

                    focusedContent!!.setPosition(0, 0, focusWidth, focusHeight)

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
        if (child is BackgroundEffectRendererLayout) {
            if (focusArea != null) {
                val hasSurrounding = focusArea!!.surroundingThickness.top > 0
                        || focusArea!!.surroundingThickness.bottom > 0
                        || focusArea!!.surroundingThickness.start > 0
                        || focusArea!!.surroundingThickness.end > 0

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    if (hasSurrounding) {
                        val isInvalidateIssued = super.drawChild(canvas, child, drawingTime)

                        canvas.translate(
                            focusArea!!.viewLocation[0].toFloat(),
                            focusArea!!.viewLocation[1].toFloat()
                        )

                        focusArea!!.view.draw(canvas)

                        return isInvalidateIssued
                    } else {
                        focusArea!!.view.draw(canvas)

                        return false
                    }
                } else {
                    if (hasSurrounding) {
                        val isInvalidateIssued = super.drawChild(canvas, child, drawingTime)

                        canvas.drawRenderNode(focusedContent!!)

                        return isInvalidateIssued
                    } else {
                        canvas.drawRenderNode(focusedContent!!)

                        return false
                    }
                }
            }
        }

        // if no focus area has been specified just render the node
        val invalidated = super.drawChild(canvas, child, drawingTime)
        return invalidated
    }
}

