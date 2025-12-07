package com.example.soap_project2.ws



import android.util.Log
import com.example.soap_project2.beans.Compte
import com.example.soap_project2.beans.TypeCompte
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE
import java.text.SimpleDateFormat
import java.util.*

class Service {
    // URL avec PORT 8080
    // "10.0.2.2" = localhost de ton PC vu par l'émulateur
    private val NAMESPACE = "http://ws.demo.example.org/"
    private val URL = "http://192.168.1.48:8080/services/ws"

    private val GET_ACCOUNTS_METHOD = "getComptes"
    private val CREATE_ACCOUNT_METHOD = "createCompte"
    private val DELETE_ACCOUNT_METHOD = "deleteCompte"

    fun fetchAccounts(): List<Compte> {
        val accountList = mutableListOf<Compte>()
        val request = SoapObject(NAMESPACE, GET_ACCOUNTS_METHOD)
        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
        envelope.dotNet = false
        envelope.setOutputSoapObject(request)

        try {
            val transport = HttpTransportSE(URL)
            // Timeout de 5 secondes pour éviter de bloquer si le serveur est éteint
            transport.debug = true
            transport.call("", envelope)
            if (envelope.bodyIn is org.ksoap2.SoapFault) {
                val str = (envelope.bodyIn as org.ksoap2.SoapFault).faultstring
                Log.e("SOAP_ERROR", "Erreur serveur : $str")
                return emptyList()
            }
            val response = envelope.bodyIn as SoapObject

            for (i in 0 until response.propertyCount) {
                val property = response.getProperty(i)
                if (property is SoapObject) {
                    val account = parseSoapToAccount(property)
                    accountList.add(account)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return accountList
    }

    fun createAccount(balance: Double, accountType: TypeCompte): Boolean {
        val request = SoapObject(NAMESPACE, CREATE_ACCOUNT_METHOD)
        request.addProperty("solde", balance.toString())
        request.addProperty("type", accountType.name)

        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
        envelope.dotNet = false
        envelope.setOutputSoapObject(request)

        return try {
            val transport = HttpTransportSE(URL)
            transport.call("", envelope)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteAccount(accountId: Long): Boolean {
        val request = SoapObject(NAMESPACE, DELETE_ACCOUNT_METHOD)
        request.addProperty("id", accountId)

        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
        envelope.dotNet = false
        envelope.setOutputSoapObject(request)

        return try {
            val transport = HttpTransportSE(URL)
            transport.call("", envelope)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun parseSoapToAccount(soapObject: SoapObject): Compte {
        val id = soapObject.getPropertySafely("id").toString().toLongOrNull()
        val balance = soapObject.getPropertySafely("solde").toString().toDoubleOrNull() ?: 0.0
        val creationDateStr = soapObject.getPropertySafely("dateCreation")

        // Gestion robuste de la date (prend les 10 premiers caractères yyyy-MM-dd)
        val creationDate = try {
            // Convert the result of take(10) to a String before parsing
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(creationDateStr.toString().take(10))
        } catch (e: Exception) { Date() }

        val accountTypeStr = soapObject.getPropertySafely("type")
        // Note: The 'as String' cast here is redundant since getPropertySafely already returns a String.
        val accountType = try { TypeCompte.valueOf(accountTypeStr as String) } catch(e:Exception) { TypeCompte.CHECKING }

        return Compte(id, balance, creationDate ?: Date(), accountType)
    }

    private fun SoapObject.getPropertySafely(name: String): String {
        return try {
            if (this.hasProperty(name)) {
                this.getProperty(name).toString()
            } else { "" }
        } catch (e: Exception) { "" }
    }
}