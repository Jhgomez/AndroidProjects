package okik.tech.myapplication.ui.home

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.setPadding

// this component is just a frame layout wrapped in a rounded corner shape, the difference
// with a material card is that this one doesn't have "card elevation" and is intended to be passed
// any view programmatically. It wraps its content automatically. Be aware you have to add padding to it
// everytime you need
class RoundContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    fun invalidateOne() {
        getChildAt(0).invalidate()
    }

    val myOver = TextView(context)

    init {
//        myOver.layoutParams = LayoutParams(500, 500)
        myOver.text = "mamonmomoanioin"
        myOver.layout(0, 0,  200,  200);
        myOver.setBackgroundColor(Color.CYAN)
    }
    fun invalidateTwo() {
//        if(getChildAt(2).isVisible) {
//            getChildAt(2).visibility = INVISIBLE
//        } else {
//            getChildAt(2).visibility = VISIBLE
//        }


        overlay.remove(myOver)
    }

    fun invalidateThree() {
//        addView(Childthree(context))
//        invalidate()

        val badge = TextView(context);
        badge.setText("New!");
        badge.setBackgroundColor(Color.GREEN);
        badge.setTextColor(Color.WHITE);
        badge.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        val w = badge.getMeasuredWidth()
                val h = badge.getMeasuredHeight();

        badge.layout(0, 0,  w,  h);
        overlay.add(myOver);

    }

    override fun drawChild(canvas: Canvas, child: View?, drawingTime: Long): Boolean {
        if (getChildAt(0).id == child?.id) {
            val isFromInvalidate = super.drawChild(canvas, child, drawingTime)
            return isFromInvalidate
        }

        if (getChildAt(1).id == child?.id) {
            val isFromInvalidate = super.drawChild(canvas, child, drawingTime)
            return isFromInvalidate
        }

        if (getChildAt(2).id == child?.id) {
            val isFromInvalidate = super.drawChild(canvas, child, drawingTime)
            return isFromInvalidate
        }

        if (getChildAt(3).id == child?.id) {
            val isFromInvalidate = super.drawChild(canvas, child, drawingTime)
            return isFromInvalidate
        }

        return super.drawChild(canvas, child, drawingTime)
    }
}