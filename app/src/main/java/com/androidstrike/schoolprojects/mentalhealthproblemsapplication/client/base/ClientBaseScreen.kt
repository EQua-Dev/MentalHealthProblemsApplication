package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.client.base

import android.graphics.Color
import android.os.Bundle
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
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.enable
import com.google.android.material.tabs.TabLayout

class ClientBaseScreen : Fragment() {

    private var _binding: FragmentClientBaseScreenBinding? = null
    private val binding get() = _binding!!

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
        with(binding) {

            (activity as AppCompatActivity).setSupportActionBar(binding.clientToolBar)

            //binding.clientToolBar.title = Common.clientName
             val tabTitles = listOf(
                            "🏠Home Screen",
                            "💳Digital Wallet",
                            "🚨View Notification",
                            "🧾View Invoice",
                            "⭐️Place Feedback"
                        )
            toolBarDropDownMenu.setText(tabTitles[0])
            val menuArrayAdapter =
                ArrayAdapter(requireContext(), R.layout.drop_down_item, tabTitles)
            toolBarDropDownMenu.setAdapter(menuArrayAdapter)

            val adapter = //childFragmentManager.let {
                ClientLandingPagerAdapter(
                    childFragmentManager
                    //activity,
                    // it,
                    //clientBaseTabTitle.tabCount
                )
            clientLandingViewPager.adapter = adapter


            toolBarDropDownMenu.setOnItemClickListener { _, _, position, _ ->
                val selectedItem =
                    menuArrayAdapter.getItem(position) // Get the selected item
                clientLandingViewPager.currentItem = position
            }





//            for (i in tabTitles.indices) {
//                val tab = clientBaseTabTitle.newTab()
//                val tabView = LayoutInflater.from(requireContext())
//                    .inflate(R.layout.client_custom_tab_item, null)
//
//                // Set the tab title
//                val tabTitle = tabView.findViewById<TextView>(R.id.client_custom_tab_title)
//                tabTitle.text = tabTitles[i]
//
//                // Disable clickability for a specific tab title
//                if (i == 3) {
//                    tabTitle.isClickable = false
//                    tab.view.enable(false)
//                    tab.view.isClickable = false
//                }
//
//                // Set the custom view for the tab
//                tab.customView = tabView
//
//                clientBaseTabTitle.addTab(tab)
//            }

//            clientBaseTabTitle.tabGravity = TabLayout.GRAVITY_FILL
//
//            //define the functionality of the tab layout
//            clientBaseTabTitle.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//                override fun onTabSelected(tab: TabLayout.Tab) {
//                    clientLandingViewPager.currentItem = tab.position
//                    clientBaseTabTitle.setSelectedTabIndicatorColor(resources.getColor(R.color.custom_client_accent_color))
//                    clientBaseTabTitle.setTabTextColors(
//                        Color.BLACK,
//                        resources.getColor(R.color.custom_client_accent_color)
//                    )
//                }
//
//                override fun onTabUnselected(tab: TabLayout.Tab) {
//                    clientBaseTabTitle.setTabTextColors(Color.WHITE, Color.BLACK)
//                }
//
//                override fun onTabReselected(tab: TabLayout.Tab) {}
//            })

        }
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