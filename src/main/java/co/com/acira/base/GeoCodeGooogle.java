/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.base;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.LatLng;
import java.io.IOException;

/**
 *
 * @author wilme
 */
public class GeoCodeGooogle {

    public String getLocationName(double lat, double lon) {
        Geocoder geocoder = new Geocoder();
        LatLng p = new LatLng(Double.toString(lat), Double.toString(lon));
        GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().setLocation(p).setLanguage("es").getGeocoderRequest();
        GeocodeResponse geocoderResponse;
        try {
            geocoderResponse = geocoder.geocode(geocoderRequest);
            System.out.println(geocoderResponse.getResults().size());
            if (geocoderResponse.getResults().size() >= 1) {
                GeocoderResult result = geocoderResponse.getResults().get(0);
                System.out.println(result.getFormattedAddress());
                System.out.println(result.getGeometry().getLocationType().toString());
                if (result.getGeometry().getLocationType().toString().equals("ROOFTOP")
                        || result.getGeometry().getLocationType().toString().equals("RANGE_INTERPOLATED")) {
                    String direccion = result.getFormattedAddress();
                    int secondIndex = direccion.indexOf(",", direccion.indexOf(",") + 1);
                    direccion = direccion.substring(0, secondIndex);
                    System.out.println("direccion google: " + direccion);
                    return direccion;
                } else {
                    System.out.println("direccion google: ninguna");
                    return "ninguna";
                }
            } else {
                return "ninguna";
            }
        } catch (IOException e) {
            System.out.println("Error: GeoCodeGooogle-getLocationName: " + e.getLocalizedMessage());
            return "ninguna";
        }
    }

}
