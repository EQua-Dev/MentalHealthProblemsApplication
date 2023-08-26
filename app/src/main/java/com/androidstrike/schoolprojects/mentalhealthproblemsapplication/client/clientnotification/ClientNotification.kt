package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.client.clientnotification

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentClientNotificationBinding
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.BookService
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Facility
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.SpecificService
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.snackBar
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.toast
import com.androidstrike.trackit.client.clientnotification.ClientBookingResponseScreenAdapter
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClientNotification : Fragment() {

    var clientBookingResponseScreenAdapter: FirestoreRecyclerAdapter<BookService, ClientBookingResponseScreenAdapter>? = null

    private var progressDialog: Dialog? = null
    lateinit var respondingFacility : Facility
    lateinit var scheduledService: SpecificService



    private var _binding: FragmentClientNotificationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentClientNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        respondingFacility = Facility()
        scheduledService = SpecificService()
        getRealtimeResponses()

        with(binding){
            val layoutManager = LinearLayoutManager(requireContext())
            rvClientBookingResult.layoutManager = layoutManager
            rvClientBookingResult.addItemDecoration(
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
            Common.appointmentsCollectionRef.whereEqualTo("clientId",mAuth.uid).orderBy("dateResponded", Query.Direction.ASCENDING)
        val options = FirestoreRecyclerOptions.Builder<BookService>()
            .setQuery(facilityResponses, BookService::class.java).build()
        try
        {
            clientBookingResponseScreenAdapter = object : FirestoreRecyclerAdapter<BookService, ClientBookingResponseScreenAdapter>(options){
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientBookingResponseScreenAdapter {
                    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.client_booking_result_custom_layout, parent, false)
                    return ClientBookingResponseScreenAdapter(itemView)
                }

                override fun onBindViewHolder(
                    holder: ClientBookingResponseScreenAdapter,
                    position: Int,
                    model: BookService
                ) {


//                    val clientBaseFragment = parentFragment
//                    val clientTabLayout =
//                        clientBaseFragment!!.view?.findViewById<TabLayout>(R.id.client_base_tab_title)


                    Log.d("EQUA", "onBindViewHolder: ${model.customerID}")
                    var responseState: String = ""
//                    when(model.status){
//                        "pending" ->{
//                            holder.clientBookingResponseStatusIndicator.setCardBackgroundColor(Color.YELLOW)
//                            responseState = "pending"
//                        }
//                        "accepted" ->{
//                            holder.clientBookingResponseStatusIndicator.setCardBackgroundColor(Color.GREEN)
//                            responseState = "accepted"
//
//                        }
//                        "rejected" ->{
//                            holder.clientBookingResponseStatusIndicator.setCardBackgroundColor(Color.RED)
//                            responseState = "rejected"
//
//                        }
//                    }
//                    holder.dateCreated.text = getDate(model.dateBooked.toLong(), "dd MMMM, yyyy")
//                    holder.timeCreated.text = getDate(model.dateBooked.toLong(), "HH:mm a")
                    holder.responseDescription.text = "Dear Customer,\n\nYour request is $responseState."

                    getFacilityDetails(model.organisationID, holder.facilityName, holder.facilityEmail, holder.facilityPhone )

                    holder.clientBookingResponseViewDetailsButton.setOnClickListener {
                        if(responseState == "accepted")
                            launchDetailDialog(model)
                        else
                            requireView().snackBar("Your request is still pending")
                    }

                    holder.clientBookingResponseRateServiceButton.setOnClickListener {
//                        val ratingsTab = clientTabLayout?.getTabAt(4)
//                        ratingsTab?.let {
//                            val fragment = clientBaseFragment as Fragment // Replace with your own fragment class
//                            val args = Bundle()
//                            args.putString("serviceId", model.selectedAppointmentServiceID)
//                            fragment.arguments = args
//                        }
//                        Common.serviceToRate = model.selectedAppointmentServiceID
//                        clientTabLayout?.getTabAt(4)?.select()
                    }

                }

            }

        }catch (e: Exception){
            requireActivity().toast(e.message.toString())
        }
        clientBookingResponseScreenAdapter?.startListening()
        binding.rvClientBookingResult.adapter = clientBookingResponseScreenAdapter
    }

    private fun launchDetailDialog(model: BookService) {

        val builder = layoutInflater.inflate(R.layout.client_notification_detail_dialog_layout, null)

        getServiceDetails(model.organisationID, model.organisationProfileServiceID)

        val tvDateCreated = builder.findViewById<TextView>(R.id.txt_client_booking_response_detail_date_created)
        val tvDateTimeCreated = builder.findViewById<TextView>(R.id.txt_client_booking_response_detail_time_created)
        val tvDetailText = builder.findViewById<TextView>(R.id.txt_client_booking_result_detail_description)

        val btnOkay = builder.findViewById<Button>(R.id.btn_client_booking_response_detail_okay)

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

    private fun getServiceDetails(facilityId: String, serviceId: String) = CoroutineScope(Dispatchers.IO).launch {
        Common.servicesCollectionRef
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->

                for (document in querySnapshot.documents) {
                    val item = document.toObject(SpecificService::class.java)
                    if (item?.specificServiceId == serviceId) {
                        scheduledService = item
                    }
                }
            }
    }

    private fun getFacilityDetails(
        facilityId: String,
        facilityName: TextView,
        facilityEmail: TextView,
        facilityPhone: TextView
    ) = CoroutineScope(Dispatchers.IO).launch {
        Common.facilityCollectionRef
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->

                Log.d("EQUA", "getRealtimeRehabs: $querySnapshot")
                for (document in querySnapshot.documents) {
                    Log.d("EQUA", "getRealtimeRehabs: $document")
                    val item = document.toObject(Facility::class.java)
                    if (item?.organisationId == facilityId) {
                        respondingFacility = item
                    }
                }
                facilityName.text = respondingFacility.organisationName
                facilityEmail.text = respondingFacility.organisationEmail
                facilityPhone.text = respondingFacility.organisationPhoneNumber
            }
    }
}