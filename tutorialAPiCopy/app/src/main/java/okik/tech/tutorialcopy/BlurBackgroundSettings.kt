package okik.tech.tutorialcopy

import android.graphics.Paint
import android.graphics.RecordingCanvas
import android.graphics.RenderEffect
import android.graphics.drawable.Drawable
import android.view.View

data class BlurBackgroundSettings(
    val renderEffect: RenderEffect?,
    val shouldClipToBackground: Boolean,
    val backgroundDrawable: Drawable,
    val backgroundOverlayPaint: Paint,
    val padding: InnerPadding,
    val renderCanvasPositionCommand: (RecordingCanvas, View) -> Unit
)
