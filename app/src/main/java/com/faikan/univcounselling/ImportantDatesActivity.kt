package com.faikan.univcounselling

import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.faikan.univcounselling.R


class ImportantDatesActivity : AppCompatActivity() {
    private var imageView: ZoomableImageView? = null
    private var languageToggle: RadioGroup? = null
    private var btnZoomIn: Button? = null
    private var btnZoomOut: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_important_days)


        // Initialize views
        imageView = findViewById(R.id.image_view)
        languageToggle = findViewById(R.id.language_toggle)
        btnZoomIn = findViewById(R.id.btn_zoom_in)
        btnZoomOut = findViewById(R.id.btn_zoom_out)


        // Set default image (English)
        imageView!!.setImageResource(R.drawable.important_dates_english)


        // Setup language toggle listener
        languageToggle!!.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.btn_tamil) {
                imageView!!.setImageResource(R.drawable.important_dates_tamil)
            } else {
                imageView!!.setImageResource(R.drawable.important_dates_english)
            }
        }


        // Setup zoom buttons
        btnZoomIn!!.setOnClickListener { imageView!!.zoomIn() }

        btnZoomOut!!.setOnClickListener { imageView!!.zoomOut() }

    }
}
