package com.example.pdfmaker

data class Constants( val IMAGE_FOLDER:String = "IMAGE TO PDF/IMAGES",
    val PDF_FOLDER:String="IMAGE TO PDF/DOCUMENTS"){
    companion object {
        const val IMAGE_FOLDER = "IMAGE TO PDF/IMAGES" // Use const for compile-time constant
        const val PDF_FOLDER = "IMAGE TO PDF/DOCUMENTS" //
    }

}