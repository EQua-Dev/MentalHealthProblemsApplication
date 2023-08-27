package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.client.clientnotification

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R
import com.google.android.material.card.MaterialCardView

class ClientBookingResponseScreenAdapter(itemView: View): RecyclerView.ViewHolder(itemView) {

    var dateCreated: TextView
    var timeCreated: TextView
    var facilityName: TextView
    var serviceName: TextView
    var clientBookingResponseStatusIndicator: CardView
    var clientBookingResponseViewDetailsButton: Button

    init {
        dateCreated = itemView.findViewById(R.id.txt_client_booking_response_date_created)
        timeCreated = itemView.findViewById(R.id.txt_client_booking_response_time_created)
        facilityName = itemView.findViewById(R.id.txt_client_booking_result_facility_name)
        serviceName = itemView.findViewById(R.id.txt_client_booking_result_service_name)
        clientBookingResponseStatusIndicator = itemView.findViewById(R.id.client_booking_response_status_indicator)
        clientBookingResponseViewDetailsButton = itemView.findViewById(R.id.client_notification_view_details_btn)


    }
}