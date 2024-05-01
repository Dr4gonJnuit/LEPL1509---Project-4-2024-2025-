package com.example.jobswype

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {
    private lateinit var dialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val buttonSettingsRedirect = view.findViewById<Button>(R.id.settingsRedirect)
        buttonSettingsRedirect.text = getString(R.string.go_to_setting)

        buttonSettingsRedirect.setOnClickListener {
            val settingsFragment = SettingsFragment() // Replace SettingsFragment with your actual fragment class
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, settingsFragment) // Replace fragment_container with the ID of the container layout in your activity
                .addToBackStack(null)
                .commit()
        }

        loadUserData(view, requireContext())
        return view
    }
}
