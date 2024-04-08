package com.example.pdfmaker.fragments

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.pdfmaker.Constants
import com.example.pdfmaker.R
import com.example.pdfmaker.databinding.FragmentImageListBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream

class ImageListFragment:Fragment(R.layout.fragment_image_list){

    private val TAG:String = "IMAGE_LIST_TAG"

    private lateinit var binding:FragmentImageListBinding
    private lateinit var mContext:Context
    private lateinit var cameraPermission: Array<String>
    private lateinit var storagePermission: Array<String>
    private val CAMERA_REQUEST_CODE = 101
    private val STORAGE_REQUEST_CODE = 100
    private lateinit var addImageFab:FloatingActionButton

    // Uri of the image picked
    private var imageUri: Uri? = null
    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraPermission = arrayOf(android.Manifest.permission.CAMERA,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermission = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        //handle addImageFab click, show input image dialog
            binding.addImageFab.setOnClickListener {
            showInputImageDialog()
        }

    }

    private fun pickImageGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.setType("image/*")
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->
        if ( result.resultCode == Activity.RESULT_OK){
            val mData =  result.data?: return@registerForActivityResult
            imageUri = mData.data
            Log.d(TAG,"onActivityResult: Picked image gallery: ${imageUri}")
            imageUri?.let { saveImageToAppLevelDirectory(it) }
        }
        else{
            // Cancelled
            Toast.makeText(requireContext(),"Cancelled...",Toast.LENGTH_LONG).show()
        }
    }

    private fun pickImageCamera(){
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE,"TEMP IMAGE TITLE")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"TEMP IMAGE DESCRIPTION")

        imageUri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->
        if (result.resultCode == Activity.RESULT_OK){
            // image is taken form camera
            // we already have the image in imageUri using function pickImageCamera()
            // save the picked image
            Log.d(TAG,"onActivityResult: Picked image camera: ${imageUri}")
            imageUri?.let { saveImageToAppLevelDirectory(it) }
        }
        else{
            Toast.makeText(requireContext(),"Cancelled...",Toast.LENGTH_LONG).show()
        }
    }

    private fun showInputImageDialog(){
        Log.d(TAG,"showInputImageDialog: ")
        val popupMenu = PopupMenu(requireContext(),binding.addImageFab)

        popupMenu.menu.add(Menu.NONE,1,1,"CAMERA")
        popupMenu.menu.add(Menu.NONE,2,2,"GALLERY")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                1 ->{
                    Log.d(TAG,"onMenuItemClick: camera is clicked, check if camera permission are granted or not ")
                    if (checkCameraPermission()){
                        pickImageCamera()
                    }
                    else{ pickImageGallery()
                        requestCameraPermission()
                    }
                    return@setOnMenuItemClickListener true
                }
                2 ->{
                    Log.d(TAG,"onMenuItemClick: gallery is clicked, check if storage permission is granted or not")
                   if (checkStoragePermission()){
                       pickImageGallery()
                   }
                    else{
                        requestStoragePermission()
                   }
                    return@setOnMenuItemClickListener  true
                }
                else -> false
            }
        }
    }

    private fun saveImageToAppLevelDirectory(imageUriToBeSaved:Uri){
        val constants = Constants()
        try {
           // TODO("1. Get Bitmap from image uri")
            val bitmap:Bitmap
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.P) {
                bitmap = ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(
                        (requireContext().contentResolver),
                        imageUriToBeSaved
                    )
                )
            }
            else{
                // TODO(" Method to get bitmap from uri below API 28")
                bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver,imageUriToBeSaved)
            }
                // TODO(" 2. Create folder where we will save the image, dosen't requires any storage permission, and is not accessible by any other app")
            val directory = File(requireContext().getExternalFilesDir(null),constants.IMAGE_FOLDER)
            directory.mkdirs()

            // TODO("3. Name with extension of the image")
            val timestamp:Long = System.currentTimeMillis()
            val fileName:String = "${timestamp}.jpeg"

            // TODO("4. Sub folder and file name to saved")
            val file = File(requireContext().getExternalFilesDir(null),"${constants.IMAGE_FOLDER}/${fileName}")

            // TODO("save image")
            try {
                val fos = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos)
                fos.flush()
                fos.close()
                Toast.makeText(requireContext(),"Image Saved",Toast.LENGTH_LONG).show()
            }
            catch (e:Exception){
                Toast.makeText(requireContext(),"Failed to save image due to ${e.message}",Toast.LENGTH_LONG).show()
            }
        }
        catch(e:Exception){
            Toast.makeText(requireContext(),"Failed to prepare image due to ${e.message}",Toast.LENGTH_LONG).show()
        }
    }

    private fun checkStoragePermission():Boolean{
        Log.d(TAG,"checkStoragePermission: ")
        val result:Boolean = ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        return result
    }

    private fun requestStoragePermission(){
        Log.d(TAG,"requestStoragePermission: ")
        requestPermissions(storagePermission,STORAGE_REQUEST_CODE)
    }

    private fun checkCameraPermission():Boolean{
        val cameraResult:Boolean = ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val storageResult:Boolean = ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        return cameraResult && storageResult
    }

    private fun requestCameraPermission(){
        requestPermissions(cameraPermission,CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode){
            CAMERA_REQUEST_CODE ->{
                if (grantResults.isNotEmpty()){
                    val cameraAccepted:Boolean = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted:Boolean = grantResults[1] == PackageManager.PERMISSION_GRANTED

                    if (cameraAccepted && storageAccepted){
                       // TODO("yet to implement")
                        pickImageCamera()
                    }
                    else{
                        Toast.makeText(requireContext(),"camera & storage permission are required",Toast.LENGTH_LONG).show()
                    }
                }
                else{
                    Toast.makeText(requireContext(),"Cancelled...",Toast.LENGTH_LONG).show()
                }
            }
            STORAGE_REQUEST_CODE->{
                if (grantResults.isNotEmpty()){
                    val storageAccepted:Boolean = grantResults[0] == PackageManager.PERMISSION_GRANTED

                    if (storageAccepted){
                     //   TODO("yet to implement")
                        pickImageGallery()
                    }
                    else{
                        Toast.makeText(requireContext(),"Storage permission is required",Toast.LENGTH_LONG).show()
                    }
                }
                else{
                    Toast.makeText(requireContext(),"Cancelled...",Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}