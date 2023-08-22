package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.facility.facilitynotification

import android.app.Dialog
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
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentFacilityNotificationBinding
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.BookService
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Client
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Facility
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Service
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.getDate
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.showProgressDialog
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.toast
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FacilityNotification : Fragment() {

    private var facilitiesInvoiceNotificationAdapter: FirestoreRecyclerAdapter<BookService, FacilitiesInvoiceNotificationAdapter>? =
        null


    private var _binding: FragmentFacilityNotificationBinding? = null
    private val binding get() = _binding!!
    lateinit var requestingClient: Client
    lateinit var servingFacility: Facility
    lateinit var scheduledService: Service


    private var progressDialog: Dialog? = null

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
            Common.appointmentsCollectionRef.whereEqualTo("facilityId", mAuth.uid)
                .whereEqualTo("scheduled", true)
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

                    getClientDetails(model.clientId, holder.clientName)
                    getFacilityDetails(model.facilityId)


                    getServiceDetails(model.facilityId, model.selectedAppointmentServiceID)
                    holder.clientName.text = "${requestingClient.userFirstName} ${requestingClient.userLastName}"
                    holder.serviceName.text = model.selectedAppointmentServiceName
                    holder.dateCreated.text = getDate(model.dateCreated.toLong(), "EE, dd MMMM, yyyy")
                    holder.timeCreated.text = model.timeCreated
                    if (model.invoiceGenerated)
                        holder.invoiceStatus.text = resources.getText(R.string.txt_invoice_status_generated)
                    else
                        holder.invoiceStatus.text = resources.getText(R.string.txt_invoice_status_not_generated)

                    holder.itemView.setOnClickListener {

                        launchInvoiceDialog(model)
                        //requireContext().toast("${model.scheduledTime} clicked!")
                        Log.d("EQUA", "onBindViewHolder: ${requestingClient.userFirstName} ${requestingClient.userLastName}")
                    }

                }

            }

        } catch (e: Exception) {
            requireActivity().toast(e.message.toString())
        }
        facilitiesInvoiceNotificationAdapter?.startListening()
        binding.rvFacilityInvoiceNotification.adapter = facilitiesInvoiceNotificationAdapter
    }

    private fun getClientDetails(clientId: String, clientName: TextView) = CoroutineScope(Dispatchers.IO).launch {
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
  private fun getServiceDetails(facilityId: String, serviceId: String) = CoroutineScope(Dispatchers.IO).launch {
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

    private fun launchInvoiceDialog(model: BookService) {

        val builder = layoutInflater.inflate(R.layout.facility_custom_generate_invoice_layout, null)

        val tvFacilityName = builder.findViewById<TextView>(R.id.facility_generated_invoice_facility_name)
        val tvFacilityAddress = builder.findViewById<TextView>(R.id.facility_generated_invoice_facility_address)
        val tvFacilityPhone = builder.findViewById<TextView>(R.id.facility_generated_invoice_facility_phone_number)
        val tvFacilityEmail = builder.findViewById<TextView>(R.id.facility_generated_invoice_facility_email)
        val tvClientName = builder.findViewById<TextView>(R.id.facility_generated_invoice_customer_name)
        val tvClientPhone = builder.findViewById<TextView>(R.id.facility_generated_invoice_customer_phone)
        val tvClientEmail = builder.findViewById<TextView>(R.id.facility_generated_invoice_customer_email)
        val tvServiceName = builder.findViewById<TextView>(R.id.facility_generated_invoice_request_service_category_name)
        val tvServicePrice = builder.findViewById<TextView>(R.id.facility_generated_invoice_request_service_price)
        val tvServiceDiscountedPrice = builder.findViewById<TextView>(R.id.facility_generated_invoice_request_service_discounted_price)
        val tvServiceTotal = builder.findViewById<TextView>(R.id.facility_generated_invoice_request_service_total_price)

        val btnConfirmInvoice = builder.findViewById<Button>(R.id.facility_generated_invoice_confirm_button)


        tvFacilityName.text = servingFacility.organisationName
        tvFacilityAddress.text = servingFacility.organisationPhysicalAddress
        tvFacilityPhone.text = servingFacility.organisationPhoneNumber
        tvFacilityEmail.text = servingFacility.organisationEmail
        tvClientName.text = "${requestingClient.userFirstName} ${requestingClient.userLastName}"
        tvClientPhone.text = requestingClient.userPhoneNumber
        tvClientEmail.text = requestingClient.userEmail
        tvServiceName.setText("${model.selectedAppointmentServiceID} (service ID)")
//        tvServicePrice.setText("$${scheduledService.servicePrice} (service price)")
//        tvServiceDiscountedPrice.setText("${scheduledService.serviceDiscountedPrice}% (service discount)")

//        val servicePrice = scheduledService.servicePrice
//        val serviceDiscountedPrice = scheduledService.serviceDiscountedPrice
//        tvServiceTotal.text = "Total: $${servicePrice.toDouble() - (servicePrice.toDouble() * (serviceDiscountedPrice.toDouble()/100))}"

        btnConfirmInvoice.setOnClickListener {
            requireContext().toast("Confirmed")
        }


        val dialog = AlertDialog.Builder(requireContext())
            .setView(builder)
            .setCancelable(false)
            .create()

        dialog.show()



        btnConfirmInvoice.setOnClickListener {
            launchInvoiceRecordDialog(model, dialog)
        }


    }

    private fun launchInvoiceRecordDialog(model: BookService, generateInvoiceDialog: AlertDialog) {

        val builder = layoutInflater.inflate(R.layout.facility_custom_new_invoice_record, null)

        var invoiceBankName: String = ""
        var invoiceAccountIBAN: String = ""
        var invoiceAccountName: String = ""

        val tilBankName = builder.findViewById<TextInputLayout>(R.id.text_input_layout_facility_new_record_bank_name)
        val etBankName = builder.findViewById<TextInputEditText>(R.id.facility_new_record_bank_name)
        val tilAccountIBAN = builder.findViewById<TextInputLayout>(R.id.text_input_layout_facility_new_record_account_iban)
        val etAccountIBAN = builder.findViewById<TextInputEditText>(R.id.facility_new_record_account_iban)
        val tilAccountHolder = builder.findViewById<TextInputLayout>(R.id.text_input_layout_facility_new_record_account_holder_name)
        val etAccountHolder = builder.findViewById<TextInputEditText>(R.id.facility_new_record_account_holder_name)

        val btnConfirmInvoiceRecord = builder.findViewById<Button>(R.id.facility_new_record_confirm_btn)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(builder)
            .setCancelable(false)
            .create()

        btnConfirmInvoiceRecord.setOnClickListener {
            invoiceBankName = etBankName.text.toString().trim()
            invoiceAccountIBAN = etAccountIBAN.text.toString().trim()
            invoiceAccountName = etAccountHolder.text.toString().trim()

            if (invoiceBankName.isEmpty()){
                tilBankName.error = "Bank name required"
                etBankName.requestFocus()
            }
            if (invoiceAccountIBAN.isEmpty()){
                tilAccountIBAN.error = "Account IBAN required"
                etAccountIBAN.requestFocus()
            }
            if (invoiceAccountName.isEmpty()){
                tilAccountHolder.error = "Account holder name required"
            }else{
                uploadInvoiceRecord(invoiceBankName, invoiceAccountIBAN, invoiceAccountName, model, dialog)
                generateInvoiceDialog.dismiss()
            }

        }



        dialog.show()


    }

    private fun uploadInvoiceRecord(
        invoiceBankName: String,
        invoiceAccountIBAN: String,
        invoiceAccountName: String,
        model: BookService,
        dialog: AlertDialog
    ) = CoroutineScope(Dispatchers.IO).launch {
        val documentRef = Common.appointmentsCollectionRef.document(model.requestFormId)

        val updates = hashMapOf<String, Any>(
            "invoiceGenerated" to true,
            "invoiceBankName" to invoiceBankName,
            "invoiceAccountIBAN" to invoiceAccountIBAN,
            "invoiceAccountName" to invoiceAccountName,
            "invoiceGeneratedTime" to System.currentTimeMillis()
        )

        documentRef.update(updates)
            .addOnSuccessListener {
                // Update successful
                requireContext().toast("Invoice Generated")
                dialog.dismiss()

            }
            .addOnFailureListener { e ->
                // Handle error
                requireContext().toast(e.message.toString())
            }

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