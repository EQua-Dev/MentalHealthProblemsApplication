package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.client.clientnotification

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentClientNotificationMeetingScheduleBinding
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Facility
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.ScheduleForm
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Service
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.enable
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.hideProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.showProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.toast
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class ClientNotificationMeetingSchedule : Fragment() {

    private var _binding: FragmentClientNotificationMeetingScheduleBinding? = null
    private val binding get() = _binding!!

    private var clientBookingResponseScheduleAdapter: FirestoreRecyclerAdapter<ScheduleForm, ClientBookingResponseScheduleAdapter>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentClientNotificationMeetingScheduleBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getRealtimeResponses()

        with(binding){
            val layoutManager = LinearLayoutManager(requireContext())
            rvClientBookingResultSchedule.layoutManager = layoutManager
            rvClientBookingResultSchedule.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    layoutManager.orientation
                )
            )
        }
    }

    private fun getRealtimeResponses() {
        val mAuth = FirebaseAuth.getInstance()

        val facilityResponses =
            Common.meetingScheduleFormCollectionRef.whereEqualTo("customerID",mAuth.uid).orderBy("notificationOfAcceptedCustomerRequestScheduleMeetingID", Query.Direction.ASCENDING)
        val options = FirestoreRecyclerOptions.Builder<ScheduleForm>()
            .setQuery(facilityResponses, ScheduleForm::class.java).build()
        try
        {
            clientBookingResponseScheduleAdapter = object : FirestoreRecyclerAdapter<ScheduleForm, ClientBookingResponseScheduleAdapter>(options){
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientBookingResponseScheduleAdapter {
                    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.client_booking_result_schedules_custom_layout, parent, false)
                    return ClientBookingResponseScheduleAdapter(itemView)
                }

                override fun onBindViewHolder(
                    holder: ClientBookingResponseScheduleAdapter,
                    position: Int,
                    model: ScheduleForm
                ) {

                    holder.facilityName.text = getOrganisation(model.organisationID)!!.organisationName
                    holder.serviceName.text = getService(model.organisationProfileServiceID)!!.serviceName

                    holder.clientBookingResponseViewDetailsButton.setOnClickListener {
                            launchDetailDialog(model)

                    }

                }

            }

        }catch (e: Exception){
            requireActivity().toast(e.message.toString())
        }
        clientBookingResponseScheduleAdapter?.startListening()
        binding.rvClientBookingResultSchedule.adapter = clientBookingResponseScheduleAdapter
    }

    private fun launchDetailDialog(model: ScheduleForm) {

        val builder = layoutInflater.inflate(R.layout.client_notification_detail_dialog_layout, null)

        //getServiceDetails(model.organisationID, model.organisationProfileServiceID)

        val tvCompanyName = builder.findViewById<TextView>(R.id.txt_client_booking_response_detail_company_name)
        val tvCompanyPhysicalAddress = builder.findViewById<TextView>(R.id.txt_client_booking_response_detail_company_physical_address)
        val tvCompanyEmail = builder.findViewById<TextView>(R.id.txt_client_booking_response_detail_company_email)
        val tvCompanyContactNumber = builder.findViewById<TextView>(R.id.txt_client_booking_response_detail_company_contact_number)
        val tvNotificationText = builder.findViewById<TextView>(R.id.txt_client_booking_result_detail_notification_text)
        val tvDateCreated = builder.findViewById<TextView>(R.id.txt_client_booking_response_detail_date_created)
        val tvTimeCreated = builder.findViewById<TextView>(R.id.txt_client_booking_response_detail_time_created)

        val btnOkay = builder.findViewById<Button>(R.id.btn_client_booking_response_detail_okay)

        tvDateCreated.enable(false)
        tvTimeCreated.enable(false)

        val organisation = getOrganisation(model.organisationID)!!

        tvCompanyName.text = resources.getString(R.string.facility_generate_request_invoice_company_name, organisation.organisationName)
        tvCompanyPhysicalAddress.text = resources.getString(R.string.facility_generate_request_invoice_company_physical_address, organisation.organisationPhysicalAddress)
        tvCompanyEmail.text = resources.getString(R.string.facility_generate_request_invoice_company_email, organisation.organisationEmail)
        tvCompanyContactNumber.text = resources.getString(R.string.facility_generate_request_invoice_company_contact_number, organisation.organisationPhoneNumber)
        tvNotificationText.text = model.notificationText

//        tvDateCreated.text = getDate(model.dateResponded.toLong(), "dd MMMM, yyyy")
//        tvDateTimeCreated.text = getDate(model.dateResponded.toLong(), "hh:mm a")
//        tvDetailText.text = "Dear Customer,\n\nThis is to confirm that your request has been accepted for service: ${scheduledService.specificServiceName}\n\nPlease come for the initial meeting at:\n\nDate: ${model.scheduledDate}\nTime: ${model.scheduledTime}\n\nWe will be happy to see you there"

        val dialog = AlertDialog.Builder(requireContext())
            .setView(builder)
            .setCancelable(false)
            .create()


        btnOkay.setOnClickListener {
            dialog.dismiss()
        }



        dialog.show()



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
        hideProgress()

        return service
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}