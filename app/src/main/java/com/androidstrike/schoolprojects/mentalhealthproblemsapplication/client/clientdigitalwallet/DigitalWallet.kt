package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.client.clientdigitalwallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DigitalWallet : Fragment() {

    private var _binding: FragmentDigitalWalletBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDigitalWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            //create wallet
            createWallet(newWallet)
        }
        binding.fundDigitalWallet.setOnClickListener {
            val bottomSheetFragment = FundWalletBottomSheet.newInstance()
            bottomSheetFragment.show(childFragmentManager, "bottomSheetTag")
        }
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