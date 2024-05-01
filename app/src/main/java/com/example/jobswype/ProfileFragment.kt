package com.example.jobswype

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private var firestore: FirebaseFirestore? = null
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firestore?.collection("users")?.document(auth.currentUser?.uid!!)
            ?.let { loadPreview(view, it, savedInstanceState) }
    }

    private fun loadPreview(view: View, userRef: DocumentReference, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val previewFile = view.findViewById<ImageView>(R.id.cvoPicture)
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
                    previewFile.setImageResource(R.drawable.default_cv)
                } else {
                    previewFile.setImageResource(R.drawable.default_job_offer)
                }
            }
        }
    }
}
