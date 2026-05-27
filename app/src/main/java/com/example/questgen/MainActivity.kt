package com.example.questgen

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * MainActivity funciona como splash/launcher.
 * No MVP redireciona direto para HomeActivity.
 * Futuramente aqui ficará a tela de Login.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}