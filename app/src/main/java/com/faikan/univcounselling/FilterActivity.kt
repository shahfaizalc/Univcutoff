package com.faikan.univcounselling

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdListener
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.facebook.ads.AudienceNetworkAds
import com.facebook.ads.CacheFlag.ALL
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import com.faikan.univcounselling.R
import com.faikan.univcounselling.data.DataManager
import com.faikan.univcounselling.model.FilterOptions
import androidx.appcompat.widget.SwitchCompat

class FilterActivity : AppCompatActivity() {
    private var spinnerYear: Spinner? = null
  //  private var spinnerCategory: Spinner? = null
    private var spinnerCasteCategory: Spinner? = null
    private var tvCutoffRangeLabel: TextView? = null
    private var etMinCutoff: EditText? = null
    private var etMaxCutoff: EditText? = null
    private var spinnerBranch: Spinner? = null
    private var spinnerDistrict: Spinner? = null
    private var btnApply: Button? = null
    private var switchCategory: SwitchCompat? = null

    private var dataManager: DataManager? = null
    private var currentFilter: FilterOptions? = null

    lateinit var mInterstitialAd: InterstitialAd
    lateinit var adView: AdView
    lateinit var adView2: AdView
    lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)

        // Initialize views
        initViews()

        // Get current filter from data manager
        dataManager = DataManager.getInstance
        currentFilter = dataManager!!.currentFilter

        // Set up spinners with available options
        setupSpinners()

        // Pre-fill values based on current filter
        prefillValues()

        // Set up category spinner listener to update cutoff label
        setupCategoryListener()

        // Set up apply button
        btnApply!!.setOnClickListener {
            if (validateFilter()) {
                applyFilter()
                setResult(RESULT_OK)
                finish()
            }
        }
        // Set up RecyclerView
        handler = Handler(Looper.myLooper()!!)

        AudienceNetworkAds.initialize(this);
        adViewLoad()
        adViewTopLoad()

    }

    private fun initViews() {
        spinnerYear = findViewById(R.id.spinnerYear)
        switchCategory = findViewById(R.id.switchCategory)
      //  spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerCasteCategory = findViewById(R.id.spinnerCasteCategory)
        tvCutoffRangeLabel = findViewById(R.id.tvCutoffRangeLabel)
        etMinCutoff = findViewById(R.id.etMinCutoff)
        etMaxCutoff = findViewById(R.id.etMaxCutoff)
        spinnerBranch = findViewById(R.id.spinnerBranch)
        spinnerDistrict = findViewById(R.id.spinnerDistrict)
        btnApply = findViewById(R.id.btnApply)
    }

    private fun setupSpinners() {
                // Set up year spinner
        val years = ArrayList<Int>()
        // Direct addition to ensure all years are present
        years.add(2020)
        years.add(2021)
        years.add(2022)
        years.add(2023)
        years.add(2024)
        
        // Sort in descending order (newest first)
        years.sortDescending()
        
        Log.d("FilterActivity", "Years for spinner: $years")
        
        val yearsAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, years
        )
        yearsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYear!!.adapter = yearsAdapter

        // Set up category type spinner (only cutoffs and ranks)
//        val categoryTypes = listOf("Select Category Type", "cutoffs", "ranks")
//        val categoryAdapter = ArrayAdapter(
//            this, android.R.layout.simple_spinner_item, categoryTypes
//        )
//        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinnerCategory!!.adapter = categoryAdapter

        // Set up caste category spinner
        val casteCategories: MutableList<String> = ArrayList()
        casteCategories.add("All Caste Categories")

        // Filter to include only caste categories (OC, BC, etc.)
        val casteCats = dataManager!!.getCategories().filter {
            it != "cutoffs" && it != "ranks"
        }
        casteCategories.addAll(casteCats)

        val casteCategoryAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, casteCategories
        )
        casteCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCasteCategory!!.adapter = casteCategoryAdapter

        // Set up branch spinner
        val branches: MutableList<String> = ArrayList()
        branches.add("All Branches")
        branches.addAll(dataManager!!.getBranches())
        val branchAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, branches
        )
        branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBranch!!.adapter = branchAdapter

        // Set up district spinner
        val districts: MutableList<String> = ArrayList()
        districts.add("All Districts")
        districts.addAll(dataManager!!.getDistricts())
        val districtAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, districts
        )
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDistrict!!.adapter = districtAdapter
    }

    private fun setupCategoryListener() {
        // Set initial text based on default state (unchecked = cutoffs, checked = ranks)
        updateCutoffRangeLabel(switchCategory!!.isChecked)

        // Add listener for switch changes
        switchCategory!!.setOnCheckedChangeListener { _, isChecked ->
            updateCutoffRangeLabel(isChecked)
            if (isChecked) {
                // For ranks - typical range might be 1-10000
                etMinCutoff!!.setText("1")
                etMaxCutoff!!.setText("1000")
            } else {
                // For cutoffs - typical range might be 100-200
                etMinCutoff!!.setText("0")
                etMaxCutoff!!.setText("200")
            }
        }
    }

    private fun updateCutoffRangeLabel(isRanks: Boolean) {
        if (isRanks) {
            tvCutoffRangeLabel!!.text = "Rank Range (Higher is better)"
        } else {
            tvCutoffRangeLabel!!.text = "Cutoff Range (Between 0 to 200)"
        }
    }


//
//    private fun setupCategoryListener() {
//        spinnerCategory!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                val selectedCategory = parent?.getItemAtPosition(position).toString()
//
//                if (selectedCategory.equals("ranks", ignoreCase = true)) {
//                    tvCutoffRangeLabel!!.text = "Rank Range (Higher is better)"
//                } else {
//                    tvCutoffRangeLabel!!.text = "Cutoff Range (Between 0 to 200)"
//                }
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//                // Do nothing
//            }
//        }
//    }

    private fun prefillValues() {
        // Set year spinner
        val yearPosition = (spinnerYear?.adapter as ArrayAdapter<Any?>)
            .getPosition(currentFilter!!.year)
        if (yearPosition >= 0) {
            spinnerYear!!.setSelection(yearPosition)
        }

        // Set category spinner
//        if (currentFilter!!.category.isNotEmpty()) {
//            val categoryPosition = (spinnerCategory!!.adapter as ArrayAdapter<String?>)
//                .getPosition(currentFilter!!.category)
//            if (categoryPosition > 0) {
//                spinnerCategory!!.setSelection(categoryPosition)
//            }
//        }

        // Set caste category spinner
        if (currentFilter!!.casteCategory.isNotEmpty()) {
            val casteCategoryPosition = (spinnerCasteCategory!!.adapter as ArrayAdapter<String?>)
                .getPosition(currentFilter!!.casteCategory)
            if (casteCategoryPosition > 0) {
                spinnerCasteCategory!!.setSelection(casteCategoryPosition)
            }
        }

        // Set cutoff range
        etMinCutoff!!.setText(currentFilter!!.minCutoff.toString())
        etMaxCutoff!!.setText(currentFilter!!.maxCutoff.toString())

        // Set branch spinner
        if (currentFilter!!.branch.isNotEmpty()) {
            val branchPosition = (spinnerBranch!!.adapter as ArrayAdapter<String?>)
                .getPosition(currentFilter!!.branch)
            if (branchPosition > 0) {
                spinnerBranch!!.setSelection(branchPosition)
            }
        }

        // Set district spinner
        if (currentFilter!!.district.isNotEmpty()) {
            val districtPosition = (spinnerDistrict!!.adapter as ArrayAdapter<String?>)
                .getPosition(currentFilter!!.district)
            if (districtPosition > 0) {
                spinnerDistrict!!.setSelection(districtPosition)
            }
        }
    }

    private fun validateFilter(): Boolean {
        // Check if category type is selected (mandatory)
//        if (spinnerCategory!!.selectedItemPosition == 0) {
//            Toast.makeText(this, "Please select a category type (cutoffs or ranks)", Toast.LENGTH_SHORT).show()
//            return false
//        }

        // Validate cutoff/rank range
        try {
            val minCutoff = etMinCutoff!!.text.toString().toInt()
            val maxCutoff = etMaxCutoff!!.text.toString().toInt()

            if (minCutoff > maxCutoff) {
                Toast.makeText(
                    this,
                    "Min value should be less than max value",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Please enter valid numeric values", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun applyFilter() {
        val filter = FilterOptions()

        // Set year
        filter.year = Integer.parseInt(spinnerYear!!.selectedItem.toString())

        // Set category type (mandatory - cutoffs or ranks)
//        val categoryType = switchCategory!!.text
//        filter.category = categoryType.toString()
        // In the applyFilter() method of FilterActivity.kt, replace these lines:
// val categoryType = switchCategory!!.text
// filter.category = categoryType.toString()

// With this code:
        filter.category = if (switchCategory!!.isChecked) "ranks" else "cutoffs"


        // Set caste category (optional)
        if (spinnerCasteCategory!!.selectedItemPosition > 0) {
            filter.casteCategory = spinnerCasteCategory!!.selectedItem.toString()
        }

        // Set cutoff/rank range
        val minCutoff = etMinCutoff!!.text.toString().toInt()
        val maxCutoff = etMaxCutoff!!.text.toString().toInt()
        filter.minCutoff = minCutoff
        filter.maxCutoff = maxCutoff

        // Set branch (optional)
        if (spinnerBranch!!.selectedItemPosition > 0) {
            filter.branch = spinnerBranch!!.selectedItem.toString()
        }

        // Set district (optional)
        if (spinnerDistrict!!.selectedItemPosition > 0) {
            filter.district = spinnerDistrict!!.selectedItem.toString()
        }

        // Apply filter
        dataManager!!.setFilter(filter)
        dataManager!!.applyFilter()

        // Log the filter for debugging
        Log.d("FilterActivity", "Applied filter: ${filter.category}, caste: ${filter.casteCategory}, " +
                "min: ${filter.minCutoff}, max: ${filter.maxCutoff}")
    }
    private fun adViewLoad() {
        try {
            prepareInterstitialAd()
            adView = AdView(
                this,
                this.resources.getString(R.string.banner_filter_footer),
                AdSize.BANNER_HEIGHT_50
            )
            // Find the Ad Container
            val adContainer = findViewById(R.id.banner_container1) as LinearLayout

            // Add the ad view to your activity layout
            adContainer.addView(adView)
            var adListener = object : AdListener {
                override fun onError(p0: Ad?, p1: AdError?) {
                }

                override fun onAdLoaded(p0: Ad?) {
                }

                override fun onAdClicked(p0: Ad?) {
                }

                override fun onLoggingImpression(p0: Ad?) {
                }
            }
            // Request an ad
            adView.loadAd(adView.buildLoadAdConfig().withAdListener(adListener).build());


        } catch (e: Exception) {

        }
    }

    private fun adViewTopLoad() {
        try {

            adView2 = AdView(
                this,
                this.resources.getString(R.string.banner_filter_top),
                AdSize.BANNER_HEIGHT_50
            )
            // Find the Ad Container
            val adContainer = findViewById(R.id.banner_container_top2) as LinearLayout

            // Add the ad view to your activity layout
            adContainer.addView(adView2)
            var adListener = object : AdListener {
                override fun onError(p0: Ad?, p1: AdError?) {
                }

                override fun onAdLoaded(p0: Ad?) {
                }

                override fun onAdClicked(p0: Ad?) {
                }

                override fun onLoggingImpression(p0: Ad?) {
                }
            }
            // Request an ad
            adView2.loadAd(adView2.buildLoadAdConfig().withAdListener(adListener).build());
        } catch (e: Exception) {

        }
    }


    var MAX_ATTEMPTS = 5;
    private fun prepareInterstitialAd() {
        var ATTEMPTS = 0;
        mInterstitialAd =
            InterstitialAd(this, this.resources.getString(R.string.interstitial_full_screen))

        var nativeAdListener = object : InterstitialAdListener {
            override fun onError(p0: Ad?, p1: AdError?) {
                ATTEMPTS = ATTEMPTS + 1;
                retryAdLoad(ATTEMPTS)
            }

            override fun onAdLoaded(p0: Ad?) {
                mInterstitialAd.show()
            }

            override fun onAdClicked(p0: Ad?) {
            }

            override fun onLoggingImpression(p0: Ad?) {
            }

            override fun onInterstitialDisplayed(p0: Ad?) {
            }

            override fun onInterstitialDismissed(p0: Ad?) {
            }
        }
        mInterstitialAd.loadAd(
            mInterstitialAd.buildLoadAdConfig()
                .withAdListener(nativeAdListener)
                .withCacheFlags(ALL)
                .build()
        );

        showAdWithDelay()

    }

    private fun retryAdLoad(attempt: Int) {
        if (attempt <= MAX_ATTEMPTS) {
            Handler().postDelayed(
                { mInterstitialAd.loadAd() },
                (60000).toLong()
            ) // Increasing delay with each attempt
        }
    }

    private fun showAdWithDelay() {
        handler.postDelayed(Runnable {
            // Check if interstitialAd has been loaded successfully
            if (!mInterstitialAd.isAdLoaded()) {
                return@Runnable
            }
            if (mInterstitialAd.isAdInvalidated()) {
                return@Runnable
            }
            // Show the ad
            mInterstitialAd.show()
        }, 1000 * 60 * 3) // Show the ad after 10 minutes
    }

    override fun onDestroy() {
        if (adView != null) {
            adView.destroy()
        }
        if (adView2 != null) {
            adView2.destroy()
        }
        if (mInterstitialAd != null) {
            mInterstitialAd.destroy();
        }
        super.onDestroy()
    }
}
