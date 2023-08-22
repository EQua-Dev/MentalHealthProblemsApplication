package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentForgotPasswordBinding
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.toast
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : Fragment() {

    lateinit var facilityEmail: String

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    val args: ForgotPasswordArgs by navArgs()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.accountForgotPasswordBtnResetPassword.setOnClickListener {
            facilityEmail = binding.forgotPasswordEmail.text.toString()

            val role = args.role

            FirebaseAuth.getInstance().sendPasswordResetEmail(facilityEmail)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Password reset email sent successfully
                        requireContext().toast("Password reset email sent")
                        val navToSignIn = ForgotPasswordDirections.actionForgotPasswordToSignIn(role)
                        findNavController().navigate(navToSignIn)
                    } else {
                        // Error occurred while sending password reset email
                        requireContext().toast("Failed to send password reset email")
                    }
                }
        }//luomyequa@gmail.com
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}