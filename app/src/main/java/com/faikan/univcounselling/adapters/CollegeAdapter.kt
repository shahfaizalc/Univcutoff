package com.faikan.univcounselling.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.faikan.univcounselling.MainActivity
import com.faikan.univcounselling.R
import com.faikan.univcounselling.model.CollegeData
import java.util.ArrayList
import java.util.Locale

class CollegeAdapter(initialList: List<CollegeData>) :
    RecyclerView.Adapter<CollegeAdapter.ViewHolder>(), Filterable {

    // Add reference to MainActivity
    private var mainActivity: MainActivity? = null

    // Add TextView reference for the no data message
    private var noDataTextView: TextView? = null

    // Add RecyclerView reference
    private var recyclerView: RecyclerView? = null

    // Setter method to pass references from MainActivity
    fun setupReferences(activity: MainActivity, recyclerView: RecyclerView, noDataTextView: TextView) {
        this.mainActivity = activity
        this.recyclerView = recyclerView
        this.noDataTextView = noDataTextView
    }

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

    // Add this method to your CollegeAdapter class
    fun resetFilter() {
        // Clear the current list
        collegeList.clear()
        // Re-add all items from the full list
        collegeList.addAll(collegeListFull)
        // Notify the adapter that data has changed
        notifyDataSetChanged()
    }

    fun updateData(newData: List<CollegeData>) {
        collegeList.clear()
        collegeList.addAll(newData)
        collegeListFull.clear()
        collegeListFull.addAll(newData)
        notifyDataSetChanged()

        // Update UI visibility
        updateNoDataVisibility(null)
    }

    // Add method to update UI visibility
    private fun updateNoDataVisibility(searchQuery: String?) {
        if (noDataTextView == null || recyclerView == null) return

        if (collegeList.isEmpty()) {
            if (searchQuery != null && searchQuery.isNotEmpty()) {
                // Show search-specific message
                noDataTextView?.text = "No colleges found matching: \"$searchQuery\""
            } else {
                // Show default message
                noDataTextView?.text = "No colleges match your criteria"
            }
            recyclerView?.visibility = View.GONE
            noDataTextView?.visibility = View.VISIBLE
        } else {
            recyclerView?.visibility = View.VISIBLE
            noDataTextView?.visibility = View.GONE
        }
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

            // Update UI visibility with search query
            updateNoDataVisibility(constraint?.toString())
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCollegeName: TextView = itemView.findViewById(R.id.tvCollegeName)
        val tvBranch: TextView = itemView.findViewById(R.id.tvBranch)
        val tvCutoff: TextView = itemView.findViewById(R.id.tvCutoff)
        val tvDistrict: TextView = itemView.findViewById(R.id.tvDistrict)
    }
}
