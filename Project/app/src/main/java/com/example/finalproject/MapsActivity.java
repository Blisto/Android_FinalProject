package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,DirectionFinderListener {

    private GoogleMap mMap;

    //Debug
    private EditText editText;
    private EditText deseditText;
    private Button testBtn;
    private Button getXYBtn;
    private TextView latlngTextView;
    private Button saveBtn;
    private Button loadBtn;
    private Button intenBtn;
    private ConstraintLayout testingUnit;


    //Operation
    private Button h_startBtn;
    private Button h_stopBtn;
    private Button h_saveBtn;
    private TextView h_Status;


    private boolean IsRecording=false;

    private Location mLocation;
    private LocationManager mLocationManager;
    public static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private List<LatLng> Nowline;
    private List<LatLng> Loadline = new ArrayList<>();
    private List<LatLng> GPSline = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        TestingLoadingWidget();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.DEBUG_option){
            if(testingUnit.getVisibility()==View.VISIBLE){
                testingUnit.setVisibility(View.GONE);
                Log.i("menu","operation");
            }else if(testingUnit.getVisibility()==View.GONE){
                testingUnit.setVisibility(View.VISIBLE);
                Log.i("menu","debug");
            }
        }
        if(item.getItemId()==R.id.Listallrecord){
            Intent intent;
            intent = new Intent(MapsActivity.this, RecordActivity.class);
            startActivity(intent);
        }

        if(item.getItemId()==R.id.createPolyData){
            initPolylineTestObj();
            Toast.makeText(getApplicationContext(),"足跡資料已建立",Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //註冊權限
        if(ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED&&
                ActivityCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_ACCESS_COARSE_LOCATION);
            return;
        }

        if (!gpsIsOpen())
            return;
        mLocation = getLocation();
        if (mLocation != null){
            latlngTextView.setText("維度:" + mLocation.getLatitude() + " 經度:" + mLocation.getLongitude());
            h_Status.setText("尚未開始記錄 預設跟隨模式");
        }else{
            latlngTextView.setText("獲取不到資料");
            h_Status.setText("GPS開啟失敗");
        }
    }


    /**
     * 下面列出所有按鈕組件
     *
     **********************************************************************
     * *********************************************************************
     */
    //讀寫檔案組件
    public Button.OnClickListener SaveClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            FileOutputStream fp = null;

            try {
                fp = new FileOutputStream(getApplicationContext().getFilesDir().getPath().toString()+"/dataasc.txt");
                ObjectOutputStream fpObj =  new ObjectOutputStream(fp);

                for(LatLng pline:Nowline ){
                    fpObj.writeDouble(pline.latitude);
                    fpObj.writeDouble(pline.longitude);
                    Log.i("check :",pline.latitude+","+pline.longitude);
                }
                fpObj.flush();
                fpObj.close();
                Toast.makeText(getApplicationContext(),"Data has saved " +getFilesDir().toString(),Toast.LENGTH_LONG).show();

            }catch (FileNotFoundException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(fp!=null){
                    try {
                        fp.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    public final Button.OnClickListener LoadClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            FileInputStream fp = null;
            try {

                fp = new FileInputStream(getApplicationContext().getFilesDir().getPath().toString() + "/dataasc.txt");
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

                Toast.makeText(getApplicationContext(), "Data has load success " + getFilesDir().toString(), Toast.LENGTH_LONG).show();

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
    };

    //GPS組件
    public Button.OnClickListener btnClickListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Button btn = (Button) v;
            if (btn.getId() == R.id.getXY) {
                if (!gpsIsOpen())
                    return;
                mLocation = getLocation();
                if (mLocation != null){
                    latlngTextView.setText("維度:" + mLocation.getLatitude() + " 經度:" + mLocation.getLongitude());
                    mMap.clear();
                    originMarkers.add(mMap.addMarker(new MarkerOptions().title("你在這裡").position(new LatLng(mLocation.getLatitude(),mLocation.getLongitude()))));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLocation.getLatitude(),mLocation.getLongitude())));
                }else{
                    latlngTextView.setText("獲取不到資料");
                }
            }
        }
    };


    public Button.OnClickListener intentbtn = new Button.OnClickListener() {
        public void onClick(View v) {
            Button btn = (Button) v;
            Intent intent;
            intent = new Intent(MapsActivity.this, RecordActivity.class);
            if (btn.getId() == R.id.newact) {
                intent.putExtra("test","123");
                startActivity(intent);
            }
        }
    };


    public Button.OnClickListener StartRecord = new Button.OnClickListener() {
        public void onClick(View v) {
            if(IsRecording==true){
                Toast.makeText(getApplicationContext(),"你已經開始紀錄了",Toast.LENGTH_SHORT).show();

            }else{
                IsRecording=true;
                h_Status.setText("正在紀錄...");
            }
        }
    };

    public Button.OnClickListener StopRecording = new Button.OnClickListener() {
        public void onClick(View v) {
            if(IsRecording==true){
                IsRecording=false;
                h_Status.setText("已停止紀錄 清除地圖上所有路徑");
                mMap.clear();
                GPSline.clear();
            }else{
                h_Status.setText("未進行記錄");
            }
        }
    };

    public Button.OnClickListener SaveRecord = new Button.OnClickListener() {
        public void onClick(View v) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String dateString = formatter.format(new Date(Long.parseLong(String.valueOf(System.currentTimeMillis()))));
            if(IsRecording==true){
                IsRecording=false;
                FileOutputStream fp = null;
                try {
                    fp = new FileOutputStream(getApplicationContext().getFilesDir().getPath().toString()+"/"+dateString+".PolyLinedat");
                    ObjectOutputStream fpObj =  new ObjectOutputStream(fp);
                    for(LatLng pline:GPSline ){
                        fpObj.writeDouble(pline.latitude);
                        fpObj.writeDouble(pline.longitude);
                        Log.i("check :",pline.latitude+","+pline.longitude);
                    }
                    fpObj.flush();
                    fpObj.close();
                    Toast.makeText(getApplicationContext(),"Data has saved " +getFilesDir().getPath().toString()+"/record/"+dateString+".dat",Toast.LENGTH_SHORT).show();

                }catch (FileNotFoundException e){
                    e.printStackTrace();
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(fp!=null){
                        try {
                            fp.close();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }
                h_Status.setText("已儲存紀錄 清除地圖上所有路徑");
                mMap.clear();
                GPSline.clear();
            }else{
                h_Status.setText("未進行記錄");
            }
        }
    };
    /**
     **********************************************************************
     * *********************************************************************
     */



    private boolean gpsIsOpen() {
        boolean bRet = true;
        LocationManager alm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!alm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "未開啟GPS", Toast.LENGTH_SHORT).show();
            bRet = false;
        } else {
            Toast.makeText(this, "GPS已開啟", Toast.LENGTH_SHORT).show();
        }
        return bRet;
    }


    private Location getLocation() {
        //獲取位置管理服務
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //查詢服務資訊
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); //定位精度: 最高
        criteria.setAltitudeRequired(false); //海拔資訊：不需要
        criteria.setBearingRequired(false); //方位資訊: 不需要
        criteria.setCostAllowed(false);  //是否允許付費
        criteria.setPowerRequirement(Criteria.POWER_HIGH); //耗電量: 低功耗
        String provider = mLocationManager.getBestProvider(criteria, true); //獲取GPS資訊
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        Location location = mLocationManager.getLastKnownLocation(provider);
        mLocationManager.requestLocationUpdates(provider, 5000, 3, locationListener);
        return location;
    }

    private final LocationListener locationListener = new LocationListener()
    {
        public void onLocationChanged(Location location)
        {
// TODO Auto-generated method stub
            if(location != null){
                latlngTextView.setText("維度:" + location.getLatitude() +" 經度:" +location.getLongitude());
                //mMap.clear();
                //要在此加入變更位置時畫入新的點

                if(IsRecording==true){
                    GPSline.add(new LatLng(location.getLatitude(),location.getLongitude()));
                    changeMapLine(GPSline);
                }else{
                    mMap.clear();
                }

                originMarkers.add(mMap.addMarker(new MarkerOptions().title("你在這裡").position(new LatLng(location.getLatitude(),location.getLongitude()))));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude())));
            }else{
                latlngTextView.setText("獲取不到資料");
            }
        }
        public void onProviderDisabled(String provider)
        {
// TODO Auto-generated method stub
        }
        public void onProviderEnabled(String provider)
        {
// TODO Auto-generated method stub
        }
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
// TODO Auto-generated method stub
        }
    };





    //取得API資料與解析
    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.", "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Direction.Leg> routes) {

        for (Direction.Leg route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.start_location, 8));

            originMarkers.add(mMap.addMarker(new MarkerOptions().title(route.start_address).position(route.start_location)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions().title(route.end_address).position(route.end_location)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(20);

            Log.i("route size check", "you get the = =" + route.steps.size());
            for (int i = 0; i < route.steps.size(); i++) {
                try {
                    polylineOptions.add(route.steps.get(i));
                } catch (Exception e) {
                    Log.i("fault", String.valueOf(i));
                }

            }
            polylineOptions.add(route.end_location);
            polylinePaths.add(mMap.addPolyline(polylineOptions));
            mMap.addPolyline(polylineOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(route.end_location));
            Nowline=polylineOptions.getPoints();
        }
        progressDialog.dismiss();
    }




    //GPS按鈕事件
    public void test() {
        mMap.clear();
        String startPoint;
        String DesPoint;
        startPoint = editText.getText().toString();
        DesPoint = deseditText.getText().toString();
        AsyncGetMapData task = new AsyncGetMapData(this, getApplicationContext(), progressDialog, startPoint, DesPoint);
        String result = null;
        try {
            result = task.execute("").get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(25.052688333, 121.5203866666);
        mMap.addMarker(new MarkerOptions().position(sydney).title("中山站"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,27));
    }

    void initPolylineTestObj(){
        List<LatLng> a = new ArrayList<>();
        List<LatLng> b = new ArrayList<>();

        a.add(new LatLng(25.0375226708,121.561982632));
        a.add(new LatLng(25.0374424754,121.561961174));
        a.add(new LatLng(25.0375080899,121.5620175));
        a.add(new LatLng(25.0375141653,121.561941057));
        a.add(new LatLng(25.0374667771,121.562022864));
        a.add(new LatLng(25.0375226708,121.561982632));

        b.add(new LatLng(25.0376162322,121.561515667));
        b.add(new LatLng(25.0391375043,121.561537125));
        b.add(new LatLng(25.0391861069,121.563610652));
        b.add(new LatLng(25.0391511022,121.564344492));
        b.add(new LatLng(25.039119825,121.565516964));
        b.add(new LatLng(25.0378925451,121.565538422));




        for(int i=0;i<2;i++){
            List<LatLng> tmp=null;
            String Fn="obj";
            if(i==0){Fn="obj1";tmp=a;}
            if(i==1){Fn="obj2";tmp=b;}

            FileOutputStream fp = null;
            try {

                fp = new FileOutputStream(getApplicationContext().getFilesDir().getPath().toString()+"/"+Fn+".PolyLinedat");
                ObjectOutputStream fpObj =  new ObjectOutputStream(fp);
                for(LatLng pline:tmp ){
                    fpObj.writeDouble(pline.latitude);
                    fpObj.writeDouble(pline.longitude);
                    Log.i("check :",pline.latitude+","+pline.longitude);
                }
                fpObj.flush();
                fpObj.close();
                //Toast.makeText(getApplicationContext(),"Data has saved " +getFilesDir().getPath().toString()+"/record/"+Fn+".dat",Toast.LENGTH_SHORT).show();
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if(fp!=null){
                    try {
                        fp.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    //顯示的所有物件
    void TestingLoadingWidget(){
        testingUnit = (ConstraintLayout) findViewById(R.id.forTestingWidgets);
        testingUnit.setVisibility(View.GONE);
        deseditText = (EditText) findViewById(R.id.editText2);
        editText = (EditText) findViewById(R.id.editText);
        testBtn = (Button) findViewById(R.id.button5);
        getXYBtn = (Button) findViewById(R.id.getXY);

        latlngTextView = (TextView) findViewById(R.id.textView2);

        getXYBtn.setOnClickListener(btnClickListener);
        editText.getBackground().setAlpha(80);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
            }
        });

        saveBtn = (Button) findViewById(R.id.saveBtn);
        loadBtn = (Button) findViewById(R.id.loadBtn);

        saveBtn.setOnClickListener(SaveClickListener);
        loadBtn.setOnClickListener(LoadClickListener);

        intenBtn = (Button) findViewById(R.id.newact);
        intenBtn.setOnClickListener(intentbtn);


        h_startBtn = (Button) findViewById(R.id.StartBtn);
        h_startBtn.setOnClickListener(StartRecord);
        h_stopBtn = (Button) findViewById(R.id.StopBtn);
        h_stopBtn.setOnClickListener(StopRecording);
        h_saveBtn = (Button) findViewById(R.id.SaveBtn);
        h_saveBtn.setOnClickListener(SaveRecord);
        h_Status = (TextView) findViewById(R.id.Status);
    }

    void changeMapLine(List<LatLng> line){
        mMap.clear();
        PolylineOptions polylineOptions = new PolylineOptions().
                geodesic(true).
                color(Color.BLUE).
                width(20);
        polylineOptions.addAll(line);
        polylinePaths.add(mMap.addPolyline(polylineOptions));
        mMap.addPolyline(polylineOptions);
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(line.get(line.size()-1)));
    }

}
