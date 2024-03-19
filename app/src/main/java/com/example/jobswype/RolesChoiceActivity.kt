package com.example.jobswype

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.jobswype.databinding.ActivityRolesChoiceBinding
import com.example.jobswype.session.LoginSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class RolesChoice : AppCompatActivity() {

    private lateinit var binding: ActivityRolesChoiceBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRolesChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        Toast.makeText(this, "Choice beginning", Toast.LENGTH_SHORT).show()
        binding.employerButton.setOnClickListener {
            updateRole(currentUser?.uid, "Recrutor")
        }

        binding.employeeButton.setOnClickListener {
            updateRole(currentUser?.uid, "JobSeeker")
        }
    }

    private fun updateRole(userId: String?, role: String) {
        userId?.let {
            val userRef = db.collection("users").document(userId)
            val userData = hashMapOf("role" to role)
            userRef.set(userData, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Rôle mis à jour avec succès", Toast.LENGTH_SHORT).show()
                    // Check le rôle et renvoie vers la page correspondante
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Échec de la mise à jour du rôle: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
