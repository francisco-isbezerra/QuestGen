package com.example.questgen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.questgen.api.LojaResponse
import com.example.questgen.databinding.ItemLojaBinding

/**
 * Adapter para o RecyclerView de produtos na LojaActivity.
 */
class LojaAdapter(
    private val produtos: List<LojaResponse>,
    private val onComprarClick: (LojaResponse) -> Unit
) : RecyclerView.Adapter<LojaAdapter.LojaViewHolder>() {

    inner class LojaViewHolder(val binding: ItemLojaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LojaViewHolder {
        val binding = ItemLojaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LojaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LojaViewHolder, position: Int) {
        val produto = produtos[position]
        with(holder.binding) {
            tvProdutoNome.text  = produto.produtoNome
            tvProdutoPreco.text = "${produto.produtoPreco} GC"

            // Carrega imagem via Glide
            if (produto.produtoImagem.isNotBlank()) {
                com.bumptech.glide.Glide.with(ivProdutoImagem.context)
                    .load(produto.produtoImagem)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(ivProdutoImagem)
            }

            btnComprar.setOnClickListener { onComprarClick(produto) }
        }
    }

    override fun getItemCount() = produtos.size
}
