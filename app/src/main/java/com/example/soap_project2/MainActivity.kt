package com.example.soap_project2

import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.soap_project2.adapter.CompteAdapter
import com.example.soap_project2.beans.TypeCompte
import com.example.soap_project2.beans.Compte
import com.example.soap_project2.ws.Service

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addAccountFab: ExtendedFloatingActionButton
    private val adapter = CompteAdapter()
    private val service = Service()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialisation des vues
        recyclerView = findViewById(R.id.recyclerView)
        addAccountFab = findViewById(R.id.fabAdd)

        setupRecyclerView()

        addAccountFab.setOnClickListener { showAddAccountDialog() }

        // Chargement initial
        loadAccounts()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Listener sur le bouton supprimer
        adapter.onDeleteClick = { account ->
            showDeleteConfirmationDialog(account)
        }
    }

    private fun loadAccounts() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val accounts = service.fetchAccounts()
                withContext(Dispatchers.Main) {
                    if (accounts.isEmpty()) {
                        Toast.makeText(this@MainActivity, "Aucun compte trouvé ou erreur serveur", Toast.LENGTH_LONG).show()
                    }
                    adapter.updateComptes(accounts)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Erreur connexion: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun createAccount(balance: Double, accountType: TypeCompte) {
        lifecycleScope.launch(Dispatchers.IO) {
            val success = service.createAccount(balance, accountType)
            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(this@MainActivity, "Compte créé avec succès", Toast.LENGTH_SHORT).show()
                    loadAccounts()
                } else {
                    Toast.makeText(this@MainActivity, "Échec de la création", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteAccount(account: Compte) {
        lifecycleScope.launch(Dispatchers.IO) {
            if (account.id != null) {
                val success = service.deleteAccount(account.id)
                withContext(Dispatchers.Main) {
                    if (success) {
                        adapter.removeAccount(account)
                        Toast.makeText(this@MainActivity, "Compte supprimé", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showAddAccountDialog() {
        val dialogView = layoutInflater.inflate(R.layout.popup, null)

        MaterialAlertDialogBuilder(this)
            .setTitle("Nouveau Compte")
            .setView(dialogView)
            .setPositiveButton("Ajouter") { _, _ ->
                val balanceEditText = dialogView.findViewById<TextInputEditText>(R.id.etSolde)
                val checkingAccountRadioButton = dialogView.findViewById<RadioButton>(R.id.radioChecking)

                val balanceStr = balanceEditText.text.toString()
                if (balanceStr.isNotEmpty()) {
                    val balance = balanceStr.toDoubleOrNull() ?: 0.0
                    val accountType = if (checkingAccountRadioButton.isChecked) TypeCompte.CHECKING else TypeCompte.SAVINGS

                    createAccount(balance, accountType)
                } else {
                    Toast.makeText(this, "Solde invalide", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(account: Compte) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirmation")
            .setMessage("Voulez-vous vraiment supprimer le compte N°${account.id} ?")
            .setPositiveButton("Supprimer") { _, _ ->
                deleteAccount(account)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
}