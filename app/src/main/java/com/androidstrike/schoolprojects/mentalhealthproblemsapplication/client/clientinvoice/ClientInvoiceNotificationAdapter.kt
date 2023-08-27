package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.client.clientinvoice

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R

class ClientInvoiceNotificationAdapter(itemView: View): RecyclerView.ViewHolder(itemView) {

    var dateCreated: TextView
    var timeCreated: TextView
    var facilityName: TextView
    var serviceName: TextView
    var invoice_payment_view_btn: TextView


    init {
        dateCreated = itemView.findViewById(R.id.txt_client_invoice_date_generated)
        timeCreated = itemView.findViewById(R.id.txt_client_invoice_time_generated)
        facilityName = itemView.findViewById(R.id.txt_client_invoice_facility_name)
        serviceName = itemView.findViewById(R.id.txt_client_invoice_service_type)
        invoice_payment_view_btn = itemView.findViewById(R.id.btn_client_invoice_generated_payment_status)


    }
}