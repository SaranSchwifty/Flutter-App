package com.example.jaisankar_application;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements Response.Listener<Bitmap> {
    private ImageView viewer;
    private TextView city;
    private TextView temp;
    private TextView temp_max;
    private TextView temp_min;
    private TextView speed;
    private TextView pression_atm;
    private TextView humidite;
    private ProgressDialog progress;
    private WeatherMODEL weather;
    private static String BASE_URL = "https://api.openweathermap.org/data/2.5/weather?q=FRANCE,fr&APPID=652aea456e53fb3cc5a3c9e56f265b81";
    private static String IMG_URL = "https://openweathermap.org/img/w/";
    private Double latitude;
    private Double longitude;
    private String cityName;
    RequestQueue queue;
    //GPS
    private LocationManager locationMgr = null;
    private LocationListener onLocationChange = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onLocationChanged(Location location) {


            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Double altitude = location.getAltitude();
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            cityName = addresses.get(0).getAddressLine(0);

            MiseAJour();
//            Toast.makeText(
//                    getBaseContext(),
//                    "Coordinates: " + latitude
//                            + " " + longitude + " altitude=" + altitude, Toast.LENGTH_LONG).show();


        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);

        //GPS

        locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE}, 1);

        }
        //todo
        locationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, onLocationChange);


        this.viewer = (ImageView) findViewById(R.id.imageV);
        this.city = (TextView) findViewById(R.id.city);
        this.temp = (TextView) findViewById(R.id.temp);
        this.temp_max = (TextView) findViewById(R.id.temp_max);
        this.temp_min = (TextView) findViewById(R.id.temp_min);
        this.speed = (TextView) findViewById(R.id.speed);
        this.pression_atm = (TextView) findViewById(R.id.pressure);
        this.humidite = (TextView) findViewById(R.id.humidity);

        this.progress = new ProgressDialog(this);
        this.progress.setTitle("Please wait");
        this.progress.setMessage("Retrieving weather information ......");
        this.progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.progress.show();
    }



    public void MiseAJour(){


        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, BASE_URL +"&lat="+latitude.toString()+"&lon="+longitude.toString(), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        weather = new WeatherMODEL();
                        try {
                            if(latitude!=null && longitude!=null) {
                                weather = JSONWeatherParser.getWeather(response.toString());
                                Log.e("TEST",response.toString());
//                                Toast.makeText(
//                                        getBaseContext(),
//                                        "Response : "+response.toString(), Toast.LENGTH_LONG).show();
                                city.setText(cityName);
                                temp.setText("temperature: " + weather.temperature.getTemp() + "degrees Celsius");
                                temp_max.setText("temperature max: " + weather.temperature.getMaxTemp() + "degrees Celsius");
                                temp_min.setText("temperature min: " + weather.temperature.getMinTemp() + "degrees Celsius");
                                humidite.setText("Humidity:" + weather.currentCondition.getHumidity() + " %");
                                pression_atm.setText("Atmospheric pressure: " + weather.currentCondition.getPressure() + "HP");
                                speed.setText("Wind speed:" + weather.wind.getSpeed());

                                downloadImage(weather.currentCondition.getIcon(), queue);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.e("error Volley", error.toString());

                    }
                });
        queue.add(jsObjRequest);

    }

    @Override
    public void onResponse(Bitmap response) { //callback en cas de succès
        if (this.progress.isShowing()) this.progress.dismiss();
        Bitmap bm = Bitmap.createScaledBitmap(response, 400, 400, true);
        this.viewer.setImageBitmap(bm);


    }

    public void downloadImage(String pathImg, RequestQueue queue) {
        Log.i("Image down path:", pathImg);
        ImageRequest picRequest = new ImageRequest(IMG_URL + pathImg + ".png?APPID=652aea456e53fb3cc5a3c9e56f265b81", this, 0, 0, null, null);
        queue.add(picRequest);

    }

    @Override
    public void onDestroy() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return;
        }
        locationMgr.removeUpdates(onLocationChange);
        super.onDestroy();

    }
}

