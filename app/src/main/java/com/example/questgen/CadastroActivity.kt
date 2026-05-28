package com.example.questgen

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.questgen.api.CadastroResponse
import com.example.questgen.api.RetrofitClient
import com.example.questgen.databinding.ActivityCadastroBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Clique no botão "CRIAR CONTA"
        binding.btnCadastrar.setOnClickListener {
            realizarCadastro()
        }

        // Link "Já tenho uma conta" → volta para a LoginActivity
        binding.tvJaTenhoConta.setOnClickListener {
            finish()
        }
    }

    // ──────────────────────────────────────────
    // Valida os campos e faz o POST no cadastro.php
    // ──────────────────────────────────────────
    private fun realizarCadastro() {
        val nome  = binding.etNome.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val senha = binding.etSenha.text.toString()

        // Validações locais antes de chamar a API
        if (nome.isEmpty()) {
            mostrarMensagem("Por favor, insira seu nome.", isErro = true)
            return
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mostrarMensagem("Por favor, insira um e-mail válido.", isErro = true)
            return
        }
        if (senha.length < 6) {
            mostrarMensagem("A senha deve ter no mínimo 6 caracteres.", isErro = true)
            return
        }

        // Inicia loading
        mostrarLoading(true)
        esconderMensagem()

        RetrofitClient.instance.cadastrarUsuario(nome, email, senha)
            .enqueue(object : Callback<CadastroResponse> {
                override fun onResponse(
                    call: Call<CadastroResponse>,
                    response: Response<CadastroResponse>
                ) {
                    mostrarLoading(false)

                    if (response.isSuccessful) {
                        val resposta = response.body()
                        if (resposta != null) {
                            val isErro = resposta.status.equals("Erro", ignoreCase = true)
                            mostrarMensagem(resposta.mensagem, isErro = isErro)

                            // Se cadastro bem-sucedido, volta para o login após 2 segundos
                            if (!isErro) {
                                binding.root.postDelayed({ finish() }, 2000)
                            }
                        } else {
                            mostrarMensagem("Resposta inesperada do servidor.", isErro = true)
                        }
                    } else {
                        mostrarMensagem("Erro do servidor: ${response.code()}", isErro = true)
                    }
                }

                override fun onFailure(call: Call<CadastroResponse>, t: Throwable) {
                    mostrarLoading(false)
                    mostrarMensagem(
                        "Sem conexão com o servidor.\nVerifique o IP no RetrofitClient.",
                        isErro = true
                    )
                }
            })
    }

    // ──────────────────────────────────────────
    // Helpers de UI
    // ──────────────────────────────────────────
    private fun mostrarLoading(show: Boolean) {
        binding.progressBar.visibility   = if (show) View.VISIBLE else View.GONE
        binding.btnCadastrar.isEnabled   = !show
    }

    private fun mostrarMensagem(texto: String, isErro: Boolean) {
        binding.tvMensagem.text      = texto
        binding.tvMensagem.setTextColor(
            if (isErro)
                android.graphics.Color.parseColor("#FF6B6B") // Vermelho neon
            else
                android.graphics.Color.parseColor("#00FF7F") // Verde neon
        )
        binding.tvMensagem.visibility = View.VISIBLE
    }

    private fun esconderMensagem() {
        binding.tvMensagem.visibility = View.GONE
    }
}
