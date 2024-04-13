package com.example.pdfmaker.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.pdfmaker.Constants
import com.example.pdfmaker.R
import com.example.pdfmaker.adapter.AdapterPdf
import com.example.pdfmaker.databinding.FragmentPdfListBinding
import com.example.pdfmaker.getset.ModelPdf
import java.io.File

class PDFListFragment:Fragment(R.layout.fragment_pdf_list) {
    private lateinit var binding:FragmentPdfListBinding
    private lateinit var mcontext:Context
    private lateinit var pdfArrayList:ArrayList<ModelPdf>
    private lateinit var adapterPdf:AdapterPdf
    private lateinit var pdfRv:RecyclerView
    private val TAG:String = "PDF_LIST_TAG"

    override fun onAttach(context: Context) {
        mcontext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPdfListBinding.inflate(inflater)
        pdfArrayList = arrayListOf()
        pdfRv = binding.pdfRv
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadPdfDocuments()
    }

    private fun loadPdfDocuments() {
        pdfArrayList = arrayListOf()
        adapterPdf = AdapterPdf(requireContext(),pdfArrayList)
        pdfRv.adapter = adapterPdf

        val folder:File = File(requireContext().getExternalFilesDir(null),Constants.PDF_FOLDER)

        if (folder.exists()){
            val file = folder.listFiles()
            Log.d(TAG,"loadPdfDocuments: Files Count: ${file.size}")

            for (fileEntry:File in file){
                Log.d(TAG,"loadPdfDocuments: File Name: ${fileEntry.name}")

                val uri:Uri = Uri.fromFile(fileEntry)

                val modelPdf:ModelPdf = ModelPdf(fileEntry,uri)

                pdfArrayList.add(modelPdf)
                adapterPdf.notifyItemInserted(pdfArrayList.size)
            }
        }
    }
}