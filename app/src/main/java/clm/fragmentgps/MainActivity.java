package clm.fragmentgps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    TextView weatherTV;
    double alt;
    double lng;
    Location lastKnownLocation;
    String provider;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weatherTV= (TextView) findViewById(R.id.weatherTV) ;
        //provider = "gps";
        // String provider = "network";
        Criteria criteria = new Criteria();

        criteria.setAccuracy(Criteria.ACCURACY_LOW);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        // for GPS


//        criteria.setAccuracy(Criteria.ACCURACY_FINE);
//        criteria.setPowerRequirement(Criteria.POWER_HIGH);



        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        provider= locationManager.getBestProvider(criteria,true);

        refreshLocation();

        TextView locationTV = (TextView) findViewById(R.id.locationTV);
        locationTV.setText("Location: "+ alt +" , "+lng);

        Button openBTN = (Button) findViewById(R.id.openMapBTN);
        openBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshLocation();

            }
        });
    }


    public  class DownloadWebsite extends AsyncTask<String, Integer, String >
    {

        @Override
        protected String doInBackground(String... params) {

            //start download....
            int lineConut=0;

            BufferedReader input = null;
            HttpURLConnection connection = null;
            StringBuilder response = new StringBuilder();
            try {
                //create a url:
                URL url = new URL(params[0]);
                //create a connection and open it:
                connection = (HttpURLConnection) url.openConnection();

                //status check:
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                    //connection not good - return.
                }

                //get a buffer reader to read the data stream as characters(letters)
                //in a buffered way.
                input = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                //go over the input, line by line
                String line="";
                while ((line=input.readLine())!=null){
                    //append it to a StringBuilder to hold the
                    //resulting string
                    response.append(line+"\n");
                    lineConut++;

                    try {
                        //current thread - simulating long task
                        Thread.sleep(200);

                        publishProgress(lineConut);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally{
                if (input!=null){
                    try {
                        //must close the reader
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if(connection!=null){
                    //must disconnect the connection
                    connection.disconnect();
                }
            }




            return response.toString();
        }


        @Override
        protected void onPostExecute(String resutFromWebsite) {

            String currentWeather="current weather: ";

            try {

                //the main JSON object - initialize with string
                JSONObject mainObject= new JSONObject(resutFromWebsite);

                //extract data with getString, getInt getJsonObject - for inner objects or JSONArray- for inner arrays

                JSONArray myArray= mainObject.getJSONArray("weather");


                for(int i=0; i<myArray.length(); i++)
                {
                    //inner objects inside the array
                    JSONObject innerObj= myArray.getJSONObject(i);
                    String description= innerObj.getString("description");
                    Log.d("json", description);
                    currentWeather=currentWeather+ description;
                }

                JSONObject tempObject=   mainObject.getJSONObject("main");
                double  tmeper=   tempObject.getDouble("temp");

                currentWeather=currentWeather+ " Temp: "+tmeper;
                Log.d("json", ""+tmeper);

            } catch (JSONException e) {
                e.printStackTrace();

            }

            weatherTV.setText(currentWeather);
        }
    }

    public void refreshLocation ()
    {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location lastKnownLocation=locationManager.getLastKnownLocation(provider);

        //ALWAYS CHECK IF NOT NULL- LAST KNOWN LOCATION CAN BE NULL

        if (lastKnownLocation!= null) {
            String urlTodownload = "http://api.openweathermap.org/data/2.5/weather?alt=" + lastKnownLocation.getLatitude() + "&lon=" + lastKnownLocation.getLongitude() + "&appid=a69961c7031783d91d299c95e15920da";
            Log.d("URL", urlTodownload);
            DownloadWebsite downloadWebsite = new DownloadWebsite();
            downloadWebsite.execute(urlTodownload);
            alt=lastKnownLocation.getAltitude();
            lng=lastKnownLocation.getLatitude();
        } else
        {
            alt=5;
            lng=3;
        }
    }

    public void getHandler()
    {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                Toast.makeText(MainActivity.this, "check", Toast.LENGTH_SHORT).show();
//         call again for interval handler
//        handler.postDelayed(this, 2000);
            }
        }, 20000);




    }
}




