// Author: James Williams
// File: MainActivity.java
// Date: 12/30/16
//
// Description: A simple weather app that takes the user's network and GPS coordinates and
// submits them to a weathermap API which returns information about the weather in JSON format.
// The information includes the name of the city, the temperature and the description of the weather

package jameswilliams.weather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.util.Log;
import android.view.View;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import org.json.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    final static int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;
    final static String API_URL = "http://api.openweathermap.org/data/2.5/weather";
    final static String KEY = "ommitted";

    int latitude = -1;
    int longitude = -1;

    JSONObject jsonObject;

    ProgressBar progressBar;
    TextView loadingText;
    Button reload;


    TextView city;
    TextView temp;
    TextView weather;
    TextView lonText;
    TextView latText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingText = (TextView) findViewById(R.id.loadingText);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        reload = (Button) findViewById(R.id.reload);
        reload.setVisibility(View.VISIBLE);

        city = (TextView) findViewById(R.id.cityText);
        temp = (TextView) findViewById(R.id.tempText);
        weather = (TextView) findViewById(R.id.weatherText);
        lonText = (TextView) findViewById(R.id.longitude);
        latText = (TextView) findViewById(R.id.latitude);

        // "this" is MainActivity
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_ACCESS_FINE_LOCATION);

        }

        getGPS();

        // initial weather call
        new fetchWeatherTask().execute();

        // refresh new weather when pressed
        reload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                new fetchWeatherTask().execute();
            }
        });

    }

    public void getGPS() {
        // "this" is MainActivity
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            List<String> providers = lm.getProviders(true);

            Location l = null;

            for (int i = providers.size() - 1; i >= 0; i--) {
                l = lm.getLastKnownLocation(providers.get(i));
                if (l != null) break;
            }

            if (l != null) {
                latitude = (int) (l.getLatitude());
                longitude = (int) (l.getLongitude());
            }
        }

        // Keep pestering the user for access to location
        else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            // Case 1: Access to user's fine location
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getGPS();

                }

                // Keep pestering the user for access to fine location
                else {

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_ACCESS_FINE_LOCATION);
                }

                return;
            }
        }
    }

    class fetchWeatherTask extends AsyncTask<Void, Void, String> {

        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            loadingText.setVisibility(View.VISIBLE);
        }

        protected String doInBackground(Void... urls) {

            try {

                URL url = new URL(API_URL + "?lat=" + Integer.toString(latitude) + "&lon=" + Integer.toString(longitude) + "&appid=" + KEY);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {

                    BufferedReader bufferedReader =
                            new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    String line;
                    StringBuffer buffer = new StringBuffer();

                    while ((line = bufferedReader.readLine()) != null)
                        buffer.append(line + "\n");

                    bufferedReader.close();

                    return buffer.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {

                Log.e("ERROR", e.getMessage(), e);

                return null;
            }
        }

        protected void onPostExecute(String response) {

            lonText.setText(Integer.toString(longitude));
            latText.setText(Integer.toString(latitude));

            if (response == null) {

                loadingText.setText("Error getting data");
                progressBar.setVisibility(View.GONE);

                return;
            }

            try {
                jsonObject = new JSONObject(response);
            } catch (JSONException e) {

                Log.e("log_tag", "Error parsing data " + e.toString());

                loadingText.setText(e.getMessage());
            }

            try {
                city.setText(jsonObject.getString("name"));
                temp.setText(((Integer.toString((int) (Double.parseDouble(jsonObject.getJSONObject("main").getString("temp")) * 1.8) - 459)) + "°F"));
                weather.setText(jsonObject.getJSONArray("weather").getJSONObject(0).getString("description"));
            } catch (JSONException e) {

                Log.e("ERROR", e.getMessage());

                e.printStackTrace();
            }

            progressBar.setVisibility(View.GONE);
            loadingText.setVisibility(View.GONE);

        }
    }
}