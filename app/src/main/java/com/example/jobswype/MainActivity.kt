package com.example.jobswype

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
import androidx.fragment.app.Fragment
import com.example.jobswype.ui.theme.*
import com.example.jobswype.R
import com.example.jobswype.session.LoginSession
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /*
        setContent {
            // Use the Surface component with a modifier to set the background color
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White), // Modifier.fillMaxSize() ensures the background fills the entire screen
                color = MaterialTheme.colorScheme.background
            ) {
                // Your app content goes here
                MyAppContent()
                LogoutAction()
            }
        }*/
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
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
    }
}

@Composable
fun MyAppContent() {
    val images = listOf(
        R.drawable.cv1, R.drawable.cv2, R.drawable.cv3,
        R.drawable.cv4, R.drawable.cv5, R.drawable.cv6,
        R.drawable.cv7, R.drawable.cv8, R.drawable.cv9,
    )
    var currentIndex by remember { mutableStateOf(0) }
    val image = images[currentIndex % images.size]

    // State for tracking drag amount
    var offsetX by remember { mutableStateOf(0f) }
    // Animate back to original position when not dragging
    var coroutineScope = rememberCoroutineScope()
    var animatedOffsetX = remember { Animatable (0f) }
    /*
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        finishedListener = { offsetX = 0f })
    */
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Blue_light)
            .padding(top = 80.dp), // Ajuster la valeur de top selon l'espace souhaité
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
                            currentIndex = (currentIndex + 1) % images.size
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
                            coroutineScope.launch{
                                animatedOffsetX.animateTo(-300f, animationSpec = TweenSpec(durationMillis = 200)) // target value is -300 because its swiping to left
                                animatedOffsetX.animateTo(0f, animationSpec = TweenSpec(durationMillis = 200))
                                currentIndex = (currentIndex + 1) % images.size
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
                            coroutineScope.launch{
                                animatedOffsetX.animateTo(300f, animationSpec = TweenSpec(durationMillis = 200)) // tweenspeec is custom animation, targetvalue is 300f because swiping to the right
                                animatedOffsetX.animateTo(0f, animationSpec = TweenSpec(durationMillis = 200))
                                currentIndex = (currentIndex + 1) % images.size
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

@Composable
fun LogoutAction() {
    val context = LocalContext.current
    val loginSession = LoginSession(context)
    Button(
        onClick = {
            loginSession.logoutUser() // clear the session and redirect to LoginActivity
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
    ) {
        Text(text = "Logout", color = Color.White)
    }
}