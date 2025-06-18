package com.faikan.univcutoff

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.faikan.univcutoff.adapters.CollegeAdapter
import com.faikan.univcutoff.data.DataManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdListener
import com.facebook.ads.AdSettings
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.facebook.ads.AudienceNetworkAds
import com.facebook.ads.CacheFlag.ALL
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import com.faikan.univcutoff.R

class MainActivity : AppCompatActivity() {

    lateinit var mInterstitialAd: InterstitialAd
    lateinit var adView: AdView
    lateinit var adView2: AdView
    lateinit var handler: Handler


    private var recyclerView: RecyclerView? = null
    private var adapter: CollegeAdapter? = null
    private var fabFilter: FloatingActionButton? = null
    private var tvNoData: TextView? = null
    private var dataManager: DataManager? = null

    private var tvFilterDetails: TextView? = null
    private var tvItemCount: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView)
        fabFilter = findViewById(R.id.fabFilter)
        tvNoData = findViewById(R.id.tvNoData)
        tvFilterDetails = findViewById(R.id.tvFilterDetails)
        tvItemCount = findViewById(R.id.tvItemCount)


        // Initialize data manager

        dataManager = DataManager.getInstance
        dataManager!!.loadData(this)

        // Set up RecyclerView
        handler = Handler(Looper.myLooper()!!)

        AudienceNetworkAds.initialize(this);
        adViewLoad()
        adViewTopLoad()

        setupRecyclerView()

        // Set up filter button
        fabFilter!!.setOnClickListener(View.OnClickListener {
            val intent = Intent(
                this@MainActivity,
                FilterActivity::class.java
            )
            startActivityForResult(intent, FILTER_REQUEST_CODE)
        })
        updateFilterDisplay()
    }



    override fun onResume() {
        super.onResume()
        // Update filter display whenever returning to this activity
        updateFilterDisplay()
    }

    private fun updateFilterDisplay() {
        // Get current filter from DataManager
        val filter = dataManager!!.currentFilter

        // Get the filtered colleges
        val filteredColleges = dataManager!!.getFilteredColleges()

        // Get actual category from DataManager
        // The issue is that filter.category might be empty initially
        val actualCategory = if (filter.category.isEmpty()) {
            // If the category is empty, get the category from the first item
            // or default to "cutoffs" if no items
            if (filteredColleges.isNotEmpty()) {
                filteredColleges[0].jsonCategory
            } else {
                "cutoffs"
            }
        } else {
            filter.category
        }

        // Determine category display text
        val categoryText = when {
            actualCategory == "cutoffs" -> "Cutoffs"
            actualCategory == "ranks" -> "Ranks"
            else -> actualCategory
        }

        var filterCategory = if (filter.casteCategory.isNullOrEmpty()) {
            "All"
        } else {
            filter.casteCategory
        }
        var districtCategory = if (filter.district.isNullOrEmpty()) {
            "All"
        } else {
            filter.district
        }

        // Update the filter details
        tvFilterDetails?.text = "Year: ${filter.year} | Category: $categoryText" +
                "\nCaste: $filterCategory | District: $districtCategory"

        // Update the item count
        tvItemCount?.text = "Results: ${filteredColleges.size} items"
    }



    private fun setupRecyclerView() {
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        adapter = CollegeAdapter(dataManager!!.getFilteredColleges())
        recyclerView!!.adapter = adapter

        updateNoDataVisibility()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView?
        searchView!!.queryHint ="College Name, District"

        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter!!.filter.filter(newText)
                return true
            }
        })

        return true
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == FILTER_REQUEST_CODE && resultCode == RESULT_OK) {
//            // Update the list with filtered data
//            val filteredColleges = dataManager!!.getFilteredColleges()
//            adapter!!.updateData(filteredColleges)
//            updateNoDataVisibility()
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILTER_REQUEST_CODE && resultCode == RESULT_OK) {
            // Update the adapter with filtered colleges
            adapter?.updateData(dataManager!!.getFilteredColleges())

            // Update the filter display
            updateFilterDisplay()

            // Show or hide "No data" message based on filtered results
            if (dataManager!!.getFilteredColleges().isEmpty()) {
                tvNoData?.visibility = View.VISIBLE
            } else {
                tvNoData?.visibility = View.GONE
            }
        }
    }


    private fun updateNoDataVisibility() {
        if (adapter!!.itemCount == 0) {
            recyclerView!!.visibility = View.GONE
            tvNoData!!.visibility = View.VISIBLE
        } else {
            recyclerView!!.visibility = View.VISIBLE
            tvNoData!!.visibility = View.GONE
        }
    }
    private fun adViewLoad() {
        try {
            prepareInterstitialAd()

            adView = AdView(
                this,
                this.resources.getString(R.string.banner_main_footer),
                AdSize.BANNER_HEIGHT_50
            )
            // Find the Ad Container
            val adContainer = findViewById(R.id.banner_container_bottommain) as LinearLayout

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
                this.resources.getString(R.string.banner_main_top),
                AdSize.BANNER_HEIGHT_50
            )
            // Find the Ad Container
            val adContainer = findViewById(R.id.banner_container_main_top) as LinearLayout

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
            InterstitialAd(this, this.resources.getString(R.string.interstitial_full_screen2))

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
    companion object {
        private const val FILTER_REQUEST_CODE = 100
    }
}
