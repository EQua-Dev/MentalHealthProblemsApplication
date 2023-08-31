package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.client.clientdigitalwallet

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.widget.addTextChangedListener
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Client
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Service
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.WalletData
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.WalletHistory
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.DATE_FORMAT_LONG
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.REASON_ACCOUNT_FUND
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.WALLET_HISTORY_REF
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.auth
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.clientCollectionRef
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.walletCollectionRef
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.WalletBottomSheetListener
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.enable
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.getDate
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.hideProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.showProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.*

class FundWalletBottomSheet : BottomSheetDialogFragment() {

    private lateinit var selectedAppointmentServiceName: String
    private lateinit var selectedAppointmentServiceID: String
    private lateinit var selectedAppointmentDate: String
    private lateinit var selectedAppointmentTime: String
    private lateinit var selectedAppointmentDescription: String
    private lateinit var dateBooked: String
    private lateinit var client: String
    //private lateinit var facilityId: String

    private val calendar = Calendar.getInstance()

    val facilityServices: MutableList<Service> = mutableListOf()
    val facilityServicesNames: MutableList<String> = mutableListOf()

    private var walletBottomSheetListener: WalletBottomSheetListener? = null



    private var progressDialog: Dialog? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.client_fund_wallet_bottom_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val bottomSheetAmountField = view.findViewById<TextInputEditText>(R.id.fund_digital_wallet_amount)
        val bottomSheetProceedButton = view.findViewById<Button>(R.id.fund_wallet_proceed)
        val bottomSheetCancelButton = view.findViewById<Button>(R.id.fund_wallet_cancel)

        bottomSheetProceedButton.enable(false)

        bottomSheetAmountField.addTextChangedListener {amount ->
            val fundAmount = amount.toString().trim()
            bottomSheetProceedButton.apply {
                enable(fundAmount.isNotEmpty())
                setOnClickListener {
                    fundWallet(fundAmount)
                }
            }

        }
//        bottomSheetProceedButton.setOnClickListener {
//
//            dismiss()
//        }
        bottomSheetCancelButton.setOnClickListener {
            dismiss()
        }
    }

    fun setListener(listener: WalletBottomSheetListener) {
        walletBottomSheetListener = listener
    }


    private fun fundWallet(fundAmount: String){
        val walletId = getUser(auth.uid!!)!!.wallet
            val newBalance =
                getWalletInfo(walletId)!!.walletBalance.toDouble() + fundAmount.toDouble()
            CoroutineScope(Dispatchers.IO).launch {
                val walletReference =
                    walletCollectionRef.document(walletId)

                val updates = hashMapOf<String, Any>(
                    "walletBalance" to newBalance.toString(),
                )

                walletReference.update(updates)
                    .addOnSuccessListener {
                        //hideProgress()
                        //launch dialog to enter picker details
                        //update wallet transaction
                        CoroutineScope(Dispatchers.IO).launch {
                            val walletHistoryReference =
                                walletCollectionRef.document(walletId).collection(
                                    WALLET_HISTORY_REF
                                )
                            val walletTransaction = WalletHistory(
                                transactionDate = getDate(
                                    System.currentTimeMillis(),
                                    DATE_FORMAT_LONG
                                ),
                                transactionType = "CR",
                                transactionAmount = resources.getString(
                                    R.string.money_text,
                                    fundAmount
                                ),
                                transactionReason = REASON_ACCOUNT_FUND
                            )

                            walletHistoryReference.document(
                                System.currentTimeMillis().toString()
                            ).set(walletTransaction).await()
                            getWalletDetails()

                        }

                    }
            }

    }

    private fun getWalletDetails() {
        walletBottomSheetListener?.refreshWalletDetails()
        dismiss()

    }

    private fun getUser(userId: String): Client? {
        requireContext().showProgress()
        val deferred = CoroutineScope(Dispatchers.IO).async {
            try {
                val snapshot = clientCollectionRef.document(userId).get().await()
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
        hideProgress()

        return clientUser
    }

    private fun getWalletInfo(walletId: String): WalletData? {

        requireContext().showProgress()
        val deferred = CoroutineScope(Dispatchers.IO).async {
            try {
                val snapshot = walletCollectionRef.document(walletId).get().await()
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

        val walletInfo = runBlocking { deferred.await() }
        hideProgress()

        return walletInfo
    }



    companion object {

    fun newInstance(): FundWalletBottomSheet {
        val fragment = FundWalletBottomSheet()
//        val args = Bundle().apply {
//            putParcelable(ARG_FACILITY_DATA, facility)
//
//        }
//        fragment.arguments = args
        return fragment
    }
}
}