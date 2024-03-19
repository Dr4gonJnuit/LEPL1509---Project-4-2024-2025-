package com.example.jobswype

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.jobswype.databinding.ActivitySignupBinding
import com.example.jobswype.session.LoginSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    private lateinit var session: LoginSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        session = LoginSession(this)

        binding.signupButton.setOnClickListener {
            val email = binding.signupEmail.text.toString().trimEnd()
            val password = binding.signupPassword.text.toString()
            val confirmPassword = binding.signupConfirm.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // User created successfully, now add to Firestore
                                val userId = task.result?.user?.uid ?: ""
                                val userMap = hashMapOf(
                                    "userId" to userId,
                                    "email" to email,
                                    "role" to "none"
                                )

                                db.collection("users").document(userId).set(userMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this,
                                            "User added to Firestore",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        // Redirect to MainActivity
                                        session.createLoginSession(password, email)
                                        val intent = Intent(this, RolesChoice::class.java)
                                        startActivity(intent)
                                        finish()
                                    }.addOnFailureListener { e ->
                                        Toast.makeText(
                                            this,
                                            "Error adding user to Firestore: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            } else {
                                Toast.makeText(
                                    this,
                                    "Signup failed: ${task.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.loginRedirectText.setOnClickListener {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }

        if (session.isLoggedIn()) {
            val i = Intent(applicationContext, MainActivity::class.java)
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
            finish()
        }
    }
}