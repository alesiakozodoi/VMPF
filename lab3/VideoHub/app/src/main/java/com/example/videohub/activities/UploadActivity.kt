package com.example.videohub.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.videohub.R
import com.example.videohub.models.DataManager

class UploadActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        val etTitle = findViewById<EditText>(R.id.etUploadTitle)
        val etDescription = findViewById<EditText>(R.id.etUploadDescription)
        val etUrl = findViewById<EditText>(R.id.etUploadUrl)
        val btnUpload = findViewById<Button>(R.id.btnUpload)
        val btnBack = findViewById<Button>(R.id.btnUploadBack)

        btnUpload.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val desc = etDescription.text.toString().trim()
            val url = etUrl.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Введіть назву відео", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            DataManager.addVideo(this, title, desc, url)
            Toast.makeText(this, "Відео завантажено!", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnBack.setOnClickListener { finish() }
    }
}
