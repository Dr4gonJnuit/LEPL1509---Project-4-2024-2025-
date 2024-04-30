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
        buttonInformation.text = redirectInfo

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
                val messageInformationFragment = MessageInformationFragment("Add cv/offer, to be able to swipe", "Settings")
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, messageInformationFragment) // Replace fragment_container with the ID of the container layout in your activity
                    .addToBackStack(null)
                    .commit()
            }
        }

        return view
    }
}