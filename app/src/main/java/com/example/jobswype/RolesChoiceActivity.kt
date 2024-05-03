package com.example.jobswype

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.jobswype.databinding.ActivityRolesChoiceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class RolesChoice : AppCompatActivity() {

    private lateinit var binding: ActivityRolesChoiceBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private var isChoiceMade = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRolesChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        binding.employerButton.setOnClickListener {
            updateRole(currentUser?.uid, "Recruiter")
        }

        binding.employeeButton.setOnClickListener {
            updateRole(currentUser?.uid, "JobSeeker")
        }

        // Intercepte les actions du bouton de retour
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!isChoiceMade) {
                    // Ne rien faire si aucun choix n'a été fait
                    Toast.makeText(
                        this@RolesChoice,
                        "Please make a choice to continue",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                isEnabled = false // Désactive le callback pour autoriser la navigation arrière normale
                onBackPressed() // Effectue la navigation arrière
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }
    private fun updateRole(userId: String?, role: String) {
        userId?.let {
            val userRef = db.collection("users").document(userId)
            val userData = hashMapOf("role" to role)
            userRef.set(userData, SetOptions.merge())
                .addOnSuccessListener {
                    // Check le rôle et renvoie vers la page correspondante
                    isChoiceMade = true
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Ferme l'activité actuelle
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Échec de la mise à jour du rôle: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}
