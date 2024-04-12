package com.example.pdfmaker.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.pdfmaker.Constants
import com.example.pdfmaker.R
import com.example.pdfmaker.adapter.AdapterImage
import com.example.pdfmaker.databinding.FragmentImageListBinding
import com.example.pdfmaker.getset.ModelImages
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ImageListFragment:Fragment(R.layout.fragment_image_list){

    private val TAG:String = "IMAGE_LIST_TAG"

    private lateinit var binding:FragmentImageListBinding
    private lateinit var mContext:Context
    private lateinit var cameraPermission: Array<String>
    private lateinit var storagePermission: Array<String>
    private val CAMERA_REQUEST_CODE = 101
    private val STORAGE_REQUEST_CODE = 100
    private lateinit var imagesRv:RecyclerView
    private lateinit var allImageArrayList:ArrayList<ModelImages>
    private lateinit var adapterImage:AdapterImage
    private lateinit var progressDialog: ProgressDialog
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
        imagesRv = binding.imageRv
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
        allImageArrayList = arrayListOf()
        adapterImage = AdapterImage(requireContext(),allImageArrayList)
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        loadImages()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_images,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId:Int = item.itemId
        if (itemId == R.id.image_item_delete){
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Delete Images")
                .setMessage("Are you sure you want to delete all/selected images?")
                .setPositiveButton("Delete All", DialogInterface.OnClickListener { dialog, which ->

                })
                .setNeutralButton("Delete Selected", DialogInterface.OnClickListener { dialog, which ->
                    // delete selected clicked, delete only selected images from list
                    deleteImages(false)
                })
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                    // cancel clicked , dismiss dialog
                    dialog.dismiss()
                })
                .show()
        }
        else if(itemId == R.id.images_item_pdf){
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Convert to PDF")
                .setMessage("Convert All/Selected Images to PDF")
                .setPositiveButton("CONVERT ALL",DialogInterface.OnClickListener { dialog, which ->
                    // convert all dialog button clicked, convert all images to pdf
                    convertImagesToPdf(true)
                })
                .setNeutralButton("CONVERT SELECTED",DialogInterface.OnClickListener { dialog, which ->
                    // convert selected dialog button clicked, convert only selected images to pdf
                    convertImagesToPdf(false)
                })
                .setNegativeButton("CANCEL",DialogInterface.OnClickListener { dialog, which ->
                    // cancel dialog clicked, dismiss dialog
                    dialog.dismiss()

                }).show()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun convertImagesToPdf(convertAll:Boolean){
        Log.d(TAG,"convertImagesToPdf: convertAll $convertAll")

        progressDialog.setMessage("Converting to PDF...")
        progressDialog.show()

        val executorService:ExecutorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        executorService.execute {
            Log.d(TAG,"run: BG work start...")
            var imagesToPdfList:ArrayList<ModelImages> = arrayListOf()
            if (convertAll){
                imagesToPdfList = allImageArrayList
            }
            else{
                // convert the selected images only,add selected images to imagesToPdfList
                for (i in allImageArrayList.indices){
                    if (allImageArrayList.get(i).checked){
                        imagesToPdfList.add(allImageArrayList[i])
                    }
                }
            }
            Log.d(TAG,"run: imagesToPdfList size: $imagesToPdfList")
            try {
                val root:File = File(requireContext().getExternalFilesDir(null),Constants.PDF_FOLDER)
                root.mkdirs()

                //2) Name with extension of the image
                val timestamp = System.currentTimeMillis()
                val fileName:String = "PDF_$timestamp$.pdf"

                Log.d(TAG,"run: fileName: $fileName")

                val file = File(root,fileName)

                val fileOutputStream = FileOutputStream(file)
                val pdfDocument = PdfDocument()

                for (i in imagesToPdfList.indices){
                    // hry uri of the image that will be added to PDF as PDF page
                    val imageToAdInPdfUri = imagesToPdfList.get(i).imageUri
                    // get bitmap
                    val bitmap:Bitmap
                    try {
                        //get bitmap using new API for android P (28) and above
                        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.P){
                            bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver,imageToAdInPdfUri))
                        }
                        else{
                            // get bitmap in android devices below Android P (28)
                            bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver,imageToAdInPdfUri)
                        }
                        // to resolve "IllegalArgumentException: Software rendering doesn't support hardware bitmaps"
                        var bitmap = bitmap.copy(Bitmap.Config.ARGB_8888,false)

                        // Setup Pdf Page info e.g page height, page width, page number, Since value of i will starts from 0 so we will do i+1
                        val pageInfo:PdfDocument.PageInfo = PdfDocument.PageInfo.Builder(bitmap.width,bitmap.height,i+1).create()

                        //create pdf page
                        val page:PdfDocument.Page = pdfDocument.startPage(pageInfo)

                        // for page color
                        val paint:Paint = Paint()
                        paint.setColor(Color.WHITE)

                        // setup canvas with bitmap to add in pdf page
                        val canvas = page.canvas
                        canvas.drawPaint(paint)
                        canvas.drawBitmap(bitmap,0f,0f,null)

                        //finish the page
                        pdfDocument.finishPage(page)
                        // if you want to free memory ASAP you should call recycle() just before decoding the second bitmap
                        bitmap.recycle()
                    }
                    catch (e:Exception){

                        Log.d(TAG,"run: $e")
                    }
                }

                pdfDocument.writeTo(fileOutputStream)
                pdfDocument.close()
            }
            catch (e:Exception){
                progressDialog.dismiss()
                Log.d(TAG,"run: $e")
            }

            handler.post {
                Log.d(TAG,"run: Converted...")
                progressDialog.dismiss()
                Toast.makeText(requireContext(),"Converted...",Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun deleteImages(deleteAll:Boolean){
        var imagesToDeleteList:ArrayList<ModelImages> = arrayListOf()
        if (deleteAll){
            imagesToDeleteList = allImageArrayList
        }
        else{
            for (i in allImageArrayList.indices){
                if (allImageArrayList[i].checked){
                    imagesToDeleteList.add(allImageArrayList[i])
                }
            }
        }
        for (i in imagesToDeleteList.indices){

            try {
                val pathOfImageToDelete = imagesToDeleteList[i].imageUri.path
                val file = File(pathOfImageToDelete)
                if (file.exists()){
                    // delete file and get result as true/false
                    val isDeleted:Boolean = file.delete()
                    // show in log
                    Log.d(TAG,"deleted: isDeleted $isDeleted")
                }

            }
            catch (e:Exception){
                Log.d(TAG,"deletedImages: $e")
            }
        }
        Toast.makeText(requireContext(),"Deleted",Toast.LENGTH_LONG).show()
        // all or selected images are deleted, reload images
        loadImages()
    }
    private fun loadImages() {
        Log.d(TAG,"loadImages: ")

        val folder = File(requireContext().getExternalFilesDir(null),Constants.IMAGE_FOLDER)
        imagesRv.adapter = adapterImage

        if (folder.exists()){
            // folder exists , try to load files from it
            Log.d(TAG,"loadImages: folder exists")
            val files = folder.listFiles()
            if (files!=null){
                // files exists, lets load them
                Log.d(TAG,"loadImages: Folder exists and have images")

                for (file:File in files){
                    Log.d(TAG,"loadImages: ${file.name}")
                    // get uri of the image, which we need to pass in mode
                    val imageUri = Uri.fromFile(file)
                    // create new instance of model and pass imageUri we just got
                    val modelImage = ModelImages(imageUri,false)
                    // add model to arraylist allImageArrayList
                    allImageArrayList.add(modelImage)
                    adapterImage.notifyItemInserted(allImageArrayList.size)
                }
            }
            else{
                Log.d(TAG,"loadImages: Folder exists but empty")
            }
        }
        else{
            Log.d(TAG,"loadImages: Folder dosen't exists")
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
            Log.d(TAG,"onActivityResult: Picked image gallery: $imageUri")
            imageUri?.let { saveImageToAppLevelDirectory(it) }

            val modelImage = imageUri?.let { ModelImages(it,false) }
            if (modelImage != null) {
                allImageArrayList.add(modelImage)
            }
            adapterImage.notifyItemInserted(allImageArrayList.size)
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
            Log.d(TAG,"onActivityResult: Picked image camera: $imageUri")
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