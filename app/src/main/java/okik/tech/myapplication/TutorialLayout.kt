package okik.tech.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.material.card.MaterialCardView

class TutorialLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {
    val paint: Paint= Paint()
    val some = TextView(context)

    init {
        layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        id = generateViewId()

//        setBackgroundColor(Color.GREEN)

        paint.alpha = 1
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        addView(some)
    }

    fun setUpClone(position: IntArray, clone: View, aView: View?) {
        setUpCloneBackground(position, clone, aView)

        clone.id = generateViewId()

        addView(clone)

        val cs = ConstraintSet()
        cs.clone(this)

        cs.connect(clone.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT, position[0])
        cs.connect(clone.id, ConstraintSet.TOP, id, ConstraintSet.TOP, position[1] )

        cs.applyTo(this)
    }

    fun setUpCloneBackground(position: IntArray, clone: View, aView: View?) {
        val card = TutorialNodeBackground(context)
        card.layoutParams = ViewGroup.LayoutParams(
            (aView?.width ?: 0) + 60,
            (aView?.height ?: 0) + 60
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

    fun setUpDialog() {

    }
}