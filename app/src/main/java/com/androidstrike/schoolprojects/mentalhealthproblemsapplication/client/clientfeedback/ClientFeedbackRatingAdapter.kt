package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.client.clientfeedback

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R

class ClientFeedbackRatingAdapter(itemView: View): RecyclerView.ViewHolder(itemView) {

    var dateCreated: TextView
    var timeCreated: TextView
    var facilityName: TextView
    var serviceName: TextView
    //var serviceRating: TextView
    var rateServiceButton: Button


    init {
        dateCreated = itemView.findViewById(R.id.txt_client_rate_invoice_date_generated)
        timeCreated = itemView.findViewById(R.id.txt_client_rate_invoice_time_generated)
        facilityName = itemView.findViewById(R.id.txt_client_rate_invoice_facility_name)
        serviceName = itemView.findViewById(R.id.txt_client_rate_invoice_service_type)
        //serviceRating = itemView.findViewById(R.id.txt_client_service_rating)
        rateServiceButton = itemView.findViewById(R.id.btn_client_rate_invoice_generated)










    }
}