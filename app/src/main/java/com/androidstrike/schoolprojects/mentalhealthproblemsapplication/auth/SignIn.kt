package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.auth

import android.app.Dialog
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentSignInBinding
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.clientName
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.enable
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.showProgressDialog
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.toast
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.visible
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignIn : Fragment() {

    lateinit var email: String
    lateinit var password: String

    private var progressDialog: Dialog? = null

    val args: SignInArgs by navArgs()
    lateinit var role: String

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

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
        showProgress()
        val mAuth = Firebase.auth
        email.let { mAuth.signInWithEmailAndPassword(it, password) }
            .addOnCompleteListener { it ->
                if (it.isSuccessful) {
                    //login success
                    //Log.d("Equa", "signIn: ${Common.userId}")
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            //val querySnapshot =Common.userCollectionRef.document(Common.userId.toString()).get().await()//.whereEqualTo("uid", Common.userId!!).get().await()
                            //Common.userName = querySnapshot["name"].toString()
//                            }
//                            Log.d("Equa", "signIn: ${Common.userOccupation}")
                            withContext(Dispatchers.Main){
//                                pbLoading.visible(false)
                                hideProgress()

                                if (role == "client"){
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val clientRef = Common.clientCollectionRef.document(mAuth.uid.toString())
                                        clientRef.get()
                                            .addOnSuccessListener { documentSnapshot ->
                                                if (documentSnapshot.exists()) {
                                                    clientName = documentSnapshot.getString("userFirstName")!!
                                                    // Use the value here
                                                }
                                            }
                                            .addOnFailureListener { exception ->
                                                // Handle the failure case here
                                            }
                                        withContext(Dispatchers.Main){
                                        }
                                    }
//                                    requireContext().toast("Sign In Success")
                                    val navToHome = SignInDirections.actionSignInToClientBaseScreen()
                                    findNavController().navigate(navToHome)
                                }else{
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val facilityRef = Common.facilityCollectionRef.document(
                                            Common.auth.uid.toString())
                                        facilityRef.get()
                                            .addOnSuccessListener { documentSnapshot ->
                                                if (documentSnapshot.exists()) {
                                                    Common.facilityName = documentSnapshot.getString("facilityName")!!
                                                    // Use the value here
                                                }
                                            }
                                            .addOnFailureListener { exception ->
                                                // Handle the failure case here
                                            }
                                    }
                                    requireContext().toast("Sign In Success")
//                                    val navToFacilityHome = SignInDirections.actionSignInToFacilityBaseScreen()
//                                    findNavController().navigate(navToFacilityHome)

                                }

                            }


                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                hideProgress()
                                //pbLoading.visible(false)
                                requireActivity().toast(e.message.toString())
                                //Log.d("Equa", "signIn: ${e.message.toString()}")
                            }
                        }
                    }

//                    Common.currentUser = firebaseUser?.uid!!
                } else {
                    //pbLoading.visible(false)
                    hideProgress()
                    activity?.toast(it.exception?.message.toString())
                }
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