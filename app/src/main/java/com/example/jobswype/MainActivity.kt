package com.example.jobswype

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import coil.compose.rememberAsyncImagePainter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.jobswype.session.LoginSession
import com.example.jobswype.ui.theme.Blue_dark
import com.example.jobswype.ui.theme.Blue_light
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.launch
import kotlin.math.abs


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

        // Add Contacts to Navigation Drawer if the user have contacts
        addContactsMenu(context = applicationContext, navigationView)


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
            R.id.nav_settings -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, SettingsFragment()).commit()
            }

            R.id.nav_logout -> {
                AlertDialog.Builder(this) // Use 'this' (activity's context) instead of 'context'
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes") { _, _ ->
                        FirebaseAuth.getInstance().signOut()
                        val loginSession = LoginSession(this)
                        loginSession.logoutUser()
                        finish()
                    }
                    .setNegativeButton("No", null)
                    .show() // Show the AlertDialog
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
            val email = user.getString("email")

            var username = user.getString("username")
            if (username == "none") {
                username = email?.substring(0, email.indexOf('@'))
            }

            var phone = user.getString("phone")
            if (phone == "none") {
                phone = ""
            }

            var aboutMe = user.getString("aboutme")
            if (aboutMe == "none") {
                aboutMe = ""
            }
            val profileImageUrl = user.getString("profilePic")


            // Load profile image using Glide
            profileImageUrl?.let {
                Glide.with(context)
                    .load(it)
                    .apply(RequestOptions.bitmapTransform(CircleCrop())) // Apply a transform circle
                    .placeholder(R.drawable.default_pdp) // Placeholder image while loading
                    .error(R.drawable.default_pdp) // Image to show if loading fails
                    .into(profileImg)
            }
            // Set user data to views
            profileUsername.text = username
            profileEmail.text = email
            profilePhone.text = phone
            profileAboutMe.text = aboutMe


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

// Update liked users dynamically
fun saveMatch(
    context: Context,
    firestore: FirebaseFirestore,
    recruiter: String,
    jobSeeker: String
) {
    val combinedId = recruiter + jobSeeker
    val match = hashMapOf(
        "Recruiter" to recruiter,
        "JobSeeker" to jobSeeker,
        "matchId" to combinedId
    )

    firestore.collection("matchmaking")
        .document(combinedId)
        .set(match)
        .addOnSuccessListener {
            Toast.makeText(context, "It's a Match!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Fail to Match", Toast.LENGTH_SHORT).show()
        }
}

fun saveUserLiked(context: Context, imageUrl: String, liked: Boolean) {
    // Initialize Firebase instances
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val storageRef = Firebase.storage

    val currentUser = auth.currentUser
    val userId = currentUser?.uid
    val userRef = firestore.collection("users").document(userId!!)

    // Get user profiles liked
    userRef.get().addOnSuccessListener { userData ->
        val userRole = userData.get("role")
        val likedMap = userData.get("liked") as? HashMap<String, Boolean> ?: hashMapOf()

        // Get the image reference from the Firebase Storage URL
        val imageRef = imageUrl.let { storageRef.getReferenceFromUrl(it) }

        // Retrieve metadata for the image
        imageRef.metadata.addOnSuccessListener { metadata ->
            // Extract the UID associated with the image
            val userLikedId = metadata.getCustomMetadata("userID")

            // Check if the UID exists and if the user liked the image
            if (userLikedId != null) {
                // Add the UID with associated liked status to the liked HashMap
                likedMap[userLikedId] = liked

                // Matchmaking
                if (liked) { // Not usefully to check if there a match if the user didn't like
                    val userLikedRef = firestore.collection("users").document(userLikedId)
                    userLikedRef.get().addOnSuccessListener { userLikedData ->
                        val userLikedLikedMap =
                            userLikedData.get("liked") as? HashMap<String, Boolean>
                                ?: hashMapOf()
                        if (userLikedLikedMap[userId] == true) {
                            if (userRole == "Recruiter") {
                                saveMatch(
                                    context = context,
                                    firestore = firestore,
                                    recruiter = userId,
                                    jobSeeker = userLikedId
                                )
                            } else {
                                saveMatch(
                                    context = context,
                                    firestore = firestore,
                                    recruiter = userLikedId,
                                    jobSeeker = userId
                                )
                            }
                        }
                    }
                }

                // Update the user's data in Firestore
                userRef.update(
                    mapOf(
                        "liked" to likedMap
                    )
                ).addOnSuccessListener {
                    println("User's liked/disliked images updated successfully")
                }.addOnFailureListener { e ->
                    println("Error updating user's liked/disliked images: $e")
                }
            } else {
                println("User ID not found in image metadata")
            }
        }.addOnFailureListener { e ->
            println("Error getting image metadata: $e")
        }
    }.addOnFailureListener { e ->
        println("Error getting user data: $e")
    }
}

fun addContactsMenu(context: Context, navigationView: NavigationView) {
    val menu: Menu = navigationView.menu
    val subMenu: SubMenu = menu.addSubMenu("Contacts")

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    val currentUser = auth.currentUser
    val userId = currentUser?.uid
    val userRef = firestore.collection("users").document(userId!!)

    userRef.get().addOnSuccessListener { user ->
        if (user != null) {
            // Get user data
            val userRole = user.getString("Role")
            val otherRole = if (userRole == "JobSeeker") "Recruiter" else "JobSeeker"

            userRole?.let {
                firestore.collection("matchmaking").whereEqualTo(it, userId).get()
                    .addOnSuccessListener { matchs ->
                        var nbr_of_none: Int = 0
                        for (match in matchs) {
                            val otherID = match.getString(otherRole)

                            val otherRef = firestore.collection("users").document(otherID!!)
                            otherRef.get().addOnSuccessListener { otherUser ->
                                if (otherUser != null) {
                                    val otherName = otherUser.getString("username")

                                    if (otherName == "none") {
                                        nbr_of_none += 1
                                        subMenu.add("Contact without name $nbr_of_none")
                                    } else {
                                        subMenu.add(otherName)
                                    }
                                    // Invalidate the menu after adding all items
                                    navigationView.invalidate()
                                }
                            }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(
                                        context,
                                        "No contact find :$exception",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(context, "No match find :$exception", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }
    }

    //navigationView.invalidate()
}

@Composable
fun MyAppContent(context: Context, imageUrls: List<String>) {
    var currentIndex by remember { mutableStateOf(0) }
    val imageUrl = imageUrls.getOrNull(currentIndex)

    // State for tracking drag amount
    var offsetX by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()
    val animatedOffsetX = remember { Animatable(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Blue_light)
            .padding(top = 20.dp, bottom = 50.dp), // Adjusted top and bottom padding
        contentAlignment = Alignment.TopCenter
    ) {
        // Image container
        Box(
            modifier = Modifier
                .fillMaxWidth() // Ensures the image fills the width of its container
                .height(550.dp) // Specifies a fixed height for the image
                .clip(RoundedCornerShape(20.dp)) // Applies rounded corners
                .graphicsLayer {
                    translationX = animatedOffsetX.value
                    rotationZ = animatedOffsetX.value * 0.1f
                }
                .pointerInput(Unit) {
                    detectDragGestures(onDragEnd = {
                        coroutineScope.launch {
                            animatedOffsetX.animateTo(0f)
                        }
                        if (abs(offsetX) > 200) {
                            if (currentIndex + 1 <= imageUrls.size) {
                                currentIndex += 1
                            } else {
                                Toast
                                    .makeText(
                                        context,
                                        "No more offers available",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }
                        }
                        offsetX = 0f
                    }) { change, dragAmount ->
                        if (change.positionChange() != Offset.Zero) change.consume()
                        offsetX += dragAmount.x
                        coroutineScope.launch {
                            animatedOffsetX.snapTo(offsetX)
                        }
                    }
                },
            contentAlignment = Alignment.Center // Aligning content in the center
        ) {
            if (imageUrl != null && offsetX < -200) {
                saveUserLiked(context = context, imageUrl = imageUrl, liked = false)
            } else if (imageUrl != null && offsetX > 200) {
                saveUserLiked(context = context, imageUrl = imageUrl, liked = true)
            }

            if (currentIndex < imageUrls.size) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageUrl),
                    contentDescription = "Swipeable Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds // Stretches the image to fill the specified width and height
                )


            } else {
                Text(
                    "No more offers available",
                    color = Color.White
                )
            }
        }

        // Buttons layout
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
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

                            if (imageUrl != null) {
                                saveUserLiked(
                                    context = context,
                                    imageUrl = imageUrl,
                                    liked = false
                                )
                            }

                            if (currentIndex + 1 <= imageUrls.size) {
                                currentIndex += 1
                            } else {
                                Toast.makeText(
                                    context,
                                    "No more offers available",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue_dark),
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

                            if (imageUrl != null) {
                                saveUserLiked(
                                    context = context,
                                    imageUrl = imageUrl,
                                    liked = true
                                )
                            }

                            if (currentIndex + 1 <= imageUrls.size) {
                                currentIndex += 1
                            } else {
                                Toast.makeText(
                                    context,
                                    "No more offers available",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue_dark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Like", color = Color.White)
                }
            }
        }
    }
}