package com.example.questgen

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.questgen.api.JogoResponse
import com.example.questgen.api.RetrofitClient
import com.example.questgen.databinding.ActivityGameSelectionBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GameSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameSelectionBinding

    // ID do usuário logado recebido via Intent
    private var usuarioId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recupera o ID do usuário passado pela LoginActivity / HomeActivity
        usuarioId = intent.getIntExtra("usuario_id", 1)

        carregarJogos()
    }

    // ──────────────────────────────────────────
    // Busca a lista de jogos via GET buscar_jogos.php
    // ──────────────────────────────────────────
    private fun carregarJogos() {
        mostrarLoading(true)

        RetrofitClient.instance.buscarJogos()
            .enqueue(object : Callback<List<JogoResponse>> {
                override fun onResponse(
                    call: Call<List<JogoResponse>>,
                    response: Response<List<JogoResponse>>
                ) {
                    mostrarLoading(false)

                    if (response.isSuccessful) {
                        val jogos = response.body()
                        if (!jogos.isNullOrEmpty()) {
                            configurarRecyclerView(jogos)
                        } else {
                            mostrarErro("Nenhum jogo disponível no momento.")
                        }
                    } else {
                        mostrarErro("Erro do servidor: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<JogoResponse>>, t: Throwable) {
                    mostrarLoading(false)
                    mostrarErro("Sem conexão com o servidor.\nVerifique o IP no RetrofitClient.")
                }
            })
    }

    // ──────────────────────────────────────────
    // Configura o RecyclerView com o adapter
    // ──────────────────────────────────────────
    private fun configurarRecyclerView(jogos: List<JogoResponse>) {
        val adapter = JogoAdapter(jogos) { jogoSelecionado ->
            abrirDetalheDesafio(jogoSelecionado)
        }

        binding.rvJogos.apply {
            layoutManager = LinearLayoutManager(this@GameSelectionActivity)
            this.adapter = adapter
        }
    }

    // ──────────────────────────────────────────
    // Navega para ChallengeDetailActivity passando
    // o jogo selecionado e o ID do usuário
    // ──────────────────────────────────────────
    private fun abrirDetalheDesafio(jogo: JogoResponse) {
        val intent = Intent(this, ChallengeDetailActivity::class.java).apply {
            putExtra("usuario_id", usuarioId)
            putExtra("jogo_id",    jogo.jogoId)
            putExtra("jogo_nome",  jogo.jogoNome)
        }
        startActivity(intent)
    }

    // ──────────────────────────────────────────
    // Helpers de UI
    // ──────────────────────────────────────────
    private fun mostrarLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun mostrarErro(msg: String) {
        binding.tvErro.text       = msg
        binding.tvErro.visibility = View.VISIBLE
    }
}
