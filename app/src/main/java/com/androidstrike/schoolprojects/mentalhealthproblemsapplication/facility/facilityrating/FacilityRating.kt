package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.facility.facilityrating

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentFacilityRatingBinding
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Client
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.ClientFeedBack
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Service
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.hideProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.showProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.toast
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
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
                    holder.serviceRating.text = model.ratings
                    holder.serviceRatingText.text = model.feedBackText

                }

            }

        } catch (e: Exception) {
            requireActivity().toast(e.message.toString())
        }
        facilityRatingAdapter?.startListening()
        binding.rvFacilityClientsRating.adapter = facilityRatingAdapter
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}