package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.client.base

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentClientBaseScreenBinding
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.Common.auth
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.NavigationMenuListener
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.enable
import com.google.android.material.tabs.TabLayout

class ClientBaseScreen : Fragment() {

    private var _binding: FragmentClientBaseScreenBinding? = null
    private val binding get() = _binding!!
private val TAG = "ClientBaseScreen"

    val tabTitles = listOf(
        "🏠Home Screen",
        "💳Digital Wallet",
        "🚨View Notification",
        "🧾View Invoice",
        "⭐️Place Feedback"
    )
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentClientBaseScreenBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity).setSupportActionBar(binding.clientToolBar)

        with(binding) {

            toolBarDropDownMenu.setText(tabTitles[0])


            val menuArrayAdapter =
                ArrayAdapter(requireContext(), R.layout.drop_down_item, tabTitles)
            toolBarDropDownMenu.setAdapter(menuArrayAdapter)

//            toolBarDropDownMenu.setText(menuArrayAdapter.getItem(position), false)


            val adapter = //childFragmentManager.let {
                ClientLandingPagerAdapter(
                    childFragmentManager
                    //activity,
                    // it,
                    //clientBaseTabTitle.tabCount
                )
            clientLandingViewPager.adapter = adapter

//            Log.d(TAG, "dropdown position: ${toolBarDropDownMenu.adapter.getPosition}")

            toolBarDropDownMenu.setOnItemClickListener { _, _, position, _ ->
                val selectedItem =
                    menuArrayAdapter.getItem(position) // Get the selected item
                //toolBarDropDownMenu.selectedItemText = selectedItem
                clientLandingViewPager.currentItem = position
            }

        }

        //binding.clientToolBar.title = Common.clientName

    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_logout, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                auth.signOut()
                val navToStart =
                    ClientBaseScreenDirections.actionClientBaseScreenToLandingFragment()
                findNavController().navigate(navToStart)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}