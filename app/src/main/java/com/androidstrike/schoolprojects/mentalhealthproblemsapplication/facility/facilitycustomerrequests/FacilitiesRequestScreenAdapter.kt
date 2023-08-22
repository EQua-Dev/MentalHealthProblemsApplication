package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.facility.facilitycustomerrequests

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R
import com.google.android.material.card.MaterialCardView

class FacilitiesRequestScreenAdapter(itemView: View): RecyclerView.ViewHolder(itemView) {

    var dateCreated: TextView
    var timeCreated: TextView
    var requestDescription: TextView
    var acceptButton: Button
    var rejectButton: Button
    var statusIndicator: MaterialCardView

    init {
        dateCreated = itemView.findViewById(R.id.txt_date_created)
        timeCreated = itemView.findViewById(R.id.txt_time_created)
        requestDescription = itemView.findViewById(R.id.txt_service_description)
        rejectButton = itemView.findViewById(R.id.btn_reject_customer_request)
        acceptButton = itemView.findViewById(R.id.btn_accept_customer_request)
        statusIndicator = itemView.findViewById(R.id.status_indicator)

    }
}