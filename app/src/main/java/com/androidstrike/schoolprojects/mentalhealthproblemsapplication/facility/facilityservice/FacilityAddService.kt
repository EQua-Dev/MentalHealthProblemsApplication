package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.facility.facilityservice

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentFacilityAddServiceBinding
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Service
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.auth
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.showProgressDialog
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.snackBar
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.toast
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FacilityAddService : Fragment() {

    private var _binding: FragmentFacilityAddServiceBinding? = null
    private val binding get() = _binding!!

    lateinit var servicePrice: String
    lateinit var serviceName: String
    lateinit var serviceDetails: String
    lateinit var serviceType: String
    lateinit var serviceDiscountedPrice: String
    lateinit var serviceAvailablePlacesOption: String
    lateinit var serviceStartEndDate: String
    lateinit var serviceSchedule: String
    lateinit var serviceID: String


    private val calendar = Calendar.getInstance()

    private var progressDialog: Dialog? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentFacilityAddServiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            val newServiceCategoryArray = resources.getStringArray(R.array.provided_service)
            val newServiceCategoryArrayAdapter =
                ArrayAdapter(requireContext(), R.layout.drop_down_item, newServiceCategoryArray)
            facilityAddServiceCategory.setAdapter(newServiceCategoryArrayAdapter)


            val newServiceAvailablePlacesArray =
                resources.getStringArray(R.array.service_available_places_option)
            val newServiceAvailablePlacesArrayAdapter =
                ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_item,
                    newServiceAvailablePlacesArray
                )
            facilityAddServiceAvailablePlaces.setAdapter(newServiceAvailablePlacesArrayAdapter)

            val newServiceScheduleArray =
                resources.getStringArray(R.array.service_schedule)
            val newServiceScheduleArrayAdapter =
                ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_item,
                    newServiceScheduleArray
                )
            facilityAddServiceSchedule.setAdapter(newServiceScheduleArrayAdapter)


            facilityAddServiceStartEndDate.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    showDatePicker(view)
                }
            }

            facilityAddServiceSubmitButton.setOnClickListener {
                servicePrice = facilityAddServicePrice.text.toString().trim().ifEmpty { "0" }
                serviceName = facilityAddServiceName.text.toString().trim()
                serviceType = facilityAddServiceCategory.text.toString().trim()
                serviceDiscountedPrice = facilityAddServiceDiscountedPrice.text.toString().trim().ifEmpty { "0" }
                serviceAvailablePlacesOption =
                    facilityAddServiceAvailablePlaces.text.toString().trim().ifEmpty { "No" }
                serviceStartEndDate = facilityAddServiceStartEndDate.text.toString().trim()
                serviceSchedule = facilityAddServiceSchedule.text.toString().trim()
                serviceID = System.currentTimeMillis().toString()
                serviceDetails = facilityAddServiceDetails.text.toString().trim()

                validateInputs()
            }

            facilityAddServiceNextServiceButton.setOnClickListener {
                facilityAddServiceCategory.text.clear()
                facilityAddServiceName.text!!.clear()
                facilityAddServicePrice.text!!.clear()
                facilityAddServiceDiscountedPrice.text!!.clear()
                facilityAddServiceAvailablePlaces.text.clear()
                facilityAddServiceStartEndDate.text!!.clear()
                facilityAddServiceSchedule.text!!.clear()
                facilityAddServiceDetails.text!!.clear()

                requireContext().toast("Enter new service details")
            }


        }


    }

    private fun validateInputs() {
        with(binding) {
            textInputLayoutFacilityAddServiceCategory.error = null
            textInputLayoutFacilityAddServiceName.error = null
            textInputLayoutFacilityAddServiceDetails.error = null
            textInputLayoutFacilityAddServiceAvailablePlaces.error = null

            if (serviceType.isEmpty()) {
                textInputLayoutFacilityAddServiceCategory.error = "Select Service Type"
                facilityAddServiceCategory.requestFocus()
                return
            }
//            if (servicePrice.isEmpty()) {
//                textInputLayoutFacilityAddServicePrice.error = "Enter Service Price"
//                facilityAddServicePrice.requestFocus()
//            }
            if (serviceName.isEmpty()) {
                textInputLayoutFacilityAddServiceName.error = "Enter Service Description"
                facilityAddServiceName.requestFocus()
                return
            }
            if (serviceDetails.isEmpty()) {
                textInputLayoutFacilityAddServiceDetails.error =
                    "Enter Service Details"
                facilityAddServiceDetails.requestFocus()
                return
            }
            if (serviceAvailablePlacesOption.isEmpty()) {
                textInputLayoutFacilityAddServiceAvailablePlaces.error =
                    "Select Service Available Places Option"
                facilityAddServiceAvailablePlaces.requestFocus()
                return
            } else {
                textInputLayoutFacilityAddServiceCategory.error = null
                    textInputLayoutFacilityAddServiceName.error = null
                    textInputLayoutFacilityAddServiceDetails.error = null
                    textInputLayoutFacilityAddServiceAvailablePlaces.error = null
                createService()
            }

        }
    }

    private fun createService() {
        showProgress()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val addService = Service(

                    serviceId = serviceID,
                            serviceName = serviceName,
                            serviceType = serviceType,
                            serviceOrganisationOwner = auth.uid!!,
                            serviceDetail = serviceDetails,
                            servicePrice = servicePrice,
                            serviceDiscountedPrice = serviceDiscountedPrice,
                            serviceAvailablePlace = serviceAvailablePlacesOption

                )

                //val appointmentQuery = Common.facilityCollectionRef.whereEqualTo("facilityId", facility.facilityId).get().await()
                Common.servicesCollectionRef.document(serviceID).set(addService)

                withContext(Dispatchers.Main) {
                    with(binding) {
                        facilityAddServiceCategory.text.clear()
                        facilityAddServiceName.text!!.clear()
                        facilityAddServicePrice.text!!.clear()
                        facilityAddServiceDiscountedPrice.text!!.clear()
                        facilityAddServiceAvailablePlaces.text.clear()
                        facilityAddServiceStartEndDate.text!!.clear()
                        facilityAddServiceSchedule.text!!.clear()
                    }
                    hideProgress()
                    requireView().snackBar("Service Added")
                }
                //dismiss bottom sheet

            } catch (e: Exception) {
                requireContext().toast(e.message.toString())
            }
        }
    }

    private fun showDatePicker(view: View) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                // Update the selected date in the calendar instance
                calendar.set(selectedYear, selectedMonth, selectedDay)

                // Perform any desired action with the selected date
                // For example, update a TextView with the selected date
                val formattedDate =
                    SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()).format(calendar.time)
                val serviceScheduleStartDate = view as TextInputEditText
                serviceScheduleStartDate.setText(formattedDate)
            }, year, month, day)

        datePickerDialog.show()
    }

    private fun showProgress() {
        hideProgress()
        progressDialog = requireActivity().showProgressDialog()
    }

    private fun hideProgress() {
        progressDialog?.let { if (it.isShowing) it.cancel() }
    }


}