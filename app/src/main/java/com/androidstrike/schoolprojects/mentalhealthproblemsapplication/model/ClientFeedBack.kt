package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model

data class ClientFeedBack(
    val feedbackAndRatingsID: String = "",
    val organisationID: String = "",
    val customerID: String = "",
    val organisationProfileService: String = "",
    val serviceType: String = "",
    val feedBackText: String = "",
    val ratings: String = "",
    val dateCreated: String = "",
    val timeCreated: String = ""
)
