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
import androidx.viewpager.widget.ViewPager
import com.example.jobswype.ui.theme.JobSwypeTheme
import kotlin.math.abs

class MainActivity : ComponentActivity(), GestureDetector.OnGestureListener {

    private lateinit var gestureDetector: GestureDetector
    private lateinit var likeButton: Button
    private lateinit var dislikeButton: Button

    private lateinit var viewPager: ViewPager
    private lateinit var imagePagerAdapter: ImagePagerAdapter
    private val imageArray = intArrayOf(R.drawable.cv1, R.drawable.cv2, R.drawable.cv3, R.drawable.cv4, R.drawable.cv5, R.drawable.cv6, R.drawable.cv7, R.drawable.cv8, R.drawable.cv9)


    private val SWIPE_THRESHOLD = 100
    private val SWIPE_VELOCITY_THRESHOLD = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)
        imagePagerAdapter = ImagePagerAdapter(this, imageArray)
        viewPager.adapter = imagePagerAdapter

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
                Toast.makeText(this@MainActivity, "To Right!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "To Left!", Toast.LENGTH_SHORT).show()
            }
            return true
        }
        return false
    }

    private fun swipeRightAction() {
        // Mettez ici la logique que vous souhaitez exécuter lors d'un swipe vers la droite (match)
        val currentImage = viewPager.currentItem
        if (currentImage < imageArray.size - 1) {
            viewPager.setCurrentItem(currentImage + 1, true)
        }
    }

    private fun swipeLeftAction() {
        val currentImage = viewPager.currentItem
        if (currentImage > 0) {
            viewPager.setCurrentItem(currentImage - 1, true)
        }
    }
}