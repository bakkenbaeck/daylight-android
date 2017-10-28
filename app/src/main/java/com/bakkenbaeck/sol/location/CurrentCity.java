package com.bakkenbaeck.sol.location;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;

import com.bakkenbaeck.sol.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CurrentCity {
    private final Context context;

    public CurrentCity(final Context context) {
        this.context = context;
    }

    public String get(final double lat, final double lng) {
        final Geocoder geocoder = new Geocoder(this.context, Locale.getDefault());
        try {
            final List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            final Address address = addresses.get(0);

            if (address.getSubAdminArea() != null) {
                return address.getSubAdminArea();
            }

            if (address.getLocality() != null) {
                return address.getLocality();
            }

            return defaultLocation();
        } catch (final IOException e) {
            return defaultLocation();
        }
    }

    public String getCityAndCountry(final double latitude,
                                    final double longitude) {
        final Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            final List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            final Address address = getAddress(addresses);

            if (address == null) {
                return defaultLocation();
            }

            final String locality = getLocality(address);
            final String country = getCountry(address);
            return String.format("%s%s", locality, country);
        } catch (IOException e) {
            return defaultLocation();
        }
    }

    private Address getAddress(final List<Address> addresses) {
        return addresses != null && addresses.size() > 0
                ? addresses.get(0)
                : null;
    }

    private String getLocality(final Address address) {
        return address.getLocality() != null
                ? address.getLocality()
                : defaultLocation();
    }

    private String getCountry(final Address address) {
        return address.getCountryName() != null
                ? ", " + address.getCountryName()
                : "";
    }

    @NonNull
    private String defaultLocation() {
        return context.getString(R.string.default_city);
    }
}
