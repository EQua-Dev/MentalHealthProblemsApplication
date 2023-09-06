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
    private const val SERVICES = "Mental Health Organisation’s Service Profile"
    private const val SERVICE_TYPE = "Type of Service"
    const val SPECIALISTS = "Service Type Specialist"
    const val SPECIFIC_SERVICE = "Specific Service"
    private const val FACILITY_REF = "Mental Health Organisations"
    private const val CUSTOMER_REF = "Customer with Mental Health Problems"
    private const val APPOINTMENT_REF = "Customer's Service Request Form"
    private const val REQUEST_RESPONSE_NOTIFICATION_REF = "Notification of Customer's Service Request Form Acceptance or Rejection"
    private const val ACCEPTED_REQUEST_PAYMENT_INVOICE_REF = "Invoice for Payment of Accepted Customer's Service Request"
    private const val CUSTOMER_INVOICE_PAYMENT_REF = "Payment for Invoice of Accepted Customer's Service Request"
    private const val CUSTOMER_FEEDBACK_AND_RATING_REF = "Customer's Feedback and Ratings"
    private const val FACILITY_MEETING_SCHEDULE_FORM = "Notification of Accepted Customer Request Form Scheduled"
    private const val WALLET_REF = "Customer Digital Wallets"
    const val WALLET_HISTORY_REF = "Wallet History"

    const val REASON_ACCOUNT_FUND = "Account Fund"
    const val ACCEPTED_TEXT = "Accepted"
    const val REJECTED_TEXT = "Rejected"
    const val PROCESSED_TEXT = "Processed"

    const val DATE_FORMAT_LONG = "EEE, dd MMM, yyyy | hh:mm a"


    val auth = FirebaseAuth.getInstance()
    val clientCollectionRef = Firebase.firestore.collection(CUSTOMER_REF)
    val facilityCollectionRef = Firebase.firestore.collection(FACILITY_REF)
    val appointmentsCollectionRef = Firebase.firestore.collection(APPOINTMENT_REF)
    val servicesCollectionRef = Firebase.firestore.collection(SERVICES)
    val serviceSpecialistCollectionRef = Firebase.firestore.collection(SPECIALISTS)
    val servicesTypeCollectionRef = Firebase.firestore.collection(SERVICE_TYPE)
    val requestResponseNotificationCollectionRef = Firebase.firestore.collection(REQUEST_RESPONSE_NOTIFICATION_REF)
    val acceptedRequestInvoiceCollectionRef = Firebase.firestore.collection(ACCEPTED_REQUEST_PAYMENT_INVOICE_REF)
    val meetingScheduleFormCollectionRef = Firebase.firestore.collection(FACILITY_MEETING_SCHEDULE_FORM)
    val invoicePaymentCollectionRef = Firebase.firestore.collection(CUSTOMER_INVOICE_PAYMENT_REF)
    val walletCollectionRef = Firebase.firestore.collection(WALLET_REF)
    val feedbackCollectionRef = Firebase.firestore.collection(CUSTOMER_FEEDBACK_AND_RATING_REF)


}