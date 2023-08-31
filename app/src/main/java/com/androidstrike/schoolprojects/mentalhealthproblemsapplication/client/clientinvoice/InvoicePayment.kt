package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.client.clientinvoice

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
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentInvoicePaymentBinding
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.AcceptedRequestInvoice
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.BookService
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Client
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Facility
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.InvoicePaymentData
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Service
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.SpecificService
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.WalletData
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.WalletHistory
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.DATE_FORMAT_LONG
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.WALLET_HISTORY_REF
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.appointmentsCollectionRef
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.invoicePaymentCollectionRef
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.walletCollectionRef
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.NavigationMenuListener
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.getDate
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.hideProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.showProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.toast
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.visible
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class InvoicePayment : Fragment() {

    private var clientInvoiceNotificationAdapter: FirestoreRecyclerAdapter<AcceptedRequestInvoice, ClientInvoiceNotificationAdapter>? =
        null
    private var navigationMenuListener: NavigationMenuListener? = null

    private var _binding: FragmentInvoicePaymentBinding? = null
    private val binding get() = _binding!!
    lateinit var requestingClient: Client
    lateinit var servingFacility: Facility
    lateinit var scheduledService: SpecificService

    private val TAG = "InvoicePayment"

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
            Common.acceptedRequestInvoiceCollectionRef.whereEqualTo("customerID", mAuth.uid)

        val options = FirestoreRecyclerOptions.Builder<AcceptedRequestInvoice>()
            .setQuery(clientGeneratedInvoice, AcceptedRequestInvoice::class.java).build()
        try {
            clientInvoiceNotificationAdapter = object :
                FirestoreRecyclerAdapter<AcceptedRequestInvoice, ClientInvoiceNotificationAdapter>(
                    options
                ) {
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
                    model: AcceptedRequestInvoice
                ) {


//                    val facilityBaseFragment = parentFragment
//                    val facilityTabLayout =
//                        facilityBaseFragment!!.view?.findViewById<TabLayout>(R.id.facility_base_tab_title)

//                    val clientBaseFragment = parentFragment
//                    val clientTabLayout =
//                        clientBaseFragment!!.view?.findViewById<TabLayout>(R.id.client_base_tab_title)


                    //getServiceDetails(model.organisationID, model.organisationProfileServiceID)
//                    holder.facilityName.text = getOrganisation(model.organisationID)!!.organisationName
                    Log.d(TAG, "onBindViewHolder: $model")
                    Log.d(
                        TAG,
                        "onBindViewHolder: ${getOrganisation(model.organisationID)!!.organisationName}"
                    )
                    holder.serviceName.text =
                        getService(model.organisationProfileServiceID)!!.serviceName
                    holder.facilityName.text =
                        getOrganisation(model.organisationID)!!.organisationName

                    holder.dateCreated.text = model.dateCreated
                    holder.timeCreated.text = model.timeCreated
//                    if (model.invoicePaid)
//                        holder.invoicePaymentStatus.text = resources.getText(R.string.txt_invoice_payment_status_paid)
//                    else
//                        holder.invoicePaymentStatus.text = resources.getText(R.string.txt_invoice_payment_status_not_paid)
                    holder.invoice_payment_view_btn.setOnClickListener {
                        launchInvoiceDetailsDialog(model)
                    }
                }

            }

        } catch (e: Exception) {
            requireActivity().toast(e.message.toString())
        }
        clientInvoiceNotificationAdapter?.startListening()
        binding.rvClientInvoiceNotification.adapter = clientInvoiceNotificationAdapter
    }

    private fun launchInvoiceDetailsDialog(model: AcceptedRequestInvoice) {

        val builder =
            layoutInflater.inflate(R.layout.client_custom_generated_invoice_detail_layout, null)

        val tvFacilityName =
            builder.findViewById<TextView>(R.id.client_generated_invoice_facility_name)
        val tvFacilityAddress =
            builder.findViewById<TextView>(R.id.client_generated_invoice_facility_physical_address)
        val tvFacilityPhone =
            builder.findViewById<TextView>(R.id.client_generated_invoice_facility_phone_number)
        val tvFacilityEmail =
            builder.findViewById<TextView>(R.id.client_generated_invoice_facility_email)
        val tvInvoiceText = builder.findViewById<TextView>(R.id.client_generated_invoice_text)
        val tvInvoiceDate =
            builder.findViewById<TextView>(R.id.client_generated_invoice_date_created)
        val tvInvoiceTime =
            builder.findViewById<TextView>(R.id.client_generated_invoice_time_created)

        val btnProvidePayment =
            builder.findViewById<Button>(R.id.client_generated_invoice_payment_button)

        val organisation = getOrganisation(model.organisationID)!!
        val customer = getCustomer(model.customerID)!!
        val invoice = getInvoice(model.invoiceID)!!


        tvFacilityName.text = resources.getString(
            R.string.facility_generate_request_invoice_company_name,
            organisation.organisationName
        )
        tvFacilityPhone.text = resources.getString(
            R.string.facility_generate_request_invoice_company_contact_number,
            organisation.organisationPhoneNumber
        )
        tvFacilityAddress.text = resources.getString(
            R.string.facility_generate_request_invoice_company_physical_address,
            organisation.organisationPhysicalAddress
        )
        tvFacilityEmail.text = resources.getString(
            R.string.facility_generate_request_invoice_company_email,
            organisation.organisationEmail
        )
        tvInvoiceText.text = model.invoiceText
        tvInvoiceDate.text =
            resources.getString(R.string.client_generated_invoice_date_created, model.dateCreated)
        tvInvoiceTime.text =
            resources.getString(R.string.client_generated_invoice_time_created, model.timeCreated)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(builder)
            .setCancelable(false)
            .create()

        dialog.show()


        val requestForm = getRequestForm(model.customerRequestFormID)!!

        btnProvidePayment.apply {
            if (requestForm.requestFormStatus != "paid"){
                text = resources.getString(R.string.client_generated_invoice_payment_button)
                setOnClickListener {
                    val currentTime = getDate(System.currentTimeMillis(), "hh:mm a")
                    val currentDate = getDate(System.currentTimeMillis(), "dd-MM-yyyy")

                    val invoicePaymentData = InvoicePaymentData(
                        paymentID = System.currentTimeMillis().toString(),
                        invoiceID = model.invoiceID,
                        notificationID = model.notificationID,
                        customerRequestFormID = model.customerRequestFormID,
                        customerID = model.customerID,
                        customerDigitalWalletID = customer.wallet,
                        organisationID = model.organisationID,
                        organisationProfileServiceID = model.organisationProfileServiceID,
                        typeOfServiceID = model.typeOfServiceID,
                        typeOfServiceSpecialistID = model.typeOfServiceSpecialistID,
                        paymentAmount = model.invoiceAmount,
                        paymentBankIBAN = model.bankAccountIBAN,
                        paymentBankName = model.bankName,
                        paymentBankAccountHolderName = model.bankAccountHolderName,
                        paymentDate = currentDate,
                        paymentTime = currentTime,
                    )
                    launchPaymentDialog(invoicePaymentData, dialog)
                }
            }else{
                text = resources.getString(R.string.btn_client_booking_response_detail_okay)
                setOnClickListener {
                    dialog.dismiss()
                }
            }
        }





    }

    private fun launchPaymentDialog(
        invoicePaymentData: InvoicePaymentData,
        transferredDialog: AlertDialog
    ) {

        val builder =
            layoutInflater.inflate(R.layout.client_custom_make_payment_layout_dialog, null)

        val tvCustomerWalletBalance =
            builder.findViewById<TextView>(R.id.client_make_payment_wallet_balance)
        val tvMakePaymentText = builder.findViewById<TextView>(R.id.client_make_payment_text)
        val btnProceedPayment = builder.findViewById<Button>(R.id.btn_client_make_payment)
        val btnCancelPayment = builder.findViewById<Button>(R.id.btn_client_cancel_payment)

        val customer = getCustomer(invoicePaymentData.customerID)!!
        val wallet = getWallet(customer.wallet)!!
        val service = getService(invoicePaymentData.organisationProfileServiceID)!!
        tvCustomerWalletBalance.text =
            resources.getString(R.string.client_make_payment_wallet_balance, wallet.walletBalance)
        tvMakePaymentText.text = resources.getString(
            R.string.client_make_payment_text,
            invoicePaymentData.paymentAmount,
            service.serviceName
        )


        val dialog = AlertDialog.Builder(requireContext())
            .setView(builder)
            .setCancelable(false)
            .create()

        btnCancelPayment.setOnClickListener {
            dialog.dismiss()
        }
        btnProceedPayment.setOnClickListener {
            if (wallet.walletBalance.toDouble() < invoicePaymentData.paymentAmount.toDouble()) {
                requireContext().toast(resources.getString(R.string.insufficent_balance))
            } else {
                requireContext().showProgress()
                CoroutineScope(Dispatchers.IO).launch {

                    invoicePaymentCollectionRef.document(invoicePaymentData.paymentID)
                        .set(invoicePaymentData).addOnSuccessListener {
                            appointmentsCollectionRef.document(invoicePaymentData.customerRequestFormID)
                                .update("requestFormStatus", "paid").addOnSuccessListener {
                                    val newWalletBalance =
                                        wallet.walletBalance.toDouble() - invoicePaymentData.paymentAmount.toDouble()
                                    walletCollectionRef.document(customer.wallet)
                                        .update("walletBalance", newWalletBalance.toString())
                                        .addOnSuccessListener {
                                            val walletHistory = WalletHistory(
                                                transactionDate = getDate(
                                                    System.currentTimeMillis(),
                                                    DATE_FORMAT_LONG
                                                ),
                                                transactionReason = "Payment for ${service.serviceName}",
                                                transactionType = "Dr",
                                                transactionAmount = resources.getString(
                                                    R.string.money_text,
                                                    invoicePaymentData.paymentAmount
                                                )
                                            )
                                            walletCollectionRef.document(customer.wallet)
                                                .collection(
                                                    WALLET_HISTORY_REF
                                                ).document(System.currentTimeMillis().toString())
                                                .set(walletHistory)
                                                .addOnSuccessListener {
                                                    hideProgress()
                                                    requireContext().toast("payment made successfully")
                                                    dialog.dismiss()
                                                }.addOnFailureListener { e ->
                                                    hideProgress()
                                                    requireContext().toast(e.message.toString())
                                                }
                                        }.addOnFailureListener { e ->
                                            hideProgress()
                                            requireContext().toast(e.message.toString())
                                        }
                                }
                        }.addOnFailureListener { e ->
                            hideProgress()
                            requireContext().toast(e.message.toString())
                        }
                }
            }
        }

        dialog.show()
        transferredDialog.dismiss()

    }

    private fun getClientDetails(clientId: String, clientName: TextView) = CoroutineScope(
        Dispatchers.IO
    ).launch {
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

    private fun getServiceDetails(facilityId: String, serviceId: String) = CoroutineScope(
        Dispatchers.IO
    ).launch {
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
        Log.d(TAG, "getOrganisation: $organisationId")
        Log.d(TAG, "getOrganisation: $organisation")
        Log.d(TAG, "getOrganisation: ${organisation!!.organisationName}")


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
        Log.d(TAG, "getOrganisation: $customerId")
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
        hideProgress()

        return service
    }

    private fun getWallet(walletId: String): WalletData? {
        requireContext().showProgress()
        val deferred = CoroutineScope(Dispatchers.IO).async {
            try {
                val snapshot = Common.walletCollectionRef.document(walletId).get().await()
                if (snapshot.exists()) {
                    return@async snapshot.toObject(WalletData::class.java)
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

        val wallet = runBlocking { deferred.await() }
        hideProgress()

        return wallet
    }

    private fun getRequestForm(requestFormId: String): BookService? {
        requireContext().showProgress()
        val deferred = CoroutineScope(Dispatchers.IO).async {
            try {
                val snapshot = Common.appointmentsCollectionRef.document(requestFormId).get().await()
                if (snapshot.exists()) {
                    return@async snapshot.toObject(BookService::class.java)
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

        val requestForm = runBlocking { deferred.await() }
        hideProgress()

        return requestForm
    }

    private fun getInvoice(invoiceId: String): AcceptedRequestInvoice? {
        requireContext().showProgress()
        val deferred = CoroutineScope(Dispatchers.IO).async {
            try {
                val snapshot =
                    Common.acceptedRequestInvoiceCollectionRef.document(invoiceId).get().await()
                if (snapshot.exists()) {
                    return@async snapshot.toObject(AcceptedRequestInvoice::class.java)
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

        val invoice = runBlocking { deferred.await() }
        com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.hideProgress()

        return invoice
    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        if (context is NavigationMenuListener) {
//            navigationMenuListener = context
//        } else {
//            throw RuntimeException("$context must implement NavigationListener")
//        }
//    }
//
//    override fun onDetach() {
//        super.onDetach()
//        navigationMenuListener = null
//    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}