package com.archiedavid.www.booklisting;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private BookAdapter mBookAdapter;

    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static String searchStr = "Gladwell";
    private static final String BOOK_REQUEST_URL =
            ("https://www.googleapis.com/books/v1/volumes?q=" + searchStr);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView bookListView = (ListView) findViewById(R.id.list);
        mBookAdapter = new BookAdapter(this, new ArrayList<Book>());

        bookListView.setAdapter(mBookAdapter);

        BookAsyncTask task = new BookAsyncTask();
        task.execute();
    }

    private class BookAsyncTask extends AsyncTask<URL, Void, List<Book>>{

        @Override
        protected List<Book> doInBackground(URL... urls) {

            //example url = https://www.googleapis.com/books/v1/volumes?q=Gladwell
            URL url = createUrl(BOOK_REQUEST_URL);

            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem making the HTTP request.", e);
            }
            List<Book> books = extractFeatureFromJson(jsonResponse);
            return books;
        }

        @Override
        protected void onPostExecute(List<Book> books) {
            mBookAdapter.clear();

            if (books != null) {
                mBookAdapter.addAll(books);
            }
        }

        private URL createUrl(String stringUrl) {
            URL url;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }

        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";

            if (url == null) {
                return jsonResponse;
            }

            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();

                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem retrieving the book JSON results.", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        private List<Book> extractFeatureFromJson(String bookJSON) {

            if (TextUtils.isEmpty(bookJSON)) {
                return null;
            }

            List<Book> books = new ArrayList<>();

            try {
                JSONObject baseJsonResponse = new JSONObject(bookJSON);
                JSONArray itemsArray = baseJsonResponse.getJSONArray("items");
                if (itemsArray.length() > 0) {
                    for (int i = 0; i < itemsArray.length(); i++) {
                        JSONObject itemsArrayJSONObject = itemsArray.getJSONObject(i);
                        JSONObject volumeInfo = itemsArrayJSONObject.getJSONObject("volumeInfo");
                        String title = volumeInfo.getString("title");

                        StringBuilder authors = new StringBuilder();
                        if (volumeInfo.has("authors")) {
                            JSONArray authorsArray = volumeInfo.getJSONArray("authors");

                            for (int j = 0; j < authorsArray.length(); j++) {
                                if(j >0){
                                    authors.append(", ");
                                }
                                authors.append(authorsArray.getString(j));
                            }
                            Book book = new Book(title, authors.toString());
                            books.add(book);
                        }
                    }
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the book JSON results", e);
            }
            return books;
        }
    }
}
