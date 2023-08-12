package com.androidstrike.schoolprojects.mentalhealthproblemsapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentSplashScreenBinding

class SplashScreen : Fragment() {
   
    private var _binding: FragmentSplashScreenBinding? = null
    private val binding get()= _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSplashScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        with(binding) {
            txtSplash.animate().setDuration(2000).alpha(1f).withEndAction {
                val navToSignIn = SplashScreenDirections.actionSplashScreenToLandingFragment()
                findNavController().navigate(navToSignIn)
//
//                if (auth.currentUser != null) {
//                    // User is logged in
//                    // Perform necessary actions
//                    val navToHome = SplashScreenDirections.actionSplashScreenToClientHome(resources.getString(R.string.client))
//                    findNavController().navigate(navToHome)
//                } else {
//                    // User is not logged in
//                    // Redirect to login screen or perform other actions
//
//                }


            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}