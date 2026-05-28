package com.example.questgen

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.questgen.api.AcaoResponse
import com.example.questgen.api.DesafioResponse
import com.example.questgen.api.RetrofitClient
import com.example.questgen.databinding.ActivityChallengeDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChallengeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChallengeDetailBinding
    private var cronometro: CountDownTimer? = null

    // Dados recebidos via Intent
    private var usuarioId: Int = 1
    private var jogoId:    Int = 1
    private var jogoNome:  String = ""

    // Desafio carregado da API
    private var desafioAtual: DesafioResponse? = null

    // Duração do cronômetro: 24 horas em milissegundos
    private val DURACAO_MS = 24 * 60 * 60 * 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChallengeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recebe dados do Intent
        usuarioId = intent.getIntExtra("usuario_id", 1)
        jogoId    = intent.getIntExtra("jogo_id",    1)
        jogoNome  = intent.getStringExtra("jogo_nome") ?: "Desafio"

        binding.tvNomeJogo.text = jogoNome.uppercase()

        // Botão voltar
        binding.btnVoltar.setOnClickListener { finish() }

        // Botões de ação (inicialmente ocultos)
        binding.btnAceitar.setOnClickListener    { aceitarDesafio() }
        binding.btnReivindicar.setOnClickListener { reivindicarRecompensa() }

        // Carrega o desafio
        buscarDesafio()
    }

    override fun onDestroy() {
        super.onDestroy()
        cronometro?.cancel()
    }

    // ──────────────────────────────────────────
    // GET buscar_desafio.php?jogo_id=&usuario_id=
    // ──────────────────────────────────────────
    private fun buscarDesafio() {
        mostrarLoading(true)

        RetrofitClient.instance.buscarDesafio(usuarioId = usuarioId, jogoId = jogoId)
            .enqueue(object : Callback<DesafioResponse> {
                override fun onResponse(
                    call: Call<DesafioResponse>,
                    response: Response<DesafioResponse>
                ) {
                    mostrarLoading(false)

                    if (response.isSuccessful && response.body() != null) {
                        desafioAtual = response.body()!!
                        preencherTela(desafioAtual!!)
                    } else {
                        mostrarFeedback("Erro ao buscar desafio: ${response.code()}", isErro = true)
                    }
                }

                override fun onFailure(call: Call<DesafioResponse>, t: Throwable) {
                    mostrarLoading(false)
                    mostrarFeedback("Sem conexão com o servidor.", isErro = true)
                }
            })
    }

    // ──────────────────────────────────────────
    // Preenche todos os campos da tela e decide
    // qual botão exibir conforme o STATUS
    // ──────────────────────────────────────────
    private fun preencherTela(desafio: DesafioResponse) {
        binding.tvTituloDesafio.text   = desafio.titulo ?: "Sem Título"
        binding.tvDescricaoDesafio.text = desafio.descricao ?: "Sem Descrição"
        binding.tvRecompensa.text       = "${desafio.recompensa ?: 0} GC"

        // Correção do erro da linha 99 - Passando Int puro com fallback
        renderizarBarrinhasDificuldade(desafio.dificuldade ?: 1)

        when (desafio.status) {
            null -> {
                // Desafio novo — ainda não foi aceito
                binding.tvStatusDesafio.text      = "NOVO DESAFIO"
                binding.tvStatusDesafio.setTextColor(Color.parseColor("#00BFFF"))
                binding.btnAceitar.visibility     = View.VISIBLE
                binding.btnReivindicar.visibility = View.GONE
                iniciarCronometro(DURACAO_MS)
            }
            "EM_CURSO" -> {
                // Desafio aceito, em andamento
                binding.tvStatusDesafio.text      = "EM ANDAMENTO"
                binding.tvStatusDesafio.setTextColor(Color.parseColor("#FFD700"))
                binding.btnAceitar.visibility     = View.GONE
                binding.btnReivindicar.visibility = View.GONE
                iniciarCronometro(DURACAO_MS)
            }
            "CONCLUIDO" -> {
                // Missão concluída — pode reivindicar
                binding.tvStatusDesafio.text      = "✅ CONCLUÍDO"
                binding.tvStatusDesafio.setTextColor(Color.parseColor("#00FF7F"))
                binding.btnAceitar.visibility     = View.GONE
                binding.btnReivindicar.visibility = View.VISIBLE
                binding.tvCronometro.text         = "00:00:00"
                cronometro?.cancel()
            }
            "REIVINDICADO" -> {
                // Recompensa já foi pega
                binding.tvStatusDesafio.text      = "🏆 RECOMPENSA RECEBIDA"
                binding.tvStatusDesafio.setTextColor(Color.parseColor("#8A2BE2"))
                binding.btnAceitar.visibility     = View.GONE
                binding.btnReivindicar.visibility = View.GONE
                binding.tvCronometro.text         = "00:00:00"
                cronometro?.cancel()
            }
        }
    }

    // ──────────────────────────────────────────
    // Renderiza as barrinhas de dificuldade (1–5)
    // ──────────────────────────────────────────
    private fun renderizarBarrinhasDificuldade(nivel: Int) {
        binding.layoutDificuldade.removeAllViews()
        val dpToPx = resources.displayMetrics.density

        for (i in 1..5) {
            val barra = View(this).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    (28 * dpToPx).toInt(),
                    (10 * dpToPx).toInt()
                ).also { params ->
                    params.marginEnd = (6 * dpToPx).toInt()
                }
                background = android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius = (5 * dpToPx)
                    setColor(
                        if (i <= nivel) Color.parseColor("#00BFFF")
                        else Color.parseColor("#2A2A2A")
                    )
                }
            }
            binding.layoutDificuldade.addView(barra)
        }
    }

    // ──────────────────────────────────────────
    // Cronômetro regressivo de 24 horas
    // ──────────────────────────────────────────
    private fun iniciarCronometro(duracaoMs: Long) {
        cronometro?.cancel()
        cronometro = object : CountDownTimer(duracaoMs, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val horas   = millisUntilFinished / 3_600_000
                val minutos = (millisUntilFinished % 3_600_000) / 60_000
                val segundos = (millisUntilFinished % 60_000) / 1000
                binding.tvCronometro.text =
                    String.format("%02d:%02d:%02d", horas, minutos, segundos)
            }

            override fun onFinish() {
                binding.tvCronometro.text = "00:00:00"
                // Quando o tempo acaba num desafio EM_CURSO, habilita reivindicar
                if (desafioAtual?.status == "EM_CURSO") {
                    binding.btnReivindicar.visibility = View.VISIBLE
                    binding.tvStatusDesafio.text      = "✅ CONCLUÍDO"
                    binding.tvStatusDesafio.setTextColor(Color.parseColor("#00FF7F"))
                }
            }
        }.start()
    }

    // ──────────────────────────────────────────
    // POST aceitar_desafio.php
    // ──────────────────────────────────────────
    private fun aceitarDesafio() {
        val desafio = desafioAtual ?: return
        mostrarLoading(true)
        binding.btnAceitar.isEnabled = false

        // Adicionado fallback numérico para o ID do desafio opcional (? ou 0)
        RetrofitClient.instance.aceitarDesafio(usuarioId, desafio.desafioId ?: 0)
            .enqueue(object : Callback<AcaoResponse> {
                override fun onResponse(
                    call: Call<AcaoResponse>,
                    response: Response<AcaoResponse>
                ) {
                    mostrarLoading(false)
                    val resposta = response.body()

                    if (response.isSuccessful && resposta?.status == "Sucesso") {
                        mostrarFeedback("Desafio aceito! Boa sorte!", isErro = false)
                        binding.btnAceitar.visibility     = View.GONE
                        binding.tvStatusDesafio.text      = "EM ANDAMENTO"
                        binding.tvStatusDesafio.setTextColor(Color.parseColor("#FFD700"))
                        iniciarCronometro(DURACAO_MS)
                    } else {
                        binding.btnAceitar.isEnabled = true
                        mostrarFeedback(
                            resposta?.mensagem ?: "Erro ao aceitar desafio.",
                            isErro = true
                        )
                    }
                }

                override fun onFailure(call: Call<AcaoResponse>, t: Throwable) {
                    mostrarLoading(false)
                    binding.btnAceitar.isEnabled = true
                    mostrarFeedback("Sem conexão com o servidor.", isErro = true)
                }
            })
    }

    // ──────────────────────────────────────────
    // POST reivindicar_recompensa.php
    // ──────────────────────────────────────────
    private fun reivindicarRecompensa() {
        val desafio = desafioAtual ?: return
        mostrarLoading(true)
        binding.btnReivindicar.isEnabled = false

        // Tratamento de falhas para valores numéricos opcionais
        val recompensaFinal = desafio.recompensa ?: 0

        RetrofitClient.instance.reivindicarRecompensa(
            usuarioId,
            desafio.desafioId ?: 0,
            recompensaFinal
        ).enqueue(object : Callback<AcaoResponse> {
            override fun onResponse(
                call: Call<AcaoResponse>,
                response: Response<AcaoResponse>
            ) {
                mostrarLoading(false)
                val resposta = response.body()

                if (response.isSuccessful && resposta?.status == "Sucesso") {
                    val novosCoins = resposta.novosCoins
                    val mensagem = if (novosCoins != null)
                        "🏆 +$recompensaFinal GC! Saldo: $novosCoins GC"
                    else
                        "🏆 Recompensa de $recompensaFinal GC recebida!"

                    mostrarFeedback(mensagem, isErro = false)
                    binding.btnReivindicar.visibility = View.GONE
                    binding.tvStatusDesafio.text      = "🏆 RECOMPENSA RECEBIDA"
                    binding.tvStatusDesafio.setTextColor(Color.parseColor("#8A2BE2"))

                    Toast.makeText(this@ChallengeDetailActivity, mensagem, Toast.LENGTH_LONG).show()
                } else {
                    binding.btnReivindicar.isEnabled = true
                    mostrarFeedback(
                        resposta?.mensagem ?: "Erro ao reivindicar.",
                        isErro = true
                    )
                }
            }

            override fun onFailure(call: Call<AcaoResponse>, t: Throwable) {
                mostrarLoading(false)
                binding.btnReivindicar.isEnabled = true
                mostrarFeedback("Sem conexão com o servidor.", isErro = true)
            }
        })
    }

    // ──────────────────────────────────────────
    // Helpers de UI
    // ──────────────────────────────────────────
    private fun mostrarLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun mostrarFeedback(texto: String, isErro: Boolean) {
        binding.tvMensagem.text = texto
        binding.tvMensagem.setTextColor(
            if (isErro) Color.parseColor("#FF6B6B")
            else        Color.parseColor("#00FF7F")
        )
        binding.tvMensagem.visibility = View.VISIBLE
    }
}