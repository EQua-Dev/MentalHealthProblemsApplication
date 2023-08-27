package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InvoicePaymentData(
    val paymentID: String = "",
    val invoiceID: String = "",
    val notificationID: String = "",
    val customerRequestFormID: String = "",
    val customerID: String = "",
    val customerDigitalWalletID: String = "",
    val organisationID: String = "",
    val organisationProfileServiceID: String = "",
    val typeOfServiceID: String = "",
    val typeOfServiceSpecialistID: String = "",
    val paymentAmount: String = "",
    val paymentBankIBAN: String = "",
    val paymentBankName: String = "",
    val paymentBankAccountHolderName: String = "",
    val paymentDate: String = "",
    val paymentTime: String = ""
): Parcelable
