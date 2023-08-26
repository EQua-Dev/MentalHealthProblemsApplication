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
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.SPECIALISTS
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
    private lateinit var facility: Facility

    private val calendar = Calendar.getInstance()

    private val facilityServices: MutableList<Service> = mutableListOf()
    private val facilityServicesTypes: MutableList<String> = mutableListOf()
    private val facilitySpecificServices: MutableList<Service> = mutableListOf()
    private val facilitySpecificServicesNames: MutableList<String> = mutableListOf()
    private val facilitySpecialists: MutableList<Specialists> = mutableListOf()
    private val facilitySpecialistsNames: MutableList<String> = mutableListOf()


    private var progressDialog: Dialog? = null

    private val TAG = "MapFacilityDetailBottomSheet"


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.facility_maps_detail_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        facility = arguments?.getParcelable<Facility>(ARG_FACILITY_DATA)!!

        if (facility != null) {

            getFacilityServiceDetails(facility.organisationId)


        }
    }

    private fun loadView(){
        val bottomSheetBookButton = requireView().findViewById<Button>(R.id.book_btn)
        val bottomSheetFacilityName = requireView().findViewById<TextView>(R.id.facility_name)
        val bottomSheetFacilityAddress =
            requireView().findViewById<TextView>(R.id.facility_address)
        val bottomSheetFacilityEmail =
            requireView().findViewById<TextView>(R.id.facility_email)
        val bottomSheetFacilityPhone =
            requireView().findViewById<TextView>(R.id.facility_phone)
        val bottomSheetFacilityPhoneImage =
            requireView().findViewById<ImageView>(R.id.img_map_facility_phone)
        val bottomSheetFacilityDirectionImage =
            requireView().findViewById<ImageView>(R.id.img_map_facility_direction)
        val bottomSheetFacilityEmailImage =
            requireView().findViewById<ImageView>(R.id.img_map_facility_email)
        val bottomSheetBookAppointmentServiceTextView =
            requireView().findViewById<AutoCompleteTextView>(R.id.auto_complete_select_service)
        val bottomSheetBookAppointmentSpecialistsTextView =
            requireView().findViewById<AutoCompleteTextView>(R.id.auto_complete_select_specialist)
        val bottomSheetBookAppointmentSpecificServiceTextView =
            requireView().findViewById<AutoCompleteTextView>(R.id.auto_complete_select_specific_service)

//            val bottomSheetBookAppointmentDate =
//                view.findViewById<TextInputEditText>(R.id.book_appointment_date)
//            val bottomSheetBookAppointmentTime =
//                view.findViewById<TextInputEditText>(R.id.book_appointment_time)
//            val bottomSheetBookAppointmentDescription =
//                view.findViewById<TextInputEditText>(R.id.et_book_appointment_description)

        bottomSheetFacilityName.text = facility.organisationName
        bottomSheetFacilityAddress.text = facility.organisationPhysicalAddress
        bottomSheetFacilityEmail.text = facility.organisationEmail
        bottomSheetFacilityPhone.text = facility.organisationPhoneNumber



        Log.d("EQUA", "onViewCreated: $facilityServicesTypes")
        Log.d("EQUA", "onViewCreated: $facilityServices")

        val servicesTypesArrayAdapter =
            ArrayAdapter(requireContext(), R.layout.drop_down_item, facilityServicesTypes.distinct())
        bottomSheetBookAppointmentServiceTextView.setAdapter(servicesTypesArrayAdapter)

        bottomSheetBookAppointmentServiceTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedItem =
                servicesTypesArrayAdapter.getItem(position) // Get the selected item

            var serviceId = ""

            for (service in facilityServices)
                if (selectedItem == service.serviceType)
                    facilitySpecificServices.add(service)

            for (specificService in facilitySpecificServices)
                facilitySpecificServicesNames.add(specificService.serviceName)

            //getFacilitySpecificServiceDetails(facility.organisationId, serviceId)

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
                if (bottomSheetBookAppointmentSpecificServiceTextView.text.toString() == service.serviceName)
                    selectedServiceId = service.serviceId
            for (specialist in facilitySpecialists)
                if (bottomSheetBookAppointmentSpecialistsTextView.text.toString() == specialist.name)
                    selectedSpecialistId = specialist.id
            for (specificService in facilitySpecificServices)
                if (bottomSheetBookAppointmentSpecificServiceTextView.text.toString() == specificService.serviceName) {
                    selectedSpecificServicePrice = specificService.servicePrice
                    selectedSpecificServiceDiscountPrice =
                        specificService.serviceDiscountedPrice
                    selectedAppointmentServiceAvailablePlaces =
                        specificService.serviceAvailablePlace

                    selectedAppointmentServiceName =
                        bottomSheetBookAppointmentSpecificServiceTextView.text.toString().trim()

                }
            val bookService = BookService(
                requestFormId = System.currentTimeMillis().toString(),
                customerID = auth.uid!!,
                organisationID = facility.organisationId,
                organisationProfileServiceID = selectedServiceId,
                typeOfService = bottomSheetBookAppointmentServiceTextView.text.toString(),
                typeOfServiceSpecialistID = selectedSpecialistId,
                //selectedAppointmentSpecificServiceName = bottomSheetBookAppointmentSpecificServiceTextView.text.toString(),
//                            selectedAppointmentDate = ,
//                            selectedAppointmentRequestText = ,
//                            dateCreated = ,
//                            timeCreated = ,
//                            requestStatus = ,
            )
            Log.d(TAG, "loadView: $selectedServiceId")
            launchPlaceRequestDialog(bookService, selectedSpecificServicePrice,
                    selectedSpecificServiceDiscountPrice,
                    selectedAppointmentServiceAvailablePlaces, selectedAppointmentServiceName)
//
        }

        bottomSheetFacilityPhoneImage.setOnClickListener {
            // navigate to phone call
            val dialIntent = Intent(Intent.ACTION_DIAL)
            //dialIntent.data = Uri.fromParts("tel",phoneNumber,null)
            dialIntent.data = Uri.fromParts("tel", facility.organisationPhoneNumber, null)
            startActivity(dialIntent)

        }

        bottomSheetFacilityDirectionImage.setOnClickListener {
            // navigate to maps direction
        }

        bottomSheetFacilityEmailImage.setOnClickListener {
            // navigate to email
        }


    }

    private fun launchPlaceRequestDialog(
        bookService: BookService,
        selectedSpecificServicePrice: String,
        selectedSpecificServiceDiscountPrice: String,
        selectedAppointmentServiceAvailablePlaces: String,
        selectedAppointmentServiceName: String
    ) {
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
            if (bookService.typeOfServiceSpecialistID == specialist.id)
                specialistName = specialist.name //fetch the name from the get User function using the ID

        tvServiceType.text =
            resources.getString(R.string.service_type, bookService.typeOfService)
        tvServiceSpecialistName.text =
            resources.getString(R.string.service_specialist_name, specialistName)
        tvServiceName.text = resources.getString(
            R.string.service_name,
            selectedAppointmentServiceName
        )
        tvServiceDetails.text = resources.getString(
            R.string.service_details,
            bookService.typeOfService,
            selectedAppointmentServiceName //fetch the service from a getService function using its ID
        )
        tvServicePrice.text =
            resources.getString(R.string.service_price, selectedSpecificServicePrice)
        tvServiceDiscountPrice.text = resources.getString(
            R.string.service_discount_price,
            selectedSpecificServiceDiscountPrice
        )
        tvAvailablePlaces.text = resources.getString(
            R.string.available_places,
            selectedAppointmentServiceAvailablePlaces
        )

        etServiceStartDate.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                showDatePicker(view)
            }
        }
        var serviceStartDate = ""


        val client = getUser(auth.uid!!)
        val clientName = "${client!!.customerFirstName} ${client.customerLastName}"
        val clientPhoneNumber = client.customerMobileNumber
        val clientEmailAddress = client.customerEmail
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
                "${bookService.typeOfService} ($selectedAppointmentServiceName)",
                //tvServiceDetails.text.toString(),
                serviceStartDate,
                dateCreated,
                timeCreated
            )

            btnSubmitRequest.apply {
                enable(tvRequestText.isVisible)
                setOnClickListener {
                    //bookService.selectedAppointmentDate = serviceStartDate
                    bookService.requestFormText = tvRequestText.text.toString()
                    bookService.dateCreated = dateCreated
                    bookService.timeCreated = timeCreated
                    bookService.requestedStartDate = etServiceStartDate.text.toString()

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

            Common.servicesCollectionRef
                .get()
                .addOnSuccessListener { querySnapshot: QuerySnapshot ->

                    for (document in querySnapshot.documents) {
                        val item = document.toObject(Service::class.java)

                        if (item != null && item.serviceOrganisationOwner == facilityId) {
                            facilityServices.add(item)
                        }
                        Log.d("EQUA", "getFacilityServiceDetails: $facilityServices")

                    }
                    for (service in facilityServices) {
                        facilityServicesTypes.add(service.serviceType)
                    }
                    Log.d("EQUA", "facilityServicesTypes: $facilityServicesTypes")
                    loadView()
                    getFacilitySpecialistsDetails(facilityId)

                    hideProgress()
                    //hideProgress()
                }
        }
    }

    private fun getFacilitySpecialistsDetails(facilityId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("EQUA", "getFacilitySpecialistDetails: $facilityId")

            Common.serviceSpecialistCollectionRef
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

//    private fun getFacilitySpecificServiceDetails(facilityId: String, serviceId: String) {
//        showProgress()
//        CoroutineScope(Dispatchers.IO).launch {
//            Log.d("EQUA", "getFacilitySpecificServiceDetails: $serviceId")
//
//            Common.facilityCollectionRef.document(facilityId).collection(SERVICES)
//                .document(serviceId).collection(SPECIFIC_SERVICE)
//                .get()
//                .addOnSuccessListener { querySnapshot: QuerySnapshot ->
//
//                    for (document in querySnapshot.documents) {
//                        val item = document.toObject(SpecificService::class.java)
//
//                        if (item != null) {
//                            facilitySpecificServices.add(item)
//                        }
//                        Log.d(
//                            "EQUA",
//                            "getFacilitySpecificServiceDetails: $facilitySpecificServices"
//                        )
//
//
//                    }
//                    for (specificService in facilitySpecificServices) {
//                        facilitySpecificServicesNames.add(specificService.specificServiceName)
//                        Log.d(
//                            "EQUA",
//                            "getFacilitySpecificServiceDetails: $facilitySpecificServicesNames"
//                        )
//                    }
//                    Log.d("EQUA", "getFacilitySpecificServiceDetails: Here")
//                    hideProgress()
//
//                }
//        }
//    }

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