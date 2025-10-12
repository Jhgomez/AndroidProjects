package okik.tech.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

class TutorialLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {
    val paint: Paint= Paint()
    val some = TextView(context)
    private var backgroundId = -1
    private var cloneId = -1

    init {
//        layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        id = generateViewId()

        paint.alpha = 0
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        addView(some)
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
        val card = DialogContainer(context)
        card.layoutParams = ViewGroup.LayoutParams(
            aView.width + 60,
            aView.height + 60
        )

        card.id = generateViewId()

        backgroundId = card.id

        addView(card)

        val cs = ConstraintSet()
        cs.clone(this)
//        card.setRenderEffect(RenderEffect.createBlurEffect(100f, 100f, Shader.TileMode.DECAL))

        cs.connect(card.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT, position[0] - 30)
        cs.connect(card.id, ConstraintSet.TOP, id, ConstraintSet.TOP, position[1] - 30)

        cs.applyTo(this)
    }

    fun setUpCloneWithDialog(
        position: IntArray,
        clone: View,
        dialogContent: View,
        gravity: String,
        dialogXOffsetDp: Float,
        dialogYOffsetDp: Float,
        originOffsetDp: Float
    ) {
        setUpClone(position, clone)

        setUpDialog(dialogContent, gravity, dialogXOffsetDp, dialogYOffsetDp, originOffsetDp, position, true)
    }

    fun setUpCloneWithBackgroundAndDialog(
        position: IntArray,
        clone: View,
        aView: View,
        dialogContent: View,
        gravity: String,
        dialogXOffset: Float,
        dialogYOffsetDp: Float,
        originOffsetDp: Float
    ) {
        setCloneWithBackground(position, clone, aView)

        setUpDialog(dialogContent, gravity, dialogXOffset, dialogYOffsetDp, originOffsetDp, position, false)
    }

    // in gravity "top" xOffset is from the clone's first X edge to dialog's first x edge, yOffset is from clone's top edge to dialog's bottom edge
    // in gravity "Bottom" xOffset is from the clone's first X edge to dialog's first x edge, yOffset is from clone's bottom edge to dialog's top edge
    // in gravity "right" xOffset is from the clone's last X edge to dialog first x edge, yOffset is from clone's top edge to dialog's top edge
    // in gravity "left" xOffset is from the clone's first X edge to dialog last x edge, yOffset is from clone's top edge to dialog's top edge
    // Origin offset is the offset from the middle of the chosen edge(determined by the gravity), a positive value will move it forward and negative backwards
    // if shouldClipToClone is True the dialog's origin will be from clone's chosen edge, if false it will automatically clip to the
    // background drawn behind the clone, it throws an exception if no background exits and this is set to true
    private fun setUpDialog(
        dialogContent: View,
        gravity: String,
        dialogXOffsetDp: Float,
        dialogYOffsetDp: Float,
        originOffsetDp: Float,
        position: IntArray,
        shouldClipToClone: Boolean
    ) {
        if(shouldClipToClone && cloneId == -1 || !shouldClipToClone && backgroundId == -1) {
            throw IllegalStateException("Set up background before displaying dialog")
        }

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

        val dialog = DialogContainer(context)

        dialog.id = generateViewId()

        dialog.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )

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
                    dialogCs.connect(dialog.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT, xMargin.toInt())
                    dialogCs.connect(dialog.id, ConstraintSet.BOTTOM, nodeToConstraintToId, ConstraintSet.TOP, yMargin.toInt())

                    dialogCs.applyTo(this)
                } else {
                    dialogCs.connect(dialog.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT, xMargin.toInt())
                    dialogCs.connect(dialog.id, ConstraintSet.TOP, nodeToConstraintToId, ConstraintSet.BOTTOM, yMargin.toInt())

                    dialogCs.applyTo(this)
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
}