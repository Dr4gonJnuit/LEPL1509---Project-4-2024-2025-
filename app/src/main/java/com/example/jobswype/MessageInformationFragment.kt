package com.example.jobswype

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class MessageInformationFragment(private val message: String, private val redirectInfo: String) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_message_information, container, false)

        // Find id
        val messageInformation = view.findViewById<TextView>(R.id.messageInformation)
        val buttonInformation = view.findViewById<Button>(R.id.buttonInformation)

        // Put correct text on the information
        messageInformation.text = message

        // Put the correct text to the button
        val buttonTextToShow: String = when(redirectInfo) {
            "Settings" -> getString(R.string.settings)
            "Home" -> getString(R.string.home)
            "HomeWithoutCVOffer" -> getString(R.string.home)
            else -> getString(R.string.no_page)
        }
        buttonInformation.text = buttonTextToShow

        buttonInformation.setOnClickListener{
            if (redirectInfo == "Settings") {
                val settingsFragment = SettingsFragment() // Replace SettingsFragment with your actual fragment class
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, settingsFragment) // Replace fragment_container with the ID of the container layout in your activity
                    .addToBackStack(null)
                    .commit()
            }
            if (redirectInfo == "Home") {
                val homeFragment = HomeFragment() // Replace HomeFragment with your actual fragment class
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, homeFragment) // Replace fragment_container with the ID of the container layout in your activity
                    .addToBackStack(null)
                    .commit()
            }
            if (redirectInfo == "HomeWithoutCVOffer") {
                val messageInformationFragment = MessageInformationFragment(getString(R.string.no_swipe_if_no_cv), "Settings")
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, messageInformationFragment) // Replace fragment_container with the ID of the container layout in your activity
                    .addToBackStack(null)
                    .commit()
            }
        }

        return view
    }
}