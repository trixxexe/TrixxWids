package com.trixxwids.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.trixxwids.app.R
import com.trixxwids.app.data.WidgetEntity
import java.io.File

class GalleryAdapter(private val onApplyClick: (WidgetEntity) -> Unit) :
    RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    private var widgets = listOf<WidgetEntity>()

    fun submitList(newList: List<WidgetEntity>) {
        widgets = newList
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
        val tvName: TextView = view.findViewById(R.id.tvWidgetName)
        val btnApply: Button = view.findViewById(R.id.btnApply)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_widget_gallery, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val widget = widgets[position]
        holder.tvName.text = widget.name
        
        Glide.with(holder.itemView.context)
            .load(File(widget.previewImagePath))
            .into(holder.ivThumbnail)

        holder.btnApply.setOnClickListener {
            onApplyClick(widget)
        }
    }

    override fun getItemCount() = widgets.size
}
