package okik.tech.tutorialcopy

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.TypedValue
import android.view.View

fun dpToPx(dp: Short, context: Context): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        context.resources.displayMetrics
    )
}

/**
 * We prefer getting drawables like this because drawables defined in XML files can be a little
 * hard to configure
 */
fun dispatchDefaultDrawable(context: Context): ShapeDrawable {
    val n = dpToPx(16f, context)

    val roundShape = RoundRectShape(
        floatArrayOf(n, n, n, n, n, n, n ,n),
        null,
        null
    )

    return ShapeDrawable(roundShape)
}