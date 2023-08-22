package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.auth

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentFacilitySignUpBinding
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Facility
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.showProgressDialog
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class FacilitySignUp : Fragment() {

    private lateinit var facilityName: String
    private lateinit var facilityEmail: String
    private lateinit var facilityAddress: String
    private lateinit var facilityAddressLongitude: String
    private lateinit var facilityAddressLatitude: String
    private lateinit var facilityPhoneNumber: String
    private lateinit var facilityPassword: String
    private lateinit var facilityConfirmPassword: String

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder

    var passwordOkay = false
    var emailOkay = false
    var confirmPasswordOkay = false

    private var progressDialog: Dialog? = null


    private var _binding: FragmentFacilitySignUpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFacilitySignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        geocoder = Geocoder(requireContext(), Locale.getDefault())
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())


        binding.signUpFacilityPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                val input = p0.toString()
                if (isPasswordValid(input)) {
                    binding.textInputLayoutSignUpFacilityPassword.error =
                        null // Clear any previous error
                    passwordOkay = true
                } else {
                    binding.textInputLayoutSignUpFacilityPassword.error =
                        "Password must contain at least one digit, uppercase, lowercase, special character and 8 characters" // Display an error message
                }
            }

        })
        binding.signUpFacilityConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                val input = p0.toString()
                if (input == binding.signUpFacilityPassword.text.toString().trim()) {
                    binding.textInputLayoutSignUpFacilityConfirmPassword.error =
                        null // Clear any previous error
                    confirmPasswordOkay = true
                } else {
                    binding.textInputLayoutSignUpFacilityConfirmPassword.error =
                        "Does not match password" // Display an error message
                }
            }

        })
        binding.signUpFacilityEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                val input = p0.toString()
                if (Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
                    binding.textInputLayoutSignUpFacilityEmail.error =
                        null // Clear any previous error
                    emailOkay = true
                } else {
                    binding.textInputLayoutSignUpFacilityEmail.error =
                        "Enter valid email" // Display an error message
                }
            }

        })

        binding.signUpFacilityAddressSelectMyLocation.setOnClickListener {
            getCurrentLocation()
        }

        binding.accountSignupFacilityBtnSignup.setOnClickListener {
            facilityName = binding.signUpFacilityName.text.toString().trim()
            facilityEmail = binding.signUpFacilityEmail.text.toString().trim()
            facilityAddress = binding.signUpFacilityPhysicalAddress.text.toString().trim()
            //facilityAddressLongitude = binding.signUpAddress.text.toString().trim()
            //facilityAddressLatitude = binding.signUpPassword.text.toString().trim()
            facilityPhoneNumber = binding.signUpFacilityPhoneNumber.text.toString().trim()
            facilityPassword = binding.signUpFacilityPassword.text.toString().trim()
            //userAddressLongitude, userAddressLatitude

            registerFacility(
                facilityName,
                facilityEmail,
                facilityAddress,
                facilityAddressLongitude,
                facilityAddressLatitude,
                facilityPhoneNumber,
                facilityPassword
            )
        }


    }

    private fun registerFacility(
        facilityName: String,
        facilityEmail: String,
        facilityAddress: String,
        facilityAddressLongitude: String,
        facilityAddressLatitude: String,
        facilityPhoneNumber: String,
        facilityPassword: String,

        ) {
        val mAuth = FirebaseAuth.getInstance()

        showProgress()
        mAuth.createUserWithEmailAndPassword(facilityEmail, facilityPassword)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val newUserId = mAuth.uid
                    val dateJoined = System.currentTimeMillis().toString()
                    //saves user's details to the cloud db (fireStore)
                    saveFacility(
                        facilityName,
                        facilityEmail,
                        facilityAddress,
                        facilityAddressLongitude,
                        facilityAddressLatitude,
                        facilityPhoneNumber,
                        newUserId!!,
                        dateJoined
                    )
//                    userId = Common.mAuth.currentUser?.uid
                    hideProgress()
                    val navBackToSign =
                        FacilitySignUpDirections.actionFacilitySignUpToSignIn(role = "facility")
                    findNavController().navigate(navBackToSign)
                } else {
                    it.exception?.message?.let { message ->
                        hideProgress()
//                        pbLoading.visible(false)
                        requireActivity().toast(message)
                    }
                }
            }
    }

    private fun saveFacility(
        facilityName: String,
        facilityEmail: String,
        facilityAddress: String,
        facilityAddressLongitude: String,
        facilityAddressLatitude: String,
        facilityPhoneNumber: String,
        newUserId: String,
        dateJoined: String
    ) {
        val facility = getFacility(
            facilityName,
            facilityEmail,
            facilityAddress,
            facilityAddressLongitude,
            facilityAddressLatitude,
            facilityPhoneNumber,
            newUserId,
            dateJoined
        )
        saveNewFacility(facility)

    }

    private fun getFacility(
        facilityName: String,
        facilityEmail: String,
        facilityAddress: String,
        facilityAddressLongitude: String,
        facilityAddressLatitude: String,
        facilityPhoneNumber: String,
        newFacilityId: String,
        dateJoined: String
    ): Facility {
        return Facility(
            facilityName,
            facilityEmail,
            facilityAddress,
            facilityAddressLongitude,
            facilityAddressLatitude,
            facilityPhoneNumber,
            newFacilityId,
            dateJoined
        )
    }

    private fun saveNewFacility(facility: Facility) = CoroutineScope(Dispatchers.IO).launch {
        try {
            Common.facilityCollectionRef.document(facility.organisationId.toString()).set(facility).await()
            //isFirstTime()
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                activity?.toast(e.message.toString())
            }
        }
    }
    fun isPasswordValid(password: String): Boolean {
        val pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$".toRegex()
        return pattern.matches(password)
    }

    private fun getCurrentLocation() {


        Log.d("EQua", "getCurrentLocation: $mFusedLocationClient")
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        // Request location updates
        mFusedLocationClient.lastLocation
            .addOnSuccessListener(requireActivity(), OnSuccessListener<Location> { location ->
                // Handle the retrieved location
                if (location != null) {
                    //userAddressLatitude = location.latitude
                    //userAddressLongitude = location.longitude
                    getAddressFromLocation(location)
                    // Do something with the latitude and longitude values
                }
            })
        //}
//        mFusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
//            val location: Location? = task.result
//
//            Log.d("EQua", "getCurrentLocation: $location")
//
//            val geocoder = Geocoder(requireContext(), Locale.getDefault())
//            val list: List<Address> =
//                geocoder.getFromLocation(location!!.latitude, location.longitude, 1)
//
//            //mUsageLocality = "Locality\n${list[0].locality}"
//            val currentLocation = list[0].subLocality// .getAddressLine(0)
//            userAddressLatitude = location.latitude.toString()
//            userAddressLongitude = location.longitude.toString()
//            binding.signUpAddress.setText(currentLocation)
//        }

    }


    private fun getAddressFromLocation(location: Location) = try {
        val addresses: MutableList<Address>? = geocoder.getFromLocation(
            location.latitude,
            location.longitude,
            1
        )

        if (addresses!!.isNotEmpty()) {
            val address: Address = addresses[0]
            val fullAddress: String = address.getAddressLine(0)
            // Do something with the address
            binding.signUpFacilityPhysicalAddress.setText(fullAddress)
            facilityAddressLongitude = location.longitude.toString()
            facilityAddressLatitude = location.latitude.toString()
        } else {
            requireContext().toast("Location not found!")
        }
    } catch (e: IOException) {
        e.printStackTrace()
        // Handle the exception
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