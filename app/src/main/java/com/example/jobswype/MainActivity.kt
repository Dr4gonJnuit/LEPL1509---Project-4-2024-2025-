package com.example.jobswype

import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.example.jobswype.ui.theme.JobSwypeTheme
import kotlin.math.abs

class MainActivity : ComponentActivity(), GestureDetector.OnGestureListener {

    // Declaring gesture detector, swipe threshold, and swipe velocity threshold
    private lateinit var imageView: ImageView
    private lateinit var gestureDetector: GestureDetector
    private lateinit var likeButton: Button
    private lateinit var dislikeButton: Button


    private val SWIPE_THRESHOLD = 100
    private val SWIPE_VELOCITY_THRESHOLD = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        gestureDetector = GestureDetector(this, this)

        imageView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }

        val imageList = listOf(
            R.drawable.cv1,
            R.drawable.cv2,
            R.drawable.cv3
            // ... Ajoutez d'autres images ici
        )

        val imageView: ImageView = findViewById(R.id.myImageView)

        // Charger une image à partir des ressources
        imageView.setImageResource(R.drawable.cv1) // Remplacez "votre_image" par le nom de votre image dans le dossier 'res/drawable'


        likeButton = findViewById(R.id.like)
        dislikeButton = findViewById(R.id.dislike)

        likeButton.setOnClickListener {
            swipeRightAction()
        }
        dislikeButton.setOnClickListener {
            swipeLeftAction()
        }
    }

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    override fun onShowPress(e: MotionEvent) {
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return true
    }

    override fun onLongPress(e: MotionEvent) {
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val distanceX = e2.x - e1!!.x
        if (Math.abs(distanceX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
            if (distanceX > 0) {
                // Swipe vers la droite (match)
                swipeRightAction()
            } else {
                // Swipe vers la gauche (dismatch)
                swipeLeftAction()
            }
            return true
        }
        return false
    }

    private fun swipeRightAction() {
        // Mettez ici la logique que vous souhaitez exécuter lors d'un swipe vers la droite (match)
        Toast.makeText(this@MainActivity, "To Right!", Toast.LENGTH_SHORT).show()
    }

    private fun swipeLeftAction() {
        // Mettez ici la logique que vous souhaitez exécuter lors d'un swipe vers la gauche (dismatch)
        Toast.makeText(this@MainActivity, "To Left!", Toast.LENGTH_SHORT).show()
    }
}

/*
@Composable
fun SwipeZone(modifier: Modifier) {
    Box(
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = R.drawable.kotlin),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(translationX = 0f) // Add other transformations as needed
        )
    }
}

@Composable
fun GreetingImage(message: String, from: String, modifier: Modifier = Modifier) {
    Box(modifier) {
        SwipeZone(Modifier)
    }
}


@Composable
fun GreetingText(message: String, from: String, modifier: Modifier = Modifier) {
    // Create a column so that texts don't overlap
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Text(
            text = message,
            fontSize = 100.sp,
            lineHeight = 116.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = from,
            fontSize = 36.sp,
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(end = 16.dp)
                .align(alignment = Alignment.End)

        )
    }
}

@Composable
fun GreetingImage(message: String, from: String, modifier: Modifier = Modifier) {
    // Create a box to overlap image and texts
    Box(modifier) {
        SwipeZone(Modifier)
        GreetingText(
            message = message,
            from = from,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BirthdayCardPreview() {
    JobSwypeTheme {
        GreetingImage(
            stringResource(R.string.happy_birthday_text),
            stringResource(R.string.signature_text)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SwipeZonePreview() {
    JobSwypeTheme {
        SwipeZone(Modifier)
    }
}*/