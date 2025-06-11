package com.faikan.univcutoff.model

data class CollegeData(
    var collegeCode: Int,
    var collegeName: String,
    var branchCode: String,
    var branchName: String,
    var year: Int,
    var category: String, // This is the caste category (OC, BC, etc.)
    var cutoff: Double,
    var district: String,
    var address: String,
    var pincode: Int,
    var jsonCategory: String // This is the JSON category (cutoffs or ranks)
)
