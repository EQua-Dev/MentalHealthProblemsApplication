/*
 * Copyright (c) 2022.
 * Richard Uzor
 * For Afro Connect Technologies
 * Under the Authority of Devstrike Digital Ltd.
 *
 */

package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R
import com.google.android.material.snackbar.Snackbar
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

/**
 * this is a file that contains definitions of various ui operations that could be used in multiple places
 * the functions in here define the operations and allow simplified form passing only the required parameter
 * Created by Richard Uzor  on 15/12/2022
 */


//toast function
fun Context.toast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

var progressDialog: Dialog? = null

fun Context.showProgress() {
    hideProgress()
    progressDialog = this.showProgressDialog()
}

fun hideProgress() {
    progressDialog?.let { if (it.isShowing) it.cancel() }
}


//snack bar function
fun View.snackBar(message: String, action: (() -> Unit)? = null) {
    val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_LONG).setTextColor(resources.getColor(R.color.red)).setBackgroundTint(Color.TRANSPARENT)
    action?.let {
        snackbar.setAction("Okay") {
            it()
        }
    }
    snackbar.show()
}
fun View.loginSnackBar(message: String, action: (() -> Unit)? = null) {
    val snackBar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)
    action?.let {
        snackBar.setAction("Login") {
            it()
        }
    }
    snackBar.show()
}


//common function to handle progress bar visibility
fun View.visible(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}


//common function to handle all intent activity launches
fun <A : Activity> Activity.startNewActivity(activity: Class<A>) {
    Intent(this, activity).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }
}

//common function to handle enabling the views (buttons)
fun View.enable(enabled: Boolean) {
    isEnabled = enabled
    isClickable = enabled
    alpha = if (enabled) 1f else 0.5f
}



fun Context.showProgressDialog(): Dialog {
    val progressDialog = Dialog(this)

    progressDialog.let {
        it.show()
        it.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        it.setContentView(R.layout.loading_progress_dialog)
        it.setCancelable(false)
        it.setCanceledOnTouchOutside(false)
        return it
    }
}

//function to change milliseconds to date format
fun getDate(milliSeconds: Long?, dateFormat: String?): String {
    // Create a DateFormatter object for displaying date in specified format.
    val formatter = SimpleDateFormat(dateFormat)

    // Create a calendar object that will convert the date and time value in milliseconds to date.
    val calendar: Calendar? = Calendar.getInstance()
    calendar?.timeInMillis = milliSeconds!!
    return formatter.format(calendar?.time!!)
}

fun convertISODateToMonthAndYear(isoDate: String?): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    val date = dateFormat.parse(isoDate!!)
    val monthAndYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    return monthAndYearFormat.format(date!!)
}

fun convertISODateToMonthYearAndTime(isoDate: String?): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    val date = dateFormat.parse(isoDate!!)
    val monthAndYearFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    val hourAndMinuteFormat = SimpleDateFormat("HH:mm a", Locale.getDefault())
    val monthDate = monthAndYearFormat.format(date!!)
    val hourDate = hourAndMinuteFormat.format(date)
    return "$monthDate by $hourDate"
}

fun convertMillisToISODate(milliseconds: Long?){
    val date = Date(milliseconds!!)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    val iso8601 = dateFormat.format(date)

}

//fun getYearFromISODate(isoDate: String?): Common.CalendarDetails {
//    //val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
//    //val date = dateFormat.parse(isoDate!!)
//    //val iso8601Date = "2023-04-06T23:00:30.000Z"
//    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
//    formatter.timeZone = TimeZone.getTimeZone("UTC")
//    val date = formatter.parse(isoDate!!)
//    val milliseconds = date!!.time
//
//    val calendar = Calendar.getInstance()
//    calendar.timeInMillis = milliseconds
//
//    return Common.CalendarDetails(
//        calendar.get(Calendar.YEAR),
//        calendar.get(Calendar.MONTH),
//        calendar.get(Calendar.DAY_OF_MONTH),
//        calendar.get(Calendar.DAY_OF_WEEK),
//        calendar.get(Calendar.HOUR),
//        calendar.get(Calendar.MINUTE),
//        calendar.get(Calendar.SECOND),
//    )
//
//}

fun getMonthFromISODate(isoDate: String?): Int{
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    val date = dateFormat.parse(isoDate!!)
    val calendar = Calendar.getInstance()
    calendar.time = date!!
    return calendar.get(Calendar.MONTH)
}

fun convertISODateToMillis(isoDate: String): Long {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    val date = dateFormat.parse(isoDate)
    return date!!.time
}

fun hashString(input: String): String {
    val messageDigest = MessageDigest.getInstance("MD5")
    val bytes = messageDigest.digest(input.toByteArray())
    val stringBuilder = StringBuilder()

    for (byte in bytes) {
        // Convert each byte to a hex string
        stringBuilder.append(String.format("%02x", byte))
    }

    return stringBuilder.toString()
}


