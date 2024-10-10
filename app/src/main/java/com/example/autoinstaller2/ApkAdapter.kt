package com.example.autoinstaller2

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class ApkAdapter(private val apkList: List<File>) : RecyclerView.Adapter<ApkAdapter.ApkViewHolder>() {

    var itemClickListener: ((view: View, item: List<File>, position: Int) -> Unit)? = null

    class ApkViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val apkName: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ApkViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApkViewHolder, position: Int) {
        holder.apkName.text = apkList[position].name
        holder.itemView.setOnClickListener() {
          itemClickListener?.invoke(it, apkList, position)
        }
    }

    override fun getItemCount(): Int {
        return apkList.size
    }


}
