package com.square.android.ui.fragment.map

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.square.android.R
import com.square.android.extensions.loadImageCenterInside

class MapPlaceImagesAdapter (var imageUrls: List<String>) : RecyclerView.Adapter<MapPlaceImagesAdapter.ViewHolder>(){

    fun setUrls(urls: List<String>){
        imageUrls = urls
        notifyDataSetChanged()
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var image: ImageView = v.findViewById(R.id.photoImage) as ImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapPlaceImagesAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.place_photo_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.image.loadImageCenterInside(imageUrls!![position])
    }

    override fun getItemCount(): Int {
        return if(imageUrls.isEmpty()) 0 else imageUrls.size
    }

}