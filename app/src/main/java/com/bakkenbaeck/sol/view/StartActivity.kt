package com.bakkenbaeck.sol.view

import android.os.Bundle
import androidx.core.content.ContextCompat
import com.bakkenbaeck.sol.R
import com.bakkenbaeck.sol.extension.isLocationPermissionGranted
import com.bakkenbaeck.sol.extension.requestLocationPermission
import com.bakkenbaeck.sol.extension.startActivityAndFinish
import com.bakkenbaeck.sol.extension.toHtml
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
        if (!isLocationPermissionGranted()) {
            initView()
        } else {
            startActivityAndFinish<SunActivity>()
        }
    }

    private fun initView() {
        assignClickListeners()
    }

    private fun assignClickListeners() {
        infoMessage.text = requestLocationMessage.toHtml()
        root.setOnClickListener { requestLocationPermission(PERMISSION_REQUEST_CODE_LOCATION) }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {

        if (requestCode == PERMISSION_REQUEST_CODE_LOCATION && isLocationPermissionGranted()) {
            startActivityAndFinish<SunActivity>()
        }
    }
}
