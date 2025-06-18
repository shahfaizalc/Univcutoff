package com.faikan.univcutoff.data

import android.content.Context
import android.util.Log
import com.faikan.univcutoff.R
import com.faikan.univcutoff.model.CollegeData
import com.faikan.univcutoff.model.FilterOptions
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class DataManager private constructor() {
    private val allColleges: MutableList<CollegeData> = ArrayList()
    private val filteredColleges: MutableList<CollegeData> = ArrayList()
    var currentFilter: FilterOptions = FilterOptions()
        private set
    private val categories: MutableSet<String> = HashSet()
    private val jsonCategories: MutableSet<String> = HashSet() // To store "cutoffs" and "ranks"
    private val years: MutableSet<Int> = HashSet(listOf(2020, 2021, 2022, 2023, 2024))
    private val branches: MutableSet<String> = HashSet()
    private val districts: MutableSet<String> = HashSet()

    companion object {
        var instance: DataManager? = null

        @JvmStatic
        @get:Synchronized
        val getInstance: DataManager
            get() {
                if (instance == null) {
                    instance = DataManager()
                }
                return instance!!
            }
    }

    // Load data from JSON files
    fun loadData(context: Context) {
        try {
            // Load cutoffdetails.json
            val cutoffStream = context.resources.openRawResource(R.raw.cutoffdetails)
            val cutoffJson = convertStreamToString(cutoffStream)
            val cutoffArray = JSONArray(cutoffJson)

            // Load univ_details.json
            val univStream = context.resources.openRawResource(R.raw.univ_details)
            val univJson = convertStreamToString(univStream)
            val univArray = JSONArray(univJson)

            // Create a map of university details for easy lookup
            val univMap: MutableMap<Int, JSONObject> = HashMap()
            for (i in 0 until univArray.length()) {
                val univObj = univArray.getJSONObject(i)
                univMap[univObj.getInt("College Code")] = univObj
            }

            // Process cutoff data
            for (i in 0 until cutoffArray.length()) {
                val cutoffObj = cutoffArray.getJSONObject(i)
                val collegeCode = cutoffObj.getInt("College Code")

                // Get university details
                val univObj = univMap[collegeCode] ?: continue

                // Extract data
                val year = cutoffObj.getInt("Year")
                val jsonCategory =
                    cutoffObj.getString("Category") // This is either "cutoffs" or "ranks"
                val branchCode = cutoffObj.getString("Branch Code")
                val branchName = cutoffObj.getString("Branch Name")
                val collegeName = univObj.getString("College Name")
                val district = univObj.getString("District")
                val address = if (univObj.has("Address")) univObj.getString("Address") else ""
                val pincode = univObj.getInt("Pincode")

                // Add to filter options
                years.add(year)
                jsonCategories.add(jsonCategory) // Store the JSON category
                branches.add(branchName)
                districts.add(district)

                // Process different cutoff categories
                processCutoffCategory(
                    cutoffObj, "OC", collegeCode, collegeName, branchCode,
                    branchName, year, jsonCategory, district, address, pincode
                )
                processCutoffCategory(
                    cutoffObj, "BC", collegeCode, collegeName, branchCode,
                    branchName, year, jsonCategory, district, address, pincode
                )
                processCutoffCategory(
                    cutoffObj, "BCM", collegeCode, collegeName, branchCode,
                    branchName, year, jsonCategory, district, address, pincode
                )
                processCutoffCategory(
                    cutoffObj, "MBC", collegeCode, collegeName, branchCode,
                    branchName, year, jsonCategory, district, address, pincode
                )
                processCutoffCategory(
                    cutoffObj, "SC", collegeCode, collegeName, branchCode,
                    branchName, year, jsonCategory, district, address, pincode
                )
                processCutoffCategory(
                    cutoffObj, "SCA", collegeCode, collegeName, branchCode,
                    branchName, year, jsonCategory, district, address, pincode
                )
                processCutoffCategory(
                    cutoffObj, "ST", collegeCode, collegeName, branchCode,
                    branchName, year, jsonCategory, district, address, pincode
                )
            }

            // Set default filter values
            // currentFilter.year = if (years.isEmpty()) 2024 else years.iterator().next()
            currentFilter.year = if (years.isEmpty()) 2024 else years.maxOrNull() ?: 2024
            currentFilter.category = "cutoffs" // Start with empty category to show all
            currentFilter.casteCategory = "" // Start with empty caste category
            currentFilter.minCutoff = 0
            currentFilter.maxCutoff = 200

            // Apply default filter
            applyFilter()

            // Log categories for debugging
            Log.d("DataManager", "Available caste categories: ${categories.joinToString()}")
            Log.d("DataManager", "Available JSON categories: ${jsonCategories.joinToString()}")
            Log.d("DataManager", "Loaded ${allColleges.size} colleges")


            // Debug logging
            Log.d("DataManager", "Years before forced addition: $years")

            // Force add all years to ensure they're available
            years.add(2020)
            years.add(2021)
            years.add(2022)
            years.add(2023)
            years.add(2024)

            Log.d("DataManager", "Final years after forcing: $years")
        } catch (e: IOException) {
            Log.e("DataManager", "Error loading data", e)
        } catch (e: JSONException) {
            Log.e("DataManager", "Error parsing JSON", e)
        }
    }

    private fun processCutoffCategory(
        cutoffObj: JSONObject, categoryKey: String,
        collegeCode: Int, collegeName: String, branchCode: String,
        branchName: String, year: Int, jsonCategory: String,
        district: String, address: String, pincode: Int
    ) {
        try {
            if (cutoffObj.has(categoryKey) && !cutoffObj.isNull(categoryKey)) {
                val cutoff = parseDouble(cutoffObj.getString(categoryKey))

                if (cutoff > 0) {
                    // Create a CollegeData object with BOTH the JSON category AND the caste category
                    allColleges.add(
                        CollegeData(
                            collegeCode = collegeCode,
                            collegeName = collegeName,
                            branchCode = branchCode,
                            branchName = branchName,
                            year = year,
                            category = categoryKey, // This is the caste category (OC, BC, etc.)
                            cutoff = cutoff,
                            district = district,
                            address = address,
                            pincode = pincode,
                            jsonCategory = jsonCategory // Add the JSON category (cutoffs or ranks)
                        )
                    )

                    // Add caste category to available filter options
                    categories.add(categoryKey)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    // Apply the current filter
    fun applyFilter() {

        filteredColleges.clear()

        // For debugging
        Log.d(
            "DataManager", "Applying filter: year=${currentFilter.year}, " +
                    "category=${currentFilter.category}, " +
                    "casteCategory=${currentFilter.casteCategory}, " +
                    "minCutoff=${currentFilter.minCutoff}, " +
                    "maxCutoff=${currentFilter.maxCutoff}"
        )

        Log.d("DataManager", "Filter not yet applied: all colleges ${allColleges.size}")

        for (college in allColleges) {
            // Check year (mandatory)
            if (college.year != currentFilter.year) {
                continue
            }
            if (currentFilter.category.isNotEmpty() &&
                currentFilter.category != "Select Category Type"
            ) {
                // Handle both "cutoff" and "cutoffs" as the same category
                if (currentFilter.category == "cutoffs" && college.jsonCategory != "cutoffs") {
                    continue
                } else if (currentFilter.category == "ranks" && college.jsonCategory != "ranks") {
                    continue
                }
            }


//
//            // Check JSON category type (if selected)
//            if (currentFilter.category.isNotEmpty() &&
//                currentFilter.category != "Select Category Type") {
//                // If a specific category type is selected (cutoffs or ranks), filter by jsonCategory
//                if (college.jsonCategory != currentFilter.category) {
//                    continue
//                }
//            }

            // Check caste category (optional)
            if (currentFilter.casteCategory.isNotEmpty() &&
                !currentFilter.casteCategory.equals("All Caste Categories", ignoreCase = true) &&
                !college.category.equals(currentFilter.casteCategory, ignoreCase = true)
            ) {
                continue
            }

            // In the applyFilter() method of DataManager.kt

// The current implementation is correct for most cases, but we should add better debug logging
// Add this at the beginning of applyFilter():
            Log.d("DataManager", "Filtering with category: ${currentFilter.category}")

// Replace the cutoff range check with this more nuanced version:
// Check cutoff range based on category type
            if (currentFilter.category == "ranks") {
                // For ranks, higher numbers mean worse ranks, so we want lower values
                if (college.cutoff < currentFilter.minCutoff || college.cutoff > currentFilter.maxCutoff) {
                    continue
                }
            } else {
                // For cutoffs, higher numbers are better
                if (college.cutoff < currentFilter.minCutoff || college.cutoff > currentFilter.maxCutoff) {
                    continue
                }
            }


//            // Check cutoff range
//            if (college.cutoff < currentFilter.minCutoff || college.cutoff > currentFilter.maxCutoff) {
//                continue
//            }

            // Check branch (optional)
            if (currentFilter.branch.isNotEmpty() &&
                !currentFilter.branch.equals("All Branches", ignoreCase = true) &&
                !college.branchName.equals(currentFilter.branch, ignoreCase = true)
            ) {
                continue
            }

            // Check district (optional)
            if (currentFilter.district.isNotEmpty() &&
                !currentFilter.district.equals("All Districts", ignoreCase = true) &&
                !college.district.equals(currentFilter.district, ignoreCase = true)
            ) {
                continue
            }

            // All criteria passed, add to filtered list
            filteredColleges.add(college)

        }

        Log.d("DataManager", "Filter applied: ${filteredColleges.size} colleges match criteria")
    }

    // Getter methods for UI components
    fun getAllColleges(): List<CollegeData> {
        return allColleges
    }

    fun getFilteredColleges(): List<CollegeData> {
        return filteredColleges
    }

    fun getCategories(): Set<String> {
        return categories
    }

    fun getJsonCategories(): Set<String> {
        return jsonCategories
    }

    fun getYears(): Set<Int> {
        // Ensure all years are included
        years.add(2020)
        years.add(2021)
        years.add(2022)
        years.add(2023)
        years.add(2024)
        Log.d("DataManager", "Years in getYears: $years")
        return years
    }

    fun getBranches(): Set<String> {
        return branches
    }

    fun getDistricts(): Set<String> {
        return districts
    }

    fun setFilter(filter: FilterOptions) {
        this.currentFilter = filter
    }

    // Helper methods
    @Throws(IOException::class)
    private fun convertStreamToString(inputStream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val sb = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            sb.append(line).append("\n")
        }
        reader.close()
        return sb.toString()
    }

    private fun parseDouble(value: String): Double {
        return try {
            value.toDouble()
        } catch (e: NumberFormatException) {
            0.0
        } catch (e: NullPointerException) {
            0.0
        }
    }
}
