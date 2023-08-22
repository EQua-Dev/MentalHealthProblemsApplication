package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

object Common {

    const val REQUEST_PERMISSION = 100


    val IRISH_CODE = "+353"
    var facilityName = "Facility"
    var clientName = "Client"
    const val SERVICES = "Services"
    const val SPECIALISTS = "Specialists"
    const val SPECIFIC_SERVICE = "Specific Service"
    private const val WALLET_REF = "Customer Digital Wallets"
    const val WALLET_HISTORY_REF = "Wallet History"

    const val REASON_ACCOUNT_FUND = "Account Fund"

    const val DATE_FORMAT_LONG = "EEE, dd MMM, yyyy | hh:mm a"



    var serviceToRate = ""

    //lateinit var mAuth: FirebaseAuth// = FirebaseAuth.getInstance()
    val auth = FirebaseAuth.getInstance()
    val clientCollectionRef = Firebase.firestore.collection("Clients")
    val facilityCollectionRef = Firebase.firestore.collection("Mental Health Organisations")
    val appointmentsCollectionRef = Firebase.firestore.collection("Appointments")
    val servicesCollectionRef = Firebase.firestore.collection("Mental Health organisation’s Service Profile")
    val walletCollectionRef = Firebase.firestore.collection(WALLET_REF)
    val feedbackCollectionRef = Firebase.firestore.collection("Feedbacks")

    fun convertTimeStampToDate(time: Long): Date {
        return Date(Timestamp(time).time)
    }

    fun getDateFormatted(date: Date): String? {
        return SimpleDateFormat("dd-MM-yyyy HH:mm").format(date).toString()
    }



}