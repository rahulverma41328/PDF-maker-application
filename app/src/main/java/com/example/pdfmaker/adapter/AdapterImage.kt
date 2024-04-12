package com.example.pdfmaker.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.pdfmaker.R
import com.example.pdfmaker.activity.ImageViewActivity
import com.example.pdfmaker.databinding.RowImageBinding
import com.example.pdfmaker.getset.ModelImages

class AdapterImage(
    private val context: Context,
    private var imageArrayList: MutableList<ModelImages>
) : RecyclerView.Adapter<AdapterImage.HolderImage>() {

    inner class HolderImage(itemView: View) :ViewHolder(itemView){
        val imageIv: ImageView = itemView.findViewById(R.id.imageIv)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImage {
        val view = LayoutInflater.from(context).inflate(R.layout.row_image,parent,false)
        return HolderImage(view)
    }

    override fun getItemCount(): Int {
       return imageArrayList.size
    }

    override fun onBindViewHolder(holder: HolderImage, position: Int) {
        val modelImage = imageArrayList.get(position)
        val imageUri = modelImage.imageUri
        Glide.with(context)
            .load(imageUri)
            .placeholder(R.drawable.image)
            .into(holder.imageIv)

        holder.itemView.setOnClickListener {
            val intent= Intent(context,ImageViewActivity::class.java)
            intent.putExtra("imageUri","$imageUri")
            context.startActivity(intent)
        }
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            // update value in model isChecked is either true or false
            modelImage.checked = isChecked

        }
    }
}