package com.github.handewo.neon// Replace with your package name

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.util.Log
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
    private var cutoutLeft = 0
    private var cutoutTop = 0
    private var cutoutRight = 0
    private var cutoutBottom = 0


    @SuppressLint("SourceLockedOrientationActivity")
    private fun setOrientation(o: Int) {
        when (o) {
            1 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            2 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            3 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            4 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)
        val displayView = findViewById<View>(R.id.display_scroll)
        val cutoutMode = intent.getBooleanExtra("CUTOUT", false)
        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        val rootView = window.decorView.findViewById<ViewGroup>(android.R.id.content)
        rootView.setOnApplyWindowInsetsListener { _, insets ->
            val cutout = insets.displayCutout
            if (cutout != null && !cutoutMode) {
                cutoutLeft = cutout.safeInsetLeft
                cutoutTop = cutout.safeInsetTop
                cutoutRight = cutout.safeInsetRight
                cutoutBottom = cutout.safeInsetBottom
                // Adjust layout to cover the cutout area
                displayView.setPadding(
                    cutoutLeft,
                    cutoutTop,
                    cutoutRight,
                    cutoutBottom
                )
            }
            insets
        }

        Log.d(
            "DisplayActivity",
            "cutout, left: $cutoutLeft, top: $cutoutTop, right: $cutoutRight, bottom: $cutoutBottom"
        )
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
        val displayConstraint =
            findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.display_constraint)

        val neonText = intent.getStringExtra("NEON_TEXT") ?: getString(R.string.default_text)
        // Get font size, default 60
        val fontSize = intent.getIntExtra("FONT_SIZE", 60)
        val fontColor = intent.getIntExtra("FONT_COLOR", 0xFF000000.toInt())
        val bgColor = intent.getIntExtra("BG_COLOR", 0xFFFFFFFF.toInt())
        val speed = intent.getLongExtra("SPEED", 200)
        val shadow = intent.getFloatExtra("SHADOW", 30f)
        val orientation = intent.getIntExtra("ORIENTATION", 0)
        val verticalMode = intent.getBooleanExtra("VERTICAL_MODE", false)
        // val maxLine = intent.getIntExtra("MAX_LINE", 1)

        setOrientation(orientation)
        displayView.setBackgroundColor(bgColor)
        displayText.text = neonText
        // Set font size
        displayText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())

        val screenWidth = getScreenWidth(this, cutoutLeft, cutoutRight)
        val screenHeight = getScreenHeight(this, cutoutTop, cutoutBottom)
        //displayConstraint.minHeight = screenHeight

        if (verticalMode) {
            displayText.ellipsize = TextUtils.TruncateAt.START
            val height = getTextHeight(neonText, screenWidth, cutoutLeft, cutoutRight, displayText)
            displayText.height = height.toInt()
            displayText.layoutParams.height = height.toInt()
            displayText.layoutParams.width = screenWidth
            displayConstraint.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            displayText.setHorizontallyScrolling(false)
            Log.d(
                "DisplayActivity",
                "vertical mode, text height: $height, screen height: $screenHeight, screen width: $screenWidth"
            )
        } else {
            Log.d("DisplayActivity", "horizontal mode")
            displayText.maxLines = 1
        }

        val paint = displayText.paint
        val textWidth = paint.measureText(neonText)

        // Scrolling animation
        val animation = if (verticalMode) {
            val height = getTextHeight(neonText, screenWidth, cutoutLeft, cutoutRight, displayText)
            ObjectAnimator.ofFloat(
                displayText,
                "translationY",
                screenHeight.toFloat() + (height - screenHeight) / 2,
                -height
            )
        } else {
            ObjectAnimator.ofFloat(
                displayText,
                "translationX",
                screenWidth.toFloat(),
                -textWidth
            )
        }

        // Set neon-like color and glow
        displayText.setTextColor(fontColor)
        displayText.setShadowLayer(
            shadow,
            0f,
            0f,
            fontColor
        )

        Log.d("Editor", "Speed: " + (20450 - speed * 10).toString())
        animation.duration = 20450 - speed * 10 // Adjust duration as needed
        animation.repeatCount = ObjectAnimator.INFINITE
        animation.repeatMode = ObjectAnimator.RESTART
        animation.interpolator = LinearInterpolator()
        if (!verticalMode && screenWidth < textWidth) {
            animation.start()
        }
        if (verticalMode && screenHeight < getTextHeight(
                neonText,
                screenWidth,
                cutoutLeft,
                cutoutRight,
                displayText
            )
        ) {
            animation.start()
        }
    }
}

fun getScreenWidth(context: Context, cutoutLeft: Int, cutoutRight: Int): Int {
    val metrics: DisplayMetrics = context.resources.displayMetrics
    return metrics.widthPixels - cutoutLeft - cutoutRight
}

fun getScreenHeight(context: Context, cutoutTop: Int, cutoutBottom: Int): Int {
    val metrics: DisplayMetrics = context.resources.displayMetrics
    return metrics.heightPixels - cutoutTop - cutoutBottom
}

fun getTextHeight(
    text: String,
    screenWidth: Int,
    cutoutLeft: Int,
    cutoutRight: Int,
    displayText: MaterialTextView
): Float {
    val paint = displayText.paint
    val textPaint = TextPaint()
    textPaint.textSize = displayText.textSize
    textPaint.typeface = displayText.typeface
    val staticLayout = StaticLayout.Builder.obtain(
        text,
        0,
        text.length,
        paint,
        screenWidth - cutoutLeft - cutoutRight
    )
        .build()
    val numLines = staticLayout.lineCount
    val fm = paint.fontMetrics
    return numLines * (fm.bottom - fm.top) + (numLines - 1) * fm.leading
}