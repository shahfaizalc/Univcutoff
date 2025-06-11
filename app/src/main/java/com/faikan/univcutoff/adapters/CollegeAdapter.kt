package com.faikan.univcutoff.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.faikan.univcutoff.R
import com.faikan.univcutoff.model.CollegeData
import java.util.ArrayList
import java.util.Locale

class CollegeAdapter(initialList: List<CollegeData>) :
    RecyclerView.Adapter<CollegeAdapter.ViewHolder>(), Filterable {

    // Explicitly declare as MutableList with ArrayList implementation
    private val collegeList = ArrayList<CollegeData>()
    private val collegeListFull = ArrayList<CollegeData>()

    // Initialize lists in init block
    init {
        collegeList.addAll(initialList)
        collegeListFull.addAll(initialList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_college, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val college = collegeList[position]

        holder.tvCollegeName.text = college.collegeName
        holder.tvBranch.text = "Branch: " + college.branchName
        holder.tvCutoff.text = college.jsonCategory +"(" + college.category + "): " + college.cutoff
        holder.tvDistrict.text = "District: " + college.district + "| Year: " + college.year
    }

    override fun getItemCount(): Int {
        return collegeList.size
    }

    fun updateData(newData: List<CollegeData>) {
        collegeList.clear()
        collegeList.addAll(newData)
        collegeListFull.clear()
        collegeListFull.addAll(newData)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return collegeFilter
    }

    private val collegeFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = ArrayList<CollegeData>()

            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(collegeListFull)
            } else {
                val filterPattern = constraint.toString().lowercase(Locale.getDefault()).trim()

                for (college in collegeListFull) {
                    if (college.collegeName.lowercase(Locale.getDefault()).contains(filterPattern) ||
                        college.branchName.lowercase(Locale.getDefault()).contains(filterPattern) ||
                        college.district.lowercase(Locale.getDefault()).contains(filterPattern) ||
                        college.category.lowercase(Locale.getDefault()).contains(filterPattern)
                    ) {
                        filteredList.add(college)
                    }
                }
            }

            val results = FilterResults()
            results.values = filteredList
            return results
        }

        @Suppress("UNCHECKED_CAST")
        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            collegeList.clear()
            collegeList.addAll(results.values as ArrayList<CollegeData>)
            notifyDataSetChanged()
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCollegeName: TextView = itemView.findViewById(R.id.tvCollegeName)
        val tvBranch: TextView = itemView.findViewById(R.id.tvBranch)
        val tvCutoff: TextView = itemView.findViewById(R.id.tvCutoff)
        val tvDistrict: TextView = itemView.findViewById(R.id.tvDistrict)
    }
}
