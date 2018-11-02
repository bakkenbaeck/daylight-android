package com.bakkenbaeck.sol.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.Html
import android.text.Spanned
import com.bakkenbaeck.sol.R
import kotlinx.android.synthetic.main.activity_start.infoMessage
import kotlinx.android.synthetic.main.activity_start.root

class StartActivity : BaseActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE_LOCATION = 123
    }

    private val requestLocationMessage: String
        get() {
            val message = getString(R.string.start__permission_description)
            val color = ContextCompat.getColor(this, R.color.daylight_text2)
            return message.replace("{color}", color.toString())
        }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        checkForLocationPermission()
    }

    private fun checkForLocationPermission() {
        val locationPermissionGranted = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!locationPermissionGranted) {
            initView()
        } else {
            val intent = Intent(this, SunActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private fun initView() {
        assignClickListeners()
    }

    private fun assignClickListeners() {
        infoMessage.text = convertToHtml(requestLocationMessage)
        root.setOnClickListener {
            ActivityCompat.requestPermissions(this@StartActivity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    PERMISSION_REQUEST_CODE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE_LOCATION && grantResults.size == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(this, SunActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private fun convertToHtml(message: String): Spanned {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(message)
        }
    }
}
