package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Facility(
    val organisationName: String = "",
    val organisationEmail: String = "",
    val organisationPhysicalAddress: String = "",
    val organisationLongitude: String = "",
    val organisationLatitude: String = "",
    val organisationPhoneNumber: String = "",
    val organisationId: String = "",
    val dateJoined: String = "",
    val role: String = "facility",
    //val services: List<String> = listOf()
): Parcelable

