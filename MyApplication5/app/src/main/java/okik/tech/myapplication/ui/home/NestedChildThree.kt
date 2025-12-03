package okik.tech.myapplication.ui.home

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

// this component is just a frame layout wrapped in a rounded corner shape, the difference
// with a material card is that this one doesn't have "card elevation" and is intended to be passed
// any view programmatically. It wraps its content automatically. Be aware you have to add padding to it
// everytime you need
class NestedChildThree @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}