package com.example.jobswype

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storageMetadata
import java.util.UUID

private const val UPLOAD_PP_REQUEST_CODE = 1001
private const val UPLOAD_FILE_REQUEST_CODE = 1002

class SettingsFragment : Fragment() {

    // Firebase instances
    private var storage: FirebaseStorage? = null
    private var storageRef: StorageReference? = null
    private var firestore: FirebaseFirestore? = null
    private var requestCode: Int = 0
    private var auth: FirebaseAuth? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val selectedImageUri = data?.data
                if (selectedImageUri != null) {
                    uploadImageToFirebase(selectedImageUri, requestCode)
                }
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
        val uploadFileButton = view.findViewById<Button>(R.id.uploadResumeorJobButton)
        uploadPPButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            requestCode = UPLOAD_PP_REQUEST_CODE
            pickImageLauncher.launch(intent)
        }
        uploadFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            requestCode = UPLOAD_FILE_REQUEST_CODE
            pickImageLauncher.launch(intent)
        }

        val editUsername = view.findViewById<EditText>(R.id.editUsername)
        val editEmail = view.findViewById<EditText>(R.id.editEmail)
        val editPhone = view.findViewById<EditText>(R.id.editPhone)
        val editPassword = view.findViewById<EditText>(R.id.editPassword)
        val editAboutMe = view.findViewById<EditText>(R.id.editAboutMe)
        val saveButton = view.findViewById<Button>(R.id.saveButton)

        saveButton.setOnClickListener {
            val username = editUsername.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val phone = editPhone.text.toString().trim()
            val password = editPassword.text.toString().trim()
            val aboutMe = editAboutMe.text.toString().trim()

            if (username.isNotEmpty()) {
                editProfileInfo("username", username)
            }
            if (email.isNotEmpty()) {
                editProfileInfo("email", email)
            }
            if (phone.isNotEmpty()) {
                editProfileInfo("phone", phone)
            }
            if (password.isNotEmpty()) {
                editProfileInfo("password", password)
            }
            if (aboutMe.isNotEmpty()) {
                editProfileInfo("aboutme", aboutMe)
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore?.collection("users")?.document(auth?.currentUser?.uid!!)
            ?.let { loadPreview(view, it, savedInstanceState) }
    }

    private fun loadPreview(view: View, userRef: DocumentReference, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val previewFile = view.findViewById<ImageView>(R.id.previewFile)
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        // Récupérer l'ID de l'utilisateur actuel
        userRef.get().addOnSuccessListener { document ->
            val cv = document.getString("cv")
            val job_offer = document.getString("job_offer")
            val role = document.getString("role")
            if (cv != null) {
                Glide.with(requireContext())
                    .load(cv)
                    .into(previewFile)

            } else if (job_offer != null) {
                Glide.with(requireContext())
                    .load(job_offer)
                    .into(previewFile)
            } else {
                if (role == "JobSeeker") {
                    // if the user has not uploaded a CV, load the default drawable image
                    previewFile.setImageResource(R.drawable.cv9)
                } else {
                    previewFile.setImageResource(R.drawable.default_job_offer)
                }
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri?, requestCode: Int) {
        val user = auth?.currentUser

        if (imageUri != null) {
            val imageFileName = UUID.randomUUID().toString()
            val imageRef = storageRef?.child("images/$imageFileName")
            val metadata = storageMetadata {
                setCustomMetadata("userID", user?.uid)
            }

            val uploadTask = imageRef?.putFile(imageUri, metadata)
            uploadTask?.addOnSuccessListener { _ ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    println(requestCode)
                    if (requestCode == UPLOAD_PP_REQUEST_CODE) {
                        savePPDataToFirestore(downloadUrl) // Save image data to Firestore
                    } else if (requestCode == UPLOAD_FILE_REQUEST_CODE) {
                        saveImageDataToFirestore(downloadUrl) // Save image data to Firestore
                    }
                }
            }?.addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Image upload failed: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
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
                            Toast.makeText(
                                requireContext(),
                                "Image data saved to CV",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                requireContext(),
                                "Error saving image data: $e",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    loadPreview(
                        requireView(),
                        userRef,
                        null
                    ) // Mettre à jour l'aperçu après le téléchargement
                } else {
                    userRef.update("job_offer", imageUrl)
                        .addOnSuccessListener {
                            Toast.makeText(
                                requireContext(),
                                "Image data saved to job offer",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                requireContext(),
                                "Error saving image data: $e",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    loadPreview(
                        requireView(),
                        userRef,
                        null
                    ) // Mettre à jour l'aperçu après le téléchargement
                }
            }
                // Handle failure to get user role
                ?.addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Error getting user role: $e",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun savePPDataToFirestore(imageUrl: String) {
        val user = auth?.currentUser
        user?.let {
            val userId = it.uid
            // Create a reference for the user's profile in Firestore
            val userRef = firestore?.collection("users")?.document(userId)
            userRef?.get()?.addOnSuccessListener { documentSnapshot ->
                userRef.update("profilePic", imageUrl)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Image data saved to profilePic",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Error saving image data: $e",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }

    private fun editProfileInfo(field: String, info: String) {
        val user = auth?.currentUser
        user?.let {
            val userId = it.uid
            val userRef = firestore?.collection("users")?.document(userId)
            userRef?.get()?.addOnSuccessListener { documentSnapshot ->
                userRef.update(field, info)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Field(s) updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Error updating field(s): $e",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }
}
