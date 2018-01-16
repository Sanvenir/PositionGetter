package com.sanvenir.positiongetter;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListPopupWindow;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = MapsActivity.class.getSimpleName();

    LocationService mService;
    boolean mBound = false;

    private GoogleMap mMap;
    private static final int REQ_ACCESS_FINE_LOCATION = 101;

    SearchView searchView;
    ListPopupWindow listPopupWindow;
    Toolbar toolbar;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        SharedPreferences sp = getSharedPreferences("SI", MODE_PRIVATE);
        HttpMethod.getPos = sp.getBoolean("GetPos", false);

        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int menuItemId = menuItem.getItemId();
                if(menuItemId == R.id.action_login) {
                    Intent intent = new Intent();
                    intent.setClass(MapsActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else if(menuItemId == R.id.action_settings) {

                }
                return true;
            }
        });

        Switch sw = findViewById(R.id.switchGetPos);
        sw.setChecked(HttpMethod.getPos);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SetGetPosAsyncTask task = new SetGetPosAsyncTask(b);
                task.execute();
            }
        });

        Button buttonSetGroup = findViewById(R.id.buttonSetGroup);
        final EditText textSetGroup = findViewById(R.id.textSetGroup);
        buttonSetGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetGroupAsyncTask task = new SetGroupAsyncTask(textSetGroup.getText().toString());
                task.execute();
            }
        });

        while(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_ACCESS_FINE_LOCATION);
        }
        Intent intent = new Intent();
        intent.setClass(MapsActivity.this, LocationService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        SharedPreferences sp = getSharedPreferences("SI", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("GetPos", HttpMethod.getPos);
        editor.apply();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) iBinder;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    class SetGroupAsyncTask extends AsyncTask<Void, Void, Boolean>{
        String group = null;
        public SetGroupAsyncTask(String g) {
            group = g;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                UserData.setTrackingUser(null);
                String response = HttpMethod.setGroup(group);
                if(response.equals("success")) {
                    String[] userInfo = HttpMethod.getPos();
                    UserData.setAllUserData(userInfo);
                    return true;
                }
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean) {
                Toast.makeText(getApplicationContext(), "Setting success", Toast.LENGTH_SHORT).show();
                listPopupWindow = new ListPopupWindow(MapsActivity.this);
                listPopupWindow.setAnchorView(toolbar);

                listPopupWindow.setAdapter(new ArrayAdapter<>(
                        MapsActivity.this,
                        android.R.layout.simple_list_item_1,
                        new ArrayList<>(UserData.allUserData.keySet())
                ));

                listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        UserData.setTrackingUser(adapterView.getItemAtPosition(i).toString());
                        Log.d("Tracking user:========>", UserData.getTrackingUser());
                        listPopupWindow.dismiss();
                        UserData trackUser = UserData.allUserData.get(UserData.getTrackingUser());
                        LatLng trackingUser = new LatLng(trackUser.getLatitude(), trackUser.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(trackingUser));
                    }
                });
                listPopupWindow.show();
            } else {
                Toast.makeText(getApplicationContext(), "Setting fail", Toast.LENGTH_SHORT).show();
            }
        }
    }
    class SetGetPosAsyncTask extends AsyncTask<Void, Void, Boolean> {
        boolean isGetPos = false;
        public SetGetPosAsyncTask(boolean b) {
            isGetPos = b;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String response = HttpMethod.setGetPos(isGetPos);
                return response.equals("success");
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean) {
                Toast.makeText(getApplicationContext(), "Setting success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Setting fail", Toast.LENGTH_SHORT).show();
            }
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_ACCESS_FINE_LOCATION);
        }
    }
}
