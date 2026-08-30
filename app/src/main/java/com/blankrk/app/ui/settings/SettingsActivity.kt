package com.blankrk.app.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.blankrk.app.R
import com.blankrk.app.databinding.ActivitySettingsBinding
import com.blankrk.app.ui.auth.AuthActivity
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val languages = resources.getStringArray(R.array.languages)
        val codes = resources.getStringArray(R.array.language_codes)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)
        binding.languageSpinner.adapter = adapter

        val prefs = getSharedPreferences("blank_rk_prefs", MODE_PRIVATE)
        val savedCode = prefs.getString("language_code", "en")
        binding.languageSpinner.setSelection(codes.indexOf(savedCode).coerceAtLeast(0))

        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedCode = codes[position]
                if (selectedCode != savedCode) {
                    prefs.edit().putString("language_code", selectedCode).apply()
                    setAppLocale(selectedCode)
                    recreate()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, AuthActivity::class.java))
            finishAffinity()
        }
    }

    private fun setAppLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
