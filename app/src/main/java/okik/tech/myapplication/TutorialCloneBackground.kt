package okik.tech.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.material.card.MaterialCardView

class OverLayLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {
    val paint: Paint= Paint()

    init {
//        setRenderEffect(RenderEffect.createBlurEffect(100f, 100f, Shader.TileMode.CLAMP))

        setBackgroundColor(Color.GRAY)
        alpha = 0.0f

//
        paint.alpha = 1
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL

        val card = MaterialCardView(context)
        card.id = generateViewId()


        addView(card)

        val cs = ConstraintSet()
        cs.clone(this)

        val params = card.layoutParams

        params.width = 400
        params.height = 400


        cs.connect(card.id, ConstraintSet.LEFT, id, ConstraintSet.LEFT)
        cs.connect(card.id, ConstraintSet.TOP, id, ConstraintSet.TOP)



//        addView(card)

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

//        canvas.drawRect(50f, 50f, 500f, 500f, paint)
    }

    fun setUpCloneBackground(position: IntArray) {

    }
}