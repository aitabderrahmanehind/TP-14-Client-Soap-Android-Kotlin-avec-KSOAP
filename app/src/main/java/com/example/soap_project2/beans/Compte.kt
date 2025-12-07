package com.example.soap_project2.beans

import java.util.Date

data class Compte(
    val id: Long?,
    val balance: Double,
    val creationDate: Date,
    val accountType: TypeCompte
)