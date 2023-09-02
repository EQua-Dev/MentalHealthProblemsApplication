package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.client.clientfeedback

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
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentFeedbackRatingBinding
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.BookService
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Client
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.ClientFeedBack
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Facility
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Service
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.ServiceType
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.appointmentsCollectionRef
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.feedbackCollectionRef
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.getDate
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.hideProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.showProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.snackBar
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.toast
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.iarcuschin.simpleratingbar.SimpleRatingBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FeedbackRating : Fragment() {

    private var _binding: FragmentFeedbackRatingBinding? = null
    private val binding get() = _binding!!
    private var clientRatingFeedbackAdapter: FirestoreRecyclerAdapter<BookService, ClientFeedbackRatingAdapter>? =
        null
//    lateinit var scheduledService: BookService
//    lateinit var respondingFacility: Facility


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFeedbackRatingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            val layoutManager = LinearLayoutManager(requireContext())
            rvClientServiceRatings.layoutManager = layoutManager
            rvClientServiceRatings.addItemDecoration(
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

        val clientPaidService =
            Common.appointmentsCollectionRef.whereEqualTo("customerID", mAuth.uid)
                .whereEqualTo("requestFormStatus", "paid").whereEqualTo("rated", false)

        val options = FirestoreRecyclerOptions.Builder<BookService>()
            .setQuery(clientPaidService, BookService::class.java).build()
        try {
            clientRatingFeedbackAdapter = object :
                FirestoreRecyclerAdapter<BookService, ClientFeedbackRatingAdapter>(
                    options
                ) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): ClientFeedbackRatingAdapter {
                    val itemView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.client_custom_ratings_layout, parent, false)
                    return ClientFeedbackRatingAdapter(itemView)
                }

                override fun onBindViewHolder(
                    holder: ClientFeedbackRatingAdapter,
                    position: Int,
                    model: BookService
                ) {


                    val organisation = getOrganisation(model.organisationID)!!
                    val service = getService(model.organisationProfileServiceID)!!

//                    if (model.ratings.isEmpty()) {
//                        holder.serviceRating.visible(false)
//                    } else {
//                        holder.serviceRating.text = model.ratings
//                    }

                    holder.dateCreated.text = model.dateCreated
                    holder.timeCreated.text = model.timeCreated
                    holder.facilityName.text = organisation.organisationName
                    holder.serviceName.text = service.serviceName

                    holder.rateServiceButton.setOnClickListener {
                        launchRatingDialog(model)
                    }

                }

            }

        } catch (e: Exception) {
            requireActivity().toast(e.message.toString())
        }
        clientRatingFeedbackAdapter?.startListening()
        binding.rvClientServiceRatings.adapter = clientRatingFeedbackAdapter
    }

    private fun launchRatingDialog(model: BookService) {
        val builder =
            layoutInflater.inflate(R.layout.custom_client_rate_service_dialog, null)

        val tvFacilityName =
            builder.findViewById<TextView>(R.id.client_rating_facility_name)
        val tvFacilityAddress =
            builder.findViewById<TextView>(R.id.client_rating_facility_address)
        val tvFacilityEmail =
            builder.findViewById<TextView>(R.id.client_rating_facility_email)
        val tvFacilityPhoneNumber =
            builder.findViewById<TextView>(R.id.client_rating_facility_phone_number)
        val tvClientName =
            builder.findViewById<TextView>(R.id.client_rating_client_name)
        val clientRatingBar =
            builder.findViewById<SimpleRatingBar>(R.id.client_rating_bar)
        val etFeedbackText =
            builder.findViewById<TextInputEditText>(R.id.client_rating_feedback_text)
        val tilFeedbackText =
            builder.findViewById<TextInputLayout>(R.id.text_input_layout_client_rating_feedback_text)
        val tvDateCreated =
            builder.findViewById<TextView>(R.id.rating_date_created)
        val tvTimeCreated =
            builder.findViewById<TextView>(R.id.rating_time_created)
        val btnSubmitRating =
            builder.findViewById<Button>(R.id.client_rating_submit_button)


        val dialog = AlertDialog.Builder(requireContext())
            .setView(builder)
            .setCancelable(false)
            .create()

        val user = getCustomer(model.customerID)!!
        val organisation = getOrganisation(model.organisationID)!!

        tvFacilityName.text = resources.getString(
            R.string.facility_generate_request_invoice_company_name,
            organisation.organisationName
        )
        tvFacilityAddress.text = resources.getString(
            R.string.facility_generate_request_invoice_company_physical_address,
            organisation.organisationPhysicalAddress
        )
        tvFacilityEmail.text = resources.getString(
            R.string.facility_generate_request_invoice_company_email,
            organisation.organisationEmail
        )
        tvFacilityPhoneNumber.text = resources.getString(
            R.string.facility_generate_request_invoice_company_contact_number,
            organisation.organisationPhoneNumber
        )
        tvClientName.text = resources.getString(
            R.string.facility_generate_request_invoice_customer_name,
            user.customerFirstName,
            user.customerLastName
        )
        tvDateCreated.text = resources.getString(
            R.string.requesting_date_created,
            getDate(System.currentTimeMillis(), "dd-MM-yyyy")
        )
        tvTimeCreated.text = resources.getString(
            R.string.requesting_time_created,
            getDate(System.currentTimeMillis(), "hh:mm a")
        )

        var customerRating: Float = 0.0F



        clientRatingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            // Handle the rating change event
            if (rating.isNaN()) {
                requireView().snackBar("Give at least one star")
            } else {
                customerRating = rating
            }
            // `rating` represents the selected rating value
        }

        btnSubmitRating.setOnClickListener {
            if (customerRating == 0.0F)
                requireView().snackBar("Give at least one star")
            if (etFeedbackText.text.toString().trim().isEmpty())
                tilFeedbackText.error = "Please enter a comment"
            else {
                val clientFeedBack = ClientFeedBack(
                    feedbackAndRatingsID = System.currentTimeMillis().toString(),
                    organisationID = model.organisationID,
                    customerID = model.customerID,
                    organisationProfileService = model.organisationProfileServiceID,
                    serviceType = model.typeOfService,
                    feedBackText = etFeedbackText.text.toString().trim(),
                    ratings = customerRating.toString(),
                    dateCreated = getDate(System.currentTimeMillis(), "dd-MM-yyyy"),
                    timeCreated = getDate(System.currentTimeMillis(), "hh:mm a"),
                )
                submitRating(clientFeedBack, dialog, model)
            }
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
                val snapshot = Common.servicesCollectionRef.document(serviceTypeId).get().await()
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
        hideProgress()

        return serviceType
    }


    private fun submitRating(
        clientFeedBack: ClientFeedBack,
        dialog: AlertDialog,
        model: BookService
    ) {

        requireContext().showProgress()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                feedbackCollectionRef.document(clientFeedBack.feedbackAndRatingsID)
                    .set(clientFeedBack).addOnSuccessListener {
                    appointmentsCollectionRef.document(model.requestFormId).update("rated", true)
                        .addOnSuccessListener {
                            requireContext().toast("Feedback Sent")
                            getRealtimeRatings()
                            dialog.dismiss()
                        }
                    hideProgress()
                }
            } catch (e: Exception) {
                requireContext().toast(e.message.toString())
            }
        }

    }
}