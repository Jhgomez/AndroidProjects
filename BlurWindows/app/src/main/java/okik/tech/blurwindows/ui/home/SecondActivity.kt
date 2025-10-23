package okik.tech.blurwindows.ui.home

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import okik.tech.blurwindows.R
import okik.tech.blurwindows.databinding.ActivityMainBinding
import okik.tech.blurwindows.databinding.ActivityTwoBinding
import java.util.function.Consumer

class SecondActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTwoBinding

    private val mBackgroundBlurRadius = 80
    private  val mBlurBehindRadius = 1

    // We set a different dim amount depending on whether window blur is enabled or disabled
    private  val mDimAmountWithBlur = 0.1f
    private  val mDimAmountNoBlur = 0.4f

    // We set a different alpha depending on whether window blur is enabled or disabled
    private  val mWindowBackgroundAlphaWithBlur = 20
    private  val mWindowBackgroundAlphaNoBlur = 20

    // Use a rectangular shape drawable for the window background. The outline of this drawable
    // dictates the shape and rounded corners for the window background blur area.
    private var mWindowBackgroundDrawable: Drawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTwoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.container.setBackgroundColor(Color.TRANSPARENT)
        binding.mc.setBackgroundColor(Color.BLUE)

        mWindowBackgroundDrawable = getDrawable(R.drawable.window_background);
        getWindow().setBackgroundDrawable(mWindowBackgroundDrawable);

        if (buildIsAtLeastS()) {
            // Enable blur behind. This can also be done in xml with R.attr#windowBlurBehindEnabled
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

            // Register a listener to adjust window UI whenever window blurs are enabled/disabled
            setupWindowBlurListener();
        } else {
            // Window blurs are not available prior to Android S
            updateWindowForBlurs(false /* blursEnabled */);
        }

        // Enable dim. This can also be done in xml, see R.attr#backgroundDimEnabled
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
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
        getWindow().getDecorView().addOnAttachStateChangeListener(
            object : OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    getWindowManager().addCrossWindowBlurEnabledListener(
                        windowBlurEnabledListener
                    )
                }

                override fun onViewDetachedFromWindow(v: View) {
                    getWindowManager().removeCrossWindowBlurEnabledListener(
                        windowBlurEnabledListener
                    )
                }
            })
    }

    private fun updateWindowForBlurs(blursEnabled: Boolean) {
        mWindowBackgroundDrawable!!.setAlpha(if (blursEnabled && mBackgroundBlurRadius > 0) mWindowBackgroundAlphaWithBlur else mWindowBackgroundAlphaNoBlur)
        getWindow().setDimAmount(if (blursEnabled && mBlurBehindRadius > 0) mDimAmountWithBlur else mDimAmountNoBlur)

        if (buildIsAtLeastS()) {
            // Set the window background blur and blur behind radii
            getWindow().setBackgroundBlurRadius(mBackgroundBlurRadius)
            getWindow().getAttributes().setBlurBehindRadius(mBlurBehindRadius)
            getWindow().setAttributes(getWindow().getAttributes())
        }
    }

    private fun buildIsAtLeastS(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }
}