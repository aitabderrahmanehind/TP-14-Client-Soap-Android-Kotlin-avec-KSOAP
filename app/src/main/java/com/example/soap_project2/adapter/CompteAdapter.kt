package com.example.soap_project2.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.soap_project2.R
import com.example.soap_project2.beans.Compte
import com.google.android.material.chip.Chip

import java.text.SimpleDateFormat
import java.util.*

class CompteAdapter : RecyclerView.Adapter<CompteAdapter.CompteViewHolder>() {
    private var accounts = mutableListOf<Compte>()
    var onDeleteClick: ((Compte) -> Unit)? = null

    fun updateComptes(newAccounts: List<Compte>) {
        accounts.clear()
        accounts.addAll(newAccounts)
        notifyDataSetChanged()
    }

    fun removeAccount(account: Compte) {
        val index = accounts.indexOfFirst { it.id == account.id }
        if (index != -1) {
            accounts.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return CompteViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompteViewHolder, position: Int) {
        holder.bind(accounts[position])
    }

    override fun getItemCount() = accounts.size

    inner class CompteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val idTextView: TextView = view.findViewById(R.id.textId)
        private val balanceTextView: TextView = view.findViewById(R.id.textSolde)
        private val typeChipView: Chip = view.findViewById(R.id.textType)
        private val dateTextView: TextView = view.findViewById(R.id.textDate)
        private val deleteButton: ImageButton = view.findViewById(R.id.btnDelete)

        fun bind(account: Compte) {
            idTextView.text = "Compte N° ${account.id}"
            balanceTextView.text = "${account.balance} DH"
            typeChipView.text = account.accountType.name

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dateTextView.text = "Créé le : ${sdf.format(account.creationDate)}"

            deleteButton.setOnClickListener {
                onDeleteClick?.invoke(account)
            }
        }
    }
}