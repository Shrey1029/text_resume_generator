package com.example.text_resume_generator

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.example.text_resume_generator.databinding.ActivityMainBinding
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var fontColor: Int = Color.parseColor("#212121")
    private var bgColor: Int = Color.parseColor("#FFFFFF")
    private var fontSize: Float = 16f

    private val name = "shreyansh"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateFontSizeDisplay()
        setupEventListeners()
        fetchResume(name)
    }

    private fun fetchResume(name: String) {
        binding.loadingLayout.visibility = View.VISIBLE
        binding.resumeTextView.visibility = View.GONE

        RetrofitInstance.api.getResume(name).enqueue(object : Callback<ResumeResponse> {
            override fun onResponse(call: Call<ResumeResponse>, response: Response<ResumeResponse>) {
                binding.loadingLayout.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val formattedText = formatResumeText(response.body()!!)
                    updateResumeText(formattedText)
                } else {
                    Toast.makeText(this@MainActivity, "Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResumeResponse>, t: Throwable) {
                binding.loadingLayout.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Network Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun formatResumeText(resume: ResumeResponse): String {
        return buildString {
            appendLine("👤 ${resume.name.uppercase()}")
            appendLine("=".repeat(40))
            appendLine()

            appendLine("📞 CONTACT INFORMATION")
            appendLine("-".repeat(25))
            resume.email?.let { appendLine("✉️  Email: $it") }
            resume.phone?.let { appendLine("📱 Phone: $it") }
            resume.twitter?.let { appendLine("🐦 Twitter: $it") }
            resume.address?.let { appendLine("🏠 Address: $it") }
            appendLine()

            resume.summary?.let {
                appendLine("📄 SUMMARY")
                appendLine("-".repeat(10))
                appendLine(it)
                appendLine()
            }

            if (resume.skills.isNotEmpty()) {
                appendLine("🛠️  SKILLS")
                appendLine("-".repeat(10))
                resume.skills.forEach { appendLine("• $it") }
                appendLine()
            }

            if (resume.projects.isNotEmpty()) {
                appendLine("🚀 PROJECTS")
                appendLine("-".repeat(12))
                resume.projects.forEachIndexed { index, project ->
                    appendLine("${index + 1}. ${project.title}")
                    appendLine("   • ${project.description}")
                    appendLine("   • ${project.startDate} to ${project.endDate}")
                    appendLine()
                }
            }

            resume.experience?.takeIf { it.isNotEmpty() }?.let {
                appendLine("💼 EXPERIENCE")
                appendLine("-".repeat(15))
                it.forEach { exp -> appendLine("• $exp") }
                appendLine()
            }

            resume.education?.let {
                appendLine("🎓 EDUCATION")
                appendLine("-".repeat(14))
                appendLine(it)
                appendLine()
            }

            appendLine("=".repeat(40))
            appendLine("Generated on ${SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault()).format(Date())}")
        }
    }

    private fun setupEventListeners() {
        binding.fontSizeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                fontSize = progress.toFloat().coerceAtLeast(10f)
                updateFontSizeDisplay()
                applyCustomization()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.colorPickerBtn.setOnClickListener { showColorPicker(true) }
        binding.backgroundColorBtn.setOnClickListener { showColorPicker(false) }
        binding.refreshBtn.setOnClickListener { refreshResume() }
        binding.fabMore.setOnClickListener { showMoreOptions() }
    }

    private fun updateFontSizeDisplay() {
        binding.fontSizeValue.text = "${fontSize.toInt()}sp"
    }

    private fun showColorPicker(isFont: Boolean) {
        val title = if (isFont) "Pick Font Color" else "Pick Background Color"

        ColorPickerDialog.Builder(this)
            .setTitle(title)
            .setPreferenceName("color_picker_dialog")
            .setPositiveButton("Select", object : ColorEnvelopeListener {
                override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                    envelope?.let {
                        if (isFont) fontColor = it.color else bgColor = it.color
                        applyCustomization()
                    }
                }
            })
            .setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.dismiss() }
            .attachAlphaSlideBar(true)
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)
            .show()
    }

    private fun applyCustomization() {
        binding.resumeTextView.textSize = fontSize
        binding.resumeTextView.setTextColor(fontColor)
        binding.contentCard.setCardBackgroundColor(bgColor)
        updateUIContrast()
    }

    private fun updateUIContrast() {
        val luminance = (0.299 * Color.red(bgColor) + 0.587 * Color.green(bgColor) + 0.114 * Color.blue(bgColor)) / 255
        val isLightBackground = luminance > 0.5
        if (isLightBackground && fontColor == Color.WHITE) {
            fontColor = Color.parseColor("#212121")
            binding.resumeTextView.setTextColor(fontColor)
        }
    }

    private fun updateResumeText(text: String) {
        binding.loadingLayout.visibility = View.GONE
        binding.resumeTextView.visibility = View.VISIBLE
        binding.resumeTextView.text = text
        applyCustomization()
    }

    private fun refreshResume() {
        binding.loadingLayout.visibility = View.VISIBLE
        binding.resumeTextView.visibility = View.GONE
        fetchResume(name)
        binding.refreshBtn.animate().rotation(360f).setDuration(500).start()
    }

    private fun showMoreOptions() {
        val popupMenu = PopupMenu(this, binding.fabMore)
        popupMenu.menuInflater.inflate(R.menu.more_options_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_reset -> {
                    resetToDefaults()
                    true
                }
                R.id.action_share -> {
                    shareResume()
                    true
                }
                R.id.action_export -> {
                    exportResume()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun resetToDefaults() {
        fontSize = 16f
        fontColor = Color.parseColor("#212121")
        bgColor = Color.parseColor("#FFFFFF")

        binding.fontSizeSlider.progress = fontSize.toInt()
        updateFontSizeDisplay()
        applyCustomization()

        Toast.makeText(this, "Settings reset to default", Toast.LENGTH_SHORT).show()
    }

    private fun shareResume() {
        val resumeContent = binding.resumeTextView.text.toString()
        if (resumeContent.isNotEmpty() && resumeContent != "Loading resume...") {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, resumeContent)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "Share Resume"))
        } else {
            Toast.makeText(this, "No resume content to share", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportResume() {
        Toast.makeText(this, "Export feature coming soon!", Toast.LENGTH_SHORT).show()
    }
}
