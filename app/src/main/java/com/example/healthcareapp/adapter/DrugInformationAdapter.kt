package com.example.healthcareapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.healthcareapp.R
import com.example.healthcareapp.models.drugs.OpenFDAResponse
import com.google.android.material.textview.MaterialTextView

class DrugInformationAdapter(private val dInfo : OpenFDAResponse) : RecyclerView.Adapter<DrugInformationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrugInformationAdapter.ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.drug_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemsViewModel = dInfo
        holder.textView1.text = itemsViewModel.results[0].openFDA.generic_name[0]
        holder.textView.text = itemsViewModel.results[0].description[0]
    }

    override fun getItemCount(): Int {
        return dInfo.meta.results.total
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val textView1: MaterialTextView = itemView.findViewById(R.id.drug_title)
        val textView: MaterialTextView = itemView.findViewById(R.id.drug_description)
    }
}