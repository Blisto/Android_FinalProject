package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class RouteActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private List<LatLng> Loadline = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private String route;
    private TextView distence;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        distence = (TextView) findViewById(R.id.distance);
        route=getIntent().getStringExtra("filePath");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        DecimalFormat df = new DecimalFormat("######0.0");
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(25.052688333, 121.5203866666);
        mMap.addMarker(new MarkerOptions().position(sydney).title("中山站"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,27));
        readData(route);
        distence.setText("本次移動 :"+df.format(getTotalDistance(Loadline))+" 公尺");
    }

    private void readData(String fpath){
        FileInputStream fp = null;
        try {

            fp = new FileInputStream(fpath);
            ObjectInputStream fpObj = new ObjectInputStream(fp);
            Double lat, lng;
            int i=0;
            try {
                while (true){
                    lat = (Double) fpObj.readDouble();
                    lng = (Double) fpObj.readDouble();
                    Loadline.add(new LatLng(lat,lng));
                    Log.i("double1", lat + "," + lng + " now i:" + i);
                    i++;
                }
            }catch (EOFException e){
                //e.printStackTrace();
            }finally {
                fpObj.close();
            }

            //Toast.makeText(getApplicationContext(), "Data has load success " + getFilesDir().toString(), Toast.LENGTH_LONG).show();

            mMap.clear();
            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(20);
            polylineOptions.addAll(Loadline);
            polylinePaths.add(mMap.addPolyline(polylineOptions));
            mMap.addPolyline(polylineOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(Loadline.get(Loadline.size()-1)));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fp != null) fp.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Double getTotalDistance(List<LatLng> routes){
        Double total= Double.valueOf(0);
        float[] result = new float[1];
        if(routes==null || routes.size()<=1)return Double.valueOf(Float.valueOf(0));

        for(int i=0;i<routes.size();i++){
            if(i<routes.size()-1){
                Location.distanceBetween(routes.get(i).latitude,routes.get(i).longitude,routes.get(i+1).latitude,routes.get(i+1).longitude,result);
                total+=result[0];
            }
        }
        return total;
    }
}
