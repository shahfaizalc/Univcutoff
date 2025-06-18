package com.faikan.univcutoff

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdListener
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.facebook.ads.AudienceNetworkAds


class HomeActivity : AppCompatActivity() {
    private var cardImportantDates: CardView? = null
    private var cardPreviousCutoffs: CardView? = null
    lateinit var adView: AdView
    lateinit var adView2: AdView
    lateinit var handler: Handler


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        // Initialize views
        cardImportantDates = findViewById<CardView>(R.id.card_important_dates)
        cardPreviousCutoffs = findViewById<CardView>(R.id.card_previous_cutoffs)

        // Set click listeners for navigation
        cardImportantDates!!.setOnClickListener(View.OnClickListener {
            val intent = Intent(
                this@HomeActivity,
                ImportantDatesActivity::class.java
            )
            startActivity(intent)
        })

        cardPreviousCutoffs!!.setOnClickListener(View.OnClickListener {
            val intent = Intent(
                this@HomeActivity,
                MainActivity::class.java
            )
            Toast.makeText(this.applicationContext, "Launching previous years cutoffs", Toast.LENGTH_SHORT).show();
            startActivity(intent)
        })

        // Set up RecyclerView
        handler = Handler(Looper.myLooper()!!)

        AudienceNetworkAds.initialize(this);
        adViewLoad()
        adViewTopLoad()

    }

    private fun adViewLoad() {
        try {
            adView = AdView(
                this,
                this.resources.getString(R.string.banner_home_top),
                AdSize.BANNER_HEIGHT_50
            )
            // Find the Ad Container
            val adContainer = findViewById(R.id.banner_container_tophome) as LinearLayout

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
                this.resources.getString(R.string.banner_home_footer),
                AdSize.BANNER_HEIGHT_50
            )
            // Find the Ad Container
            val adContainer = findViewById(R.id.banner_container_bottomhome) as LinearLayout

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

    override fun onDestroy() {
        if (adView != null) {
            adView.destroy()
        }
        if (adView2 != null) {
            adView2.destroy()
        }

        super.onDestroy()
    }


}
