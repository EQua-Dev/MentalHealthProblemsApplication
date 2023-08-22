package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.facility.facilityprofile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentFacilityProfileBinding
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Facility
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Service
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.auth
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.toast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FacilityProfile : Fragment() {
    private var _binding: FragmentFacilityProfileBinding? = null
    private val binding get() = _binding!!

    lateinit var facility: Facility

    var serviceNames: MutableList<String> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFacilityProfileBinding.inflate(inflater, container, false)
        return binding.root    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mAuth = FirebaseAuth.getInstance()
        getOrganisationDetails()




        binding.facilityEditProfileBtn.setOnClickListener {
            launchEditProfileDialog(facility)
        }


    }

    private fun getOrganisationDetails() {
        facility = Facility()
        Common.facilityCollectionRef
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->

                Log.d("EQUA", "getRealtimeRehabs: $querySnapshot")
                for (document in querySnapshot.documents) {
                    Log.d("EQUA", "getRealtimeRehabs: $document")
                    val item = document.toObject(Facility::class.java)
                    if (item?.organisationId == auth.uid) {
                        facility = item!!
                        //Log.d("EQUA", "onViewCreatedItem: $item")
                        //Log.d("EQUA", "onViewCreatedFacility: $facility")
                    }
                    //getServiceDetails(facility.organisationId)
                    Log.d("EQUA", "onViewCreatedFacility: $facility")

                }

                binding.txtFacilityProfileName.text = facility.organisationName
                binding.txtFacilityProfileAddress.text = facility.organisationPhysicalAddress
                binding.txtFacilityProfileEmail.text = facility.organisationEmail
                binding.txtFacilityProfilePhone.text = facility.organisationPhoneNumber
                //binding.txtFacilityProfileDateJoined.text = getDate(facility.dateJoined.toLong(), "dd MMMM, yyyy")

            }

    }

//    private fun getServiceDetails(facilityId: String) = CoroutineScope(Dispatchers.IO).launch {
//        Common.servicesCollectionRef
//            .get()
//            .addOnSuccessListener { querySnapshot: QuerySnapshot ->
//
//                var privateService = Service()
//               // var serviceList: MutableList<Service> = mutableListOf()
//                for (document in querySnapshot.documents) {
//                    privateService = document.toObject(Service::class.java)!!
//                    if (privateService.serviceOwnerID == facilityId) {
//                        Log.d("EQUA", "getServiceDetails: ${privateService.serviceDescription}")
//                        serviceNames.add(privateService.serviceDescription)
////                        for (name in serviceNames) {
////                            if (privateService.serviceDescription != name)
////                                serviceNames.add(privateService.serviceDescription)
////                        }
//                        //serviceList.add(privateService)
//                    }
//                }
//
////                    if (privateService.serviceOwnerID == facilityId) {
////                        serviceNames.add(privateService.serviceDescription)
////
////                        //privateServiceName = item.serviceDescription
////
////                }
//
//
//                Log.d("EQUA", "getServiceDetails: $serviceNames")
////                for(service in serviceNames.distinct()){
////                    binding.txtFacilityProfileServices.append("$service\n")
////                }
//            }
//    }


    private fun launchEditProfileDialog(facility: Facility) {

        val builder = layoutInflater.inflate(R.layout.facility_update_profile_dialog_layout, null)

        var newPhoneNumber: String = ""

        val tilNewPhoneNumber = builder.findViewById<TextInputLayout>(R.id.text_input_layout_facility_update_profile_phone_number)
        val etNewPhoneNumber = builder.findViewById<TextInputEditText>(R.id.facility_update_profile_phone_number)
        val tvFacilityName = builder.findViewById<TextView>(R.id.facility_update_profile_name_text)
        val tvFacilityAddress = builder.findViewById<TextView>(R.id.facility_update_profile_address_text)

        val btnSubmitUpdate = builder.findViewById<Button>(R.id.facility_submit_update_profile)
        val btnCancelUpdate = builder.findViewById<Button>(R.id.facility_cancel_update_profile)

        tvFacilityName.text = facility.organisationName
        tvFacilityAddress.text = facility.organisationPhysicalAddress

        val dialog = AlertDialog.Builder(requireContext())
            .setView(builder)
            .setCancelable(false)
            .create()

        btnSubmitUpdate.setOnClickListener {
            newPhoneNumber = etNewPhoneNumber.text.toString().trim()

            val isIrishNumber = newPhoneNumber.startsWith("083")|| newPhoneNumber.startsWith("085")|| newPhoneNumber.startsWith("086")|| newPhoneNumber.startsWith("087")|| newPhoneNumber.startsWith("089")


            if (newPhoneNumber.isEmpty()|| newPhoneNumber.length < resources.getInteger(R.integer.phone_number_length) || !isIrishNumber){
                tilNewPhoneNumber.error = resources.getString(R.string.invalid_phone_number)
                etNewPhoneNumber.requestFocus()
            }
            else{
                updateProfile(newPhoneNumber, facility, dialog)
            }

        }

        btnCancelUpdate.setOnClickListener {
            dialog.dismiss()
        }



        dialog.show()


    }

    private fun updateProfile(
        newNumber: String,
        model: Facility,
        dialog: AlertDialog
    ) = CoroutineScope(Dispatchers.IO).launch {
        val documentRef = Common.facilityCollectionRef.document(model.organisationId)

        val updates = hashMapOf<String, Any>(
            "organisationPhoneNumber" to newNumber,
        )

        documentRef.update(updates)
            .addOnSuccessListener {
                // Update successful
                requireContext().toast("Update successful")
                getOrganisationDetails()
                dialog.dismiss()

            }
            .addOnFailureListener { e ->
                // Handle error
                requireContext().toast(e.message.toString())
            }

    }

}