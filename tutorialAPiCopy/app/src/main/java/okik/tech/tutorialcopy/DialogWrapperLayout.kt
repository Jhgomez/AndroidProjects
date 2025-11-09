package okik.tech.tutorialcopy

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.RenderNode
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.graphics.ColorUtils
import androidx.core.view.isEmpty

/**
 *
 */
class DialogWrapperLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {
    var focusArea: FocusArea? = null

    private var contentCopy: RenderNode? = null
    private val contentWithEffect: RenderNode?
    private val focusedContent: RenderNode?

    init {
        setBackgroundColor(Color.TRANSPARENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            contentWithEffect = RenderNode("BlurredContent")
            focusedContent = RenderNode("FocusContent")
        } else {
            contentWithEffect = null
            focusedContent = null
        }
    }

    private fun addSurroundingFocusAreaView() {
        // view at index 0
        val focusSurrounding = BackgroundEffectRendererLayout(context)
        focusSurrounding.id = generateViewId()

        addView(focusSurrounding)
    }

    private fun configureSurroundingFocusAreaView(
        fa: FocusArea,
        renderNode: RenderNode?
    ) {
        val referenceViewWidth = fa.view.width +
                fa.surroundingThickness.start +
                fa.surroundingThickness.end

        val referenceViewHeight = fa.view.height +
                fa.surroundingThickness.top +
                fa.surroundingThickness.bottom

        val focusViewXLoc = fa.viewLocation[0] - fa.surroundingThickness.start
        val focusViewYLoc = fa.viewLocation[1] - fa.surroundingThickness.top

        val isDrawnB4Start = focusViewXLoc  < 0
        val isDrawnB4Top = focusViewYLoc < 0

        val constraintSet = ConstraintSet()
        constraintSet.clone(this)

        val xConstraint = if (isDrawnB4Start) ConstraintSet.END else ConstraintSet.START
        val yConstraint = if (isDrawnB4Top) ConstraintSet.BOTTOM else ConstraintSet.TOP

        val focusSurrounding = getChildAt(0) as BackgroundEffectRendererLayout;

//        focusSurrounding.visibility = VISIBLE

        constraintSet.connect(focusSurrounding.id, yConstraint, id, ConstraintSet.TOP)
        constraintSet.connect(focusSurrounding.id, xConstraint, id, ConstraintSet.START)

        constraintSet.applyTo(this)

        val params = focusSurrounding.layoutParams as LayoutParams

        if (isDrawnB4Start) {
            params.marginEnd = (-referenceViewWidth - focusViewXLoc).toInt()
        } else{
            params.marginStart = focusViewXLoc.toInt()
        }

        if (isDrawnB4Top) {
            params.bottomMargin = (-referenceViewHeight - focusViewYLoc).toInt()
        }else {
            params.topMargin = focusViewYLoc.toInt()
        }

        params.width = referenceViewWidth.toInt()
        params.height = referenceViewHeight.toInt()

        val backgroundSettings = fa.generateBackgroundSettings (
            { recordingCanvas, _ ->
                recordingCanvas.translate(
                    -fa.viewLocation[0].toFloat() + fa.surroundingThickness.start,
                    -fa.viewLocation[1].toFloat() + fa.surroundingThickness.top,
                )
            }
        )

        focusSurrounding.setBackgroundConfigs(backgroundSettings, renderNode)

        if (context is Activity) {
            focusSurrounding.setFallbackBackground((context as Activity).window.decorView.background)
        }
    }

    fun configuredDialog(fa: FocusArea, fd: FocusDialog, renderNode: RenderNode?) {
        contentCopy = renderNode
        focusArea = fa

        if (fd.dialogView is BackgroundEffectRendererLayout) {
            fd.dialogView.setBackgroundRenderNode(renderNode)

            if (context is Activity) {
                val act = getContext() as Activity
                fd.dialogView.setFallbackBackground(act.window.decorView.background)
            }
        }

        if (isEmpty()) {
            if (id == NO_ID) id = generateViewId()

            addSurroundingFocusAreaView()

            val bridgeView = RenderNodeBehindPathView(context)
            bridgeView.id = generateViewId()

            val constraintSet = ConstraintSet()
            constraintSet.clone(this)

            constraintSet.connect(bridgeView.id, ConstraintSet.TOP, id, ConstraintSet.TOP)
            constraintSet.connect(bridgeView.id, ConstraintSet.START, id, ConstraintSet.START)
            constraintSet.applyTo(this)

            // bridge view has to fill entire screen to be able to draw the path in the required location
            bridgeView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

            if (context is Activity) {
                bridgeView.setFallbackBackground((context as Activity).window.decorView.background)
            }

            addView(bridgeView)

            if (fd.dialogView.id == NO_ID) fd.dialogView.id = generateViewId()
            addView(fd.dialogView)
        }

        configureSurroundingFocusAreaView(fa, renderNode)

        val dialogPreDrawListener = getOnPreDrawListener(fd, renderNode)

        fd.dialogView.viewTreeObserver.addOnPreDrawListener(dialogPreDrawListener)

        this.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}
            override fun onViewDetachedFromWindow(v: View) {
                fd.dialogView.viewTreeObserver.removeOnPreDrawListener(dialogPreDrawListener)
            }
        })

        val referenceView = getChildAt(0)
        fd.dialogConstraintsCommand(this, referenceView, fd.dialogView)
    }

    private fun getOnPreDrawListener(fd: FocusDialog, renderNode: RenderNode?):
            ViewTreeObserver.OnPreDrawListener = object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            val referenceView = getChildAt(0)

            if (fd.pathViewPathGeneratorCommand != null) {

                val path = fd.pathViewPathGeneratorCommand(referenceView, fd.dialogView)

                val bridgeView = getChildAt(1) as RenderNodeBehindPathView

                bridgeView.setBackgroundConfigs(
                    renderNode,
                    path,
                    fd.originBackgroundPaint,
                    true,
                    true,
                    fd.backgroundRenderEffect,
                    fd.pathViewRenderCanvasPositionCommand
                )

                fd.dialogView.viewTreeObserver.removeOnPreDrawListener(this)
            }

            return true
        }
    }

    override fun draw(canvas: Canvas) {
            focusArea?.also { fa ->
                val overlayColor =
                    ColorUtils.setAlphaComponent(fa.overlayColor, fa.overlayAlpha.toInt())

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    canvas.drawColor(overlayColor)
                }

                if (contentCopy != null) {
                    // create a version of the original view that lives in "contentCopy" and apply the
                    // passed effect
                    contentWithEffect!!.setRenderEffect(fa.outerAreaEffect)

                    contentWithEffect.setPosition(0, 0, width, height)
                    val contentWithEffectRecordingCanvas = contentWithEffect.beginRecording()
                    contentWithEffectRecordingCanvas.drawRenderNode(contentCopy!!)

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

                        focusAreaRecordingCanvas.drawRenderNode(contentCopy!!)

                        focusedContent.endRecording()

                        // note the focus area was not draw here yet, that is because it will be draw above its
                        // specified surrounding area which at the same time has to be rendered above
                        // the background overlay

                    }
                }
            }

            super.draw(canvas)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    fun updateBackground(renderNode: RenderNode) {
        val focusAreaView = getChildAt(0) as BackgroundEffectRendererLayout
        focusAreaView.setBackgroundRenderNode(renderNode)
        focusAreaView.invalidate()

        val bridgeView = getChildAt(1) as RenderNodeBehindPathView
        bridgeView.setBackgroundViewRenderNode(renderNode)
        bridgeView.invalidate()

        val dialogView = getChildAt(2) as BackgroundEffectRendererLayout
        dialogView.setBackgroundRenderNode(renderNode)
        dialogView.invalidate()

        invalidate()
    }


    override fun drawChild(canvas: Canvas, child: View?, drawingTime: Long): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            // since we are not using render nodes in this case, we have to wait until the
            // last child is draw to then be able to draw the view we want to focus on and
            // then move the canvas to match its location on the screen, if we move the canvas
            // before this, then children draw after moving canvas will be affected
            if (getChildAt(2).id == child?.id) {
                if (focusArea != null) {
                    val hasSurrounding = focusArea!!.surroundingThickness.top > 0
                            || focusArea!!.surroundingThickness.bottom > 0
                            || focusArea!!.surroundingThickness.start > 0
                            || focusArea!!.surroundingThickness.end > 0

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
                }
            }
        } else {
            if (getChildAt(0).id == child?.id) {
                if (focusArea != null) {
                    val hasSurrounding = focusArea!!.surroundingThickness.top > 0
                            || focusArea!!.surroundingThickness.bottom > 0
                            || focusArea!!.surroundingThickness.start > 0
                            || focusArea!!.surroundingThickness.end > 0

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

    companion object {
        fun constraintDialogToBottom(
            constraintLayout: ConstraintLayout,
            referenceView: View,
            dialogView: View,
            dialogXMarginPx: Double,
            dialogYMarginPx: Double,
            centerDialogOnMainAxis: Boolean
        ) {
            val dialogCs = ConstraintSet()
            dialogCs.clone(constraintLayout)

            if (centerDialogOnMainAxis) {
                dialogCs.connect(dialogView.id, ConstraintSet.LEFT, constraintLayout.id, ConstraintSet.LEFT)
                dialogCs.connect(dialogView.id, ConstraintSet.RIGHT, constraintLayout.id, ConstraintSet.RIGHT)
                dialogCs.connect(dialogView.id, ConstraintSet.TOP, referenceView.id, ConstraintSet.BOTTOM)
                dialogCs.connect(dialogView.id, ConstraintSet.BOTTOM, constraintLayout.id, ConstraintSet.BOTTOM)
                dialogCs.setVerticalBias(dialogView.id, 0f)

                dialogCs.applyTo(constraintLayout)
            } else {
                dialogCs.connect(dialogView.id, ConstraintSet.LEFT, referenceView.id, ConstraintSet.LEFT)
                dialogCs.connect(dialogView.id, ConstraintSet.TOP, referenceView.id, ConstraintSet.BOTTOM)
                dialogCs.connect(dialogView.id, ConstraintSet.BOTTOM, constraintLayout.id, ConstraintSet.BOTTOM)
                dialogCs.setVerticalBias(dialogView.id, 0f)

                dialogCs.applyTo(constraintLayout)
            }

            (dialogView.layoutParams as MarginLayoutParams).marginStart = dialogXMarginPx.toInt()
            (dialogView.layoutParams as MarginLayoutParams).topMargin = dialogYMarginPx.toInt()
        }

        fun constraintDialogToTop(
            constraintLayout: ConstraintLayout,
            referenceView: View,
            dialogView: View,
            dialogXMarginPx: Double,
            dialogYMarginPx: Double,
            centerDialogOnMainAxis: Boolean
        ) {
            val dialogCs = ConstraintSet()
            dialogCs.clone(constraintLayout)

            if (centerDialogOnMainAxis) {
                dialogCs.connect(dialogView.id, ConstraintSet.LEFT, constraintLayout.id, ConstraintSet.LEFT)
                dialogCs.connect(dialogView.id, ConstraintSet.RIGHT, constraintLayout.id, ConstraintSet.RIGHT)
                dialogCs.connect(dialogView.id, ConstraintSet.BOTTOM, referenceView.id, ConstraintSet.TOP)
                dialogCs.connect(dialogView.id, ConstraintSet.TOP, constraintLayout.id, ConstraintSet.TOP)
                dialogCs.setVerticalBias(dialogView.id, 1f)

                dialogCs.applyTo(constraintLayout)
            } else {
                dialogCs.connect(dialogView.id, ConstraintSet.LEFT, referenceView.id, ConstraintSet.LEFT)
                dialogCs.connect(dialogView.id, ConstraintSet.BOTTOM, referenceView.id, ConstraintSet.TOP)
                dialogCs.connect(dialogView.id, ConstraintSet.TOP, constraintLayout.id, ConstraintSet.TOP)
                dialogCs.setVerticalBias(dialogView.id, 1f)

                dialogCs.applyTo(constraintLayout)
            }

            (dialogView.layoutParams as MarginLayoutParams).marginStart = dialogXMarginPx.toInt()
            (dialogView.layoutParams as MarginLayoutParams).bottomMargin = dialogYMarginPx.toInt()
        }

        fun constraintDialogToStart(
            constraintLayout: ConstraintLayout,
            referenceView: View,
            dialogView: View,
            dialogXMarginPx: Double,
            dialogYMarginPx: Double,
            centerDialogOnMainAxis: Boolean
        ) {
            val dialogCs = ConstraintSet()
            dialogCs.clone(constraintLayout)

            if (centerDialogOnMainAxis) {
                dialogCs.connect(dialogView.id, ConstraintSet.TOP, constraintLayout.id, ConstraintSet.TOP)
                dialogCs.connect(dialogView.id, ConstraintSet.BOTTOM, constraintLayout.id, ConstraintSet.BOTTOM)
                dialogCs.setHorizontalBias(dialogView.id, 1f)
                dialogCs.connect(dialogView.id, ConstraintSet.RIGHT, referenceView.id, ConstraintSet.LEFT)
                dialogCs.connect(dialogView.id, ConstraintSet.LEFT, constraintLayout.id, ConstraintSet.LEFT)

                dialogCs.applyTo(constraintLayout)
            } else {
                dialogCs.connect(dialogView.id, ConstraintSet.TOP, referenceView.id, ConstraintSet.TOP)
                dialogCs.setVerticalBias(dialogView.id, 1f)
                dialogCs.connect(dialogView.id, ConstraintSet.LEFT, constraintLayout.id, ConstraintSet.LEFT)
                dialogCs.connect(dialogView.id, ConstraintSet.RIGHT, referenceView.id, ConstraintSet.LEFT)

                dialogCs.applyTo(constraintLayout)
            }

            (dialogView.layoutParams as MarginLayoutParams).marginEnd = dialogXMarginPx.toInt()
            (dialogView.layoutParams as MarginLayoutParams).topMargin = dialogYMarginPx.toInt()
        }

        fun constraintDialogToEnd(
            constraintLayout: ConstraintLayout,
            referenceView: View,
            dialogView: View,
            dialogXMarginPx: Double,
            dialogYMarginPx: Double,
            centerDialogOnMainAxis: Boolean
        ) {
            val dialogCs = ConstraintSet()
            dialogCs.clone(constraintLayout)

            if (centerDialogOnMainAxis) {
                dialogCs.connect(dialogView.id, ConstraintSet.TOP, constraintLayout.id, ConstraintSet.TOP)
                dialogCs.connect(dialogView.id, ConstraintSet.BOTTOM, constraintLayout.id, ConstraintSet.BOTTOM)
                dialogCs.setHorizontalBias(dialogView.id, 0f)
                dialogCs.connect(dialogView.id, ConstraintSet.LEFT, referenceView.id, ConstraintSet.RIGHT)
                dialogCs.connect(dialogView.id, ConstraintSet.RIGHT, constraintLayout.id, ConstraintSet.RIGHT)

                dialogCs.applyTo(constraintLayout)
            } else {
                dialogCs.connect(dialogView.id, ConstraintSet.TOP, referenceView.id, ConstraintSet.TOP)
                dialogCs.setHorizontalBias(dialogView.id, 0f)
                dialogCs.connect(dialogView.id, ConstraintSet.LEFT, referenceView.id, ConstraintSet.RIGHT)
                dialogCs.connect(dialogView.id, ConstraintSet.RIGHT, constraintLayout.id, ConstraintSet.RIGHT)

                dialogCs.applyTo(constraintLayout)
            }

            (dialogView.layoutParams as MarginLayoutParams).marginStart = dialogXMarginPx.toInt()
            (dialogView.layoutParams as MarginLayoutParams).topMargin = dialogYMarginPx.toInt()
        }

        fun drawPathToBottomDialog(
            referenceView: View,
            dialogView: View,
            originOffsetPercent: Double,
            destinationOffsetPercent: Double,
            triangleSpacingPx: Double
        ): Path {
            val parentLocation = IntArray(2)

            val parent = referenceView.parent

            if (parent is View) {
                parent.getLocationOnScreen(parentLocation)
            }

            val referenceViewLocation = IntArray(2)
            referenceView.getLocationOnScreen(referenceViewLocation)
            referenceViewLocation[0] = referenceViewLocation[0] - parentLocation[0]
            referenceViewLocation[1] = referenceViewLocation[1] - parentLocation[1]

            val dialogViewLocation = IntArray(2)
            dialogView.getLocationOnScreen(dialogViewLocation)
            dialogViewLocation[0] = dialogViewLocation[0] - parentLocation[0]
            dialogViewLocation[1] = dialogViewLocation[1] - parentLocation[1]

            // ======================================

            val startX = referenceViewLocation[0] + referenceView.width * originOffsetPercent
            val startY = referenceViewLocation[1] + referenceView.height

            val path = Path()

            path.moveTo(startX.toFloat(), startY.toFloat())

            val firstVertexX = dialogViewLocation[0] + dialogView.width * destinationOffsetPercent.toFloat()
            val firstVertexY = dialogViewLocation[1]

            path.lineTo(firstVertexX, firstVertexY.toFloat())

            val secondVertexX = firstVertexX + triangleSpacingPx

            path.lineTo(secondVertexX.toFloat(), firstVertexY.toFloat())
            path.close()

            return path
        }

        fun drawPathToTopDialog(
            referenceView: View,
            dialogView: View,
            originOffsetPercent: Double,
            destinationOffsetPercent: Double,
            triangleSpacingPx: Double
        ): Path {
            val parentLocation = IntArray(2)

            val parent = referenceView.parent

            if (parent is View) {
                parent.getLocationOnScreen(parentLocation)
            }

            val referenceViewLocation = IntArray(2)
            referenceView.getLocationOnScreen(referenceViewLocation)
            referenceViewLocation[0] = referenceViewLocation[0] - parentLocation[0]
            referenceViewLocation[1] = referenceViewLocation[1] - parentLocation[1]

            val dialogViewLocation = IntArray(2)
            dialogView.getLocationOnScreen(dialogViewLocation)
            dialogViewLocation[0] = dialogViewLocation[0] - parentLocation[0]
            dialogViewLocation[1] = dialogViewLocation[1] - parentLocation[1]

            // ======================================
            val startX = referenceViewLocation[0] + referenceView.width * originOffsetPercent
            val startY = referenceViewLocation[1]

            val path = Path()
            path.moveTo(startX.toFloat(), startY.toFloat())

            val firstVertexX = dialogViewLocation[0] + dialogView.width * destinationOffsetPercent.toFloat()
            val firstVertexY = dialogViewLocation[1] + dialogView.height

            path.lineTo(firstVertexX, firstVertexY.toFloat())

            val secondVertexX = firstVertexX + triangleSpacingPx

            path.lineTo(secondVertexX.toFloat(), firstVertexY.toFloat())
            path.close()

            return path
        }

        fun drawPathToStartDialog(
            referenceView: View,
            dialogView: View,
            originOffsetPercent: Double,
            destinationOffsetPercent: Double,
            triangleSpacingPx: Double
        ): Path {
            val parentLocation = IntArray(2)

            val parent = referenceView.parent

            if (parent is View) {
                parent.getLocationOnScreen(parentLocation)
            }

            val referenceViewLocation = IntArray(2)
            referenceView.getLocationOnScreen(referenceViewLocation)
            referenceViewLocation[0] = referenceViewLocation[0] - parentLocation[0]
            referenceViewLocation[1] = referenceViewLocation[1] - parentLocation[1]

            val dialogViewLocation = IntArray(2)
            dialogView.getLocationOnScreen(dialogViewLocation)
            dialogViewLocation[0] = dialogViewLocation[0] - parentLocation[0]
            dialogViewLocation[1] = dialogViewLocation[1] - parentLocation[1]

            // ======================================
            val startX = referenceViewLocation[0]
            val startY = referenceViewLocation[1] + referenceView.height * originOffsetPercent

            val path = Path()
            path.moveTo(startX.toFloat(), startY.toFloat())

            val firstVertexX = dialogViewLocation[0] + dialogView.width
            val firstVertexY = dialogViewLocation[1] + dialogView.height * destinationOffsetPercent.toFloat()

            path.lineTo(firstVertexX.toFloat(), firstVertexY.toFloat())

            val secondVertexY = firstVertexY + triangleSpacingPx

            path.lineTo(firstVertexX.toFloat(), secondVertexY.toFloat())
            path.close()

            return path
        }

        fun drawPathToEndDialog(
            referenceView: View,
            dialogView: View,
            originOffsetPercent: Double,
            destinationOffsetPercent: Double,
            triangleSpacingPx: Double
        ): Path {
            val parentLocation = IntArray(2)

            val parent = referenceView.parent

            if (parent is View) {
                parent.getLocationOnScreen(parentLocation)
            }

            val referenceViewLocation = IntArray(2)
            referenceView.getLocationOnScreen(referenceViewLocation)
            referenceViewLocation[0] = referenceViewLocation[0] - parentLocation[0]
            referenceViewLocation[1] = referenceViewLocation[1] - parentLocation[1]

            val dialogViewLocation = IntArray(2)
            dialogView.getLocationOnScreen(dialogViewLocation)
            dialogViewLocation[0] = dialogViewLocation[0] - parentLocation[0]
            dialogViewLocation[1] = dialogViewLocation[1] - parentLocation[1]

            // ======================================
            val startX = referenceViewLocation[0] + referenceView.width
            val startY = referenceViewLocation[1] + referenceView.height * originOffsetPercent

            val path = Path()
            path.moveTo(startX.toFloat(), startY.toFloat())

            val firstVertexX = dialogViewLocation[0]
            val firstVertexY = dialogViewLocation[1] + dialogView.height * destinationOffsetPercent.toFloat()

            path.lineTo(firstVertexX.toFloat(), firstVertexY.toFloat())

            val secondVertexY = firstVertexY + triangleSpacingPx

            path.lineTo(firstVertexX.toFloat(), secondVertexY.toFloat())
            path.close()

            return path
        }
    }
}