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
import com.bakkenbaeck.sol.extension.isLocationPermissionGranted
import com.bakkenbaeck.sol.extension.requestLocationPermission
import com.bakkenbaeck.sol.extension.startActivityAndFinish
import kotlinx.android.synthetic.main.activity_start.infoMessage
import kotlinx.android.synthetic.main.activity_start.root

class StartActivity : BaseActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE_LOCATION = 123
    }

    private val requestLocationMessage: String by lazy {
        val message = getString(R.string.start__permission_description)
        val color = ContextCompat.getColor(this, R.color.daylight_text2)
        return@lazy message.replace("{color}", color.toString())
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
        root.setOnClickListener { requestLocationPermission(PERMISSION_REQUEST_CODE_LOCATION) }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {

        if (requestCode == PERMISSION_REQUEST_CODE_LOCATION && isLocationPermissionGranted()) {
            startActivityAndFinish<SunActivity>()
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
