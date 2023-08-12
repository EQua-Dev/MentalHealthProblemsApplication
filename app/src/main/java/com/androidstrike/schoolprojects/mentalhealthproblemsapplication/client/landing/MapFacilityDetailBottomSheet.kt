package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.client.landing

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.BookService
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Client
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Facility
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Service
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Specialists
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.SpecificService
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.SERVICES
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.SPECIALISTS
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.SPECIFIC_SERVICE
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.auth
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.enable
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.getDate
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.showProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.showProgressDialog
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.toast
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.visible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MapFacilityDetailBottomSheet : BottomSheetDialogFragment() {

    private lateinit var selectedAppointmentServiceName: String
    private lateinit var selectedAppointmentServiceID: String
    private lateinit var selectedAppointmentDate: String
    private lateinit var selectedAppointmentTime: String

    //private lateinit var selectedAppointmentDescription: String
    private lateinit var dateBooked: String
    private lateinit var client: String
    //private lateinit var facilityId: String

    private val calendar = Calendar.getInstance()

    private val facilityServices: MutableList<Service> = mutableListOf()
    private val facilityServicesNames: MutableList<String> = mutableListOf()
    private val facilitySpecificServices: MutableList<SpecificService> = mutableListOf()
    private val facilitySpecificServicesNames: MutableList<String> = mutableListOf()
    private val facilitySpecialists: MutableList<Specialists> = mutableListOf()
    private val facilitySpecialistsNames: MutableList<String> = mutableListOf()


    private var progressDialog: Dialog? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.facility_maps_detail_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val facility = arguments?.getParcelable<Facility>(ARG_FACILITY_DATA)

        if (facility != null) {

            getFacilityServiceDetails(facility.facilityId)

            val bottomSheetBookButton = view.findViewById<Button>(R.id.book_btn)
            val bottomSheetFacilityName = view.findViewById<TextView>(R.id.facility_name)
            val bottomSheetFacilityAddress =
                view.findViewById<TextView>(R.id.facility_address)
            val bottomSheetFacilityEmail =
                view.findViewById<TextView>(R.id.facility_email)
            val bottomSheetFacilityPhone =
                view.findViewById<TextView>(R.id.facility_phone)
            val bottomSheetFacilityPhoneImage =
                view.findViewById<ImageView>(R.id.img_map_facility_phone)
            val bottomSheetFacilityDirectionImage =
                view.findViewById<ImageView>(R.id.img_map_facility_direction)
            val bottomSheetFacilityEmailImage =
                view.findViewById<ImageView>(R.id.img_map_facility_email)
            val bottomSheetBookAppointmentServiceTextView =
                view.findViewById<AutoCompleteTextView>(R.id.auto_complete_select_service)
            val bottomSheetBookAppointmentSpecialistsTextView =
                view.findViewById<AutoCompleteTextView>(R.id.auto_complete_select_specialist)
            val bottomSheetBookAppointmentSpecificServiceTextView =
                view.findViewById<AutoCompleteTextView>(R.id.auto_complete_select_specific_service)

//            val bottomSheetBookAppointmentDate =
//                view.findViewById<TextInputEditText>(R.id.book_appointment_date)
//            val bottomSheetBookAppointmentTime =
//                view.findViewById<TextInputEditText>(R.id.book_appointment_time)
//            val bottomSheetBookAppointmentDescription =
//                view.findViewById<TextInputEditText>(R.id.et_book_appointment_description)

            bottomSheetFacilityName.text = facility.facilityName
            bottomSheetFacilityAddress.text = facility.facilityAddress
            bottomSheetFacilityEmail.text = facility.facilityEmail
            bottomSheetFacilityPhone.text = facility.facilityPhoneNumber



            Log.d("EQUA", "onViewCreated: $facilityServicesNames")
            Log.d("EQUA", "onViewCreated: $facilityServices")

            val newRehabServicesArrayAdapter =
                ArrayAdapter(requireContext(), R.layout.drop_down_item, facilityServicesNames)
            bottomSheetBookAppointmentServiceTextView.setAdapter(newRehabServicesArrayAdapter)

            bottomSheetBookAppointmentServiceTextView.setOnItemClickListener { _, _, position, _ ->
                val selectedItem =
                    newRehabServicesArrayAdapter.getItem(position) // Get the selected item

                var serviceId = ""

                for (service in facilityServices)
                    if (selectedItem == service.serviceName)
                        serviceId = service.serviceId

                getFacilitySpecificServiceDetails(facility.facilityId, serviceId)

            }

            val newRehabSpecialistsArrayAdapter =
                ArrayAdapter(requireContext(), R.layout.drop_down_item, facilitySpecialistsNames)
            bottomSheetBookAppointmentSpecialistsTextView.setAdapter(newRehabSpecialistsArrayAdapter)


            val newRehabSpecificServicesArrayAdapter =
                ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_item,
                    facilitySpecificServicesNames
                )
            bottomSheetBookAppointmentSpecificServiceTextView.setAdapter(
                newRehabSpecificServicesArrayAdapter
            )



            bottomSheetBookButton.setOnClickListener {
                var selectedServiceId = ""
                var selectedSpecialistId = ""
                var selectedSpecificServicePrice = ""
                var selectedSpecificServiceDiscountPrice = ""
                var selectedAppointmentServiceAvailablePlaces = ""
                for (service in facilityServices)
                    if (bottomSheetBookAppointmentServiceTextView.text.toString() == service.serviceName)
                        selectedServiceId = service.serviceId
                for (specialist in facilitySpecialists)
                    if (bottomSheetBookAppointmentSpecialistsTextView.text.toString() == specialist.name)
                        selectedSpecialistId = specialist.id
                for (specificService in facilitySpecificServices)
                    if (bottomSheetBookAppointmentSpecificServiceTextView.text.toString() == specificService.specificServiceName) {
                        selectedSpecificServicePrice = specificService.specificServicePrice
                        selectedSpecificServiceDiscountPrice =
                            specificService.specificServiceDiscountedPrice
                        selectedAppointmentServiceAvailablePlaces =
                            specificService.specificServiceAvailablePlacesOption

                    }
                val bookService = BookService(
                    requestFormId = UUID.randomUUID().toString(),
                    clientId = auth.uid!!,
                    facilityId = facility.facilityId,
                    selectedAppointmentServiceID = selectedServiceId,
                    selectedAppointmentServiceName = bottomSheetBookAppointmentServiceTextView.text.toString(),
                    selectedAppointmentSpecialistID = selectedSpecialistId,
                    selectedAppointmentSpecificServiceName = bottomSheetBookAppointmentSpecificServiceTextView.text.toString(),
                    selectedAppointmentServicePrice = selectedSpecificServicePrice,
                    selectedAppointmentServiceDiscountedPrice = selectedSpecificServiceDiscountPrice,
                    selectedAppointmentServiceAvailablePlaces = selectedAppointmentServiceAvailablePlaces,
//                            selectedAppointmentDate = ,
//                            selectedAppointmentRequestText = ,
//                            dateCreated = ,
//                            timeCreated = ,
//                            requestStatus = ,
                )
                launchPlaceRequestDialog(bookService)
//                val mAuth = FirebaseAuth.getInstance()

                // book fragment
                //showProgress()

//                selectedAppointmentServiceName =
//                    bottomSheetBookAppointmentServiceTextView.text.toString().trim()
//                selectedAppointmentDate = bottomSheetBookAppointmentDate.text.toString().trim()
//                selectedAppointmentTime = bottomSheetBookAppointmentTime.text.toString().trim()
//                selectedAppointmentDescription =
//                    bottomSheetBookAppointmentDescription.text.toString().trim()
                //dateBooked = System.currentTimeMillis().toString()
                //client = mAuth.currentUser!!.uid

//
//                for (service in facilityServices) {
//                    if (service.serviceDescription == selectedAppointmentServiceName)
//                        selectedAppointmentServiceID = service.serviceID
//                }

//                bookAppointment(
//                    selectedAppointmentServiceName,
//                    selectedAppointmentServiceID,
//                    selectedAppointmentDate,
//                    selectedAppointmentTime,
//                    //selectedAppointmentDescription,
//                    dateBooked,
//                    client,
//                    facility.facilityId
//                )
            }

            bottomSheetFacilityPhoneImage.setOnClickListener {
                // navigate to phone call
                val dialIntent = Intent(Intent.ACTION_DIAL)
                //dialIntent.data = Uri.fromParts("tel",phoneNumber,null)
                dialIntent.data = Uri.fromParts("tel", facility.facilityPhoneNumber, null)
                startActivity(dialIntent)

            }

            bottomSheetFacilityDirectionImage.setOnClickListener {
                // navigate to maps direction
            }

            bottomSheetFacilityEmailImage.setOnClickListener {
                // navigate to email
            }


        }
    }

    private fun launchPlaceRequestDialog(bookService: BookService) {
        val builder =
            layoutInflater.inflate(
                R.layout.custom_place_request_dialog_layout,
                null
            )
        val dialog = AlertDialog.Builder(requireContext())
            .setView(builder)
            .setCancelable(false)
            .create()

        val tvServiceType =
            builder.findViewById<TextView>(R.id.tv_service_type)
        val tvServiceSpecialistName =
            builder.findViewById<TextView>(R.id.tv_service_specialist_name)
        val tvServiceName =
            builder.findViewById<TextView>(R.id.tv_service_name)
        val tvServiceDetails =
            builder.findViewById<TextView>(R.id.tv_service_details)
        val tvServicePrice =
            builder.findViewById<TextView>(R.id.tv_service_price)
        val tvServiceDiscountPrice =
            builder.findViewById<TextView>(R.id.tv_service_discount_price)
        val tvAvailablePlaces =
            builder.findViewById<TextView>(R.id.tv_available_places)
        val etServiceStartDate =
            builder.findViewById<TextView>(R.id.service_start_date)
        val tvRequestText =
            builder.findViewById<TextView>(R.id.tv_request_text)
        val btnSubmitRequest =
            builder.findViewById<TextView>(R.id.submit_request)


        var specialistName = ""
        for (specialist in facilitySpecialists)
            if (bookService.selectedAppointmentSpecialistID == specialist.id)
                specialistName = specialist.name

        tvServiceType.text =
            resources.getString(R.string.service_type, bookService.selectedAppointmentServiceName)
        tvServiceSpecialistName.text =
            resources.getString(R.string.service_specialist_name, specialistName)
        tvServiceName.text = resources.getString(
            R.string.service_name,
            bookService.selectedAppointmentSpecificServiceName
        )
        tvServiceDetails.text = resources.getString(
            R.string.service_details,
            bookService.selectedAppointmentServiceName,
            bookService.selectedAppointmentSpecificServiceName
        )
        tvServicePrice.text =
            resources.getString(R.string.service_price, bookService.selectedAppointmentServicePrice)
        tvServiceDiscountPrice.text = resources.getString(
            R.string.service_discount_price,
            bookService.selectedAppointmentServiceDiscountedPrice
        )
        tvAvailablePlaces.text = resources.getString(
            R.string.available_places,
            bookService.selectedAppointmentServiceAvailablePlaces
        )

        etServiceStartDate.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                showDatePicker(view)
            }
        }
        var serviceStartDate = ""


        val client = getUser(auth.uid!!)
        val clientName = "${client!!.userFirstName} ${client.userLastName}"
        val clientPhoneNumber = client.userPhoneNumber
        val clientEmailAddress = client.userEmail
        val dateCreated = getDate(System.currentTimeMillis(), "dd-MM-yyyy")
        val timeCreated = getDate(System.currentTimeMillis(), "hh:mm a")

        etServiceStartDate.addTextChangedListener {
            serviceStartDate = it.toString()
            tvRequestText.visible(serviceStartDate.isNotEmpty())

            tvRequestText.text = resources.getString(
                R.string.request_text,
                clientName,
                clientPhoneNumber,
                clientEmailAddress,
                tvServiceDetails.text.toString(),
                serviceStartDate,
                dateCreated,
                timeCreated
            )

            btnSubmitRequest.apply {
                enable(tvRequestText.isVisible)
                setOnClickListener {
                    bookService.selectedAppointmentDate = serviceStartDate
                    bookService.selectedAppointmentRequestText = tvRequestText.text.toString()
                    bookService.dateCreated = dateCreated
                    bookService.timeCreated = timeCreated

                    Log.d("EQUA", "launchPlaceRequestDialog: $bookService")
                    bookAppointment(bookService, dialog)
                }
            }
        }

        dialog.show()

    }


    private fun showDatePicker(view: View) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                // Update the selected date in the calendar instance
                calendar.set(selectedYear, selectedMonth, selectedDay)

                // Perform any desired action with the selected date
                // For example, update a TextView with the selected date
                val formattedDate =
                    SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()).format(calendar.time)
                val bookAppointmentDate = view as TextInputEditText
                bookAppointmentDate.setText(formattedDate)
            }, year, month, day)

        datePickerDialog.show()
    }

    private fun showTimePicker(view: View) {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog =
            TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                // Update the selected time in the calendar instance
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)

                // Perform any desired action with the selected time
                // For example, update a TextView with the selected time
                val formattedTime =
                    SimpleDateFormat("HH:mm a", Locale.getDefault()).format(calendar.time)
                val bookAppointmentTime = view as TextInputEditText
                bookAppointmentTime.setText(formattedTime)
            }, hour, minute, false)

        timePickerDialog.show()
    }

    private fun bookAppointment(bookService: BookService, dialog: AlertDialog) {
        showProgress()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                //val appointmentQuery = Common.facilityCollectionRef.whereEqualTo("facilityId", facility.facilityId).get().await()
                Common.appointmentsCollectionRef.document(bookService.requestFormId).set(bookService)

                withContext(Dispatchers.Main) {
                    hideProgress()
                    dialog.dismiss()
                    dismiss()
                }
                //dismiss bottom sheet

            } catch (e: Exception) {
                requireContext().toast(e.message.toString())
            }
        }
    }

    private fun getFacilityServiceDetails(facilityId: String) {
        showProgress()
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("EQUA", "getFacilityServiceDetails: $facilityId")

            Common.facilityCollectionRef.document(facilityId).collection(SERVICES)
                .get()
                .addOnSuccessListener { querySnapshot: QuerySnapshot ->

                    for (document in querySnapshot.documents) {
                        val item = document.toObject(Service::class.java)

                        if (item != null) {
                            facilityServices.add(item)
                        }
                        Log.d("EQUA", "getFacilityServiceDetails: $facilityServices")
                        for (service in facilityServices) {
                            facilityServicesNames.add(service.serviceName)
                        }



                        Log.d("EQUA", "getFacilityServiceDetails: $facilityServices")

                    }
                    getFacilitySpecialistsDetails(facilityId)

                    hideProgress()
                    //hideProgress()
                }
        }
    }

    private fun getFacilitySpecialistsDetails(facilityId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("EQUA", "getFacilitySpecialistDetails: $facilityId")

            Common.facilityCollectionRef.document(facilityId).collection(SPECIALISTS)
                .get()
                .addOnSuccessListener { querySnapshot: QuerySnapshot ->

                    for (document in querySnapshot.documents) {
                        val item = document.toObject(Specialists::class.java)

                        if (item != null) {
                            facilitySpecialists.add(item)
                        }
                        Log.d("EQUA", "getFacilitySpecialistDetails: $facilityServices")
                        for (specialist in facilitySpecialists) {
                            facilitySpecialistsNames.add(specialist.name)
                        }

                        Log.d("EQUA", "getFacilitySpecialistDetails: $facilityServices")

                    }
                    hideProgress()
                }
        }
    }

    private fun getFacilitySpecificServiceDetails(facilityId: String, serviceId: String) {
        showProgress()
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("EQUA", "getFacilitySpecificServiceDetails: $serviceId")

            Common.facilityCollectionRef.document(facilityId).collection(SERVICES)
                .document(serviceId).collection(SPECIFIC_SERVICE)
                .get()
                .addOnSuccessListener { querySnapshot: QuerySnapshot ->

                    for (document in querySnapshot.documents) {
                        val item = document.toObject(SpecificService::class.java)

                        if (item != null) {
                            facilitySpecificServices.add(item)
                        }
                        Log.d(
                            "EQUA",
                            "getFacilitySpecificServiceDetails: $facilitySpecificServices"
                        )


                    }
                    for (specificService in facilitySpecificServices) {
                        facilitySpecificServicesNames.add(specificService.specificServiceName)
                        Log.d(
                            "EQUA",
                            "getFacilitySpecificServiceDetails: $facilitySpecificServicesNames"
                        )
                    }
                    Log.d("EQUA", "getFacilitySpecificServiceDetails: Here")
                    hideProgress()

                }
        }
    }

    private fun getUser(userId: String): Client? {

        requireContext().showProgress()
        val deferred = CoroutineScope(Dispatchers.IO).async {
            try {
                val snapshot = Common.clientCollectionRef.document(userId).get().await()
                if (snapshot.exists()) {
                    return@async snapshot.toObject(Client::class.java)
                } else {
                    return@async null
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    requireContext().toast(e.message.toString())
                }
                return@async null
            }
        }

        val client = runBlocking { deferred.await() }
        hideProgress()

        return client
    }

    private fun showProgress() {
        hideProgress()
        progressDialog = requireActivity().showProgressDialog()
    }

    private fun hideProgress() {
        progressDialog?.let { if (it.isShowing) it.cancel() }
    }


    companion object {
        private const val ARG_FACILITY_DATA = "arg_facility_data"

        fun newInstance(facility: Facility): MapFacilityDetailBottomSheet {
            val fragment = MapFacilityDetailBottomSheet()
            val args = Bundle().apply {
                putParcelable(ARG_FACILITY_DATA, facility)

            }
            fragment.arguments = args
            return fragment
        }
    }
}