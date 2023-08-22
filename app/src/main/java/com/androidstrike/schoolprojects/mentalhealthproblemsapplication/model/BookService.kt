package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model

data class BookService(
    val requestFormId: String = "",
    val clientId: String = "",
    val facilityId: String = "",
    val selectedAppointmentServiceID: String = "",
    val selectedAppointmentServiceName: String = "",
    val selectedAppointmentSpecialistID: String = "",
    val selectedAppointmentSpecificServiceName: String = "",
    val selectedAppointmentServicePrice: String = "",
    val selectedAppointmentServiceDiscountedPrice: String = "",
    val selectedAppointmentServiceAvailablePlaces: String = "",
    var selectedAppointmentDate: String = "",
    var selectedAppointmentRequestText: String = "",
    var dateCreated: String = "",
    var timeCreated: String = "",
    val requestStatus: String = "pending",
    val invoiceGenerated: Boolean = false
//    val selectedAppointmentDescription: String = "",
//    val selectedServiceType: String = "",
//    val selectedSpecialistID: String = "",
//    val status: String = "",
//    val dateResponded: String = "",
//    val scheduled: Boolean = false,
//    val scheduledDate: String = "",
//    val scheduledTime: String = "",
//    val dateScheduled: String = "",
//    val invoiceBankName: String = "",
//    val invoiceAccountIBAN: String = "",
//    val invoiceAccountName: String = "",
//    val invoiceGeneratedTime: Long = 0,
//    val invoiceGenerated: Boolean = false,
//    val invoicePaid: Boolean = false
    )
//
//1. Create New Record of Customer Request  in DB Table Customer Service Request Form by :
//- Request Form ID (PK) - Auto created by Google Firebase
//- Customer ID (FK)   - taken from the user Currently Logged In
//- Organization ID - from variable A
//- Organization Profile Service ID (FK) - Corresponding to the ID of record with Service Name =  Variable C
//- Type of Service ID (FK) - from variable B
//- Specialist ID (FK) - from Variable S
//- Service Details - from  variable D1
//- Service Price - from Variable D2
//- Service Discounted Price - from Variable D3
//- Available Places - from Variable D4
//- Service start date - from variable D5
//- Request text - from Variable D6
//- Date created - from Variable Date
//- Time created - from variable Time
//- Request Status (By default value "pending")
//
//2. Go back to Customer's Home Screen (Screen 1.4)