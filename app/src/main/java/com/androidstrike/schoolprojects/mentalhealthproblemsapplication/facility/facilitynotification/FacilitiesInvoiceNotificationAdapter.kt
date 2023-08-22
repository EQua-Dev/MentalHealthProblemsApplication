package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.facility.facilitynotification

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R

class FacilitiesInvoiceNotificationAdapter(itemView: View): RecyclerView.ViewHolder(itemView) {

    var dateCreated: TextView
    var timeCreated: TextView
    var clientName: TextView
    var serviceName: TextView
    var invoiceStatus: TextView


    init {
        dateCreated = itemView.findViewById(R.id.txt_facility_invoice_date_created)
        timeCreated = itemView.findViewById(R.id.txt_facility_invoice_time_created)
        clientName = itemView.findViewById(R.id.txt_facility_invoice_client_name)
        serviceName = itemView.findViewById(R.id.txt_facility_invoice_service_type)
        invoiceStatus = itemView.findViewById(R.id.txt_facility_invoice_generated_status)


    }
}