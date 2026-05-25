package com.example.videohub.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.videohub.R
import com.example.videohub.models.DataManager

class LoginActivity : AppCompatActivity() {

    private var isRegister = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        DataManager.init(this)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etChannel = findViewById<EditText>(R.id.etChannel)
        val btnAction = findViewById<Button>(R.id.btnAction)
        val tvToggle = findViewById<TextView>(R.id.tvToggle)
        val tvError = findViewById<TextView>(R.id.tvError)

        updateUI(btnAction, etChannel, tvToggle)

        btnAction.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                tvError.text = "Заповніть усі поля"
                return@setOnClickListener
            }

            if (isRegister) {
                val channel = etChannel.text.toString().trim()
                val user = DataManager.register(this, username, password, channel)
                if (user != null) {
                    goToMain()
                } else {
                    tvError.text = "Користувач вже існує"
                }
            } else {
                val user = DataManager.login(this, username, password)
                if (user != null) {
                    goToMain()
                } else {
                    tvError.text = "Невірний логін або пароль"
                }
            }
        }

        tvToggle.setOnClickListener {
            isRegister = !isRegister
            tvError.text = ""
            updateUI(btnAction, etChannel, tvToggle)
        }
    }

    private fun updateUI(btn: Button, etChannel: EditText, tvToggle: TextView) {
        if (isRegister) {
            btn.text = "Зареєструватися"
            etChannel.visibility = android.view.View.VISIBLE
            tvToggle.text = "Вже є акаунт? Увійти"
        } else {
            btn.text = "Увійти"
            etChannel.visibility = android.view.View.GONE
            tvToggle.text = "Немає акаунту? Зареєструватися"
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
