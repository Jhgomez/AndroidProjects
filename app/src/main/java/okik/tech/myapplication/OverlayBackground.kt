package okik.tech.myapplication

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout

class OverlayBackground@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,  basePointX: Int, width: Int
) : LinearLayout(context, attrs) {

    init {
        setBackgroundColor(Color.TRANSPARENT)


    }
}