package com.example.pdfmaker.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pdfmaker.R
import com.example.pdfmaker.databinding.FragmentPdfListBinding

class PDFListFragment:Fragment(R.layout.fragment_pdf_list) {
    private lateinit var binding:FragmentPdfListBinding
    private lateinit var mcontext:Context

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}