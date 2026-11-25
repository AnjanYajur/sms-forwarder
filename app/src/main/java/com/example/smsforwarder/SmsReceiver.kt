package com.example.smsforwarder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val prefs = context.getSharedPreferences("cfg", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("enabled", false)) return
        val keyword = prefs.getString("keyword", "SBM").orEmpty().trim()
        val user = prefs.getString("smtp_user", "").orEmpty().trim()
        val pass = prefs.getString("smtp_pass", "").orEmpty()
        val to = prefs.getString("to_email", "").orEmpty().trim()
        if (keyword.isEmpty() || user.isEmpty() || pass.isEmpty() || to.isEmpty()) return

        // Long messages arrive as several parts; join parts per sender.
        val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        val bySender = LinkedHashMap<String, StringBuilder>()
        for (p in parts) {
            bySender.getOrPut(p.originatingAddress ?: "unknown") { StringBuilder() }
                .append(p.messageBody ?: "")
        }
        val matches = bySender.map { it.key to it.value.toString() }
            .filter { it.second.contains(keyword, ignoreCase = true) }
        if (matches.isEmpty()) return

        // Network work must not run on the main thread; keep the receiver alive until done.
        val pending = goAsync()
        Thread {
            try {
                for ((sender, text) in matches) {
                    // Up to 3 attempts in case the network is briefly unavailable.
                    for (attempt in 1..3) {
                        try { sendEmail(user, pass, to, sender, text); break }
                        catch (e: Exception) { if (attempt < 3) Thread.sleep(5000L * attempt) }
                    }
                }
            } finally {
                pending.finish()
            }
        }.start()
    }

    private fun sendEmail(user: String, pass: String, to: String, sender: String, text: String) {
        val props = Properties().apply {
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.connectiontimeout", "15000")
            put("mail.smtp.timeout", "15000")
        }
        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication() = PasswordAuthentication(user, pass)
        })
        val msg = MimeMessage(session).apply {
            setFrom(InternetAddress(user))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            subject = "SMS from $sender"
            setText("From: $sender\n\n$text", "UTF-8")
        }
        Transport.send(msg)
    }
}
