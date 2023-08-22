package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.facility.facilityservicespecialist

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentFacilityAddServiceSpecialistBinding
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Specialists
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.REQUEST_PERMISSION
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.SPECIALISTS
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.auth
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.showProgressDialog
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.snackBar
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FacilityAddServiceSpecialist : Fragment() {

    private var _binding: FragmentFacilityAddServiceSpecialistBinding? = null
    private val binding get() = _binding!!

    lateinit var serviceSpecialistServiceType: String
    lateinit var serviceSpecialistName: String
    lateinit var serviceSpecialistQualification: String
    lateinit var serviceID: String

    var fileUri: Uri? = null;

    private var progressDialog: Dialog? = null

    val REQUEST_IMAGE_PICK = 1

    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentFacilityAddServiceSpecialistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            val newServiceSpecialistCategoryArray =
                resources.getStringArray(R.array.provided_service)
            val newServiceSpecialistCategoryArrayAdapter =
                ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_item,
                    newServiceSpecialistCategoryArray
                )
            facilityAddServiceSpecialistCategory.setAdapter(newServiceSpecialistCategoryArrayAdapter)

            facilityAddSpecialistImage.setOnClickListener {
                checkPermissionAndOpenPicker()
            }

            imagePickerLauncher =
                registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    if (uri != null) {
                        fileUri = uri
                        // Display the selected image in the ImageView
                        facilityAddSpecialistImage.setImageURI(uri)
                    }
                }

            facilityAddServiceSpecialistSubmitButton.setOnClickListener {
                serviceSpecialistServiceType =
                    facilityAddServiceSpecialistCategory.text.toString().trim()
                serviceSpecialistName = facilityAddServiceSpecialistName.text.toString().trim()
                serviceSpecialistQualification =
                    facilityAddServiceSpecialistQualification.text.toString()


                serviceID = System.currentTimeMillis().toString()

                if (serviceSpecialistServiceType.isEmpty() || serviceSpecialistName.isEmpty() || serviceSpecialistQualification.isEmpty()) {
                    requireContext().toast(resources.getString(R.string.missing_fields))
                } else {
                    val newSpecialist = Specialists(
                        qualification = serviceSpecialistQualification,
                        name = serviceSpecialistName,
                        service = serviceSpecialistServiceType,
                        id = serviceID
                    )
                    //requireContext().toast("uploading...")
                    createServiceSpecialist(newSpecialist)
                }

            }

            facilityAddServiceNextServiceSpecialistButton.setOnClickListener {
                facilityAddServiceSpecialistCategory.text.clear()
                facilityAddServiceSpecialistName.text!!.clear()
                facilityAddServiceSpecialistQualification.text!!.clear()
                facilityAddSpecialistImage.setImageResource(R.drawable.ic_person)


                requireContext().toast("Enter new service details")
            }


        }


    }


    private fun checkPermissionAndOpenPicker() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSION
            )
        } else {

            openImagePicker()
        }
    }

    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private fun createServiceSpecialist(newSpecialist: Specialists) {

        //showProgress()
        if (fileUri != null) {
            // on below line displaying a progress dialog when uploading an image.
            val progressDialog = ProgressDialog(requireContext())
            // on below line setting title and message for our progress dialog and displaying our progress dialog.
            progressDialog.setTitle("Uploading...")
            progressDialog.setMessage("Uploading specialist info..")
            progressDialog.show()

            // on below line creating a storage refrence for firebase storage and creating a child in it with
            // random uuid.
            val ref: StorageReference = FirebaseStorage.getInstance().getReference()
                .child(newSpecialist.id)
            // on below line adding a file to our storage.
            ref.putFile(fileUri!!).addOnSuccessListener {
                // this method is called when file is uploaded.
                // in this case we are dismissing our progress dialog and displaying a toast message
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        Common.facilityCollectionRef.document(auth.uid!!).collection(SPECIALISTS)
                            .document(newSpecialist.id).set(newSpecialist)
                        withContext(Dispatchers.Main) {
                            with(binding) {
                                facilityAddServiceSpecialistCategory.text.clear()
                                facilityAddServiceSpecialistName.text!!.clear()
                                facilityAddServiceSpecialistQualification.text!!.clear()
                                facilityAddSpecialistImage.setImageResource(R.drawable.ic_person)

                            }
                            //hideProgress()
                            requireView().snackBar("Specialist Added")
                        }

                        //val appointmentQuery = Common.facilityCollectionRef.whereEqualTo("facilityId", facility.facilityId).get().await()

                        //dismiss bottom sheet

                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            requireContext().toast(e.message.toString())
                        }
                    }
                }


                progressDialog.dismiss()
                //Toast.makeText(applicationContext, "Image Uploaded..", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                // this method is called when there is failure in file upload.
                // in this case we are dismissing the dialog and displaying toast message
                progressDialog.dismiss()
                requireContext().toast("Fail to Upload Image..")
            }

        }
    }

    // on below line creating a function to upload our image.
    fun uploadImage() {
        // on below line checking weather our file uri is null or not.

    }

    private fun showProgress() {
        hideProgress()
        progressDialog = requireActivity().showProgressDialog()
    }

    private fun hideProgress() {
        progressDialog?.let { if (it.isShowing) it.cancel() }
    }


}