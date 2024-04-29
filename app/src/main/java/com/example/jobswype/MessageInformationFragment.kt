package com.example.jobswype

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class MessageInformationFragment(private val message: String) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_message_information, container, false)

        val messageinformation = view.findViewById<TextView>(R.id.messageInformation)

        messageinformation.text = message

        return view
    }
}