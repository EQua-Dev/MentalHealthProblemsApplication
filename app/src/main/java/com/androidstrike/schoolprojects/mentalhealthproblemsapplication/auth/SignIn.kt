package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.auth

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentSignInBinding
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Client
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Facility
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.auth
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.clientName
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.facilityName
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.enable
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.hideProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.showProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.showProgressDialog
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.snackBar
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.toast
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.visible
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignIn : Fragment() {

    lateinit var email: String
    lateinit var password: String

    private var progressDialog: Dialog? = null

    val args: SignInArgs by navArgs()
    lateinit var role: String

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    private val TAG = "SignIn"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.accountLogInBtnLogin.enable(false)
        role = args.role

        if (role == "facility"){
            binding.tvSignInTitle.text = resources.getString(R.string.facility_sign_in_title)
            binding.accountLogInCreateAccount.visible(false)
            binding.accountLogInBtnLogin.setBackgroundColor(resources.getColor(R.color.custom_facility_accent_color))
            //binding.textInputLayoutSignInEmail.hintTextColor = ContextCompat.getColor(requireContext(), R.color.custom_facility_accent_color)
        }
        binding.accountLogInCreateAccount.setOnClickListener {
            if (role == "client"){
                val navToClientSignUp = SignInDirections.actionSignInToClientSignUp()
                findNavController().navigate(navToClientSignUp)
            }
            else{
                val navToFacilitySignUp = SignInDirections.actionSignInToFacilitySignUp()
                findNavController().navigate(navToFacilitySignUp)
            }


        }
        binding.accountLogInForgotPasswordPrompt.setOnClickListener {
            val navToForgotPassword = SignInDirections.actionSignInToForgotPassword(role)
            findNavController().navigate(navToForgotPassword)
        }

        binding.signInPassword.addTextChangedListener {
            email = binding.signInEmail.text.toString().trim()
            password = it.toString().trim()
            binding.accountLogInBtnLogin.enable(email.isNotEmpty() && password.isNotEmpty())
        }

        binding.accountLogInBtnLogin.setOnClickListener {
                signIn(email, password)

        }
    }


    private fun signIn(email: String, password: String) {
        requireContext().showProgress()
        val mAuth = Firebase.auth
        email.let { mAuth.signInWithEmailAndPassword(it, password) }
            .addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    //login success
                    //Log.d("Equa", "signIn: ${Common.userId}")
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            withContext(Dispatchers.Main){
//                                pbLoading.visible(false)
                                hideProgress()

                                if (role == "client"){
                                    clientName = getCustomer(auth.uid!!)!!.customerFirstName
//                                    requireContext().toast("Sign In Success")
                                    val navToPhoneVerification =
                                        SignInDirections.actionSignInToPhoneVerification(role = role, getCustomer(auth.uid!!)!!.customerMobileNumber)
                                    findNavController().navigate(navToPhoneVerification)
//                                    val navToHome = SignInDirections.actionSignInToClientBaseScreen()
//                                    findNavController().navigate(navToHome)
                                }else{
                                    facilityName = getOrganisation(auth.uid!!)!!.organisationName

                                    val navToPhoneVerification =
                                        SignInDirections.actionSignInToPhoneVerification(role = role, getOrganisation(auth.uid!!)!!.organisationPhoneNumber)
                                    findNavController().navigate(navToPhoneVerification)
//                                    val navToFacilityHome = SignInDirections.actionSignInToFacilityBaseScreen()
//                                    findNavController().navigate(navToFacilityHome)

                                }

                            }


                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                hideProgress()
                                //pbLoading.visible(false)
                                requireActivity().toast(e.message.toString())
                                Log.d(TAG, "signIn: ${e.message.toString()}")
                            }
                        }
                    }

//                    Common.currentUser = firebaseUser?.uid!!
                } else {
                    //pbLoading.visible(false)
                    hideProgress()
                    requireView().snackBar(resources.getString(R.string.user_records_not_found))
                    //activity?.toast(it.exception?.message.toString())
                    Log.d(TAG, "signIn: ${it.exception?.message.toString()}")
                }
            }
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

        val customer = runBlocking { deferred.await() }
        hideProgress()
        return customer
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}