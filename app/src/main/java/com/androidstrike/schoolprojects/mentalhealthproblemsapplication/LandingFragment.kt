package com.androidstrike.schoolprojects.mentalhealthproblemsapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentLandingBinding

class LandingFragment : Fragment() {

    private var _binding: FragmentLandingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLandingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.landingButtonClientRole.setOnClickListener {
            val navToLogin = LandingFragmentDirections.actionLandingFragmentToSignIn("client")
            findNavController().navigate(navToLogin)
        }

        binding.landingButtonFacilityRole.setOnClickListener {
            val navToLogin = LandingFragmentDirections.actionLandingFragmentToSignIn("facility")
            findNavController().navigate(navToLogin)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}