package com.faikan.univcutoff.model

class FilterOptions {
    var year: Int = 2024 // Default year
    var category: String = "" // Only "cutoffs" or "ranks" (mandatory)
    var casteCategory: String = "" // SC, SCA, BC, etc. (optional)
    var minCutoff: Int = 0
    var maxCutoff: Int = 200
    var branch: String = "" // Optional
    var district: String = "" // Optional
}
