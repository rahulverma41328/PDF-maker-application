package com.example.pdfmaker.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.pdfmaker.MyApplication
import com.example.pdfmaker.R
import com.example.pdfmaker.getset.ModelPdf
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AdapterPdf(
    private val context:Context,
    private val pdfArrayList:ArrayList<ModelPdf> = arrayListOf()
):RecyclerView.Adapter<AdapterPdf.HolderPdf>() {

    private val TAG:String = "ADAPTER_PDF_TAG"
    inner class HolderPdf(itemView:View):ViewHolder(itemView){
        val thumbnailTv:ImageView = itemView.findViewById(R.id.thumbnailIv)
        val nameTv:TextView = itemView.findViewById(R.id.nameTv)
        val pagesTv:TextView = itemView.findViewById(R.id.pagesTv)
        val sizeTv:TextView = itemView.findViewById(R.id.sizeTv)
        val dateTv:TextView = itemView.findViewById(R.id.dateTv)
        val moreBtn:ImageButton = itemView.findViewById(R.id.moreBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdf {
        val view:View = LayoutInflater.from(context).inflate(R.layout.row_pdf,parent,false)
        return HolderPdf(view)
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPdf, position: Int) {
        val modelPdf:ModelPdf = pdfArrayList.get(position)
        val name:String = modelPdf.file.name
        val timestamp:Long = modelPdf.file.lastModified()

        val formattedDate:String = MyApplication().formatTimestamp(timestamp)
        Log.d(TAG,"loadPdfDate: run: $formattedDate")
        loadFileSize(modelPdf,holder)
        loadThumbnailFromPdfFile(modelPdf,holder)

        holder.nameTv.text = name
        holder.dateTv.text = formattedDate
    }

    private fun loadThumbnailFromPdfFile(modelPdf: ModelPdf, holder: AdapterPdf.HolderPdf) {
        val executorService:ExecutorService = Executors.newSingleThreadExecutor()
        val handler:Handler = Handler(Looper.getMainLooper())

        executorService.execute {
            var thumbnailBitmap: Bitmap? = null
            var pageCount:Int = 0;

            try {
                // A file descriptor is an object that a process uses to read or write to an open file and open network sockets.
                val parcelFileDescriptor:ParcelFileDescriptor = ParcelFileDescriptor.open(modelPdf.file,ParcelFileDescriptor.MODE_READ_ONLY)
                // This is the PdfRenderer we use to render the PDF.
                val pdfRenderer:PdfRenderer = PdfRenderer(parcelFileDescriptor)
                // get pages count
                pageCount = pdfRenderer.pageCount
                if (pageCount<=0){
                    //No pages in pdf,can't show thumbnail
                    Log.d(TAG,"loadThumbnailFromPdfFile run: No Pages")
                }
                else{
                    // There are page(s) in pdf, can show pdf thumbnail. Use 'openPage' to open a specific page in PDf.
                    val currentPage:PdfRenderer.Page = pdfRenderer.openPage(0)
                    // Important: the destination bitmap must be ARGB (not RGB).
                    thumbnailBitmap = Bitmap.createBitmap(currentPage.width,currentPage.height,Bitmap.Config.ARGB_8888)

                    // Here, we render the page onto the Bitmap.
                    // To render a portion of the page, use the second and third parameter. Pass nulls to get the default result
                    // Pass either RENDER_MODE_DISPLAY or RENDER_FOR_PRINT for the last parameter
                    currentPage.render(thumbnailBitmap,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                }
            }
            catch (e:Exception){
                Log.d(TAG,"loadThumbnailFromPdfFile run: ",e)
            }
            val finalThumbnailBitmap:Bitmap? = thumbnailBitmap
            val finalPageCount:Int = pageCount

            handler.post {
                Log.d(TAG,"loadThumbnailFromPdfFile run: Setting thumbnail")
                // set pdf thumbnail bitmap to thumbnailTv
                Glide.with(context)
                    .load(finalThumbnailBitmap)
                    .fitCenter()
                    .placeholder(R.drawable.file_pdf_box)
                    .into(holder.thumbnailTv)

                //set pages count to pagesTv
                holder.pagesTv.text = ""+finalPageCount+"Pages"
            }
        }
    }

    private fun loadFileSize(modelPdf: ModelPdf, holder: AdapterPdf.HolderPdf) {
        //get file size in bytes
        val bytes:Double = modelPdf.file.length().toDouble()

        val kb:Double = bytes/1024
        val mb:Double = kb/1024

        var size:String = ""

        if (mb>=1){
            size = String.format("%.2f MD",mb)
        }
        else if (kb>=1){
            size = String.format("%.2f KB",kb)
        }
        else{
            size = String.format("%.2f bytes",bytes)
        }
        Log.d(TAG,"loadFileSize: Size: $size")
        //set file size to sizeTv
        holder.sizeTv.text = size

    }
}