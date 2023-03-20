package com.hashone.module.textview.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.hashone.commonutils.utils.FileUtils
import com.hashone.commonutils.utils.Utils
import com.hashone.module.textview.databinding.ActivityTextEditBinding

class TextEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTextEditBinding

    private var textString = ""
    private var textGravity = 1
    private var textFontName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        textString = if (intent.hasExtra("textString")) {
            intent.extras!!.getString("textString", "")
        } else ""
        textGravity = if (intent.hasExtra("textGravity")) {
            intent.extras!!.getInt("textGravity")
        } else 1
        textFontName = if (intent.hasExtra("textFontName")) {
            intent.extras!!.getString("textFontName", "")
        } else ""

        var fontFile = FileUtils.getFontFile(this, textFontName)

        binding.editTextNewText.typeface =
            if (fontFile.isNotEmpty()) Typeface.createFromFile((fontFile))
            else {
                fontFile = FileUtils.getFontFile(this, "LibreBaskerville-Regular")
                Typeface.createFromFile((fontFile))
            }
        binding.editTextNewText.gravity = when (textGravity) {
            0 -> Gravity.START or Gravity.CENTER_VERTICAL
            1 -> Gravity.CENTER
            2 -> Gravity.END or Gravity.CENTER_VERTICAL
            else -> Gravity.START or Gravity.CENTER_VERTICAL
        }

        binding.editTextNewText.setText(textString)

        binding.closeScreenImage.setOnClickListener {
            if (Utils.checkClickTime600()) {
                Utils.closeKeyboard(this, binding.closeScreenImage)
                binding.closeScreenImage.postDelayed({
                    onBackPressed()
                }, 120L)
            }
        }

        binding.doneScreenImage.setOnClickListener {
            if (Utils.checkClickTime600()) {
                Utils.closeKeyboard(this, binding.doneScreenImage)

                binding.closeScreenImage.postDelayed({
                    val dataIntent = Intent()
                    dataIntent.putExtra("textString", binding.editTextNewText.text.toString().trim())
                    setResult(RESULT_OK, dataIntent)
                    finish()
                }, 120L)
            }
        }

        binding.editTextNewText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.doneScreenImage.isVisible =
                    binding.editTextNewText.text.toString().trim().isNotEmpty()
            }

            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        binding.editTextNewText.requestFocus()
        Utils.openKeyboard(this)
    }
}