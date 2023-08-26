package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.auth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentClientSignUpBinding
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Client
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.hideProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.showProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class ClientSignUp : Fragment() {

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder


    private lateinit var userFirstName: String
    private lateinit var userLastName: String
    private lateinit var userEmail: String
    private lateinit var userPhoneNumber: String
//    private lateinit var userAddress: String
//    private lateinit var userCountryCode: String

    //    private lateinit var userAddressLongitude: String
//    private lateinit var userAddressLatitude: String
    private lateinit var userPassword: String
    private lateinit var userConfirmPassword: String

    private val TAG = "ClientSignUp"

    var firstNameOkay = false
    var lastNameOkay = false
    //var addressOkay = false
    var passwordOkay = false
    var emailOkay = false
    var phoneNumberOkay = false
    var confirmPasswordOkay = false

    private var progressDialog: Dialog? = null


    private var _binding: FragmentClientSignUpBinding? = null
    private val binding get() = _binding!!

    private val locationPermissionCode = 101


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentClientSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        geocoder = Geocoder(requireContext(), Locale.getDefault())
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.signUpFirstName.setOnFocusChangeListener { v, hasFocus ->
            val firstNameLayout = v as TextInputEditText
            val firstNameText = firstNameLayout.text.toString()
            userFirstName = firstNameText
            if (!hasFocus) {
                if (userFirstName.isEmpty()) {
                    binding.textInputLayoutSignUpFirstName.error =
                        "Enter first name" // Display an error message
                } else {
                    binding.textInputLayoutSignUpPassword.error = null // Clear any previous error
                    firstNameOkay = true
                }
            }
        }

        binding.signUpLastName.setOnFocusChangeListener { v, hasFocus ->
            val lastNameLayout = v as TextInputEditText
            val lastNameText = lastNameLayout.text.toString()
            userLastName = lastNameText
            if (!hasFocus) {
                if (userLastName.isEmpty()) {
                    binding.textInputLayoutSignUpLastName.error =
                        "Enter last name" // Display an error message
                } else {
                    binding.textInputLayoutSignUpLastName.error = null // Clear any previous error
                    lastNameOkay = true
                }
            }
        }


        binding.signUpPhoneNumber.setOnFocusChangeListener { v, hasFocus ->
            val phoneNumberLayout = v as TextInputEditText
            val phoneNumberText = phoneNumberLayout.text.toString()
            userPhoneNumber = phoneNumberText
            if (!hasFocus) {
                val isIrishNumber = userPhoneNumber.startsWith("083")|| userPhoneNumber.startsWith("085")|| userPhoneNumber.startsWith("086")|| userPhoneNumber.startsWith("087")|| userPhoneNumber.startsWith("089")


                if (userPhoneNumber.isEmpty() || userPhoneNumber.length < resources.getInteger(R.integer.phone_number_length) || !isIrishNumber) {
                    binding.textInputLayoutSignUpPhoneNumber.error =
                       resources.getString(R.string.invalid_phone_number)
                } else {
                    binding.textInputLayoutSignUpPhoneNumber.error =
                        null // Clear any previous error
                    phoneNumberOkay = true
                }
            }
        }

        binding.signUpPassword.setOnFocusChangeListener { v, hasFocus ->
            val passwordLayout = v as TextInputEditText
            val passwordText = passwordLayout.text.toString().trim()
            userPassword = passwordText
            if (!hasFocus) {
                if (isPasswordValid(passwordText)) {
                    binding.textInputLayoutSignUpPassword.error = null // Clear any previous error
                    passwordOkay = true
                } else {
                    binding.textInputLayoutSignUpPassword.error =
                        resources.getString(R.string.invalid_password) // Display an error message
                }
            }
        }

        binding.signUpConfirmPassword.setOnFocusChangeListener { v, hasFocus ->
            val confirmPasswordLayout = v as TextInputEditText
            val confirmPasswordText = confirmPasswordLayout.text.toString()

            if (!hasFocus) {
                if (confirmPasswordText == userPassword) {
                    binding.textInputLayoutSignUpConfirmPassword.error =
                        null // Clear any previous error
                    confirmPasswordOkay = true
                } else {
                    binding.textInputLayoutSignUpConfirmPassword.error =
                        resources.getText(R.string.password_does_not_match) // Display an error message
                }
            }
        }

        binding.signUpEmail.setOnFocusChangeListener { v, hasFocus ->
            val emailLayout = v as TextInputEditText
            val emailText = emailLayout.text.toString()
            userEmail = emailText
            if (!hasFocus) {
                if (Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                    binding.textInputLayoutSignUpEmail.error = null // Clear any previous error
                    emailOkay = true
                } else {
                    binding.textInputLayoutSignUpEmail.error =
                        resources.getString(R.string.invalid_email) // Display an error message
                }
            }
        }


        binding.accountSignupBtnSignup.setOnClickListener {

//            Log.d(TAG, "onViewCreated: $userCountryCode")

            userFirstName = binding.signUpFirstName.text.toString().trim()
            userLastName = binding.signUpLastName.text.toString().trim()
            userEmail = binding.signUpEmail.text.toString().trim()
            userPassword = binding.signUpPassword.text.toString().trim()
            userConfirmPassword = binding.signUpConfirmPassword.text.toString().trim()
            userPhoneNumber = binding.signUpPhoneNumber.text.toString().trim()
            //userAddressLongitude, userAddressLatitude

//            if (userCountryCode != Common.IRISH_CODE)
//                requireContext().toast(resources.getString(R.string.irish_number_required))

            if (!firstNameOkay || !lastNameOkay || !emailOkay || !phoneNumberOkay || !passwordOkay || !confirmPasswordOkay) {
                requireContext().toast("Invalid input")
            }
            if (
                userFirstName.isEmpty() ||
                userLastName.isEmpty() ||
                userEmail.isEmpty() ||
                userPassword.isEmpty() ||
                userConfirmPassword.isEmpty() ||
                userPhoneNumber.isEmpty()
            ) {
                requireContext().toast("Missing fields")
            } else {
                registerClient(
                    userFirstName,
                    userLastName,
                    userEmail,
                    userPassword,
                    //userAddressLongitude, userAddressLatitude,
                    userPhoneNumber
                )
            }


        }


    }

    private fun registerClient(
        userFirstName: String,
        userLastName: String,
        userEmail: String, userPassword: String,
//        userAddressLongitude: String,
//        userAddressLatitude: String,
        userPhoneNumber: String
    ) {
        val mAuth = FirebaseAuth.getInstance()
        requireContext().showProgress()
        mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val newUserId = mAuth.uid
                    val dateJoined = System.currentTimeMillis().toString()
                    //saves user's details to the cloud db (fireStore)
                    saveUser(
                        userFirstName,
                        userLastName,
                        userEmail,
//                        userAddressLongitude,
//                        userAddressLatitude,
                        newUserId!!,
                        dateJoined,
                        userPhoneNumber
                    )
//                    userId = Common.mAuth.currentUser?.uid
                    hideProgress()
                    val navBackToSign =
                        ClientSignUpDirections.actionClientSignUpToSignIn(role = "client")
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

    private fun saveUser(
        userFirstName: String,
        userLastName: String,
        userEmail: String,
//        userAddressLongitude: String,
//        userAddressLatitude: String,
        newUserId: String,
        dateJoined: String,
        userPhoneNumber: String
    ) {
        val client = getClientUser(
            userFirstName,
            userLastName,
            userEmail,
//            userAddressLongitude,
//            userAddressLatitude,
            newUserId,
            dateJoined,
            userPhoneNumber
        )
        saveNewClient(client)
    }

    private fun saveNewClient(client: Client) = CoroutineScope(Dispatchers.IO).launch {
        try {
            Common.clientCollectionRef.document(client.customerID.toString()).set(client).await()
            //isFirstTime()
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                activity?.toast(e.message.toString())
            }
        }
    }

    private fun getClientUser(
        userFirstName: String,
        userLastName: String,
        userEmail: String,
//        userAddressLongitude: String,
//        userAddressLatitude: String,
        newUserId: String,
        dateJoined: String,
        userPhoneNumber: String
    ): Client {
        return Client(
            customerID = newUserId,
            customerFirstName = userFirstName,
            customerLastName = userLastName,
            customerEmail = userEmail,
            //userAddressLongitude = userAddressLongitude,
            //userAddressLatitude = userAddressLatitude,
            dateJoined = dateJoined,
            customerMobileNumber = userPhoneNumber
        )
    }

//    private fun getAddressFromLocation(location: Location) = try {
//        val addresses: List<Address> = geocoder.getFromLocation(
//            location.latitude,
//            location.longitude,
//            1
//        )!!
//
//        if (addresses.isNotEmpty()) {
//            val address: Address = addresses[0]
//            val fullAddress: String = address.getAddressLine(0)
//            // Do something with the address
////            binding.signUpAddress.setText(fullAddress)
////            userAddressLongitude = location.longitude.toString()
////            userAddressLatitude = location.latitude.toString()
//        } else {
//            requireContext().toast("Location not found!")
//        }
//    } catch (e: IOException) {
//        e.printStackTrace()
//        // Handle the exception
//    }


    private fun isPasswordValid(password: String): Boolean {
        val pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$".toRegex()
        return pattern.matches(password)
    }

    private fun checkLocationPermission() {
        requireContext().showProgress()
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is granted, you can request location updates.
            getLocation()
        } else {
            hideProgress()
            // Request location permission
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }
    }

    // Override onRequestPermissionsResult to handle the permission request result.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can request location updates.
                getLocation()
            } else {
                // Permission denied, handle this case as needed.
                // For example, show a message to the user or disable location functionality.
                hideProgress()
                showLocationPermissionRationaleDialog()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                // Use the location object to get latitude and longitude.
//                userAddressLatitude = location.latitude.toString()
//                userAddressLongitude = location.longitude.toString()

                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val list: MutableList<Address>? =
                    geocoder.getFromLocation(location.latitude, location.longitude, 1)

                //mUsageLocality = "Locality\n${list[0].locality}"
                val currentLocation = getAddressFromLocation(location.latitude, location.longitude)
//            userAddressLatitude = location.latitude.toString()
//            userAddressLongitude = location.longitude.toString()
//                binding.signUpAddress.setText(
//                    getAddressFromLocation(
//                        location.latitude,
//                        location.longitude
//                    )
//                )
                Log.d(TAG, "getLocation: $currentLocation")
                Log.d(TAG, "getLocation: ${location.latitude}")
                hideProgress()


                //navigateToLocation(latitude, longitude)

                // Now you have the current location. You can use it as needed.
                // For example, show it on a map or send it to your server.
            } else {
                // Location is null, handle this case as needed.
                // For example, show an error message or request location updates.
                hideProgress()
                requireContext().toast(resources.getString(R.string.cannot_get_address_auto))
            }
        }.addOnFailureListener { exception: Exception ->
            // Handle the failure to get the location.
            // For example, show an error message or request location updates.
            hideProgress()
            requireContext().toast(exception.message.toString())
        }
    }

    private fun showLocationPermissionRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Location Permission")
            .setMessage("This app needs your location to provide relevant content.")
            .setPositiveButton("Grant") { dialog, which ->
                // Request permission after user consent
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    locationPermissionCode
                )
                //getLocation()
            }
            .setNegativeButton("Deny") { dialog, which ->
                // Handle denial if needed
                requireContext().toast("Location permission denied. Cannot fetch location")

            }
            .show()
    }


    private fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        var addressText = ""

        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address: Address = addresses[0]
                val addressParts = ArrayList<String>()

                for (i in 0..address.maxAddressLineIndex) {
                    addressParts.add(address.getAddressLine(i))
                }

                addressText = addressParts.joinToString(separator = "\n")
            } else {
                addressText = "No address found"
            }
        } catch (e: IOException) {
            e.printStackTrace()
            addressText = "Error fetching address"
        }

        return addressText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}