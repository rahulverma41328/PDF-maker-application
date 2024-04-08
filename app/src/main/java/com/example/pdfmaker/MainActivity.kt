package com.example.pdfmaker

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pdfmaker.databinding.ActivityMainBinding
import com.example.pdfmaker.fragments.ImageListFragment
import com.example.pdfmaker.fragments.PDFListFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream

private val REQUEST_CODE_PICK_IMAGES = 100
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId){
                R.id.bottom_menu_images -> {
                    loadImagesFragment()
                   return@setOnItemSelectedListener true
                }
                R.id.bottom_menu_pdfs ->{
                    loadPdfsFragment()
                 return@setOnItemSelectedListener   true
                }
                else -> false
            }
        }

    }

    private fun loadPdfsFragment() {
        val imageListFragment = PDFListFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout,imageListFragment,"ImageListFragment").commit()
    }

    private fun loadImagesFragment() {
        val pdfListFragment = ImageListFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLayout,pdfListFragment,"PdfListFragment").commit()
    }

    private fun convertImageToPdf(imagePath:String,outputPath:String) {
       /** try {
            document.open() // open the document

            val page = PdfPTable(1)// add new page
            document.add(page)

            val bitmap = BitmapFactory.decodeFile(imagePath) // load the image as bitmap
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream)
            val imageData = byteArrayOutputStream.toByteArray()

            val image = Image.getInstance(imageData)
           // val imageScaled = image.scaleAbsolute()
            // add the image to the page

            image.setAbsolutePosition(page.)
        } **/

    }

    private fun requestRuntimePermission():Boolean{
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),13)
        }
        return true
    }
}