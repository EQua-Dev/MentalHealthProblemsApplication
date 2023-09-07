package com.androidstrike.schoolprojects.mentalhealthproblemsapplication.client.clientnotification

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.R
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.databinding.FragmentClientNitificationBaseBinding
import com.androidstrike.schoolprojects.mentalhealthproblemsapplication.utils.enable
import com.google.android.material.tabs.TabLayout

class ClientNotificationBase : Fragment() {

    private var _binding: FragmentClientNitificationBaseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentClientNitificationBaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {




            val tabTitles = listOf(
                "Notification Responses",
                "Meeting Schedules"
            )

            for (i in tabTitles.indices) {
                val tab = clientNotificationBaseTabTitle.newTab()
                val tabView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.client_custom_tab_item, null)

                // Set the tab title
                val tabTitle = tabView.findViewById<TextView>(R.id.client_custom_tab_title)
                tabTitle.text = tabTitles[i]

                // Set the custom view for the tab
                tab.customView = tabView

                clientNotificationBaseTabTitle.addTab(tab)
            }

            clientNotificationBaseTabTitle.tabGravity = TabLayout.GRAVITY_FILL

//            customToolbar = landingScreen.toolBar() as Toolbar
//            customToolbar.title = "News"


            val adapter = //childFragmentManager.let {
                ClientNotificationPagerAdapter(
                    childFragmentManager
                    //activity,
                    // it,
                    //clientBaseTabTitle.tabCount
                )
            // }
            clientNotificationViewPager.adapter = adapter
//            clientBaseTabTitle.setupWithViewPager(clientLandingViewPager)
            //clientLandingViewPager.addOnPageChangeListener(
            //  TabLayout.TabLayoutOnPageChangeListener(
            //    clientBaseTabTitle
            //)
            //)

            //define the functionality of the tab layout
            clientNotificationBaseTabTitle.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    clientNotificationViewPager.currentItem = tab.position
                    clientNotificationBaseTabTitle.setSelectedTabIndicatorColor(resources.getColor(R.color.custom_client_accent_color))
                    clientNotificationBaseTabTitle.setTabTextColors(
                        Color.BLACK,
                        resources.getColor(R.color.custom_client_accent_color)
                    )
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    clientNotificationBaseTabTitle.setTabTextColors(Color.WHITE, Color.BLACK)
                }

                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}