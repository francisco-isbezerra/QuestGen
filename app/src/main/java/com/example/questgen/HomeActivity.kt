package com.example.questgen

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.questgen.api.HomeResponse
import com.example.questgen.api.RetrofitClient
import com.example.questgen.databinding.ActivityHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    // ID do usuário logado — recebido via Intent após login
    private var usuarioId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usuarioId = intent.getIntExtra("usuario_id", 1)

        // Pré-preenche nome se já veio no Intent (evita flash em branco)
        val nomeIntent = intent.getStringExtra("usuario_nome")
        if (!nomeIntent.isNullOrBlank()) binding.tvNomeUsuario.text = nomeIntent

        // Carrega perfil + desafio ativo em uma única chamada POST
        carregarHomeData()

        // "ESCOLHER JOGO" → GameSelectionActivity
        binding.btnGerarDesafio.setOnClickListener {
            startActivity(
                Intent(this, GameSelectionActivity::class.java)
                    .putExtra("usuario_id", usuarioId)
            )
        }
    }

    // ────────────────────────────────────────────────────────────────────
    // POST buscar_desafio.php [usuario_id, jogo_id=0]
    // Retorna HomeResponse: perfil do usuário + desafio EM_CURSO (ou null)
    // ────────────────────────────────────────────────────────────────────
    private fun carregarHomeData() {
        mostrarLoading(true)

        RetrofitClient.instance.buscarDesafioHome(usuarioId, jogoId = 0)
            .enqueue(object : Callback<HomeResponse> {

                override fun onResponse(
                    call: Call<HomeResponse>,
                    response: Response<HomeResponse>
                ) {
                    mostrarLoading(false)

                    if (!response.isSuccessful || response.body() == null) {
                        preencherFallback()
                        mostrarErro("Não foi possível carregar os dados. (${response.code()})")
                        return
                    }

                    val dados = response.body()!!

                    if (dados.status.equals("Erro", ignoreCase = true)) {
                        preencherFallback()
                        mostrarErro(dados.rankNome) // rankNome não usado aqui, mensagem pode vir via status
                        return
                    }

                    // ── Dados do perfil ──────────────────────────────────
                    binding.tvNomeUsuario.text = dados.usuarioNome
                    binding.tvGameCoins.text   = "${dados.gameCoins} GC"
                    binding.tvRankNome.text    = dados.rankNome

                    // ── Card do desafio ativo ────────────────────────────
                    if (dados.temDesafioAtivo) {
                        preencherCardDesafio(dados)
                    } else {
                        binding.cardDesafio.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<HomeResponse>, t: Throwable) {
                    mostrarLoading(false)
                    preencherFallback()
                    Toast.makeText(
                        this@HomeActivity,
                        "Sem conexão com o servidor.\nVerifique o IP no RetrofitClient.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    // ────────────────────────────────────────────────────────────────────
    // Preenche o card do desafio em curso com os dados do HomeResponse
    // ────────────────────────────────────────────────────────────────────
    private fun preencherCardDesafio(dados: HomeResponse) {
        binding.tvTituloDesafio.text = dados.titulo     ?: "—"
        binding.tvDescricao.text     = dados.descricao  ?: "—"
        binding.tvRecompensa.text    = "${dados.recompensa ?: 0} GC"

        renderizarDificuldade(dados.dificuldade ?: 3)

        // Badge de status
        val (texto, cor) = when (dados.statusDesafio) {
            "EM_CURSO"     -> Pair("EM ANDAMENTO", "#FFD700")
            "CONCLUIDO"    -> Pair("✅ CONCLUÍDO",  "#00FF7F")
            "REIVINDICADO" -> Pair("🏆 RECEBIDO",   "#8A2BE2")
            else           -> Pair("ATIVO",          "#00BFFF")
        }
        binding.tvStatusBadge.text = texto
        binding.tvStatusBadge.setBackgroundColor(Color.parseColor(cor))

        binding.cardDesafio.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(400).start()
        }
    }

    // ────────────────────────────────────────────────────────────────────
    // Barrinhas de dificuldade (1–5) geradas programaticamente
    // ────────────────────────────────────────────────────────────────────
    private fun renderizarDificuldade(nivel: Int) {
        binding.layoutDificuldade.removeAllViews()
        val dp = resources.displayMetrics.density

        for (i in 1..5) {
            val barra = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    (28 * dp).toInt(), (10 * dp).toInt()
                ).also { it.marginEnd = (6 * dp).toInt() }

                background = GradientDrawable().apply {
                    cornerRadius = 5 * dp
                    setColor(
                        if (i <= nivel) Color.parseColor("#00BFFF")
                        else            Color.parseColor("#2A2A2A")
                    )
                }
            }
            binding.layoutDificuldade.addView(barra)
        }
    }

    // ────────────────────────────────────────────────────────────────────
    // Helpers de UI
    // ────────────────────────────────────────────────────────────────────
    private fun preencherFallback() {
        binding.tvNomeUsuario.text = "Jogador #$usuarioId"
        binding.tvGameCoins.text   = "— GC"
        binding.tvRankNome.text    = "—"
    }

    private fun mostrarLoading(show: Boolean) {
        binding.progressBar.visibility    = if (show) View.VISIBLE else View.GONE
        binding.btnGerarDesafio.isEnabled = !show
    }

    private fun mostrarErro(mensagem: String) {
        binding.tvErro.text       = mensagem
        binding.tvErro.visibility = View.VISIBLE
    }
}