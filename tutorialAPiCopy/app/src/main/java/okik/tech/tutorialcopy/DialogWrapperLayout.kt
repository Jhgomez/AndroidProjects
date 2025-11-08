package okik.tech.tutorialcopy

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Path
import android.graphics.RenderNode
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
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

    init {
        setBackgroundColor(Color.TRANSPARENT)
    }

    fun configuredDialog(fd: FocusDialog, renderNode: RenderNode?) {
        if (fd.dialogView is BackgroundEffectRendererLayout) {
            fd.dialogView.setBackgroundRenderNode(renderNode)

            if (context is Activity) {
                val act = getContext() as Activity
                fd.dialogView.setFallbackBackground(act.window.decorView.background)
            }
        }

        if (isEmpty()) {
            if (id == NO_ID) id = generateViewId()

            addReferenceView()

            val bridgeView = RenderNodeBehindPathView(context)
            bridgeView.visibility = GONE
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

        cloneViewLocationAndSize(fd)

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

                bridgeView.visibility = VISIBLE

                fd.dialogView.viewTreeObserver.removeOnPreDrawListener(this)
            }

            return true
        }
    }

    private fun addReferenceView() {
        // constraints and margins will be added later
        val referenceView = View(context)
        referenceView.id = generateViewId()
        referenceView.layoutParams = LayoutParams(0, 0)
        referenceView.setBackgroundColor(ColorUtils.setAlphaComponent(Color.YELLOW, 100))
        addView(referenceView)
    }

    /**
     * @param view we will use this view to set up a clone that the dialog will use as a reference
     * to define its constraints
     */
    private fun cloneViewLocationAndSize(fa: FocusDialog) {
        // view that acts a as a clone of the original view is always at index 0
        val referenceView = getChildAt(0)
//        referenceView.setBackgroundColor(Color.BLUE)

        val isDrawnB4Start = fa.referenceViewLocation[0] < 0
        val isDrawnB4Top = fa.referenceViewLocation[1] < 0

        val constraintSet = ConstraintSet()
        constraintSet.clone(this)

        val xConstraint = if (isDrawnB4Start) ConstraintSet.END else ConstraintSet.START
        val yConstraint = if (isDrawnB4Top) ConstraintSet.BOTTOM else ConstraintSet.TOP

        constraintSet.connect(referenceView.id, yConstraint, id, ConstraintSet.TOP)
        constraintSet.connect(referenceView.id, xConstraint, id, ConstraintSet.START)

        constraintSet.applyTo(this)

        val params = referenceView.layoutParams as LayoutParams

        if (isDrawnB4Start) {

            params.marginEnd = -fa.referenceViewWidth - fa.referenceViewLocation[0]
        } else{
            params.marginStart = fa.referenceViewLocation[0]
        }

        if (isDrawnB4Top) {
            params.bottomMargin = -fa.referenceViewHeight - fa.referenceViewLocation[1]
        }else {
            params.topMargin = fa.referenceViewLocation[1]
        }

        params.width = fa.referenceViewWidth
        params.height = fa.referenceViewHeight
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

            path.lineTo(secondVertexX, firstVertexY.toFloat())
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