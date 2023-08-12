package com.androidstrike.trackit.client.clientdigitalwallet

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Service
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
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


        val bottomSheetProceedButton = view.findViewById<Button>(R.id.fund_wallet_proceed)
        val bottomSheetCancelButton = view.findViewById<Button>(R.id.fund_wallet_cancel)

        bottomSheetProceedButton.setOnClickListener {
            dismiss()
        }
        bottomSheetCancelButton.setOnClickListener {
            dismiss()
        }
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