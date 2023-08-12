package com.androidstrike.trackit.client.clientnotification

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
    var responseDescription: TextView
    var facilityName: TextView
    var facilityEmail: TextView
    var facilityPhone: TextView
    var clientBookingResponseStatusIndicator: CardView
    var clientBookingResponseViewDetailsButton: Button
    var clientBookingResponseRateServiceButton: Button

    init {
        dateCreated = itemView.findViewById(R.id.txt_client_booking_response_date_created)
        timeCreated = itemView.findViewById(R.id.txt_client_booking_response_time_created)
        responseDescription = itemView.findViewById(R.id.txt_client_booking_result_description)
        facilityName = itemView.findViewById(R.id.txt_client_booking_result_facility_name)
        facilityEmail = itemView.findViewById(R.id.txt_client_booking_result_facility_email)
        facilityPhone = itemView.findViewById(R.id.txt_client_booking_result_facility_phone)
        clientBookingResponseStatusIndicator = itemView.findViewById(R.id.client_booking_response_status_indicator)
        clientBookingResponseViewDetailsButton = itemView.findViewById(R.id.client_notification_view_details_btn)
        clientBookingResponseRateServiceButton = itemView.findViewById(R.id.client_notification_rate_service_btn)


    }
}