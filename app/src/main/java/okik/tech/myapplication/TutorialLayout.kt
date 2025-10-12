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
    var backgroundId = 0

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

    fun setUpClone(position: IntArray, clone: View, aView: View) {
        removeAllViews()
        setUpCloneBackground(position, aView)

        clone.id = generateViewId()

        addView(clone)

        val cs = ConstraintSet()
        cs.clone(this)

        cs.connect(clone.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT, position[0])
        cs.connect(clone.id, ConstraintSet.TOP, id, ConstraintSet.TOP, position[1] )

        cs.applyTo(this)
    }

    fun setUpCloneBackground(position: IntArray, aView: View) {
        val card = DialogContainer(context)
        card.layoutParams = ViewGroup.LayoutParams(
            aView.width + 60,
            aView.height + 60
        )

        card.id = generateViewId()

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
        aView: View,
        dialogContent: View,
        gravity: String,
        dialogXOffset: Float,
        dialogYOffsetDp: Float,
        originOffsetDp: Float,
        shouldClipToClone: Boolean
    ) {
        setUpClone(position, clone, aView)

        setUpDialog(dialogContent, gravity, dialogXOffset, dialogYOffsetDp, originOffsetDp, position, aView)
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
        aView: View,
        shouldClipToClone: Boolean
    ) {
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

        when(gravity) {
            "top" -> {
                val xMargin = position[0] + dialogXOffsetPx
                val yMargin = dialogYOffsetPx

                val dialogCs = ConstraintSet()
                dialogCs.clone(this)

                dialog.addView(dialogContent)

                val dialogXMargin = aView.width * dialogXOffsetDp
                dialogCs.connect(dialog.id, ConstraintSet.LEFT, origin.id, ConstraintSet.LEFT, dialogXMargin.toInt())
                dialogCs.connect(dialog.id, ConstraintSet.BOTTOM, origin.id, ConstraintSet.TOP)

                dialogCs.applyTo(this)
            }
        }
    }
}