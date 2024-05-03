package com.example.jobswype

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.example.jobswype.databinding.ActivityLoginBinding
import com.example.jobswype.session.LoginSession
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    lateinit var session: LoginSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        session = LoginSession(this)

        binding.loginButton.setOnClickListener {
            val email = binding.loginEmail.text.toString().trimEnd()
            val password = binding.loginPassword.text.toString()
            // check le role et renvoie vers la page correspondante

            if (email.isNotEmpty() && password.isNotEmpty()) {

                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        session.createLoginSession(password, email)
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Wrong email/password", Toast.LENGTH_SHORT)
                            .show() //or it.exception.toString()
                    }
                }
            } else {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        binding.forgotPassword.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_forgot, null)
            val userEmail = view.findViewById<EditText>(R.id.editBox)

            builder.setView(view)
            val dialog = builder.create()

            view.findViewById<Button>(R.id.btnReset).setOnClickListener {
                compareEmail(userEmail)
                dialog.dismiss()
            }
            view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }
            if (dialog.window != null) {
                dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
            }
            dialog.show()
        }

        binding.showPassword.setOnClickListener {
            // password visibility
            val isPasswordVisible = binding.loginPassword.transformationMethod == PasswordTransformationMethod.getInstance()
            binding.loginPassword.transformationMethod = if (isPasswordVisible) {
                binding.showPassword.background = AppCompatResources.getDrawable(this, R.drawable.baseline_close_eye_24)
                HideReturnsTransformationMethod.getInstance()
            } else {
                binding.showPassword.background = AppCompatResources.getDrawable(this, R.drawable.baseline_open_eye_24)
                PasswordTransformationMethod.getInstance()
            }
            binding.loginPassword.setSelection(binding.loginPassword.text.length)
        }

        binding.signupRedirectText.setOnClickListener {
            val signupIntent = Intent(this, SignupActivity::class.java)
            startActivity(signupIntent)
            finish()
        }

        if (session.isLoggedIn()) {
            val i = Intent(applicationContext, MainActivity::class.java)
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
            finish()
        }
    }

    private fun compareEmail(email: EditText) {
        if (email.text.toString().isEmpty()) {
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()) {
            return
        }
        firebaseAuth.sendPasswordResetEmail(email.text.toString()).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Check your email", Toast.LENGTH_SHORT).show()
            }
        }
    }
}