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
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class SettingsFragment : Fragment() {

    // Firebase instances
    private var storage: FirebaseStorage? = null
    private var storageRef: StorageReference? = null
    private var firestore: FirebaseFirestore? = null
    private var auth: FirebaseAuth? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val selectedImageUri = data?.data
            uploadImageToFirebase(selectedImageUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Initialize Firebase instances
        storage = FirebaseStorage.getInstance()
        storageRef = storage?.reference
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val uploadPPButton = view.findViewById<Button>(R.id.uploadPPButton)
        val uploadResumeButton = view.findViewById<Button>(R.id.uploadResumeButton)

        uploadPPButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }
        uploadResumeButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        return view
    }

    private fun uploadImageToFirebase(imageUri: Uri?) {
        if (imageUri != null) {
            val imageFileName = UUID.randomUUID().toString()
            val imageRef = storageRef?.child("images/$imageFileName")

            val uploadTask = imageRef?.putFile(imageUri)
            uploadTask?.addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    saveImageDataToFirestore(downloadUrl) // Save image data to Firestore
                }
            }?.addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageDataToFirestore(imageUrl: String) {
        val user = auth?.currentUser
        user?.let {
            val userId = it.uid

            // Create a reference for the user's profile in Firestore
            val userRef = firestore?.collection("users")?.document(userId)

            // Check user role
            userRef?.get()?.addOnSuccessListener { documentSnapshot ->
                val role = documentSnapshot.getString("role")

                // Update the appropriate field based on the user's role
                if (role == "JobSeeker") {
                    userRef.update("cv", imageUrl)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Image data saved to CV", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Error saving image data: $e", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    userRef.update("job_offer", imageUrl)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Image data saved to job offer", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Error saving image data: $e", Toast.LENGTH_SHORT).show()
                        }
                }
            }
                // Handle failure to get user role
                ?.addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error getting user role: $e", Toast.LENGTH_SHORT).show()
                }
        }
    }

}
