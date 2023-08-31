/*
 * Copyright (c) 2023.
 * Richard Uzor
 * For Afro Connect Technologies
 * Under the Authority of Devstrike Digital Ltd.
 *
 */

package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.facility.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.client.clientfeedback.FeedbackRating
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.facility.facilitynotification.FacilityNotification
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.facility.facilityservicespecialist.FacilityAddServiceSpecialist
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.facility.facilityprofile.FacilityProfile
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.facility.facilityrating.FacilityRating
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.facility.facilityservice.FacilityAddService

/**
 * Created by Richard Uzor  on 28/01/2023
 */
class FacilityLandingPagerAdapter (var context: FragmentActivity?,
                                   fm: FragmentManager,
                                   //var totalTabs: Int
) : FragmentPagerAdapter(fm) {
    override fun getCount(): Int {
        return 5
    }

    //when each tab is selected, define the fragment to be implemented
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                FacilityProfile()
            }
            1 -> {
                FacilityAddServiceSpecialist()
            }
            2 -> {
                FacilityAddService()
            }
            3 -> {
                FacilityNotification()
            }
            4 -> {
                FacilityRating()
            }
            else -> getItem(position)
        }
    }
}