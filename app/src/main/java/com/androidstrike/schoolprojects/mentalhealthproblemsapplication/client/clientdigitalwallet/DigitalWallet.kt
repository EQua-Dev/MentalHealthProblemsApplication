package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.client.clientdigitalwallet

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentDigitalWalletBinding
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.WalletData
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.auth
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.clientCollectionRef
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.walletCollectionRef
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.hashString
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.hideProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.showProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.toast
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.visible
import com.androidstrike.trackit.client.clientdigitalwallet.FundWalletBottomSheet
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DigitalWallet : Fragment() {

    private var _binding: FragmentDigitalWalletBinding? = null
    private val binding get() = _binding!!

    private val TAG = "DigitalWallet"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: ")

        // Inflate the layout for this fragment
        _binding = FragmentDigitalWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        binding.createDigitalWallet.setOnClickListener {
            val newWallet = WalletData(
                walletId = hashString(
                    "${auth.uid}${
                        System.currentTimeMillis().toString()
                    }"
                ),
                walletOwner = auth.uid!!,
                walletBalance = "0.0"
            )
            launchCreateWalletDialog(newWallet)
            //create wallet
            createWallet(newWallet)
        }
        binding.fundDigitalWallet.setOnClickListener {
            val bottomSheetFragment = FundWalletBottomSheet.newInstance()
            bottomSheetFragment.show(childFragmentManager, "bottomSheetTag")
        }
    }

    private fun launchCreateWalletDialog(newWallet: WalletData) {

        val builder =
            layoutInflater.inflate(R.layout.custom_create_new_wallet_details_layout_dialog, null)

        val tilCardNumber = builder.findViewById<TextInputLayout>(R.id.text_input_layout_create_wallet_card_number)
        val etCardNumber = builder.findViewById<TextInputEditText>(R.id.create_wallet_card_number)
        val tilCardExpiryMonth = builder.findViewById<TextInputLayout>(R.id.text_input_layout_create_wallet_card_expiry_month)
        val etCardExpiryMonth = builder.findViewById<TextInputEditText>(R.id.create_wallet_card_expiry_month)
        val tilCardExpiryYear = builder.findViewById<TextInputLayout>(R.id.text_input_layout_create_wallet_card_expiry_year)
        val etCardExpiryYear = builder.findViewById<TextInputEditText>(R.id.create_wallet_card_expiry_year)
        val tilCardCVV = builder.findViewById<TextInputLayout>(R.id.text_input_layout_create_wallet_card_cvv)
        val etCardCVV = builder.findViewById<TextInputEditText>(R.id.create_wallet_card_cvv)
        val tilCardHolderName = builder.findViewById<TextInputLayout>(R.id.text_input_layout_create_wallet_card_holder_name)
        val etCardHolderName = builder.findViewById<TextInputEditText>(R.id.create_wallet_card_holder_name)
        val btnCreateWallet = builder.findViewById<Button>(R.id.btn_create_wallet_card)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(builder)
            .setCancelable(false)
            .create()

        btnCreateWallet.setOnClickListener {
            val cardNumber = etCardNumber.text.toString().trim()
            val cardExpiryMonth = etCardExpiryMonth.text.toString().trim()
            val cardExpiryYear = etCardExpiryYear.text.toString().trim()
            val cardCVV = etCardCVV.text.toString().trim()
            val cardHolderName = etCardHolderName.text.toString().trim()

            tilCardNumber.error = null
            tilCardExpiryMonth.error = null
            tilCardExpiryYear.error = null
            tilCardCVV.error = null
            tilCardHolderName.error = null

            if (cardNumber.isEmpty()){
                tilCardNumber.error = resources.getString(R.string.card_number_empty)
                etCardNumber.requestFocus()
            }
            if (cardExpiryMonth.isEmpty()){
                tilCardExpiryMonth.error = resources.getString(R.string.card_month_empty)
                etCardExpiryMonth.requestFocus()
            }
            if (cardExpiryYear.isEmpty()){
                tilCardExpiryYear.error = resources.getString(R.string.card_year_empty)
                etCardExpiryYear.requestFocus()
            }
            if (cardCVV.isEmpty()){
                tilCardCVV.error = resources.getString(R.string.card_cvv_empty)
                etCardCVV.requestFocus()
            }
            if (cardHolderName.isEmpty()){
                tilCardHolderName.error = resources.getString(R.string.card_holder_empty)
                etCardHolderName.requestFocus()
            }
            else{
                tilCardNumber.error = null
                tilCardExpiryMonth.error = null
                tilCardExpiryYear.error = null
                tilCardCVV.error = null
                tilCardHolderName.error = null
                createWallet(newWallet)
            }
        }
        dialog.show()

    }

    private fun createWallet(newWallet: WalletData) {
        requireContext().showProgress()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                walletCollectionRef.document(newWallet.walletId).set(newWallet).addOnSuccessListener {
                    clientCollectionRef.document(auth.uid!!).update("wallet", newWallet.walletId)
                    hideProgress()
                    binding.createDigitalWallet.visible(false)
                    //binding.walletLayout.visible(true)
                    //fetchWalletDetails(newWallet.walletId)
                }//.await()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    requireContext().toast(e.message.toString())
                }
            }
        }


    }

//    private fun fetchWalletDetails(walletId: String) {
//
//        //requireContext().showProgress()
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val snapshot = Common.walletCollectionRef.document(walletId).get().await()
//                if (snapshot.exists()) {
//                    hideProgress()
//                    val walletInfo = snapshot.toObject(WalletData::class.java)
//                    withContext(Dispatchers.Main){
//                        binding.walletBalance.apply {
//                            visible(true)
//                            text = resources.getString(
//                                R.string.money_text,
//                                walletInfo!!.walletBalance.toString()
//                            )
//                        }
//                        binding.walletAddFundsText.apply {
//                            visible(getUser(auth.uid!!)!!.role == clientRole)
//                            setOnClickListener {
//                                launchFundWalletDialog(walletId)
//                            }
//                        }
//
//                        getWalletHistory(walletId)
//                    }
//
//                }
//            } catch (e: Exception) {
//                requireContext().toast(e.message.toString())
//            }
//        }
//    }


}