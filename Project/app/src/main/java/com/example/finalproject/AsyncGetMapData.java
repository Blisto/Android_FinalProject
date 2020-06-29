package com.example.finalproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class AsyncGetMapData extends AsyncTask<String, Void, String> {
//https://maps.googleapis.com/maps/api/directions/json?origin=Disneyland&destination=Universal+Studios+Hollywood&key=AIzaSyCDE1bTjRZC8JRNQZd4cexV2cHD1DLnXJI


    String domain ="https://maps.googleapis.com/maps/api/directions/json?";
    private  Context mcontext;
    private Direction mDirection ;
    private DirectionFinderListener listener;
    private String data;
    private ProgressDialog ProDialog;
    private String start,des;

    public AsyncGetMapData (DirectionFinderListener d, Context context, ProgressDialog Pd,String s,String des){
        this.listener=d;
        this.mcontext = context;
        this.ProDialog= Pd;
        this.start=s;
        this.des=des;
    }





    @Override
    protected String doInBackground(String... urls) {

        String result = "";
        HttpsURLConnection urlConnection = null;
        URL url;
        try {

            InputStream inputStream;

            Uri.Builder builder = new Uri.Builder()

                    .appendQueryParameter("origin",start)
                    .appendQueryParameter("destination",des)
                    .appendQueryParameter("key","AIzaSyCDE1bTjRZC8JRNQZd4cexV2cHD1DLnXJI");
            String query = builder.build().getEncodedQuery();

            url = new URL(domain+query);
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");

            Log.i("test",urlConnection.getURL().toString());
            Log.i("test", String.valueOf(urlConnection.getResponseCode()));


            if (urlConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                inputStream = urlConnection.getInputStream();
            } else {
                inputStream = urlConnection.getErrorStream();
            }

            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(inputStream));
            String inputLine;

            while ((inputLine = br.readLine()) != null) {
                result += inputLine;
            }
            br.close();
            data = result;
            return result;

        }catch (Exception e){
            e.printStackTrace();
            return "Get source failed please change other protocol or check URL.";
        }

        //return null;
    }

    @Override
    protected void onPostExecute(String res) {

        try {
            listener.onDirectionFinderStart();
            parseJSon(data);

            Toast.makeText(mcontext,"Process Successus Doing",Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(mcontext,"Process Fault Doing",Toast.LENGTH_SHORT).show();
        }



        //super.onPostExecute(res);
    }


    private void parseJSon(String Data) throws JSONException{
        if (Data==null)return;
        mDirection = new Direction();
        List<Direction.Leg> routes = new ArrayList<>();
        mDirection.routes = new ArrayList<>();
        JSONObject jsonData = new JSONObject(Data);

        JSONArray jsonRoutes = jsonData.getJSONArray("routes");
        for(int i=0;i<jsonRoutes.length();i++){
            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
            Direction.Leg route = new Direction.Leg();
            JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
            JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
            JSONObject jsonLeg = jsonLegs.getJSONObject(0);
            JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
            JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
            JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
            JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");
            route.distance = new Direction.Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));

            route.duration = new Direction.Duration(jsonDuration.getString("text"), jsonDuration.getInt("value"));
            route.end_address = jsonLeg.getString("end_address");
            route.start_address = jsonLeg.getString("start_address");
            route.start_location = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
            route.end_location = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
            route.steps = decodePolyLine(overview_polylineJson.getString("points"));
            routes.add(route);
        }
        listener.onDirectionFinderSuccess(routes);


    }

    private List<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }
}
