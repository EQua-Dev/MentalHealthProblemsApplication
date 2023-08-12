package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.client.clientinvoice

import android.os.Bundle
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
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentInvoicePaymentBinding
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.BookService
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Client
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Facility
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.SpecificService
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.getDate
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.toast
import com.androidstrike.trackit.client.clientinvoice.ClientInvoiceNotificationAdapter
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InvoicePayment : Fragment() {

    private var clientInvoiceNotificationAdapter: FirestoreRecyclerAdapter<BookService, ClientInvoiceNotificationAdapter>? =
        null

    private var _binding: FragmentInvoicePaymentBinding? = null
    private val binding get() = _binding!!
    lateinit var requestingClient: Client
    lateinit var servingFacility: Facility
    lateinit var scheduledService: SpecificService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentInvoicePaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestingClient = Client()
        servingFacility = Facility()

        getRealtimeInvoice()


        with(binding) {
            val layoutManager = LinearLayoutManager(requireContext())
            rvClientInvoiceNotification.layoutManager = layoutManager
            rvClientInvoiceNotification.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    layoutManager.orientation
                )
            )
        }


    }

    private fun getRealtimeInvoice() {
        val mAuth = FirebaseAuth.getInstance()

        val clientGeneratedInvoice =
            Common.appointmentsCollectionRef.whereEqualTo("clientId", mAuth.uid)
                .whereEqualTo("invoiceGenerated", true)
        val options = FirestoreRecyclerOptions.Builder<BookService>()
            .setQuery(clientGeneratedInvoice, BookService::class.java).build()
        try {
            clientInvoiceNotificationAdapter = object :
                FirestoreRecyclerAdapter<BookService, ClientInvoiceNotificationAdapter>(options) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): ClientInvoiceNotificationAdapter {
                    val itemView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.client_custom_invoice_notification_layout, parent, false)
                    return ClientInvoiceNotificationAdapter(itemView)
                }

                override fun onBindViewHolder(
                    holder: ClientInvoiceNotificationAdapter,
                    position: Int,
                    model: BookService
                ) {


//                    val facilityBaseFragment = parentFragment
//                    val facilityTabLayout =
//                        facilityBaseFragment!!.view?.findViewById<TabLayout>(R.id.facility_base_tab_title)

                    getClientDetails(model.clientId, holder.facilityName)
                    getFacilityDetails(model.facilityId)


                    val clientBaseFragment = parentFragment
                    val clientTabLayout =
                        clientBaseFragment!!.view?.findViewById<TabLayout>(R.id.client_base_tab_title)


                    getServiceDetails(model.facilityId, model.selectedAppointmentServiceID)
                    holder.facilityName.text = servingFacility.facilityName
                    holder.serviceName.text = model.selectedAppointmentServiceName
//                    holder.dateCreated.text = getDate(model.invoiceGeneratedTime, "dd MMMM, yyyy")
//                    holder.timeCreated.text = getDate(model.invoiceGeneratedTime, "hh:mm")
//                    if (model.invoicePaid)
//                        holder.invoicePaymentStatus.text = resources.getText(R.string.txt_invoice_payment_status_paid)
//                    else
//                        holder.invoicePaymentStatus.text = resources.getText(R.string.txt_invoice_payment_status_not_paid)

                    holder.itemView.setOnClickListener {

                        launchInvoiceDetailsDialog(model, clientTabLayout)
//                        requireContext().toast("${model.scheduledTime} clicked!")
                        Log.d("EQUA", "onBindViewHolder: ${requestingClient.userFirstName} ${requestingClient.userLastName}")
                    }

                }

            }

        } catch (e: Exception) {
            requireActivity().toast(e.message.toString())
        }
        clientInvoiceNotificationAdapter?.startListening()
        binding.rvClientInvoiceNotification.adapter = clientInvoiceNotificationAdapter
    }

    private fun launchInvoiceDetailsDialog(model: BookService, clientTabLayout: TabLayout?,) {

        val builder = layoutInflater.inflate(R.layout.client_custom_generated_invoice_detail_layout, null)

        val tvFacilityName = builder.findViewById<TextView>(R.id.client_generated_invoice_facility_name)
        val tvFacilityPhone = builder.findViewById<TextView>(R.id.client_generated_invoice_facility_phone_number)
        val tvFacilityEmail = builder.findViewById<TextView>(R.id.client_generated_invoice_facility_email)
        val tvInvoiceId = builder.findViewById<TextView>(R.id.client_generated_invoice_id)
        val tvClientName = builder.findViewById<TextView>(R.id.client_generated_invoice_customer_name)
        val tvServicePrice = builder.findViewById<TextView>(R.id.client_generated_invoice_service_price)
        val tvServiceDiscountedPrice = builder.findViewById<TextView>(R.id.client_generated_invoice_service_discount_price)
        val tvServiceSubTotal = builder.findViewById<TextView>(R.id.client_generated_invoice_service_subtotal_price)
        val tvServiceTotal = builder.findViewById<TextView>(R.id.client_generated_invoice_service_total_price)
        val tvInvoiceText = builder.findViewById<TextView>(R.id.client_generated_invoice_text)
        val tvInvoiceBankName = builder.findViewById<TextView>(R.id.client_generated_invoice_payment_bank_name)
        val tvInvoiceAccountIban = builder.findViewById<TextView>(R.id.client_generated_invoice_payment_account_iban)
        val tvInvoiceAccountHolder = builder.findViewById<TextView>(R.id.client_generated_invoice_payment_account_holder)

        val btnProvidePayment = builder.findViewById<Button>(R.id.client_generated_invoice_payment_button)


        tvFacilityName.text = servingFacility.facilityName
        tvFacilityPhone.text = servingFacility.facilityPhoneNumber
        tvFacilityEmail.text = servingFacility.facilityEmail
        tvClientName.text = "${requestingClient.userFirstName} ${requestingClient.userLastName}"
        tvInvoiceId.text = scheduledService.specificServiceId
        tvServicePrice.text = "Service Price: $${scheduledService.specificServicePrice}"

        val servicePrice = scheduledService.specificServicePrice
//        val serviceDiscountedPrice = scheduledService.specificServiceDiscountedPrice

//        tvServiceDiscountedPrice.text = "Service Discount: ${scheduledService.specificServiceDiscountedPrice}%"
//        tvServiceSubTotal.text = "Subtotal: $${servicePrice.toDouble() - (servicePrice.toDouble() * (serviceDiscountedPrice.toDouble()/100))}"


//        tvServiceTotal.text = "Total: $${servicePrice.toDouble() - (servicePrice.toDouble() * (serviceDiscountedPrice.toDouble()/100))}"

//        tvInvoiceText.text = "Dear Customer,\n\nYour request has been ${model.status}. Please provide payment for your request, in order to start your treatment to:\n\nBank Name: ${model.invoiceBankName}\n\nAccount IBAN: ${model.invoiceAccountIBAN}\n\nAccount Holder: ${model.invoiceAccountName}"


        val dialog = AlertDialog.Builder(requireContext())
            .setView(builder)
            .setCancelable(false)
            .create()

        dialog.show()

        btnProvidePayment.setOnClickListener {
            dialog.dismiss()
            clientTabLayout?.getTabAt(1)?.select()
        }


    }

    private fun getClientDetails(clientId: String, clientName: TextView) = CoroutineScope(
        Dispatchers.IO).launch {
        Common.clientCollectionRef
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->

                for (document in querySnapshot.documents) {
                    val item = document.toObject(Client::class.java)
                    if (item?.userId == clientId) {
                        requestingClient = item
                    }
                }
                clientName.text = "${requestingClient.userFirstName}, ${requestingClient.userLastName}"
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
                    if (item?.facilityId == facilityId) {
                        servingFacility = item
                    }
                }
//                tvFacilityName.text = servingFacility.facilityName
//                tvFacilityAddress.text = servingFacility.facilityAddress
//                tvFacilityEmail.text = servingFacility.facilityEmail
//                tvFacilityPhone.text = servingFacility.facilityPhoneNumber
            }
    }
    private fun getServiceDetails(facilityId: String, serviceId: String) = CoroutineScope(
        Dispatchers.IO).launch {
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}