package com.mamenko.siphappens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.mamenko.siphappens.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val initialContentBottomPadding = binding.content.paddingBottom
        val initialToolbarHeight = binding.toolbar.layoutParams.height
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { view, windowInsets ->
            val statusBarInset = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.updatePadding(top = statusBarInset)
            view.updateLayoutParams {
                height = initialToolbarHeight + statusBarInset
            }
            windowInsets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.content) { view, windowInsets ->
            view.updatePadding(
                bottom = initialContentBottomPadding +
                    windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            )
            windowInsets
        }

        setupUI()
        loadSavedData()
    }

    override fun onResume() {
        super.onResume()
        updateCounter()
        updateCurrentImage()
        updateWidgetWarning()
    }

    private fun setupUI() {
        binding.image1.setOnClickListener { selectImage(1, binding.image1) }
        binding.image2.setOnClickListener { selectImage(2, binding.image2) }
        binding.image3.setOnClickListener { selectImage(3, binding.image3) }
        binding.counterText.setOnClickListener {
            val currentCounter = PreferenceManager.getCounter(this) + 1
            PreferenceManager.setCounter(this, currentCounter)
            val maximum = PreferenceManager.getMaximum(this)

            if (currentCounter >= maximum) {
                SoundHelper.playAfterMaxSound(this)
            } else {
                SoundHelper.playBeforeMaxSound(this)
            }

            updateCounter()
            updateCurrentImage()
            SipHappensWidget.updateWidget(this)
        }

        binding.maxInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (text.isNotEmpty()) {
                    try {
                        val value = text.toInt()
                        if (value in 1..99) {
                            PreferenceManager.setMaximum(this@MainActivity, value)
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                R.string.invalid_range_message,
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }
                    } catch (_: NumberFormatException) {
                        val maximum = PreferenceManager.getMaximum(this@MainActivity)
                        binding.maxInput.setText(maximum.toString())
                    }
                    updateCurrentImage()
                    SipHappensWidget.updateWidget(this@MainActivity)
                }
            }
        })

        binding.resetButton.setOnClickListener {
            PreferenceManager.setCounter(this, 0)
            updateCounter()
            updateCurrentImage()
            SipHappensWidget.updateWidget(this)
        }

        binding.playSoundsCheckbox.setOnCheckedChangeListener { _, isChecked ->
            PreferenceManager.setPlaySounds(this, isChecked)
        }
    }

    private fun loadSavedData() {
        val selectedImageCode = PreferenceManager.getImageCode(this)
        val maximum = PreferenceManager.getMaximum(this)
        val playSounds = PreferenceManager.getPlaySounds(this)

        selectImage(selectedImageCode, getImageViewByCode(selectedImageCode))
        binding.maxInput.setText("$maximum")
        binding.playSoundsCheckbox.isChecked = playSounds
        updateCounter()
        updateCurrentImage()
    }

    private fun selectImage(imageCode: Int, imageView: ImageView) {
        binding.image1.isSelected = false
        binding.image2.isSelected = false
        binding.image3.isSelected = false

        val background = R.drawable.image_background
        binding.image1.setBackgroundResource(background)
        binding.image2.setBackgroundResource(background)
        binding.image3.setBackgroundResource(background)

        imageView.isSelected = true
        imageView.setBackgroundResource(R.drawable.selected_image_background)

        PreferenceManager.setImageCode(this, imageCode)
        updateCurrentImage()
        SipHappensWidget.updateWidget(this)
    }

    private fun getImageViewByCode(code: Int): ImageView {
        return when (code) {
            1 -> binding.image1
            2 -> binding.image2
            3 -> binding.image3
            else -> binding.image1
        }
    }

    private fun updateCurrentImage() {
        val counter = PreferenceManager.getCounter(this)
        val maximum = PreferenceManager.getMaximum(this)
        val imageCode = PreferenceManager.getImageCode(this)

        val resourceId = ImageHelper.getImageResourceId(imageCode, counter, maximum)
        binding.currentImage.setImageResource(resourceId)
    }

    private fun updateCounter() {
        val counter = PreferenceManager.getCounter(this)
        binding.counterText.text = "$counter"
    }

    private fun updateWidgetWarning() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val widgetComponent = ComponentName(this, SipHappensWidget::class.java)
        binding.widgetWarning.visibility = if (
            appWidgetManager.getAppWidgetIds(widgetComponent).isEmpty()
        ) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}
