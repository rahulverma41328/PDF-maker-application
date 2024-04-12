package com.example.pdfmaker.activity

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.pdfmaker.R
import com.example.pdfmaker.databinding.ActivityImageViewBinding

class ImageViewActivity : AppCompatActivity() {
    private lateinit var image:String
    private lateinit var binding:ActivityImageViewBinding
    private lateinit var imageIv:ImageView
    private val TAG:String = "IMAGE_TAG"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityImageViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // TODO("change actionbar title, show back button on actionbar
        supportActionBar?.title = "ImageView"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        imageIv = findViewById(R.id.imageIv)


        image = intent.getStringExtra("imageUri").toString()
        Log.d(TAG,"onCreate: Image: $image ")

        Glide.with(this)
            .load(image)
            .placeholder(R.drawable.image)
            .into(imageIv)
    }

    // handle actionbar onBack pressed (go previous activity)
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}