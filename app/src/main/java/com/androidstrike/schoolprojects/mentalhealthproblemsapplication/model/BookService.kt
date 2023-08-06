package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model

data class BookService(
    val selectedAppointmentServiceName: String = "",
    val selectedAppointmentServiceID: String = "",
    val selectedAppointmentDate: String = "",
    val selectedAppointmentTime: String = "",
    val selectedAppointmentDescription: String = "",
    val dateBooked: String = "",
    val clientId: String = "",
    val facilityId: String = "",
    val status: String = "",
    val dateResponded: String = "",
    val scheduled: Boolean = false,
    val scheduledDate: String = "",
    val scheduledTime: String = "",
    val dateScheduled: String = "",
    val invoiceBankName: String = "",
    val invoiceAccountIBAN: String = "",
    val invoiceAccountName: String = "",
    val invoiceGeneratedTime: Long = 0,
    val invoiceGenerated: Boolean = false,
    val invoicePaid: Boolean = false
    )
