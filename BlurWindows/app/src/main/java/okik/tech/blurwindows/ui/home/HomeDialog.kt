package okik.tech.blurwindows.ui.home

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import okik.tech.blurwindows.R
import okik.tech.blurwindows.databinding.ActivityTwoBinding
import java.util.function.Consumer


class HomeDialog : DialogFragment() {

    private var _binding: ActivityTwoBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val mBackgroundBlurRadius = 80
    private val mBlurBehindRadius = 20

    // We set a different dim amount depending on whether window blur is enabled or disabled
    private val mDimAmountWithBlur = .1f
    private val mDimAmountNoBlur = 0.4f

    // We set a different alpha depending on whether window blur is enabled or disabled
    private val mWindowBackgroundAlphaWithBlur = 200
    private val mWindowBackgroundAlphaNoBlur = 255

    // Use a rectangular shape drawable for the window background. The outline of this drawable
    // dictates the shape and rounded corners for the window background blur area.
    private var mWindowBackgroundDrawable: Drawable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = ActivityTwoBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        binding.root.setBackgroundColor(Color.RED)
//        binding.root.alpha = 0.5f
//
////        binding.container.setBackgroundColor(Color.TRANSPARENT)
//        binding.mc.setBackgroundColor(Color.GREEN)
//        binding.mc.alpha = 0.5f
//        binding.mc.alpha =

    binding.root.setBackgroundColor(Color.TRANSPARENT)
        binding.mc.setBackgroundColor(Color.TRANSPARENT)

        mWindowBackgroundDrawable = requireContext().getDrawable(R.drawable.window_background);
        requireDialog().window?.setBackgroundDrawable(mWindowBackgroundDrawable);

        if (buildIsAtLeastS()) {
            // Enable blur behind. This can also be done in xml with R.attr#windowBlurBehindEnabled
            requireDialog().window?.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

            // Register a listener to adjust window UI whenever window blurs are enabled/disabled
            setupWindowBlurListener();
        } else {
            // Window blurs are not available prior to Android S
            updateWindowForBlurs(false /* blursEnabled */);
        }

        // Enable dim. This can also be done in xml, see R.attr#backgroundDimEnabled
        requireDialog().window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

//        requireDialog().window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        val ft: FragmentTransaction = childFragmentManager.beginTransaction()
//            ft.show(HomeDialog())

//        HomeDialogTwo().show(ft, "more")

        return binding.root
    }

    /**
     * Set up a window blur listener.
     *
     * Window blurs might be disabled at runtime in response to user preferences or system states
     * (e.g. battery saving mode). WindowManager#addCrossWindowBlurEnabledListener allows to
     * listen for when that happens. In that callback we adjust the UI to account for the
     * added/missing window blurs.
     *
     * For the window background blur we adjust the window background drawable alpha:
     * - lower when window blurs are enabled to make the blur visible through the window
     * background drawable
     * - higher when window blurs are disabled to ensure that the window contents are readable
     *
     * For window blur behind we adjust the dim amount:
     * - higher when window blurs are disabled - the dim creates a depth of field effect,
     * bringing the user's attention to the dialog window
     * - lower when window blurs are enabled - no need for a high alpha, the blur behind is
     * enough to create a depth of field effect
     */
    @RequiresApi(api = Build.VERSION_CODES.S)
    private fun setupWindowBlurListener() {
        val windowBlurEnabledListener : Consumer<Boolean> = object : Consumer<Boolean> {
            override fun accept(value: Boolean) {
                updateWindowForBlurs(value)
            }

        }
        requireDialog().window?.getDecorView()?.addOnAttachStateChangeListener(
            object : OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    requireDialog().window?.windowManager?.addCrossWindowBlurEnabledListener(
                        windowBlurEnabledListener
                    )
                }

                override fun onViewDetachedFromWindow(v: View) {
                    requireDialog().window?.windowManager?.removeCrossWindowBlurEnabledListener(
                        windowBlurEnabledListener
                    )
                }
            })
    }

    private fun updateWindowForBlurs(blursEnabled: Boolean) {
        mWindowBackgroundDrawable!!.setAlpha(if (blursEnabled && mBackgroundBlurRadius > 0) mWindowBackgroundAlphaWithBlur else mWindowBackgroundAlphaNoBlur)
        requireDialog().window?.setDimAmount(if (blursEnabled && mBlurBehindRadius > 0) mDimAmountWithBlur else mDimAmountNoBlur)

        if (buildIsAtLeastS()) {
            // Set the window background blur and blur behind radii
            requireDialog().window?.setBackgroundBlurRadius(mBackgroundBlurRadius)
            requireDialog().window?.getAttributes()?.setBlurBehindRadius(mBlurBehindRadius)
            requireDialog().window?.setAttributes(requireDialog().window?.getAttributes())
        }
    }

    private fun buildIsAtLeastS(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}