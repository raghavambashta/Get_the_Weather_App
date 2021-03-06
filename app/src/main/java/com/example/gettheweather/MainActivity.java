package com.example.gettheweather;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    EditText cityEditText;
    TextView weatherTextView;
    TextView mainTextView;
    ImageView bgImageView;
    ImageView loadImageView;
    String showInfo = "";
    String message = "";
    String copyMainInfo = "";

    public void resetThings(View view)
    {
        cityEditText.setText("");
        weatherTextView.setText("");
        mainTextView.setText("");
        bgImageView.setImageResource(R.drawable.bg1);
        copyMainInfo = "";
    }

    public void getWeather(View view)
    {
        copyMainInfo = "";
        message = "";
        try {
            DownloadTask task = new DownloadTask();
            String encodedCityName = URLEncoder.encode(cityEditText.getText().toString(), "UTF-8");

            task.execute("https://openweathermap.org/data/2.5/weather?q=" + encodedCityName + "&appid=439d4b804bc8187953eb36d2a8c26a02");

            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(cityEditText.getWindowToken(), 0);

        }catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "Could not find weather 🙁", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String > {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result = result + current;
                    data = reader.read();
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);

                    String weatherInfo = jsonObject.getString("weather");

                    JSONArray arr = new JSONArray(weatherInfo);

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject jsonPart = arr.getJSONObject(i);

                        String mainInfo = jsonPart.getString("main");
                        String descInfo = jsonPart.getString("description");

                        copyMainInfo = mainInfo;

                        if (!mainInfo.equals("") && !descInfo.equals("")) {
                            message += mainInfo + ": " + descInfo;

                            new CountDownTimer(1000,1000 ){
                                @Override
                                public void onTick(long l) {

                                }

                                @Override
                                public void onFinish() {
                                    if(copyMainInfo.equals("Haze")){
                                        bgImageView.setImageResource(R.drawable.haze);
                                    }
                                    else if(copyMainInfo.equals("Clouds")){
                                        bgImageView.setImageResource(R.drawable.cloudy);
                                    }
                                    else if(copyMainInfo.equals("Clear")){
                                        bgImageView.setImageResource(R.drawable.clear);
                                    }
                                    else if(copyMainInfo.equals("Rain")){
                                        bgImageView.setImageResource(R.drawable.rainy);
                                    }
                                    else if(copyMainInfo.equals("Mist")){
                                        bgImageView.setImageResource(R.drawable.mist);
                                    }
                                    else if(copyMainInfo.equals("Drizzle")){
                                        bgImageView.setImageResource(R.drawable.drizzle);
                                    }
                                    if (!message.equals("")) {
                                        weatherTextView.setText(message);
                                        copyMainInfo = "";
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Could not find weather 🙁", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }.start();

                        }
                    }

                    String message1 = "";
                    String mainPartInfo = jsonObject.getString("main");
                    message1 = mainPartInfo.toString();
                    ArrayList<String> info = new ArrayList<String>();

                    Pattern p = Pattern.compile(":(.*?),");
                    Matcher m = p.matcher(message1);
                    while (m.find()) {
                        info.add(m.group(1).toString());
                    }
                    showInfo = "Temperature : " + info.get(0) + "°C \r\n" + "Feels Like : " + info.get(1) + "°C \r\n" + "Temp(Min) : " + info.get(2) + "°C \r\n" + "Temp(Max) : " + info.get(3) + "°C \r\n" + "Pressure : " + info.get(4) + " hpa \r\n";

                    String windInfo = jsonObject.getString("wind");
                    String message2 = windInfo.toString();
                    p = Pattern.compile(":(.*?),");
                    m = p.matcher(message1);
                    if (m.find()) {
                        info.add(m.group(1).toString());
                        showInfo = showInfo + "Wind : " + info.get(5) + "m/s";
                    }

                    new CountDownTimer(1000,1000) {
                        @Override
                        public void onTick(long l) {
                            loadImageView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onFinish() {
                            loadImageView.setVisibility(View.INVISIBLE);
                            mainTextView.setText(showInfo);
                        }
                    }.start();


                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Could not find weather 🙁", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Could not find weather 🙁", Toast.LENGTH_LONG).show();
                resetThings(cityEditText);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityEditText = (EditText)findViewById(R.id.cityEditText);
        weatherTextView = (TextView)findViewById(R.id.weatherTextView);
        mainTextView = (TextView)findViewById(R.id.mainTextView);
        bgImageView = (ImageView) findViewById(R.id.bgImageView);
        loadImageView = (ImageView) findViewById(R.id.loadImageView);

    }
}