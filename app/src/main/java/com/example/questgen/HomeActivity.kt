package com.example.questgen

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.questgen.api.ApiService
import com.example.questgen.api.DesafioResponse
import com.example.questgen.api.LoginResponse
import com.example.questgen.api.RetrofitClient
import com.example.questgen.databinding.ActivityHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    // IDs fixos para o MVP (simula usuário logado)
    private val USUARIO_ID = 1
    private val JOGO_ID    = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Carrega dados do usuário na abertura da tela
        carregarDadosUsuario()

        // Clique no botão "GERAR DESAFIO"
        binding.btnGerarDesafio.setOnClickListener {
            gerarDesafio()
        }
    }

    // ──────────────────────────────────────────
    // Carrega nome e gameCoins do usuário (login.php)
    // ──────────────────────────────────────────
    private fun carregarDadosUsuario() {
        RetrofitClient.instance.getUsuario(USUARIO_ID)
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful) {
                        val usuario = response.body()
                        if (usuario != null) {
                            binding.tvNomeUsuario.text = usuario.usuarioNome
                            binding.tvGameCoins.text   = "${usuario.gameCoins} coins"
                        }
                    } else {
                        binding.tvNomeUsuario.text = "Usuário #$USUARIO_ID"
                        binding.tvGameCoins.text   = "— coins"
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    // Mostra feedback sem bloquear o restante da tela
                    binding.tvNomeUsuario.text = "Usuário #$USUARIO_ID"
                    binding.tvGameCoins.text   = "— coins"
                    Toast.makeText(
                        this@HomeActivity,
                        "Sem conexão com o servidor.\nVerifique o IP no RetrofitClient.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    // ──────────────────────────────────────────
    // Gera um desafio (gerar_desafio.php)
    // ──────────────────────────────────────────
    private fun gerarDesafio() {
        // Feedback visual de carregamento
        mostrarLoading(true)
        esconderErro()
        binding.cardDesafio.visibility = View.GONE

        RetrofitClient.instance.gerarDesafio(USUARIO_ID, JOGO_ID)
            .enqueue(object : Callback<DesafioResponse> {
                override fun onResponse(
                    call: Call<DesafioResponse>,
                    response: Response<DesafioResponse>
                ) {
                    mostrarLoading(false)

                    if (response.isSuccessful) {
                        val desafio = response.body()
                        if (desafio != null) {
                            preencherCardDesafio(desafio)
                        } else {
                            mostrarErro("Resposta vazia do servidor.")
                        }
                    } else {
                        mostrarErro("Erro do servidor: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<DesafioResponse>, t: Throwable) {
                    mostrarLoading(false)
                    mostrarErro("Falha na conexão: ${t.message}")
                }
            })
    }

    // ──────────────────────────────────────────
    // Preenche o card com dados do desafio e
    // aplica a cor da raridade dinamicamente
    // ──────────────────────────────────────────
    private fun preencherCardDesafio(desafio: DesafioResponse) {
        binding.tvDescricao.text  = desafio.descricao
        binding.tvRaridade.text   = desafio.raridade.uppercase()
        binding.tvRecompensa.text = "${desafio.recompensa} coins"

        // Lógica de cor por raridade
        val corRaridade = when (desafio.raridade.lowercase().trim()) {
            "lendário", "lendario", "legendary" -> Color.parseColor("#FFD700") // Dourado
            "raro", "rare"                       -> Color.parseColor("#8A2BE2") // Roxo Neon
            "comum", "common"                    -> Color.parseColor("#00FF7F") // Verde Neon
            else                                 -> Color.parseColor("#A0A0A0") // Cinza fallback
        }

        binding.tvRaridade.setTextColor(corRaridade)
        binding.tvRecompensa.setTextColor(corRaridade)

        // Exibe o card com animação suave
        binding.cardDesafio.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(400).start()
        }
    }

    // ──────────────────────────────────────────
    // Helpers de UI
    // ──────────────────────────────────────────
    private fun mostrarLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnGerarDesafio.isEnabled = !show
    }

    private fun mostrarErro(mensagem: String) {
        binding.tvErro.text       = mensagem
        binding.tvErro.visibility = View.VISIBLE
    }

    private fun esconderErro() {
        binding.tvErro.visibility = View.GONE
    }
}