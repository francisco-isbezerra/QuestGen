package com.example.questgen

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.questgen.api.LoginResponse
import com.example.questgen.api.RetrofitClient
import com.example.questgen.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEntrar.setOnClickListener   { realizarLogin() }
        binding.tvCadastreSe.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }
    }

    // ──────────────────────────────────────────────────
    // Valida campos localmente e dispara POST login.php
    // ──────────────────────────────────────────────────
    private fun realizarLogin() {
        val email = binding.etEmail.text.toString().trim()
        val senha = binding.etSenha.text.toString()

        // Validações no cliente antes de ir à rede
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mostrarErro("Por favor, insira um e-mail válido.")
            return
        }
        if (senha.isEmpty()) {
            mostrarErro("Por favor, insira sua senha.")
            return
        }

        mostrarLoading(true)
        esconderErro()

        // POST login.php com email e senha via @FormUrlEncoded
        RetrofitClient.instance.login(email, senha)
            .enqueue(object : Callback<LoginResponse> {

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    mostrarLoading(false)

                    if (!response.isSuccessful) {
                        mostrarErro("Erro do servidor: ${response.code()}. Tente novamente.")
                        return
                    }

                    val resposta = response.body()

                    // Resposta nula ou corpo inválido
                    if (resposta == null) {
                        mostrarErro("Resposta inesperada do servidor.")
                        return
                    }

                    // Servidor retornou Erro (ex: senha incorreta, usuário não existe)
                    if (resposta.status.equals("Erro", ignoreCase = true)) {
                        mostrarErro(resposta.mensagem)
                        return
                    }

                    // ✅ Login bem-sucedido — extrai os dados e navega
                    val usuarioId    = resposta.usuarioId   ?: 0
                    val usuarioNome  = resposta.usuarioNome  ?: ""
                    val usuarioEmail = resposta.usuarioEmail ?: email
                    val gameCoins    = resposta.gameCoins    ?: 0
                    val winRate      = resposta.winRate      ?: 0
                    val rankNome     = resposta.rankNome     ?: "Recruta"

                    // Passa todos os dados do usuário para a HomeActivity via Intent
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java).apply {
                        putExtra("usuario_id",    usuarioId)
                        putExtra("usuario_nome",  usuarioNome)
                        putExtra("usuario_email", usuarioEmail)
                        putExtra("game_coins",    gameCoins)
                        putExtra("win_rate",      winRate)
                        putExtra("rank_nome",     rankNome)
                        // Limpa a pilha de activities para que o Back não volte ao Login
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    mostrarLoading(false)
                    mostrarErro(
                        "Sem conexão com o servidor.\n" +
                        "Verifique o IP em RetrofitClient.kt e se o XAMPP está ativo."
                    )
                }
            })
    }

    // ──────────────────────────────────────────────────
    // Helpers de UI
    // ──────────────────────────────────────────────────
    private fun mostrarLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnEntrar.isEnabled    = !show
    }

    private fun mostrarErro(mensagem: String) {
        binding.tvErro.text       = mensagem
        binding.tvErro.visibility = View.VISIBLE
    }

    private fun esconderErro() {
        binding.tvErro.visibility = View.GONE
    }
}