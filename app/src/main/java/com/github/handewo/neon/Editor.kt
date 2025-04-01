package com.github.handewo.neon

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.text.NumberFormat

class EditorActivity : AppCompatActivity() {

    private lateinit var editorStatusDao: EditorStatusDao
    private var fontSize = 60 // Initial font size
    private var editText: TextInputEditText? = null
    private var fontColor = 0xFF000000.toInt()
    private var bgColor = 0xFFFFFFFF.toInt()
    private var speed: Long = 200
    private var cutout: Boolean = false
    private var shadow = 30f
    private var orientation = 0


    private fun updateEditorFontColor() {
        editText?.setTextColor(fontColor)
        editText?.setShadowLayer(
            shadow,
            0f,
            0f,
            fontColor
        )
    }

    private fun updateEditorBgColor() {
        editText?.setBackgroundColor(bgColor)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_editor)


        editText = findViewById<TextInputEditText>(R.id.editor_text)
        val fontSizeSeekBar = findViewById<Slider>(R.id.font_size_seekbar)
        val speedSeekBar = findViewById<Slider>(R.id.speed_seekbar)
        val displayButton = findViewById<Button>(R.id.display_button)
        val fontColorButton = findViewById<Button>(R.id.font_color_button)
        val bgColorButton = findViewById<Button>(R.id.background_color_button)
        val cutoutSwitch = findViewById<SwitchMaterial>(R.id.cutout_switch)
        val shadowSeekBar = findViewById<Slider>(R.id.shadow_seekbar)
        val orientationMenu = findViewById<MaterialAutoCompleteTextView>(R.id.orientation_view)
        // Restore editor status
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val orientationArray = resources.obtainTypedArray(R.array.orientation_options)
            editorStatusDao = db.editorStatusDao()
            val lastStatus = editorStatusDao.getLastStatus()
            if (lastStatus != null) {
                orientationMenu.setText(orientationArray.getString(lastStatus.orientation),false)
                Log.d("EditorActivity", "last editor status: $lastStatus")
                editText?.setText(lastStatus.text)
                editText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, lastStatus.fontSize.toFloat())
                fontSizeSeekBar.value = lastStatus.fontSize.toFloat()
                speedSeekBar.value = lastStatus.speed.toFloat()
                shadowSeekBar.value = lastStatus.shadow
                cutoutSwitch.isChecked = lastStatus.cutout
                fontSize=lastStatus.fontSize
                speed = lastStatus.speed
                fontColor = lastStatus.fontColor
                bgColor = lastStatus.bgColor
                cutout = lastStatus.cutout
                shadow = lastStatus.shadow
                orientation = lastStatus.orientation
                updateEditorFontColor()
                updateEditorBgColor()
            } else {
                orientationMenu.setText(orientationArray.getString(0),false)
                Log.d("EditorActivity", "insert editor status")
                val editorStatus = EditorStatus(
                    id = 1,
                    text = editText?.text.toString(),
                    fontSize = fontSize,
                    fontColor = fontColor,
                    speed = speed,
                    bgColor = bgColor,
                    cutout = cutout,
                    shadow = shadow,
                    orientation = orientation
                )
                editorStatusDao.insert(editorStatus)
            }
            orientationArray.recycle()
        }
        val colors = arrayListOf(
            "#ffffff", "#f6e58d", "#ffbe76", "#ff7979", "#badc58", "#dff9fb",
            "#4CAF50", "#2196F3", "#F44336", "#FF9800", "#FFBB86", "#9E9E9E",
            "#7ed6df", "#e056fd", "#686de0", "#30336b", "#95afc0", "#000000"
        )
        fontColorButton.setOnClickListener {
            MaterialColorPickerDialog.Builder(this)                        // Pass Activity Instance
                .setTitle(getString(R.string.font_color))            // Default "Choose Color"
                .setColorShape(ColorShape.SQAURE)   // Default ColorShape.CIRCLE
                .setColors(colors)
                .setDefaultColor(0xFF000000.toInt())     // Pass Default Color
                .setColorListener { color, _ ->
                    // Handle Color Selection
                    fontColor = color
                    updateEditorFontColor()
                }
                .show()
        }

        bgColorButton.setOnClickListener {
            MaterialColorPickerDialog.Builder(this)
                .setTitle(getString(R.string.background_color))
                .setColorShape(ColorShape.SQAURE)
                .setColors(colors)
                .setDefaultColor(0xFFFFFFFF.toInt())
                .setColorListener { color, _ ->
                    // Handle Color Selection
                    bgColor = color
                    updateEditorBgColor()
                }
                .show()
        }

        displayButton.setOnClickListener {
            val textToDisplay = editText?.text.toString()
            val intent = Intent(this, DisplayActivity::class.java)
            intent.putExtra("NEON_TEXT", textToDisplay)
            intent.putExtra("FONT_SIZE", fontSize)
            intent.putExtra("FONT_COLOR", fontColor)
            intent.putExtra("BG_COLOR", bgColor)
            intent.putExtra("SPEED", speed)
            intent.putExtra("CUTOUT", cutout)
            intent.putExtra("SHADOW", shadow)
            intent.putExtra("ORIENTATION", orientation)
            startActivity(intent)
        }

        cutoutSwitch.setOnCheckedChangeListener { _, isChecked ->
            cutout = isChecked
        }

        fontSizeSeekBar.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                fontSize = value.toInt()
                editText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
            }
        }
        fontSizeSeekBar.setLabelFormatter { value: Float ->
            val format = NumberFormat.getNumberInstance()
            format.roundingMode = RoundingMode.DOWN
            format.maximumFractionDigits = 0
            format.format(value.toDouble())
        }
        speedSeekBar.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                speed = value.toLong()
            }
        }
        speedSeekBar.setLabelFormatter { value: Float ->
            val format = NumberFormat.getNumberInstance()
            format.roundingMode = RoundingMode.DOWN
            format.maximumFractionDigits = 0
            format.format(value.toDouble())
        }
        shadowSeekBar.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                shadow = value
                editText?.setShadowLayer(
                    shadow,
                    0f,
                    0f,
                    fontColor
                )
            }
        }
        orientationMenu.setOnItemClickListener { _, _, position, _ ->
            Log.d("EditorActivity", "Selected orientation: $position")
            orientation = position
        }


    }


        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            outState.putString("editor_text", editText?.text.toString())
            outState.putInt("font_size", fontSize)
            lifecycleScope.launch {
                val editorStatus = EditorStatus(
                    id = 1,
                    text = editText?.text.toString(),
                    fontSize = fontSize,
                    fontColor = fontColor,
                    speed = speed,
                    bgColor = bgColor,
                    cutout = cutout,
                    shadow = shadow,
                    orientation = orientation
                )
                Log.d("EditorActivity", "Saving editor status: $editorStatus")
                editorStatusDao.update(editorStatus)
            }
        }

        override fun onRestoreInstanceState(savedInstanceState: Bundle) {
            super.onRestoreInstanceState(savedInstanceState)
            val editorText =
                savedInstanceState.getString("editor_text", getString(R.string.default_text))
            val fontSize = savedInstanceState.getInt("font_size", 60) // Default font size
            editText?.setText(editorText)
            editText?.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
        }


    }