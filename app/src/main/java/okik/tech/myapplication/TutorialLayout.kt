package okik.tech.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

class TutorialLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {
    val paint: Paint = Paint()
    val path: Path = Path()
    val some = TextView(context)
    private var backgroundId = -1
    private var cloneId = -1
    private val DIALOG_PADDING_PX = 30
    private val TRIANGLE_SPACING_PX = 125

    init {
//        layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        id = generateViewId()

        paint.color = Color.WHITE
        paint.alpha = 100
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 8f
        paint.isAntiAlias = true

        setBackgroundColor(Color.TRANSPARENT)
    }

    fun setUpClone(position: IntArray, clone: View) {

        clone.id = generateViewId()

        cloneId = clone.id

        addView(clone)

        val cs = ConstraintSet()
        cs.clone(this)

        cs.connect(clone.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT, position[0])
        cs.connect(clone.id, ConstraintSet.TOP, id, ConstraintSet.TOP, position[1] )

        cs.applyTo(this)
    }

    fun setCloneWithBackground(position: IntArray, clone: View, aView: View) {
        setUpCloneBackground(position, aView)

        setUpClone(position, clone)
    }

    fun setUpCloneBackground(position: IntArray, aView: View) {
        val card = RoundContainer(context)
        card.layoutParams = ViewGroup.LayoutParams(
            aView.width + 60,
            aView.height + 60
        )

        card.id = generateViewId()

        backgroundId = card.id

        addView(card)

        val cs = ConstraintSet()
        cs.clone(this)


        cs.connect(card.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT, position[0] - 30)
        cs.connect(card.id, ConstraintSet.TOP, id, ConstraintSet.TOP, position[1] - 30)

        cs.applyTo(this)
    }

    fun setUpCloneWithDialog(
        originalView: View,
        position: IntArray,
        clone: View,
        dialogContent: View,
        gravity: String,
        dialogXOffsetDp: Float,
        dialogYOffsetDp: Float,
        originOffsetDp: Float,
        destinationOffsetDp: Float,
        shouldCenterOnMainAxis: Boolean
    ) {
        setUpClone(position, clone)

        setUpDialog(
            originalView = originalView,
            dialogContent = dialogContent,
            gravity = gravity,
            dialogXOffsetDp = dialogXOffsetDp,
            dialogYOffsetDp = dialogYOffsetDp,
            originOffsetPercent = originOffsetDp,
            destinationOffsetPercent = destinationOffsetDp,
            position = position,
            shouldClipToClone = true,
            shouldCenterOnMainAxis = shouldCenterOnMainAxis
        )
    }

    fun setUpCloneWithBackgroundAndDialog(
        position: IntArray,
        clone: View,
        aView: View,
        dialogContent: View,
        gravity: String,
        dialogXOffsetDp: Float,
        dialogYOffsetDp: Float,
        originOffsetDp: Float,
        destinationOffsetDp: Float,
        shouldCenterOnMainAxis: Boolean
    ) {
        setCloneWithBackground(position, clone, aView)

        setUpDialog(
            originalView = aView,
            dialogContent = dialogContent,
            gravity = gravity,
            dialogXOffsetDp = dialogXOffsetDp,
            dialogYOffsetDp = dialogYOffsetDp,
            originOffsetPercent = originOffsetDp,
            destinationOffsetPercent = destinationOffsetDp,
            position = position,
            shouldClipToClone = false,
            shouldCenterOnMainAxis = shouldCenterOnMainAxis
        )
    }

    /**
     * @param originalView, this is a reference to the view wiew we want to attach a dialog to. This is needed so we can effectively
     * and dinamycally move the origin of the dialog
     * @param dialogContent this view should always have a fixed size, preferably in both axis(x and y), this way we can
     * effectively and dinamycally connect the view and the dialog by drawing a triangle
     * @param gravity in gravity "top", xOffset is from the clone's first X edge to dialog's first x edge, yOffset is
     * from clone's top edge to dialog's bottom edge. In gravity "Bottom" xOffset is from the clone's first X edge to
     * dialog's first x edge, yOffset is from clone's bottom edge to dialog's top edge. In gravity "right" xOffset is
     * from the clone's last X edge to dialog first x edge, yOffset is from clone's top edge to dialog's top edge. In
     * gravity "left" xOffset is from the clone's first X edge to dialog last x edge, yOffset is from clone's top edge to dialog's top edge
     * @param dialogXOffsetDp
     * @param dialogYOffsetDp
     * @param originOffsetPercent is the offset from the start of the chosen edge(determined by the gravity) towards
     * the size of the view(in same edge), you should pass values from 0 to 1
     * @param position
     * @param shouldClipToClone if True the dialog's origin will be from clone's chosen edge, if false it will automatically
     * clip to the background drawn behind the clone, it throws an exception if no background exits and this is set to true
     * @param destinationOffsetPercent is the offset, in the axis, of the edge of the dialog that corresponds to chosen edge
     * of view we want to add the dialog(i.e: left edge of view will connect to right edge of dialog), this offset is from
     * start towards end of edge
     * @param shouldCenterOnMainAxis Main axis is X if gravity is "top" or "bottom". Main axis is Y if gravity is "left" or "right"
     */
    private fun setUpDialog(
        originalView: View,
        dialogContent: View,
        gravity: String,
        dialogXOffsetDp: Float,
        dialogYOffsetDp: Float,
        originOffsetPercent: Float,
        position: IntArray,
        shouldClipToClone: Boolean,
        destinationOffsetPercent: Float,
        shouldCenterOnMainAxis: Boolean
    ) {
        if(shouldClipToClone && cloneId == -1 || !shouldClipToClone && backgroundId == -1) {
            throw IllegalStateException("Set up background before displaying dialog")
        }

        // according to source code we should get window info by calling the below API instead of
        // `resources.displayMetrics.heightPixels` but actually the former is better as it subtracts
        // systembars top(top is the status bar) and bottom(navigation bar), leaving only the actual window
        // the below code will let us access this code `winMetrics.bounds.height()` but again this includes
        // system bars height so be aware be careful with what API you choose, accessing the windows
        // info through the resources object in this context should be safe
//        val winMetrics = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).currentWindowMetrics
//        winMetrics.bounds.height() == resources.displayMetrics.heightPixels  // this is false

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

        val dialog = RoundContainer(context)

        dialog.id = generateViewId()

        dialog.layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )

        dialog.setPadding(DIALOG_PADDING_PX, DIALOG_PADDING_PX, DIALOG_PADDING_PX, DIALOG_PADDING_PX)

        addView(dialog)

        val nodeToConstraintToId = if(shouldClipToClone) cloneId else backgroundId

        when(gravity) {
            "top", "bottom" -> {
                val xMargin = position[0] + dialogXOffsetPx
                val yMargin = dialogYOffsetPx

                val dialogCs = ConstraintSet()
                dialogCs.clone(this)

                dialog.addView(dialogContent)

                if (gravity == "top") {
                    val startX = position[0] + originalView.width * originOffsetPercent
                    val startY = position[1]

                    path.moveTo(startX, startY.toFloat())

                    if (shouldCenterOnMainAxis) {
                        dialogCs.connect(dialog.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT)
                        dialogCs.connect(dialog.id, ConstraintSet.RIGHT, id, ConstraintSet.RIGHT)
                        dialogCs.connect(dialog.id, ConstraintSet.BOTTOM, nodeToConstraintToId, ConstraintSet.TOP, yMargin.toInt())

                        val horizontalCenter = resources.displayMetrics.widthPixels/2

                        var difference = horizontalCenter - dialogContent.layoutParams.width/2

                        difference = if (difference < 0) 0 else difference

                        val firstVertexX = difference + dialogContent.layoutParams.width * destinationOffsetPercent
                        val firstVertexY = position[1] - yMargin

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX + TRIANGLE_SPACING_PX
                        val secondVertexY = firstVertexY

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(this)

                        invalidate()
                    } else {
                        dialogCs.connect(dialog.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT, xMargin.toInt())
                        dialogCs.connect(dialog.id, ConstraintSet.BOTTOM, nodeToConstraintToId, ConstraintSet.TOP, yMargin.toInt())

                        val firstVertexX = xMargin + dialogContent.layoutParams.width * destinationOffsetPercent
                        val firstVertexY = position[1] - yMargin

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX + TRIANGLE_SPACING_PX
                        val secondVertexY = firstVertexY

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(this)

                        invalidate()
                    }
                } else {

                    val startX = position[0] + originalView.width * originOffsetPercent
                    val startY = position[1] + originalView.height

                    path.moveTo(startX, startY.toFloat())

                    if (shouldCenterOnMainAxis) {
                        dialogCs.connect(dialog.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT)
                        dialogCs.connect(dialog.id, ConstraintSet.RIGHT, id, ConstraintSet.RIGHT)
                        dialogCs.connect(dialog.id, ConstraintSet.TOP, nodeToConstraintToId, ConstraintSet.BOTTOM, yMargin.toInt())

                        val horizontalCenter = resources.displayMetrics.widthPixels/2

                        var difference = horizontalCenter - dialogContent.layoutParams.width/2

                        difference = if (difference < 0) 0 else difference

                        val firstVertexX = difference + dialogContent.layoutParams.width * destinationOffsetPercent
                        val firstVertexY = position[1] + originalView.height + yMargin

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX + TRIANGLE_SPACING_PX
                        val secondVertexY = firstVertexY

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(this)

                        invalidate()
                    } else {
                        dialogCs.connect(dialog.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT, xMargin.toInt())
                        dialogCs.connect(dialog.id, ConstraintSet.TOP, nodeToConstraintToId, ConstraintSet.BOTTOM, yMargin.toInt())

                        val firstVertexX = xMargin + dialogContent.layoutParams.width * destinationOffsetPercent
                        val firstVertexY = position[1] + originalView.height + yMargin

                        path.lineTo(firstVertexX, firstVertexY)

                        val secondVertexX = firstVertexX + TRIANGLE_SPACING_PX
                        val secondVertexY = firstVertexY

                        path.lineTo(secondVertexX, secondVertexY)
                        path.close()

                        dialogCs.applyTo(this)

                        invalidate()
                    }
                }
            }
            "left", "right"-> {
                val xMargin = dialogXOffsetPx
                val yMargin = position[1] + dialogYOffsetPx

                val dialogCs = ConstraintSet()
                dialogCs.clone(this)

                dialog.addView(dialogContent)

                if (gravity == "left") {
                    dialogCs.connect(dialog.id, ConstraintSet.TOP, id, ConstraintSet.TOP, yMargin.toInt())
                    dialogCs.connect(dialog.id, ConstraintSet.RIGHT, nodeToConstraintToId, ConstraintSet.LEFT, xMargin.toInt())

                    dialogCs.applyTo(this)
                } else {
                    dialogCs.connect(dialog.id, ConstraintSet.TOP, id, ConstraintSet.TOP, yMargin.toInt())
                    dialogCs.connect(dialog.id, ConstraintSet.LEFT, nodeToConstraintToId, ConstraintSet.RIGHT, xMargin.toInt())

                    dialogCs.applyTo(this)
                }
            }
            else -> throw IllegalArgumentException("Invalid gravity value")
        }
    }

    private fun closeAndRedrawPath() {
        path.close()
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        canvas.drawPath(path, paint)
    }
}
