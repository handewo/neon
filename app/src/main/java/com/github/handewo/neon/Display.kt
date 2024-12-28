package com.github.handewo.neon// Replace with your package name

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.textview.MaterialTextView

class DisplayActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_display)
        val displayView = findViewById<View>(R.id.display_scroll)

        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        val rootView = window.decorView.findViewById<ViewGroup>(android.R.id.content)
        rootView.setOnApplyWindowInsetsListener { _, insets ->
            val cutout = insets.displayCutout
            if (cutout != null) {
                // Adjust layout to cover the cutout area
                displayView.setPadding(
                    cutout.safeInsetLeft,
                    cutout.safeInsetTop,
                    cutout.safeInsetRight,
                    cutout.safeInsetBottom
                )
                // ... adjust other margins as needed
            }
            insets
        }

        // Hide system bars using WindowInsetsControllerCompat
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(
                WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
                        or WindowInsetsCompat.Type.captionBar() or WindowInsetsCompat.Type.mandatorySystemGestures() or WindowInsetsCompat.Type.systemGestures() or WindowInsetsCompat.Type.tappableElement()
            )
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }


        val displayText = findViewById<MaterialTextView>(R.id.display_text)
        val neonText = intent.getStringExtra("NEON_TEXT") ?: getString(R.string.default_text)
        // Get font size, default 60
        val fontSize = intent.getIntExtra("FONT_SIZE", 60)
        val fontColor = intent.getIntExtra("FONT_COLOR", 0xFF000000.toInt())
        val bgColor = intent.getIntExtra("BG_COLOR", 0xFFFFFFFF.toInt())
        val speed = intent.getLongExtra("SPEED", 200)

        displayView.setBackgroundColor(bgColor)
        displayText.text = neonText
        // Set font size
        displayText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())

        // Set neon-like color and glow
        displayText.setTextColor(fontColor)
        displayText.setShadowLayer(
            30f,
            0f,
            0f,
            fontColor
        )
        val screenWidth = getScreenWidth(this)
        val paint = displayText.paint
        val textWidth = paint.measureText(neonText)

        // Scrolling animation
        val animation = ObjectAnimator.ofFloat(
            displayText,
            "translationX",

            screenWidth.toFloat(),
            -textWidth
        )
        animation.duration = speed * 10 // Adjust duration as needed
        animation.repeatCount = ObjectAnimator.INFINITE
        animation.repeatMode = ObjectAnimator.RESTART
        animation.interpolator = LinearInterpolator()
        if (screenWidth < textWidth) {
            animation.start()
        }
    }
}

fun getScreenWidth(context: Context): Int {
    val metrics: DisplayMetrics = context.resources.displayMetrics
    return metrics.widthPixels
}