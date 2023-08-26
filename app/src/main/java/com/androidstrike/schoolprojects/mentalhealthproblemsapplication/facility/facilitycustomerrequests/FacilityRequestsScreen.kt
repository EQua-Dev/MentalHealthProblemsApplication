package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.facility.facilitycustomerrequests

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentFacilityRequestsScreenBinding
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.BookService
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Client
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.enable
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.getDate
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.showProgressDialog
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.toast
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FacilityRequestsScreen : Fragment() {

    private var facilitiesRequestScreenAdapter: FirestoreRecyclerAdapter<BookService, FacilitiesRequestScreenAdapter>? =
        null

    private var progressDialog: Dialog? = null
    private var _binding: FragmentFacilityRequestsScreenBinding? = null
    private val binding get() = _binding!!

    lateinit var requestingClient: Client
    lateinit var navController: NavController

    private val calendar = Calendar.getInstance()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFacilityRequestsScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestingClient = Client()
        navController = findNavController()



        getRealtimeRequests()

        with(binding) {
            val layoutManager = LinearLayoutManager(requireContext())
            rvFacilityCustomerRequest.layoutManager = layoutManager
            rvFacilityCustomerRequest.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    layoutManager.orientation
                )
            )
        }
    }

    private fun getRealtimeRequests() {
        val mAuth = FirebaseAuth.getInstance()

        val facilityRequests =
            Common.appointmentsCollectionRef.whereEqualTo("facilityId", mAuth.uid).orderBy("dateBooked", Query.Direction.DESCENDING)
        val options = FirestoreRecyclerOptions.Builder<BookService>()
            .setQuery(facilityRequests, BookService::class.java).build()
        try {
            facilitiesRequestScreenAdapter = object :
                FirestoreRecyclerAdapter<BookService, FacilitiesRequestScreenAdapter>(options) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): FacilitiesRequestScreenAdapter {
                    val itemView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.facility_booking_request_custom_layout, parent, false)
                    return FacilitiesRequestScreenAdapter(itemView)
                }

                override fun onBindViewHolder(
                    holder: FacilitiesRequestScreenAdapter,
                    position: Int,
                    model: BookService
                ) {

//
                    val facilityBaseFragment = parentFragment
//                    val facilityTabLayout =
//                        facilityBaseFragment!!.view?.findViewById<TabLayout>(R.id.facility_base_tab_title)

                    getClientDetails(model, holder.requestDescription)
                    when (model.requestFormStatus) {
                        "pending" -> {
                            holder.statusIndicator.setCardBackgroundColor(Color.YELLOW)
                            holder.acceptButton.setOnClickListener {
                                acceptRequest(model)
                            }
                            holder.rejectButton.setOnClickListener {
                                rejectRequest(model)
                            }
                        }

                        "accepted" -> {
                            holder.statusIndicator.setCardBackgroundColor(resources.getColor(R.color.custom_client_accent_color))
                            holder.acceptButton.apply {
                                text =
                                    resources.getText(R.string.facility_customer_request_generate_invoice)
                                setOnClickListener {
                                    //launchScheduleDialog(model, facilityTabLayout)

                                }
                            }
                            holder.rejectButton.apply {
                                text = resources.getText(R.string.txt_reject_customer_request)
                                setOnClickListener {
                                    rejectRequest(model)
                                }
                            }

                        }

                        "rejected" -> {
                            holder.statusIndicator.setCardBackgroundColor(resources.getColor(R.color.custom_facility_accent_color))
                            holder.rejectButton.apply {
                                text =
                                    resources.getText(R.string.facility_customer_request_rejected)
                                enable(false)
                            }
                            holder.acceptButton.setOnClickListener {
                                acceptRequest(model)

                            }

                        }
                    }
                    holder.dateCreated.text = getDate(model.dateCreated.toLong(), "dd MMMM, yyyy")
                    holder.timeCreated.text = getDate(model.timeCreated.toLong(), "HH:mm a")
                    //holder.requestDescription.text =



                }

            }

        } catch (e: Exception) {
            requireActivity().toast(e.message.toString())
        }
        facilitiesRequestScreenAdapter?.startListening()
        binding.rvFacilityCustomerRequest.adapter = facilitiesRequestScreenAdapter
    }


    private fun launchScheduleDialog(model: BookService, facilityTabLayout: TabLayout?) {

        val builder = layoutInflater.inflate(R.layout.facility_custom_schedule_request_meeting_time, null)

        val etMeetingDate = builder.findViewById<TextInputEditText>(R.id.facility_schedule_meeting_date)
        //val tilMeetingDate = dialog.findViewById<TextInputLayout>(R.id.text_input_layout_facility_schedule_meeting_date)
        val etMeetingTime = builder.findViewById<TextInputEditText>(R.id.facility_schedule_meeting_time)
        //val tilMeetingTime = dialog.findViewById<TextInputLayout>(R.id.text_input_layout_facility_schedule_meeting_time)
        val btnConfirmSchedule = builder.findViewById<Button>(R.id.facility_confirm_meeting_schedule)


        val dialog = AlertDialog.Builder(requireContext())
            .setView(builder)
        .setCancelable(false)
            .create()

        dialog.show()



        btnConfirmSchedule!!.enable(false)

        etMeetingDate!!.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                showDatePicker(view)
            }
        }

        etMeetingTime!!.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                showTimePicker(view)
            }
        }

        etMeetingTime.addTextChangedListener {
            val confirmedMeetingTime = it.toString().trim()
            val confirmedMeetingDate = etMeetingDate.text.toString().trim()

            btnConfirmSchedule.enable(confirmedMeetingDate.isNotEmpty() && confirmedMeetingTime.isNotEmpty())
            btnConfirmSchedule.setOnClickListener {
                scheduleMeeting(confirmedMeetingTime, confirmedMeetingDate, model, facilityTabLayout, dialog)
            }
        }

    }
    private fun scheduleMeeting(
        confirmedMeetingTime: String,
        confirmedMeetingDate: String,
        model: BookService,
        facilityTabLayout: TabLayout?,
        dialog: AlertDialog
    ) = CoroutineScope(Dispatchers.IO).launch {
        val documentRef = Common.appointmentsCollectionRef.document(model.requestFormId)

        val scheduleMessage = "Dear Customer,\n\nThis is to confirm that your request has been accepted for the service ${model.organisationProfileServiceID}. Please come for the initial meeting at $confirmedMeetingTime on $confirmedMeetingDate.\n\nWe will be happy to see you then"

        val schedules = hashMapOf<String, Any>(
            "scheduled" to true,
            "scheduledDate" to confirmedMeetingDate,
            "scheduledTime" to confirmedMeetingTime,
            "dateScheduled" to System.currentTimeMillis().toString(),
            "scheduleMessage" to scheduleMessage
        )

        documentRef.update(schedules)
            .addOnSuccessListener {
                // Update successful
                requireContext().toast("Scheduled")
                dialog.dismiss()
                facilityTabLayout?.getTabAt(3)?.select()

            }
            .addOnFailureListener { e ->
                // Handle error
                requireContext().toast(e.message.toString())
            }

    }


    private fun rejectRequest(model: BookService) = CoroutineScope(Dispatchers.IO).launch {
        val documentRef = Common.appointmentsCollectionRef.document(model.requestFormId)

        val updates = hashMapOf<String, Any>(
            "status" to "rejected",
            "dateResponded" to System.currentTimeMillis().toString()
        )

        documentRef.update(updates)
            .addOnSuccessListener {
                // Update successful
                requireContext().toast("Rejected")

            }
            .addOnFailureListener { e ->
                // Handle error
                requireContext().toast(e.message.toString())
            }

    }

    private fun acceptRequest(model: BookService) = CoroutineScope(Dispatchers.IO).launch {
        val documentRef = Common.appointmentsCollectionRef.document(model.requestFormId)

        val updates = hashMapOf<String, Any>(
            "status" to "accepted",
            "dateResponded" to System.currentTimeMillis().toString()
        )

        documentRef.update(updates)
            .addOnSuccessListener {
                // Update successful
                requireContext().toast("Accepted")
                getRealtimeRequests()

            }
            .addOnFailureListener { e ->
                // Handle error
                requireContext().toast(e.message.toString())
            }

    }

    private fun getClientDetails(model: BookService, requestDescription: TextView) = CoroutineScope(Dispatchers.IO).launch {
        Common.clientCollectionRef
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->

                for (document in querySnapshot.documents) {
                    val item = document.toObject(Client::class.java)
                    if (item?.customerID == model.customerID) {
                        requestingClient = item
                    }
                }
                //requestDescription.text = "Dear Sirs,\nMy name is ${requestingClient.userFirstName} ${requestingClient.userLastName}.\n\nI require your service ${model.typeOfService}, starting from ${model.selectedAppointmentDate} if possible. My contact email is ${requestingClient.userEmail}.\n\nPlease, let me know if you can accomodate my request."
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
                val meetingScheduleDate = view as TextInputEditText
                meetingScheduleDate.setText(formattedDate)
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
                val meetingScheduleTime = view as TextInputEditText
                meetingScheduleTime.setText(formattedTime)
            }, hour, minute, false)

        timePickerDialog.show()
    }

    private fun showProgress() {
        hideProgress()
        progressDialog = requireActivity().showProgressDialog()
    }

    private fun hideProgress() {
        progressDialog?.let { if (it.isShowing) it.cancel() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}