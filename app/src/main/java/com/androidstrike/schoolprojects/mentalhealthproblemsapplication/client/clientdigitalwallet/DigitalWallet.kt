package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.client.clientdigitalwallet

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentDigitalWalletBinding
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Client
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.WalletData
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.WalletHistory
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.WALLET_HISTORY_REF
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.auth
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.clientCollectionRef
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.walletCollectionRef
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.WalletBottomSheetListener
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.hashString
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.hideProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.showProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.toast
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.visible
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DigitalWallet : Fragment(), WalletBottomSheetListener {

    private var _binding: FragmentDigitalWalletBinding? = null
    private val binding get() = _binding!!

    private var walletHistoryAdapter: FirestoreRecyclerAdapter<WalletHistory, WalletHistoryAdapter>? =
        null

    private var hasWallet = false

    private val bottomSheetResultKey = "bottomSheetResultKey"


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

        with(binding) {
            walletLayout.visible(false)
            createDigitalWallet.visible(false)
            //noWalletLayout.visible(true)
            val layoutManager = LinearLayoutManager(requireContext())
            rvWalletHistory.layoutManager = layoutManager
            rvWalletHistory.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    layoutManager.orientation
                )
            )
        }
        requireContext().showProgress()
        getWalletDetails()
        hideProgress()

    }

    private fun getWalletDetails() {
        Log.d(TAG, "getWalletDetails: ")
        val user = getUser(auth.uid!!)!!
        //requireContext().showProgress()
        CoroutineScope(Dispatchers.IO).launch {

            if (user.wallet.isEmpty()){
                //hideProgress()
                binding.createDigitalWallet.visible(true)
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
                }
            }else{
                //hideProgress()
                //binding.createDigitalWallet.visible(false)

                walletCollectionRef.document(user.wallet).get().addOnSuccessListener { doc -> 
                   val walletInfo = doc.toObject(WalletData::class.java)
                    binding.walletLayout.visible(true)
                    binding.walletBalance.text = resources.getString(R.string.money_text, walletInfo!!.walletBalance)
                    binding.fundDigitalWallet.setOnClickListener {
                        val bottomSheetFragment = FundWalletBottomSheet.newInstance()
                        bottomSheetFragment.setListener(this@DigitalWallet) // Pass the listener to the BottomSheet
                        bottomSheetFragment.show(childFragmentManager, "bottomSheetTag")
                        //fetchWalletDetails(walletId)
                    }
                    getWalletHistory(walletInfo.walletId)
                }
            }
//            walletCollectionRef//.document(user!!.wallet)
//                .get()
//                .addOnSuccessListener { wallets: QuerySnapshot ->
//                    hideProgress()
//                    if (wallets.isEmpty) {
//                        //no wallets
//                        //show to create new
//                        binding.createDigitalWallet.visible(true)
//                        binding.createDigitalWallet.setOnClickListener {
//                            val newWallet = WalletData(
//                                walletId = hashString(
//                                    "${auth.uid}${
//                                        System.currentTimeMillis().toString()
//                                    }"
//                                ),
//                                walletOwner = auth.uid!!,
//                                walletBalance = "0.0"
//                            )
//                            launchCreateWalletDialog(newWallet)
//                            //create wallet
//                            //createWallet(newWallet, dialog)
//                        }
//
//                    } else {
//                        //binding.noWalletLayout.visible(false)
//
//                        for (doc in wallets.documents) {
//                            val wallet = doc.toObject(WalletData::class.java)
//                            if (wallet?.walletOwner == auth.uid!!) {
//                                hasWallet = true
//
//                                binding.walletLayout.visible(true)
//                                Log.d(TAG, "getWalletDetails: $hasWallet")
//                                //binding.noWalletLayout.visible(false)
//                                fetchWalletDetails(walletId = wallet.walletId)
//                                return@addOnSuccessListener
//                            } else {
//                                //binding.noWalletLayout.visible(true)
//                                hasWallet = false
//                            }
//                        }
//                        Log.d(TAG, "getWalletDetails: $hasWallet")
//                        binding.createDigitalWallet.visible(!hasWallet)
//                        binding.createDigitalWallet.setOnClickListener {
//                            val newWallet = WalletData(
//                                walletId = hashString(
//                                    "${auth.uid}${
//                                        System.currentTimeMillis().toString()
//                                    }"
//                                ),
//                                walletOwner = auth.uid!!,
//                                walletBalance = "0.0"
//                            )
//                            launchCreateWalletDialog(newWallet)
//                            //create wallet
//                        }
//                    }
//                }
        }
    }

    private fun fetchWalletDetails(walletId: String) {
        //binding.noWalletLayout.visible(false)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val snapshot = Common.walletCollectionRef.document(walletId).get().await()
                if (snapshot.exists()) {
                    val walletInfo = snapshot.toObject(WalletData::class.java)
                    withContext(Dispatchers.Main) {
                        hideProgress()
                        binding.walletLayout.visible(true)
                        binding.walletBalance.apply {
                            visible(true)
                            text = resources.getString(
                                R.string.money_text,
                                walletInfo!!.walletBalance.toString()
                            )
                        }
                        binding.fundDigitalWallet.setOnClickListener {
                            val bottomSheetFragment = FundWalletBottomSheet.newInstance()
                            bottomSheetFragment.setListener(this@DigitalWallet) // Pass the listener to the BottomSheet
                            bottomSheetFragment.show(childFragmentManager, "bottomSheetTag")
                            //fetchWalletDetails(walletId)
                        }
                        getWalletHistory(walletId)

                    }

                }
            } catch (e: Exception) {
                hideProgress()
                requireContext().toast(e.message.toString())
            }
        }
    }

    private fun getWalletHistory(walletId: String) {


        val walletHistory =
            walletCollectionRef.document(walletId).collection(WALLET_HISTORY_REF).orderBy(FieldPath.documentId(), Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<WalletHistory>()
            .setQuery(walletHistory, WalletHistory::class.java).build()
        try {
            walletHistoryAdapter = object :
                FirestoreRecyclerAdapter<WalletHistory, WalletHistoryAdapter>(options) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): WalletHistoryAdapter {
                    val itemView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_wallet_history_layout, parent, false)
                    return WalletHistoryAdapter(itemView)
                }

                override fun onBindViewHolder(
                    holder: WalletHistoryAdapter,
                    position: Int,
                    model: WalletHistory
                ) {
                    //val timeToDeliver = model.supposedTime
                    holder.walletTransactionDate.text = model.transactionDate
                    holder.walletTransactionType.text = model.transactionType
                    holder.walletTransactionReason.text = model.transactionReason
                    holder.walletTransactionAmount.text = model.transactionAmount
                }
            }

        } catch (e: Exception) {
            requireActivity().toast(e.message.toString())
        }
        walletHistoryAdapter?.startListening()
        binding.rvWalletHistory.adapter = walletHistoryAdapter


    }


    private fun launchCreateWalletDialog(newWallet: WalletData) {

        val builder =
            layoutInflater.inflate(R.layout.custom_create_new_wallet_details_layout_dialog, null)

        val tilCardNumber =
            builder.findViewById<TextInputLayout>(R.id.text_input_layout_create_wallet_card_number)
        val etCardNumber = builder.findViewById<TextInputEditText>(R.id.create_wallet_card_number)
        val tilCardExpiryMonth =
            builder.findViewById<TextInputLayout>(R.id.text_input_layout_create_wallet_card_expiry_month)
        val etCardExpiryMonth =
            builder.findViewById<TextInputEditText>(R.id.create_wallet_card_expiry_month)
        val tilCardExpiryYear =
            builder.findViewById<TextInputLayout>(R.id.text_input_layout_create_wallet_card_expiry_year)
        val etCardExpiryYear =
            builder.findViewById<TextInputEditText>(R.id.create_wallet_card_expiry_year)
        val tilCardCVV =
            builder.findViewById<TextInputLayout>(R.id.text_input_layout_create_wallet_card_cvv)
        val etCardCVV = builder.findViewById<TextInputEditText>(R.id.create_wallet_card_cvv)
        val tilCardHolderName =
            builder.findViewById<TextInputLayout>(R.id.text_input_layout_create_wallet_card_holder_name)
        val etCardHolderName =
            builder.findViewById<TextInputEditText>(R.id.create_wallet_card_holder_name)
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

            if (cardNumber.isEmpty()) {
                tilCardNumber.error = resources.getString(R.string.card_number_empty)
                etCardNumber.requestFocus()
            }
            if (cardExpiryMonth.isEmpty()) {
                tilCardExpiryMonth.error = resources.getString(R.string.card_month_empty)
                etCardExpiryMonth.requestFocus()
            }
            if (cardExpiryYear.isEmpty()) {
                tilCardExpiryYear.error = resources.getString(R.string.card_year_empty)
                etCardExpiryYear.requestFocus()
            }
            if (cardCVV.isEmpty()) {
                tilCardCVV.error = resources.getString(R.string.card_cvv_empty)
                etCardCVV.requestFocus()
            }
            if (cardHolderName.isEmpty()) {
                tilCardHolderName.error = resources.getString(R.string.card_holder_empty)
                etCardHolderName.requestFocus()
            } else {
                tilCardNumber.error = null
                tilCardExpiryMonth.error = null
                tilCardExpiryYear.error = null
                tilCardCVV.error = null
                tilCardHolderName.error = null
                createWallet(newWallet, dialog)
            }
        }
        dialog.show()

    }

    private fun createWallet(newWallet: WalletData, dialog: AlertDialog) {
        requireContext().showProgress()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                walletCollectionRef.document(newWallet.walletId).set(newWallet)
                    .addOnSuccessListener {
                        clientCollectionRef.document(auth.uid!!)
                            .update("wallet", newWallet.walletId)
                        hideProgress()
                        dialog.dismiss()
                        binding.createDigitalWallet.visible(false)
                        getWalletDetails()
                        //fetchWalletDetails(newWallet.walletId)
                        
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

    private fun getUser(userId: String): Client? {
//        requireContext().showProgress()
        val deferred = CoroutineScope(Dispatchers.IO).async {
            try {
                val snapshot = Common.clientCollectionRef.document(userId).get().await()
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

        val driverUser = runBlocking { deferred.await() }
        //hideProgress()

        return driverUser
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

        val walletData = runBlocking { deferred.await() }
        hideProgress()

        return walletData
    }

    override fun refreshWalletDetails() {
//        val parentFragment = parentFragment as? DigitalWallet
//        parentFragment?.
        getWalletDetails()


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