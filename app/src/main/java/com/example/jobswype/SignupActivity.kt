package com.example.jobswype

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
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
            var username = "none"
            val password = binding.signupPassword.text.toString()
            val confirmPassword = binding.signupConfirm.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    username = email.substring(0, email.indexOf('@')).toString()
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // User created successfully, now add to Firestore
                                val userId = task.result?.user?.uid ?: ""
                                val userMap = hashMapOf(
                                    "userId" to userId,
                                    "email" to email,
                                    "password" to password,
                                    "phone" to "none",
                                    "username" to username,
                                    "profilePic" to "none",
                                    "role" to "none",
                                    "liked" to hashMapOf<String, Boolean>()
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
                                    "Your password is too short, minimum length is 6",
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

        binding.showPassword.setOnClickListener {
            // password visibility
            val isPasswordVisible = binding.signupPassword.transformationMethod == PasswordTransformationMethod.getInstance()
            binding.signupPassword.transformationMethod = if (isPasswordVisible) {
                binding.showPassword.background = AppCompatResources.getDrawable(this, R.drawable.baseline_open_eye_24)
                HideReturnsTransformationMethod.getInstance()
            } else {
                binding.showPassword.background = AppCompatResources.getDrawable(this, R.drawable.baseline_close_eye_24)
                PasswordTransformationMethod.getInstance()
            }
            binding.signupPassword.setSelection(binding.signupPassword.text.length)

            val isConfirmPasswordVisible = binding.signupConfirm.transformationMethod == PasswordTransformationMethod.getInstance()
            binding.signupConfirm.transformationMethod = if (isConfirmPasswordVisible) {
                binding.showPassword.background = AppCompatResources.getDrawable(this, R.drawable.baseline_open_eye_24)
                HideReturnsTransformationMethod.getInstance()
            } else {
                binding.showPassword.background = AppCompatResources.getDrawable(this, R.drawable.baseline_close_eye_24)
                PasswordTransformationMethod.getInstance()
            }
            binding.signupConfirm.setSelection(binding.signupConfirm.text.length)
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