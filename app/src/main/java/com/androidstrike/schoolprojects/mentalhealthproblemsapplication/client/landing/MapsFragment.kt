package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.client.landing

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentMapsBinding
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.model.Facility
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.hideProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.showProgress
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.QuerySnapshot

class MapsFragment : Fragment(), OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private val facilityList: MutableList<Facility> = mutableListOf()

    private lateinit var bottomSheetDialog: BottomSheetDialog


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.rehab_map) as SupportMapFragment

//        val mapFragment = fragmentManager
  //          ?.findFragmentById(R.id.rehab_map) as SupportMapFragment

        //getRealtimeRehabs()
        mapFragment.getMapAsync(this)
    }

//    private fun getRealtimeRehabs() {
//
//    }

    override fun onMapReady(map: GoogleMap) {
        requireContext().showProgress()
        googleMap = map
        Common.facilityCollectionRef
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                hideProgress()
                for (document in querySnapshot.documents) {
                    val item = document.toObject(Facility::class.java)
                    item?.let {
//                        facilityList.add(it)
                        facilityList.add(it)
                    }
                }

                        // Check if the location permission is granted
                        if (ContextCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            googleMap.isMyLocationEnabled = true
                        }

                Log.d("EQUA", "onMapReady: arrived here")
                for (facility in facilityList) {
                    val markerOptions = MarkerOptions()
                        .position(
                            LatLng(
//                                52.66087,
//                                -8.63479
                                facility.organisationLatitude.toDouble(),
                                facility.organisationLongitude.toDouble()
                            )
                        )
//                        .title("facilityName")
                        .title(facility.organisationName)
//                        .snippet("1 Steamboat Quay, Dock Rd, Limerick, V94 YF84")
                        .snippet(facility.organisationPhysicalAddress)
                    googleMap.addMarker(markerOptions)

                    //Log.d("EQUA", "onMapReady: ${facility.facilityName}")

                }

                // Move the camera to the first location
                val firstLocation = facilityList.firstOrNull()
                firstLocation?.let {
                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                52.66087,
                                -8.63479
//                                it.facilityAddressLatitude.toDouble(),
//                                it.facilityAddressLongitude.toDouble()
                            ), 12f
                        )
                    )
                }

                // Process the fetched itemList
                // e.g., update UI or perform further operations
            }
            .addOnFailureListener { exception: Exception ->
                // Handle any errors that occurred during the fetch
            }

        googleMap.setOnMarkerClickListener { marker ->
            for (facility in facilityList){
                if (marker.title == facility.organisationName){
                    requireContext().toast(facility.organisationEmail)
                    val bottomSheetFragment = MapFacilityDetailBottomSheet.newInstance(facility)
                    bottomSheetFragment.show(childFragmentManager, "bottomSheetTag")
                    //displayBottomSheet(facility)
                }
            }

            true
        }
    }

}