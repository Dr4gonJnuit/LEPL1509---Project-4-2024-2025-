package com.example.jobswype

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.jobswype.session.LoginSession
import com.example.jobswype.ui.theme.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.launch
import kotlin.math.abs
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions


class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    var storage = Firebase.storage
    var storageRef = storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Navigation Drawer
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this@MainActivity::setNavigationItemSelectedListener)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open_nav,
            R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            navigationView.setCheckedItem(R.id.bottom_home)
        }

        // Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_home -> {
                    replaceFragment(HomeFragment())
                    true
                }

                R.id.bottom_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }

                R.id.bottom_messages -> {
                    replaceFragment(MessagesFragment())
                    true
                }

                else -> false
            }
        }
        replaceFragment(HomeFragment())
    }

    @Override
    fun setNavigationItemSelectedListener(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_settings -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment()).commit()

            R.id.nav_logout -> {
                val loginSession = LoginSession(this)
                loginSession.logoutUser()
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


    @Deprecated("Deprecated in Java")
    @Override
    override fun onBackPressed() {
        super.onBackPressed()
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
            .commit()
    }
}

fun loadUserData(view: View, context: Context) {
    // Initialize views
    val profileImg = view.findViewById<ImageView>(R.id.profileImg)
    val profileUsername = view.findViewById<TextView>(R.id.profileUsername)
    val profileEmail = view.findViewById<TextView>(R.id.profileEmail)
    val profilePhone = view.findViewById<TextView>(R.id.profilePhone)
    val profileAboutMe = view.findViewById<TextView>(R.id.profileAboutMe)

    // Initialize Firebase instances
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    val currentUser = auth.currentUser
    val userId = currentUser?.uid
    val userRef = firestore.collection("users").document(userId!!)

    userRef.get().addOnSuccessListener { user ->
        if (user != null) {
            // Get user data
            val username = user.getString("username")
            val email = user.getString("email")
            val phone = user.getString("phone")
            val aboutMe = user.getString("aboutme")
            val profileImageUrl = user.getString("profilePic")

            // Set user data to views
            profileUsername.text = username
            profileEmail.text = email
            profilePhone.text = phone
            profileAboutMe.text = aboutMe

            // Load profile image using Glide
            profileImageUrl?.let {
                Glide.with(context)
                    .load(it)
                    .apply(RequestOptions.bitmapTransform(CircleCrop())) // Appliquer un cercle de transformation
                    .placeholder(R.drawable.default_pdp) // Placeholder image while loading
                    .error(R.drawable.default_pdp) // Image to show if loading fails
                    .into(profileImg)
                }

            } else {
            // Document does not exist
        }
    }.addOnFailureListener { e ->
        Toast.makeText(
            context,
            "Error updating field(s): $e",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
fun MyAppContent(context: Context) {
    val images = listOf(
        R.drawable.defaultimg,
        R.drawable.cv1, R.drawable.cv2, R.drawable.cv3,
        R.drawable.cv4, R.drawable.cv5, R.drawable.cv6,
        R.drawable.cv7, R.drawable.cv8, R.drawable.cv9,
    )
    var currentIndex by remember { mutableStateOf(1) }
    val image = images[currentIndex]

    // State for tracking drag amount
    var offsetX by remember { mutableStateOf(0f) }
    // Animate back to original position when not dragging
    val coroutineScope = rememberCoroutineScope()
    val animatedOffsetX = remember { Animatable(0f) }
    /*
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        finishedListener = { offsetX = 0f })
    */
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Blue_light)
            .padding(top = 60.dp), // Ajuster la valeur de top selon l'espace souhaité
        contentAlignment = Alignment.TopCenter
    ) {
        Image(
            painter = painterResource(id = image),
            contentDescription = "Swipeable Image",
            modifier = Modifier
                .size(550.dp)
                .graphicsLayer {
                    // Apply translation and rotation based on drag
                    translationX = animatedOffsetX.value
                    rotationZ = animatedOffsetX.value * 0.1f // Slight rotation for effect
                }
                .pointerInput(Unit) {
                    detectDragGestures(onDragEnd = {
                        coroutineScope.launch {
                            animatedOffsetX.animateTo(0f)
                        }
                        if (abs(offsetX) > 200) { // Threshold to consider as swipe
                            // Move to next image
                            if (currentIndex + 1 < images.size && currentIndex != 0)
                                currentIndex = (currentIndex + 1)
                            else
                                // Afficher un message pour dire qu'il n'y a plus d'offres
                                Toast.makeText(
                                    context,
                                    "Il n'y a plus d'offres disponibles",
                                    Toast.LENGTH_SHORT
                                ).show()
                        }
                        offsetX = 0f // Reset drag amount whether swiped or not
                    }) { change, dragAmount ->
                        if (change.positionChange() != Offset.Zero) change.consume()
                        offsetX += dragAmount.x // Update drag amount
                        coroutineScope.launch {
                            animatedOffsetX.snapTo(offsetX)
                        }
                    }
                }
                .clip(RoundedCornerShape(20.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Buttons layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 85.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                animatedOffsetX.animateTo(
                                    -300f,
                                    animationSpec = TweenSpec(durationMillis = 200)
                                ) // target value is -300 because its swiping to left
                                animatedOffsetX.animateTo(
                                    0f,
                                    animationSpec = TweenSpec(durationMillis = 200)
                                )
                                if (currentIndex + 1 < images.size && currentIndex != 0)
                                    currentIndex = (currentIndex + 1)
                                else
                                    // Afficher un message pour dire qu'il n'y a plus d'offres
                                    Toast.makeText(
                                        context,
                                        "Il n'y a plus d'offres disponibles",
                                        Toast.LENGTH_SHORT
                                    ).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Purple80),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Dislike", color = Color.White)
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                animatedOffsetX.animateTo(
                                    300f,
                                    animationSpec = TweenSpec(durationMillis = 200)
                                ) // tweenspeec is custom animation, targetvalue is 300f because swiping to the right
                                animatedOffsetX.animateTo(
                                    0f,
                                    animationSpec = TweenSpec(durationMillis = 200)
                                )
                                if (currentIndex + 1 < images.size && currentIndex != 0)
                                    currentIndex = (currentIndex + 1)
                                else
                                // Afficher un message pour dire qu'il n'y a plus d'offres
                                    Toast.makeText(
                                        context,
                                        "Il n'y a plus d'offres disponibles",
                                        Toast.LENGTH_SHORT
                                    ).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Purple80),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Like", color = Color.White)
                    }
                }

            }
        }
    }
}