package com.example.questgen

import android.content.Intent
import android.os.Bundle
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

        // Clique no botão "ENTRAR"
        binding.btnEntrar.setOnClickListener {
            realizarLogin()
        }

        // Link "Cadastre-se aqui" → abre CadastroActivity
        binding.tvCadastreSe.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }
    }

    // ──────────────────────────────────────────
    // Valida campos e chama login.php via Retrofit
    // ──────────────────────────────────────────
    private fun realizarLogin() {
        val email = binding.etEmail.text.toString().trim()
        val senha = binding.etSenha.text.toString()

        // Validações locais
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mostrarErro("Por favor, insira um e-mail válido.")
            return
        }
        if (senha.isEmpty()) {
            mostrarErro("Por favor, insira sua senha.")
            return
        }

        mostrarLoading(true)
        esconderErro()

        // ⚠️ Adapte a chamada conforme sua lógica de login no backend.
        // Atualmente o getUsuario usa apenas ID; quando o backend de login
        // por e-mail/senha estiver pronto, troque pela rota correspondente.
        // Por enquanto usamos ID=1 como stub para não bloquear a navegação.
        RetrofitClient.instance.getUsuario(1)
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    mostrarLoading(false)
                    if (response.isSuccessful && response.body() != null) {
                        val usuario = response.body()!!
                        val intent = Intent(this@LoginActivity, HomeActivity::class.java).apply {
                            putExtra("usuario_id",   usuario.usuarioId)
                            putExtra("usuario_nome",  usuario.usuarioNome)
                            putExtra("usuario_email", usuario.usuarioEmail)
                            putExtra("game_coins",    usuario.gameCoins)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        mostrarErro("E-mail ou senha incorretos.")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    mostrarLoading(false)
                    mostrarErro("Sem conexão com o servidor.\nVerifique o IP no RetrofitClient.")
                }
            })
    }

    // ──────────────────────────────────────────
    // Helpers de UI
    // ──────────────────────────────────────────
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