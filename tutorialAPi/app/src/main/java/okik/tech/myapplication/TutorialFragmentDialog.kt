package okik.tech.myapplication

import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout.LayoutParams
import android.widget.PopupWindow
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import okik.tech.myapplication.databinding.DialogContentBinding

/**
 * We wrap our Tutorial Dialog around this class to be able to
 * maintain dialog across configuration changes
 */
@RequiresApi(Build.VERSION_CODES.S)
class TutorialFragmentDialog: DialogFragment() {


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val tutorialLayout = TutorialDialog(requireContext())

        val content = DialogContentBinding.inflate(inflater)

        val widthInPixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            300f,
            resources.displayMetrics
        )

        val hInPixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            300f,
            resources.displayMetrics
        )

        // always specify the width and height of the content of the dialog like this
        content.root.layoutParams = LayoutParams(
            widthInPixels.toInt(),
            LayoutParams.MATCH_PARENT
        )

        (content.root.layoutParams as MarginLayoutParams).setMargins(0, 0, 0, 0)

        if (focusArea != null) {
//        tutorialLayout.addView(content.root)
            tutorialLayout.renderTutorialDialog(
                focusArea!!,
                30,
                focusArea!!.roundedCornerSurrounding?.paint,
                focusArea!!.roundedCornerSurrounding?.cornerRadius?.toFloat(),
                focusArea!!.surroundingThicknessEffect,
                Gravity.BOTTOM,
                -30f,
                30f,
                0.5f,
                0.5f,
                true,
                content.root
            )
        }

//        if (context is Activity) {

//        }
//
//        requireDialog().window?.setLayout(
//            LayoutParams.MATCH_PARENT,
//            LayoutParams.MATCH_PARENT
//        )

        return tutorialLayout
    }

    fun setFocusAreas(focusArea: FocusArea) {
        TutorialFragmentDialog.focusArea = focusArea
    }

    // TODO make FocusArea parcelable/serializable and store save it in onSaveInstanceState and retrieve it in onCreateView
    // always make sure to reset value to null when navigating back or forward the activity/fragment that is using
    // this dialog
    companion object {
        private var focusArea: FocusArea? = null
    }
}