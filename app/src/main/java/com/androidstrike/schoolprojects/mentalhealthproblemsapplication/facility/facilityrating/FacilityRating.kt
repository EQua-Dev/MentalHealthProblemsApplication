package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.facility.facilityrating

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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentFacilityRatingBinding
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Client
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.ClientFeedBack
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Service
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.ServiceType
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.hideProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.showProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.toast
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.iarcuschin.simpleratingbar.SimpleRatingBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class FacilityRating : Fragment() {

    private var _binding: FragmentFacilityRatingBinding? = null
    private val binding get() = _binding!!

    private var facilityRatingAdapter: FirestoreRecyclerAdapter<ClientFeedBack, FacilityRatingAdapter>? =
        null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFacilityRatingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            val layoutManager = LinearLayoutManager(requireContext())
            rvFacilityClientsRating.layoutManager = layoutManager
            rvFacilityClientsRating.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    layoutManager.orientation
                )
            )
        }

        getRealtimeRatings()


    }

    private fun getRealtimeRatings() {
        val mAuth = FirebaseAuth.getInstance()

        val organisationServiceRating =
            Common.feedbackCollectionRef.whereEqualTo("organisationID", mAuth.uid)

        val options = FirestoreRecyclerOptions.Builder<ClientFeedBack>()
            .setQuery(organisationServiceRating, ClientFeedBack::class.java).build()
        try {
            facilityRatingAdapter = object :
                FirestoreRecyclerAdapter<ClientFeedBack, FacilityRatingAdapter>(
                    options
                ) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): FacilityRatingAdapter {
                    val itemView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.custom_facility_clients_rating, parent, false)
                    return FacilityRatingAdapter(itemView)
                }

                override fun onBindViewHolder(
                    holder: FacilityRatingAdapter,
                    position: Int,
                    model: ClientFeedBack
                ) {
                    val customer = getCustomer(model.customerID)!!
                    val service = getService(model.organisationProfileService)!!

                    holder.dateCreated.text = model.dateCreated
                    holder.timeCreated.text = model.timeCreated
                    holder.clientName.text = resources.getString(R.string.facility_generate_request_invoice_customer_name, customer.customerFirstName, customer.customerLastName)
                    holder.serviceName.text = service.serviceName
                    holder.serviceRating.numberOfStars = model.ratings.toFloat().toInt()
                    Log.d("TAG", "ratings: ${model.ratings.toFloat().toInt()}")
                    holder.serviceRatingText.text = model.feedBackText

                    holder.ratingDetailButton.setOnClickListener {
                        launchRatingsDetailDialog(model)
                    }

                }

            }

        } catch (e: Exception) {
            requireActivity().toast(e.message.toString())
        }
        facilityRatingAdapter?.startListening()
        binding.rvFacilityClientsRating.adapter = facilityRatingAdapter
    }

    private fun launchRatingsDetailDialog(model: ClientFeedBack) {

        val builder =
            layoutInflater.inflate(
                R.layout.custom_facility_rating_detail_dialog
                ,
                null
            )
        val dialog = AlertDialog.Builder(requireContext())
            .setView(builder)
            .setCancelable(false)
            .create()

        val tvClientName =
            builder.findViewById<TextView>(R.id.facility_rating_detail_customer_name)
        val tvChosenServiceName =
            builder.findViewById<TextView>(R.id.facility_rating_detail_chosen_service_name)
        val tvChosenServiceType =
            builder.findViewById<TextView>(R.id.facility_rating_detail_chosen_service_type)
        val tvRatingStars =
            builder.findViewById<SimpleRatingBar>(R.id.txt_facility_rating_stars)
        val tvRatingsText =
            builder.findViewById<TextView>(R.id.facility_rating_detail_text)
        val tvRequestedDateCreated =
            builder.findViewById<TextView>(R.id.facility_rating_detail_date_created)
        val tvRequestedTimeCreated =
            builder.findViewById<TextView>(R.id.facility_rating_detail_time_created)
        val btnAcceptRequest =
            builder.findViewById<Button>(R.id.btn_facility_rating_okay)

        val client = getCustomer(model.customerID)!!
        val service = getService(model.organisationProfileService)!!
        val serviceType = getServiceType(model.serviceType)!!

        tvClientName.text = resources.getString(
            R.string.requesting_client_name,
            client.customerFirstName,
            client.customerLastName
        )
        tvChosenServiceName.text =
            resources.getString(R.string.requesting_chosen_service_name, service.serviceName)
        tvChosenServiceType.text = resources.getString(
            R.string.requesting_chosen_service_type,
            serviceType.serviceTypeName
        )
        tvRatingsText.text = model.feedBackText
        tvRequestedDateCreated.text =
            resources.getString(R.string.requesting_date_created, model.dateCreated)
        tvRatingStars.numberOfStars = model.ratings.toFloat().toInt()
        tvRequestedTimeCreated.text =
            resources.getString(R.string.requesting_time_created, model.timeCreated)
        btnAcceptRequest.setOnClickListener {
         dialog.dismiss()

        }

        dialog.show()

    }

    private fun getCustomer(customerId: String): Client? {
        requireContext().showProgress()
        val deferred = CoroutineScope(Dispatchers.IO).async {
            try {
                val snapshot = Common.clientCollectionRef.document(customerId).get().await()
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