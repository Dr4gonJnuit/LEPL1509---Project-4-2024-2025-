package com.example.jobswype

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val context = LocalContext.current
                //Start fetching images
                val imageUrls = remember { mutableStateListOf<String>() }
                LaunchedEffect(key1 = true) {
                    fetchImageUrls(context){ urls ->
                        imageUrls.clear()
                        imageUrls.addAll(urls)
                    }
                }
                MyAppContent(context, imageUrls)
            }
        }
    }
}
