package com.mamenko.siphappens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.mamenko.siphappens.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val soundOptions: SoundOptionsViewModel by viewModels()
    private val beforeSoundPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { soundOptions.importSound(this, SoundType.BEFORE_MAX, it) }
    }
    private val afterSoundPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { soundOptions.importSound(this, SoundType.AFTER_MAX, it) }
    }

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
        setOptionsExpanded(savedInstanceState?.getBoolean("options_expanded") ?: false)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                soundOptions.state.collect { state ->
                    updateSoundOptions()
                    state.error?.let { error ->
                        reportSoundSelectionError(error)
                        soundOptions.clearError()
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("options_expanded", binding.optionsContent.isVisible)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        updateCounter()
        updateCurrentImage()
        updateWidgetWarning()
        updateSoundOptions()
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
        binding.optionsHeader.setOnClickListener {
            setOptionsExpanded(!binding.optionsContent.isVisible)
        }
        binding.chooseBeforeSound.setOnClickListener { beforeSoundPicker.launch("audio/*") }
        binding.chooseAfterSound.setOnClickListener { afterSoundPicker.launch("audio/*") }
        binding.resetBeforeSound.setOnClickListener {
            soundOptions.resetSound(this, SoundType.BEFORE_MAX)
        }
        binding.resetAfterSound.setOnClickListener {
            soundOptions.resetSound(this, SoundType.AFTER_MAX)
        }
    }

    private fun setOptionsExpanded(expanded: Boolean) {
        binding.optionsContent.isVisible = expanded
        binding.optionsHeader.setIconResource(
            if (expanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more
        )
        ViewCompat.setStateDescription(
            binding.optionsHeader, getString(if (expanded) R.string.expanded else R.string.collapsed)
        )
    }

    private fun updateSoundOptions() {
        val soundOperationInProgress = soundOptions.state.value.isBusy
        val beforeCustom = CustomSoundStore.hasCustomSound(this, SoundType.BEFORE_MAX)
        val afterCustom = CustomSoundStore.hasCustomSound(this, SoundType.AFTER_MAX)
        binding.beforeSoundStatus.setText(
            if (beforeCustom) R.string.custom_sound else R.string.default_sound
        )
        binding.afterSoundStatus.setText(
            if (afterCustom) R.string.custom_sound else R.string.default_sound
        )
        binding.chooseBeforeSound.isEnabled = !soundOperationInProgress
        binding.chooseAfterSound.isEnabled = !soundOperationInProgress
        binding.resetBeforeSound.isEnabled = beforeCustom && !soundOperationInProgress
        binding.resetAfterSound.isEnabled = afterCustom && !soundOperationInProgress
    }

    private fun reportSoundSelectionError(error: Exception) {
        Log.e("MainActivity", "Cannot change sound", error)
        Toast.makeText(this, R.string.sound_selection_error, Toast.LENGTH_LONG).show()
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
