package com.example.jobswype

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Button
import android.content.Intent
import android.provider.MediaStore
import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
class SettingsFragment : Fragment() {




    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val selectedImageUri = data?.data
            // Now you can use the selected image URI as needed (e.g., display in ImageView)
            Toast.makeText(requireContext(), "Image selected: $selectedImageUri", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)


        val uploadPPButton = view.findViewById<Button>(R.id.uploadPPButton)
        val uploadResumeButton = view.findViewById<Button>(R.id.uploadResumeButton)


        uploadPPButton.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)

        }
        uploadResumeButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        return view
    }
}