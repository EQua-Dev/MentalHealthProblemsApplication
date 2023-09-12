package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.facility.facilitynotification

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentFacilityNotificationBinding
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.AcceptedRequestInvoice
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.BookService
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Client
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Facility
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.FacilityRequestResponse
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.ScheduleForm
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Service
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.ServiceType
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.ACCEPTED_TEXT
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.PROCESSED_TEXT
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.REJECTED_TEXT
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.auth
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.meetingScheduleFormCollectionRef
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.requestResponseNotificationCollectionRef
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.enable
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.getDate
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.hideProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.showProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.toast
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FacilityNotification : Fragment() {

    private var facilitiesInvoiceNotificationAdapter: FirestoreRecyclerAdapter<BookService, FacilitiesInvoiceNotificationAdapter>? =
        null


    private var _binding: FragmentFacilityNotificationBinding? = null
    private val binding get() = _binding!!
    lateinit var requestingClient: Client
    lateinit var servingFacility: Facility
    lateinit var scheduledService: Service


    private val calendar = Calendar.getInstance()


    private var progressDialog: Dialog? = null

    private val TAG = "FacilityNotification"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFacilityNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        requestingClient = Client()
        servingFacility = Facility()


        getRealtimeInvoice()

        with(binding) {
            val layoutManager = LinearLayoutManager(requireContext())
            rvFacilityInvoiceNotification.layoutManager = layoutManager
            rvFacilityInvoiceNotification.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    layoutManager.orientation
                )
            )
        }

    }

    private fun getRealtimeInvoice() {
        val mAuth = FirebaseAuth.getInstance()

        val facilityScheduledMeetings =
            Common.appointmentsCollectionRef.whereEqualTo("organisationID", mAuth.uid)
                .whereEqualTo("requestFormStatus", "pending")
                .orderBy("requestFormId", Query.Direction.DESCENDING)
        val options = FirestoreRecyclerOptions.Builder<BookService>()
            .setQuery(facilityScheduledMeetings, BookService::class.java).build()
        try {
            facilitiesInvoiceNotificationAdapter = object :
                FirestoreRecyclerAdapter<BookService, FacilitiesInvoiceNotificationAdapter>(options) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): FacilitiesInvoiceNotificationAdapter {
                    val itemView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.facility_custom_invoice_list_layout, parent, false)
                    return FacilitiesInvoiceNotificationAdapter(itemView)
                }

                override fun onBindViewHolder(
                    holder: FacilitiesInvoiceNotificationAdapter,
                    position: Int,
                    model: BookService
                ) {


//                    val facilityBaseFragment = parentFragment
//                    val facilityTabLayout =
//                        facilityBaseFragment!!.view?.findViewById<TabLayout>(R.id.facility_base_tab_title)

                    getClientDetails(model.customerID, holder.clientName)
                    getFacilityDetails(model.organisationID)


                    getServiceDetails(model.organisationID, model.organisationProfileServiceID)
                    holder.clientName.text = resources.getString(
                        R.string.client_name,
                        getUser(model.customerID)!!.customerFirstName,
                        getUser(model.customerID)!!.customerLastName
                    )
                    holder.serviceName.text =
                        getService(model.organisationProfileServiceID)!!.serviceName
                    Log.d(
                        TAG,
                        "onBindViewHolder: ${getService(model.organisationProfileServiceID)!!.serviceName}"
                    )
                    holder.dateCreated.text = model.dateCreated
                    //getDate(model.dateCreated.toLong(), "EE, dd MMMM, yyyy")
                    holder.timeCreated.text = model.timeCreated

                    holder.viewRequestBtn.setOnClickListener {
                        launchRequestDetailDialog(model)
                    }

                }

            }

        } catch (e: Exception) {
            requireActivity().toast(e.message.toString())
        }
        facilitiesInvoiceNotificationAdapter?.startListening()
        binding.rvFacilityInvoiceNotification.adapter = facilitiesInvoiceNotificationAdapter
    }

    private fun launchRequestDetailDialog(model: BookService) {

        val builder =
            layoutInflater.inflate(
                R.layout.facility_custom_view_request_detail_dialog_layout,
                null
            )
        val dialog = AlertDialog.Builder(requireContext())
            .setView(builder)
            .setCancelable(false)
            .create()

        val tvClientName =
            builder.findViewById<TextView>(R.id.facility_request_customer_name)
        val tvClientContactName =
            builder.findViewById<TextView>(R.id.facility_request_customer_contact_number)
        val tvClientEmail =
            builder.findViewById<TextView>(R.id.facility_request_customer_email)
        val tvChosenServiceName =
            builder.findViewById<TextView>(R.id.facility_request_chosen_service_name)
        val tvChosenServiceType =
            builder.findViewById<TextView>(R.id.facility_request_chosen_service_type)
        val tvRequestStartDate =
            builder.findViewById<TextView>(R.id.facility_request_start_date)
        val tvRequestServicePrice =
            builder.findViewById<TextView>(R.id.facility_request_service_price)
        val tvRequestServiceDiscountedPrice =
            builder.findViewById<TextView>(R.id.facility_request_service_discounted_price)
        val tvRequestText =
            builder.findViewById<TextView>(R.id.facility_request_text)
        val tvRequestedDateCreated =
            builder.findViewById<TextView>(R.id.facility_request_date_created)
        val tvRequestedTimeCreated =
            builder.findViewById<TextView>(R.id.facility_request_time_created)
        val btnAcceptRequest =
            builder.findViewById<Button>(R.id.btn_facility_accept_customer_request)
        val btnRejectRequest =
            builder.findViewById<TextView>(R.id.btn_facility_reject_customer_request)

        val client = getUser(model.customerID)!!
        val service = getService(model.organisationProfileServiceID)!!
        val serviceType = getServiceType(model.typeOfService)!!

        tvClientName.text = resources.getString(
            R.string.requesting_client_name,
            client.customerFirstName,
            client.customerLastName
        )
        tvClientContactName.text =
            resources.getString(
                R.string.requesting_client_contact_number,
                client.customerMobileNumber
            )
        tvClientEmail.text =
            resources.getString(R.string.requesting_client_email, client.customerEmail)
        tvChosenServiceName.text =
            resources.getString(R.string.requesting_chosen_service_name, service.serviceName)
        tvChosenServiceType.text = resources.getString(
            R.string.requesting_chosen_service_type,
            serviceType.serviceTypeName
        )
        tvRequestStartDate.text =
            resources.getString(R.string.requesting_start_date, model.requestedStartDate)
        tvRequestServicePrice.text =
            resources.getString(R.string.requesting_service_price, service.servicePrice)
        tvRequestServiceDiscountedPrice.text = resources.getString(
            R.string.requesting_service_discounted_price,
            service.serviceDiscountedPrice
        )
        tvRequestText.text = model.requestFormText
        tvRequestedDateCreated.text =
            resources.getString(R.string.requesting_date_created, model.dateCreated)
        tvRequestedTimeCreated.text =
            resources.getString(R.string.requesting_time_created, model.timeCreated)
        btnAcceptRequest.setOnClickListener {
            val currentDate = getDate(System.currentTimeMillis(), "dd-MM-yyyy")
            val currentTime = getDate(System.currentTimeMillis(), "hh:mm a")
            //add to db
            val facilityRequestResponse = FacilityRequestResponse(
                notificationID = System.currentTimeMillis().toString(),
                requestFormID = model.requestFormId,
                customerID = model.customerID,
                organisationID = model.organisationID,
                organisationProfileServiceID = model.organisationProfileServiceID,
                typeOfServiceID = model.typeOfService,
                typeOfServiceSpecialistID = model.typeOfServiceSpecialistID,
                requestFormStatus = ACCEPTED_TEXT,
                notificationText = resources.getString(
                    R.string.notification_text,
                    client.customerFirstName,
                    service.serviceName,
                    ACCEPTED_TEXT,
                    currentDate,
                    currentTime
                ),
                dateCreated = currentDate,
                timeCreated = currentTime
            )
            requireContext().showProgress()
            Common.appointmentsCollectionRef.document(model.requestFormId)
                .update("requestFormStatus", ACCEPTED_TEXT).addOnSuccessListener {
                    hideProgress()
                    createResponseNotification(facilityRequestResponse, model, dialog)
                }.addOnFailureListener { e ->
                    hideProgress()
                    requireContext().toast(e.message.toString())
                }


        }
        btnRejectRequest.setOnClickListener {
            //add to db
            requireContext().showProgress()
            Common.appointmentsCollectionRef.document(model.requestFormId)
                .update("requestFormStatus", REJECTED_TEXT).addOnSuccessListener {
                    hideProgress()
                    dialog.dismiss()
                }.addOnFailureListener { e ->
                    requireContext().toast(e.message.toString())
                }
        }


        dialog.show()

    }

    private fun createResponseNotification(
        facilityRequestResponse: FacilityRequestResponse,
        model: BookService,
        dialog: AlertDialog
    ) {
        requireContext().showProgress()
        CoroutineScope(Dispatchers.IO).launch {
            requestResponseNotificationCollectionRef.document(facilityRequestResponse.notificationID)
                .set(facilityRequestResponse).addOnSuccessListener {
                    hideProgress()
                    dialog.dismiss()
                    if (getServiceType(model.typeOfService)!!.serviceTypeName == "Meeting with Medical Professional")
                        launchScheduleDialog(model, facilityRequestResponse)
                    else
                        launchInvoiceDialog(model, facilityRequestResponse)
                }.addOnFailureListener { e ->
                    hideProgress()
                    requireContext().toast(e.message.toString())
                }
        }
    }

    private fun launchScheduleDialog(
        model: BookService,
        facilityRequestResponse: FacilityRequestResponse
    ) {

        val builder =
            layoutInflater.inflate(
                R.layout.facility_custom_schedule_request_meeting_time,
                null
            )
        val dialog = AlertDialog.Builder(requireContext())
            .setView(builder)
            .setCancelable(false)
            .create()

        val etScheduleDate =
            builder.findViewById<TextView>(R.id.facility_schedule_meeting_date)
        val etScheduleTime =
            builder.findViewById<TextView>(R.id.facility_schedule_meeting_time)
        val btnConfirmSchedule =
            builder.findViewById<TextView>(R.id.facility_confirm_meeting_schedule)

        btnConfirmSchedule.enable(false)

        etScheduleDate.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                showDatePicker(view)
            }
        }
        etScheduleTime.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                showTimePicker(view)
            }
        }

        etScheduleTime.addTextChangedListener {
            var scheduledDate = etScheduleDate.text.toString().trim()
            var scheduledTime = it.toString().trim()

            val client = getUser(facilityRequestResponse.customerID)!!
            val service = getService(facilityRequestResponse.organisationProfileServiceID)!!

            btnConfirmSchedule.apply {
                enable(scheduledDate.isNotEmpty() && scheduledTime.isNotEmpty())
                setOnClickListener {
                    //add to the db
                    val meetingScheduleData = ScheduleForm(
                        notificationOfAcceptedCustomerRequestScheduleMeetingID = System.currentTimeMillis().toString(),
                                notificationOfCustomerRequestFormID = facilityRequestResponse.notificationID,
                                requestFormID = facilityRequestResponse.requestFormID,
                                customerID = facilityRequestResponse.customerID,
                        organisationID = facilityRequestResponse.organisationID,
                                organisationProfileServiceID = facilityRequestResponse.organisationProfileServiceID,
                                serviceType = facilityRequestResponse.typeOfServiceID,
                                requestFormStatus = facilityRequestResponse.requestFormStatus,
                                scheduledMeetingDate = scheduledDate,
                                scheduledMeetingTime = scheduledTime,
                                notificationText = resources.getString(R.string.scheduled_meeting_text, client.customerFirstName, service.serviceName, scheduledDate, scheduledTime)
                    )
                    meetingScheduleFormCollectionRef.document(meetingScheduleData.notificationOfAcceptedCustomerRequestScheduleMeetingID).set(meetingScheduleData).addOnSuccessListener {
                        launchInvoiceDialog(model, facilityRequestResponse)
                        dialog.dismiss()
                    }.addOnFailureListener { e ->
                        requireContext().toast(e.message.toString())
                    }
                }
            }
        }

        dialog.show()


    }


    private fun launchInvoiceDialog(
        model: BookService,
        facilityRequestResponse: FacilityRequestResponse
    ) {

        val builder = layoutInflater.inflate(R.layout.facility_custom_generate_invoice_layout, null)

        val tvFacilityName =
            builder.findViewById<TextView>(R.id.facility_generated_invoice_facility_name)
        val tvFacilityAddress =
            builder.findViewById<TextView>(R.id.facility_generated_invoice_facility_address)
        val tvFacilityPhone =
            builder.findViewById<TextView>(R.id.facility_generated_invoice_facility_phone_number)
        val tvFacilityEmail =
            builder.findViewById<TextView>(R.id.facility_generated_invoice_facility_email)
        val tvClientName =
            builder.findViewById<TextView>(R.id.facility_generated_invoice_customer_name)
        val tvClientPhone =
            builder.findViewById<TextView>(R.id.facility_generated_invoice_customer_phone)
        val tvClientEmail =
            builder.findViewById<TextView>(R.id.facility_generated_invoice_customer_email)
        val tvServiceName =
            builder.findViewById<TextView>(R.id.facility_generated_invoice_request_service_name)
        val tvServicePrice =
            builder.findViewById<TextView>(R.id.facility_generated_invoice_request_service_price)
        val tvServiceDiscountedPrice =
            builder.findViewById<TextView>(R.id.facility_generated_invoice_request_service_discounted_price)
        val tvServiceTotal =
            builder.findViewById<TextView>(R.id.facility_generated_invoice_request_service_total_price)

        val btnConfirmInvoice =
            builder.findViewById<Button>(R.id.facility_generated_invoice_confirm_button)


        val organisation = getOrganisation(model.organisationID)!!
        val client = getUser(model.customerID)!!
        val service = getService(model.organisationProfileServiceID)!!


        tvFacilityName.text = resources.getString(
            R.string.facility_generate_request_invoice_company_name,
            organisation.organisationName
        )
        tvFacilityAddress.text = resources.getString(
            R.string.facility_generate_request_invoice_company_physical_address,
            organisation.organisationPhysicalAddress
        )
        tvFacilityPhone.text = resources.getString(
            R.string.facility_generate_request_invoice_company_contact_number,
            organisation.organisationPhoneNumber
        )
        tvFacilityEmail.text = resources.getString(
            R.string.facility_generate_request_invoice_company_email,
            organisation.organisationEmail
        )
        tvClientName.text = resources.getString(
            R.string.facility_generate_request_invoice_customer_name,
            client.customerFirstName,
            client.customerLastName
        )
        tvClientPhone.text = resources.getString(
            R.string.facility_generate_request_invoice_customer_contact_number,
            client.customerMobileNumber
        )
        tvClientEmail.text = resources.getString(
            R.string.facility_generate_request_invoice_customer_email_address,
            client.customerEmail
        )
        tvServiceName.text = resources.getString(
            R.string.facility_generate_request_invoice_service_name,
            service.serviceName
        )
        tvServicePrice.text = resources.getString(
            R.string.facility_generate_request_invoice_service_price,
            service.servicePrice
        )
        tvServiceDiscountedPrice.text = resources.getString(
            R.string.facility_generate_request_invoice_service_discounted_price,
            service.serviceDiscountedPrice
        )

//        val servicePrice = scheduledService.servicePrice
//        val serviceDiscountedPrice = scheduledService.serviceDiscountedPrice
        val totalServicePrice =
            if (service.servicePrice.toDouble() < service.serviceDiscountedPrice.toDouble())
                service.servicePrice.toDouble()
            else
                service.serviceDiscountedPrice.toDouble()
        //service.servicePrice.toDouble() - service.serviceDiscountedPrice.toDouble()
        tvServiceTotal.text = resources.getString(
            R.string.facility_generate_request_invoice_service_total_price,
            totalServicePrice.toString()
        )

//        btnConfirmInvoice.setOnClickListener {
//            requireContext().toast("Confirmed")
//        }


        val dialog = AlertDialog.Builder(requireContext())
            .setView(builder)
            .setCancelable(false)
            .create()

        dialog.show()

        btnConfirmInvoice.setOnClickListener {
            dialog.dismiss()
            launchInvoiceRecordDialog(model, totalServicePrice, facilityRequestResponse)
        }


    }

    private fun launchInvoiceRecordDialog(
        model: BookService,
        totalServicePrice: Double,
        facilityRequestResponse: FacilityRequestResponse
    ) {

        val builder = layoutInflater.inflate(R.layout.facility_custom_new_invoice_record, null)

        var invoiceBankName: String = ""
        var invoiceAccountIBAN: String = ""
        var invoiceAccountName: String = ""

        val tilBankName =
            builder.findViewById<TextInputLayout>(R.id.text_input_layout_facility_new_record_bank_name)
        val etBankName = builder.findViewById<TextInputEditText>(R.id.facility_new_record_bank_name)
        val tilAccountIBAN =
            builder.findViewById<TextInputLayout>(R.id.text_input_layout_facility_new_record_account_iban)
        val etAccountIBAN =
            builder.findViewById<TextInputEditText>(R.id.facility_new_record_account_iban)
        val tilAccountHolder =
            builder.findViewById<TextInputLayout>(R.id.text_input_layout_facility_new_record_account_holder_name)
        val etAccountHolder =
            builder.findViewById<TextInputEditText>(R.id.facility_new_record_account_holder_name)

        val btnConfirmInvoiceRecord =
            builder.findViewById<Button>(R.id.facility_new_record_confirm_btn)
        val btnCancelInvoiceRecord =
            builder.findViewById<Button>(R.id.facility_new_record_cancel_btn)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(builder)
            .setCancelable(false)
            .create()

        btnConfirmInvoiceRecord.setOnClickListener {
            tilBankName.error = null
            tilAccountIBAN.error = null
            tilAccountHolder.error = null

            invoiceBankName = etBankName.text.toString().trim()
            invoiceAccountIBAN = etAccountIBAN.text.toString().trim()
            invoiceAccountName = etAccountHolder.text.toString().trim()

            if (invoiceBankName.isEmpty()) {
                tilBankName.error = "Bank name required"
                etBankName.requestFocus()
            }
            if (invoiceAccountIBAN.isEmpty()) {
                tilAccountIBAN.error = "Account IBAN required"
                etAccountIBAN.requestFocus()
            }
            if (invoiceAccountName.isEmpty()) {
                tilAccountHolder.error = "Account holder name required"
            } else {
                tilBankName.error = null
                tilAccountIBAN.error = null
                tilAccountHolder.error = null

                val client = getUser(model.customerID)!!
                val service = getService(model.organisationProfileServiceID)!!

                val currentDate = getDate(System.currentTimeMillis(), "dd-MM-yyyy")
                val currentTime = getDate(System.currentTimeMillis(), "hh:mm a")

                val acceptedRequestInvoice = AcceptedRequestInvoice(
                    invoiceID = System.currentTimeMillis().toString(),
                    notificationID = facilityRequestResponse.notificationID,
                    customerRequestFormID = facilityRequestResponse.requestFormID,
                    customerID = facilityRequestResponse.customerID,
                    organisationID = facilityRequestResponse.organisationID,
                    organisationProfileServiceID = facilityRequestResponse.organisationProfileServiceID,
                    typeOfServiceID = facilityRequestResponse.typeOfServiceID,
                    typeOfServiceSpecialistID = facilityRequestResponse.typeOfServiceSpecialistID,
                    invoiceAmount = totalServicePrice.toString(),
                    bankName = invoiceBankName,
                    bankAccountIBAN = invoiceAccountIBAN,
                    bankAccountHolderName = invoiceAccountName,
                    invoiceText = resources.getString(
                        R.string.invoice_text,
                        client.customerFirstName,
                        service.serviceName,
                        PROCESSED_TEXT,
                        invoiceBankName,
                        invoiceAccountIBAN,
                        invoiceAccountName,
                    ),
                    dateCreated = currentDate,
                    timeCreated = currentTime
                )
                uploadInvoiceRecord(
                    acceptedRequestInvoice,
                    dialog
                )
            }

        }

        btnCancelInvoiceRecord.setOnClickListener {
            etBankName.setText("")
            etAccountIBAN.setText("")
            etAccountHolder.setText("")
        }

        dialog.show()

    }

    private fun uploadInvoiceRecord(
        acceptedRequestInvoice: AcceptedRequestInvoice,
        dialog: AlertDialog
    ) {
        requireContext().showProgress()
        CoroutineScope(Dispatchers.IO).launch {
            Common.acceptedRequestInvoiceCollectionRef.document(acceptedRequestInvoice.invoiceID)
                .set(acceptedRequestInvoice).addOnSuccessListener {
                    hideProgress()
                    requireContext().toast("Invoice Generated")
                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    // Handle error
                    hideProgress()
                    requireContext().toast(e.message.toString())
                }

        }
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


    private fun getClientDetails(clientId: String, clientName: TextView) =
        CoroutineScope(Dispatchers.IO).launch {
            Common.clientCollectionRef
                .get()
                .addOnSuccessListener { querySnapshot: QuerySnapshot ->

                    for (document in querySnapshot.documents) {
                        val item = document.toObject(Client::class.java)
                        if (item?.customerID == clientId) {
                            requestingClient = item
                        }
                    }
                    clientName.text =
                        "${requestingClient.customerFirstName}, ${requestingClient.customerLastName}"
                }
        }

    private fun getFacilityDetails(
        facilityId: String
    ) = CoroutineScope(Dispatchers.IO).launch {
        Common.facilityCollectionRef
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->

                for (document in querySnapshot.documents) {
                    val item = document.toObject(Facility::class.java)
                    if (item?.organisationId == facilityId) {
                        servingFacility = item
                    }
                }
//                tvFacilityName.text = servingFacility.facilityName
//                tvFacilityAddress.text = servingFacility.facilityAddress
//                tvFacilityEmail.text = servingFacility.facilityEmail
//                tvFacilityPhone.text = servingFacility.facilityPhoneNumber
            }
    }

    private fun getServiceDetails(facilityId: String, serviceId: String) =
        CoroutineScope(Dispatchers.IO).launch {
            Common.servicesCollectionRef
                .get()
                .addOnSuccessListener { querySnapshot: QuerySnapshot ->

                    for (document in querySnapshot.documents) {
                        val item = document.toObject(Service::class.java)
                        if (item?.serviceId == serviceId) {
                            scheduledService = item
                        }
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

        val clientUser = runBlocking { deferred.await() }
        com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.hideProgress()

        return clientUser
    }

    private fun getOrganisation(organisationId: String): Facility? {
        requireContext().showProgress()
        val deferred = CoroutineScope(Dispatchers.IO).async {
            try {
                val snapshot = Common.facilityCollectionRef.document(organisationId).get().await()
                if (snapshot.exists()) {
                    return@async snapshot.toObject(Facility::class.java)
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

        val organisation = runBlocking { deferred.await() }
        hideProgress()

        Log.d(TAG, "getOrganisation: $organisation")

        return organisation
    }

    private fun getService(serviceId: String): Service? {
        requireContext().showProgress()
        val deferred = CoroutineScope(Dispatchers.IO).async {
            try {
                val snapshot = Common.servicesCollectionRef.document(serviceId).get().await()
                if (snapshot.exists()) {
                    return@async snapshot.toObject(Service::class.java)
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

        val service = runBlocking { deferred.await() }
        com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.hideProgress()

        //Log.d(TAG, "getService: ${service!!.serviceName}")
        return service
    }

    private fun getServiceType(serviceTypeId: String): ServiceType? {
        requireContext().showProgress()
        val deferred = CoroutineScope(Dispatchers.IO).async {
            try {
                val snapshot =
                    Common.servicesTypeCollectionRef.document(serviceTypeId).get().await()
                if (snapshot.exists()) {
                    return@async snapshot.toObject(ServiceType::class.java)
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

        val serviceType = runBlocking { deferred.await() }
        com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.hideProgress()

        return serviceType
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}