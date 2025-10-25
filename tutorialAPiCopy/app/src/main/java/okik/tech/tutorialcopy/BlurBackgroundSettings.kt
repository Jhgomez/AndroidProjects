package okik.tech.tutorialcopy

import android.graphics.RenderEffect
import android.graphics.drawable.Drawable

data class BlurBackgroundSettings(
    val renderEffect: RenderEffect?,
    val shouldClipToBackground: Boolean,
    val backgroundDrawable: Drawable,
    val padding: InnerPadding
)
