package com.example.questgen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.questgen.api.JogoResponse
import com.example.questgen.databinding.ItemJogoBinding

/**
 * Adapter para o RecyclerView de jogos na GameSelectionActivity.
 * Carrega a imagem via URL usando a biblioteca Glide.
 */
class JogoAdapter(
    private val jogos: List<JogoResponse>,
    private val onJogoClick: (JogoResponse) -> Unit
) : RecyclerView.Adapter<JogoAdapter.JogoViewHolder>() {

    inner class JogoViewHolder(val binding: ItemJogoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JogoViewHolder {
        val binding = ItemJogoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return JogoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JogoViewHolder, position: Int) {
        val jogo = jogos[position]
        with(holder.binding) {
            tvJogoNome.text = jogo.jogoNome

            // Carrega a imagem via Glide (adicione ao build.gradle se ainda não tiver)
            // implementation("com.github.bumptech.glide:glide:4.16.0")
            // CORREÇÃO: Usando chamada segura (?.) e isNullOrBlank() para evitar NullPointerException
            if (!jogo.jogoImagemUrl.isNullOrBlank()) {
                com.bumptech.glide.Glide.with(ivJogoImagem.context)
                    .load(jogo.jogoImagemUrl)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(ivJogoImagem)
            } else {
                // Opcional: Define uma imagem padrão caso a URL venha nula ou vazia
                ivJogoImagem.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            cardJogo.setOnClickListener { onJogoClick(jogo) }
        }
    }

    override fun getItemCount() = jogos.size
}