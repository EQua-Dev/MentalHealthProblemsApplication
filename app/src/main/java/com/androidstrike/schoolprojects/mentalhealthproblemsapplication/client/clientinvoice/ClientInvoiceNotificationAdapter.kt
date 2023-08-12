package com.androidstrike.trackit.client.clientinvoice

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R

class ClientInvoiceNotificationAdapter(itemView: View): RecyclerView.ViewHolder(itemView) {

    var dateCreated: TextView
    var timeCreated: TextView
    var facilityName: TextView
    var serviceName: TextView
    var invoicePaymentStatus: TextView


    init {
        dateCreated = itemView.findViewById(R.id.txt_client_invoice_date_generated)
        timeCreated = itemView.findViewById(R.id.txt_client_invoice_time_generated)
        facilityName = itemView.findViewById(R.id.txt_client_invoice_facility_name)
        serviceName = itemView.findViewById(R.id.txt_client_invoice_service_type)
        invoicePaymentStatus = itemView.findViewById(R.id.txt_client_invoice_generated_payment_status)


    }
}