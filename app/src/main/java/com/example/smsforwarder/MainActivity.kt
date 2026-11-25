package com.example.smsforwarder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val prefs = getSharedPreferences("cfg", MODE_PRIVATE)
        val toEmail = findViewById<EditText>(R.id.to_email)
        val smtpUser = findViewById<EditText>(R.id.smtp_user)
        val smtpPass = findViewById<EditText>(R.id.smtp_pass)
        val keyword = findViewById<EditText>(R.id.keyword)
        val enabled = findViewById<Switch>(R.id.enabled)

        toEmail.setText(prefs.getString("to_email", ""))
        smtpUser.setText(prefs.getString("smtp_user", ""))
        smtpPass.setText(prefs.getString("smtp_pass", ""))
        keyword.setText(prefs.getString("keyword", "SBM"))
        enabled.isChecked = prefs.getBoolean("enabled", false)

        fun save(on: Boolean = enabled.isChecked) {
            prefs.edit()
                .putString("to_email", toEmail.text.toString().trim())
                .putString("smtp_user", smtpUser.text.toString().trim())
                .putString("smtp_pass", smtpPass.text.toString().replace(" ", ""))
                .putString("keyword", keyword.text.toString().trim())
                .putBoolean("enabled", on)
                .apply()
        }

        enabled.setOnCheckedChangeListener { _, on ->
            if (on && (toEmail.text.isBlank() || smtpUser.text.isBlank() || smtpPass.text.isBlank())) {
                enabled.isChecked = false
                Toast.makeText(this, "Fill in all email fields first", Toast.LENGTH_SHORT).show()
                return@setOnCheckedChangeListener
            }
            if (on) requestPerms()
            save(on)
        }
        findViewById<Button>(R.id.save).setOnClickListener {
            save()
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestPerms() {
        if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(arrayOf(Manifest.permission.RECEIVE_SMS), 1)
    }
}
