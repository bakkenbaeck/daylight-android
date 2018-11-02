package com.bakkenbaeck.sol.location


import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.bakkenbaeck.sol.R
import java.io.IOException
import java.util.Locale

class CurrentCity(private val context: Context) {

    operator fun get(lat: Double, lng: Double): String {
        val geocoder = Geocoder(this.context, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            val address = addresses[0]

            if (address.subAdminArea != null) {
                return address.subAdminArea
            }

            return if (address.locality != null) {
                address.locality
            } else defaultLocation()

        } catch (e: IOException) {
            return defaultLocation()
        }

    }

    fun getCityAndCountry(latitude: Double,
                          longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            val address = getAddress(addresses) ?: return defaultLocation()

            val locality = getLocality(address)
            val country = getCountry(address)
            return String.format("%s%s", locality, country)
        } catch (e: IOException) {
            return defaultLocation()
        }

    }

    private fun getAddress(addresses: List<Address>?): Address? {
        return if (addresses != null && addresses.isNotEmpty())
            addresses[0]
        else
            null
    }

    private fun getLocality(address: Address): String {
        return if (address.locality != null)
            address.locality
        else
            defaultLocation()
    }

    private fun getCountry(address: Address): String {
        return if (address.countryName != null)
            ", " + address.countryName
        else
            ""
    }

    private fun defaultLocation(): String = context.getString(R.string.default_city)
}
