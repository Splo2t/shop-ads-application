package com.example.user.pyenhalean.activity;


import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.design.widget.BottomNavigationView;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.example.user.pyenhalean.GetHTMLTask;
import com.example.user.pyenhalean.R;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Align;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.util.FusedLocationSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MapActivity extends BaseActivity implements OnMapReadyCallback {



    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);
        addToolbar();

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        configBottomNavigation(MapActivity.this, navigation);

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // Map객체 생가면 onMapReady 호출 -> callBack
    @UiThread
    @Override
    public void onMapReady(@NonNull final NaverMap naverMap) {
        naverMap.setLocationSource(locationSource);

        String[] response = null;

        //variable for get user location and store location
        double user_latitude;
        double user_longitude;
        double latitude;
        double longitude;
        String storeName;
        final Geocoder geocoder = new Geocoder(this);
        String locationName = null;
        List<Address> list = null;
        Location location;
        LocationManager manager;

        //variable for map overlay
        LocationOverlay locationOverlay;
        CameraUpdate cameraUpdate;

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if((location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) == null
                && (location = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) == null) {

            Toast.makeText(MapActivity.this,
                    "위치정보가 잡히지 않습니다.\n 잠시후 다시 시도해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        locationOverlay = naverMap.getLocationOverlay();
        locationOverlay.setVisible(true);

        latitude = user_latitude = location.getLatitude();
        longitude = user_longitude = location.getLongitude();

        locationName = setMarker(user_latitude,user_longitude,naverMap);

        // set map camera location
        // change latitude to user_latitude longitude to user_longitude after test and change 117 line
        cameraUpdate = CameraUpdate.scrollTo(new LatLng(user_latitude, user_longitude));
        locationOverlay.setPosition(new LatLng(user_latitude, user_longitude));
        naverMap.setLocationTrackingMode(LocationTrackingMode.NoFollow);
        naverMap.moveCamera(cameraUpdate);
        final String finalLocationName = locationName;

        NaverMap.OnCameraChangeListener listener = new NaverMap.OnCameraChangeListener() {
            String locationName = finalLocationName;
            double latitude;
            double longitude;

            @Override
            public void onCameraChange(final int reason, final boolean animated) {
                long now = System.currentTimeMillis();
                Thread thread = new Thread(){
                    List<Address> list = null;
                    String nLocationName = null;
                    String[] tempString;
                  @Override
                    public void run(){
                      latitude = naverMap.getCameraPosition().target.latitude;
                      longitude = naverMap.getCameraPosition().target.longitude;
                      try {
                          list = geocoder.getFromLocation(latitude, longitude, 10);
                          tempString= list.get(0).getAddressLine(0).split(" ");
                          nLocationName = tempString[1] + "_" + tempString[2] + "_" + tempString[3] + "_";
                          if(!locationName.equals(nLocationName)){
                              locationName = nLocationName;
                              setMarker(latitude,longitude,naverMap);
                              Log.d("naver","setMarker");
                          }
                      } catch (IOException e) {
                          e.printStackTrace();
                      }

                      Log.d("NaverMap", "카메라 변경 - reson: " + reason + ", animated: " + animated);
//                      Log.d("NaverMap", nLocationName);
                  }
                };
                if(now % 35 == 1){
                    thread.start();
                }

            }
        };
        naverMap.addOnCameraChangeListener(listener);

    }

    private String setMarker(double user_latitude, double user_longitude, NaverMap naverMap) {
        String[] response = null;

        //variable for get user location and store location
        double latitude;
        double longitude;
        String storeName;
        final Geocoder geocoder = new Geocoder(this);
        String locationName = null;
        List<Address> list = null;
        Location location;
        LocationManager manager;

        //variable for map overlay
        LocationOverlay locationOverlay;
        CameraUpdate cameraUpdate;
        try {
            //get current location
            list = geocoder.getFromLocation(user_latitude, user_longitude, 10);

            if(list.get(0).getAddressLine(0) == null){
                GetHTMLTask tesk = new GetHTMLTask();
                Log.d("address",list.get(0).toString());

                locationName = "서울특별시_강남구_대치동_"; //set string to test response

                response = tesk.execute("loadStore", locationName).get().split("#");
                Log.d("위치", locationName);
            }
            else {
                GetHTMLTask tesk = new GetHTMLTask();
                String[] tempString = list.get(0).getAddressLine(0).split(" ");
                locationName = tempString[1] + "_" + tempString[2] + "_" + tempString[3] + "_";
                response = tesk.execute("loadStore", locationName).get().split("#");
                Log.d("위치", locationName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // set marker
        if (response == null){
            return "null";
        }
        for(int i = 0; i < response.length; i++){
            Marker marker = new Marker();
            if(response[i].split("!").length >= 3){
                storeName = response[i].split("!")[0];
                latitude = Double.parseDouble(response[i].split("!")[1]);
                longitude = Double.parseDouble(response[i].split("!")[2]);

                marker.setPosition(new LatLng(latitude, longitude));
                marker.setCaptionText(storeName);
                marker.setCaptionAlign(Align.Top);
                marker.setTag(storeName);

                //주의!!! 마커가 많을 경우 Out Of Memory 오류
                // Dialog test 후 수정
                marker.setOnClickListener(new Overlay.OnClickListener(){
                    @Override
                    public boolean onClick(@NonNull Overlay overlay) {
                        showItem((String)overlay.getTag());
                        return true;
                    }
                });

                marker.setMap(naverMap);
            }

        }
        return locationName;
    }




    public void showItem(String storename)
    {
        final List<String> ListItems = new ArrayList<>();
        GetHTMLTask task = new GetHTMLTask();
        try {
            String[] result = task.execute("ownerItemDownload", storename).get().split("#");
            for(int i = 0; i < result.length; i++){
                result[i] = result[i].replace('!','\n');
                result[i]+='\n';
                ListItems.add(result[i]);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        final CharSequence[] items =  ListItems.toArray(new String[ ListItems.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(storename + " 할인품목");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int pos) {
                String selectedText = items[pos].toString();
                // 후에 이벤트 항목 추가
                //Toast.makeText(MapActivity.this, selectedText, Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

}

