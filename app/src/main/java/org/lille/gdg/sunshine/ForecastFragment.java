package org.lille.gdg.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> forecatAdapter;

    public ForecastFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.action_refresh == item.getItemId()){
            new FetchWeatherTask().execute("Lille, fr");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        forecatAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecat, R.id.list_item_forecast_textview);
        ((ListView) rootView.findViewById(R.id.listview_forecast)).setAdapter(forecatAdapter);

        return rootView;
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String[]>{

        private String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected void onPostExecute(String[] strings) {
            if (strings != null) {
                forecatAdapter.clear();
                forecatAdapter.addAll(new ArrayList<String>(Arrays.asList(strings)));
            }
        }

        @Override
        protected String[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            String[] result = null;
            final Integer numDays = 7;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY = "q";
                final String FORMAT = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS = "cnt";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY, params[0])
                        .appendQueryParameter(FORMAT, "json")
                        .appendQueryParameter(UNITS_PARAM, "metric")
                        .appendQueryParameter(DAYS, numDays.toString())
                        .build();
                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.v(LOG_TAG, forecastJsonStr);

                result = new WeatherDataParser().getWeatherDataFromJson(forecastJsonStr, numDays);
                Log.v(LOG_TAG, result[0]);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                result = null;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error ", e);
                result = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return result;
        }
    }
}