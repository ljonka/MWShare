package de.rosapavian.mwshare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mediawiki.api.ApiResult;
import org.mediawiki.api.MWApi;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EmptyStackException;

/**
 * Save Settings in key value db
 *
 */
public class MainActivity extends AppCompatActivity {

    public static final String SETTINGS     = "mwsettings";     //: uri, use https only, while mobile internet is unsecure
    public static final String MWPAGE     = "mwpage";     //: uri, use https only, while mobile internet is unsecure
    public static final String MWUSER     = "mwuser";     //: string
    public static final String MWPASS     = "mwpass";     //: password
    public static final String MWCHECKED  = "mwchecked";  //: boolean //did the connection worked already?

    PagesDatabase mDB;
    ListView mListView;

    Intent intent = null;
    String action;
    String type;
    String titlePage;
    String shareContent = "";

    public String sAllPagesURL = "api.php?action=query&list=allpages";

    CharSequence text = "";
    Context context;
    int duration = Toast.LENGTH_LONG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = getApplicationContext();
        mDB = new PagesDatabase(context);

        // Get intent, action and MIME type
        intent = getIntent();
        action = intent.getAction();
        type = intent.getType();


        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent); // Handle multiple images being sent
            }
        } else {
            // Handle other intents, such as being started from the home screen
        }

    }

    protected void onResume(){
        super.onResume();
        //Generate ListView from SQLite Database
        displayListView();
        new DownloadFilesTask().execute();
    }

    private void displayListView() {

        Cursor cursor = mDB.getTitleList();

        // The desired columns to be bound
        String[] columns = new String[] {
                PagesDatabase.FIELD_TITLE
        };

        // the XML defined views which the data will be bound to
        int[] to = new int[] {
                android.R.id.text1,
        };

        // create the adapter using the cursor pointing to the desired data
        //as well as the layout information
        SimpleCursorAdapter dataAdapter = new SimpleCursorAdapter(
                this, android.R.layout.simple_list_item_1,
                cursor,
                columns,
                to,
                0);

        ListView listView = (ListView) findViewById(R.id.listView);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view,
                                    int position, long id) {
                // Get the cursor, positioned to the corresponding row in the result set
                Cursor cursor = (Cursor) listView.getItemAtPosition(position);

                // Get the state's capital from this row in the database.
                titlePage = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                shareContent = intent.getStringExtra(Intent.EXTRA_TEXT);
                Log.e("content", "url: " + shareContent);
                //InsertString
                if(!shareContent.isEmpty()) {
                    new InsertString().execute();
                }
            }
        });

    }


    private class DownloadFilesTask extends AsyncTask<URL, Integer, Long>
    {
        protected Long doInBackground(URL... urls){
            SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SETTINGS, MODE_PRIVATE);
            try {
                //String apiUrl = "https://help.bluespice.com/api.php";
                //break on empty hostname
                String hostname = sharedPreferences.getString(MainActivity.MWPAGE, "");
                if(hostname.isEmpty()){
                    throw new EmptyStackException();
                }
                String apiUrl = sharedPreferences.getString(MainActivity.MWPAGE, "") + "/api.php";
                AbstractHttpClient httpClient = new DefaultHttpClient();

                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    // fetch data
                    MWApi api = new MWApi(apiUrl, httpClient) ;
                    MWApi.RequestBuilder rb = api.action("query");
                    rb.param("generator", "allpages");
                    ApiResult ar = rb.get();
                    Log.e("MWApi", "Passed");
                    ArrayList<ApiResult> iterator = ar.getNodes("/api/query/pages/page");

                    mDB.removeAll();

                    for (ApiResult title : iterator) {
                        //Log.e("Title", title.getString("@title"));
                        mDB.addRecord(title.getString("@title"));
                    }

                    //https://help.bluespice.com/api.php?action=query&list=allcategories&acprop=size

                    MWApi.RequestBuilder rb2 = api.action("query");
                    rb2.param("list", "allcategories");
                    ApiResult ar2 = rb2.get();
                    Log.e("MWApi", "Passed");
                    ArrayList<ApiResult> iterator2 = ar2.getNodes("/api/query/allcategories/c");
                    for (ApiResult title : iterator2) {
                        //Log.e("Category", title.getString("text()"));
                    }

                    //write page example
                    MWApi.RequestBuilder rb3 = api.action("edit");
                    rb3.param("title", "Diskussion:Hauptseite");
                    rb3.param("section", "new");
                    rb3.param("appendtext", "Blablub");
                    rb3.param("token", api.getEditToken());//use csrftoken
                    ApiResult ar3 = rb3.post();
                    Log.e("MWApi", ar3.getNode("/api/edit").toString());
                    Log.e("MWApi", "write Passed");

                    text = "Connected to wiki";
                } else {
                    // display error
                    Log.e("MWApi", "error with connection");
                    text = "error with Internet connection";
                }

            }catch(IOException ex){
                Log.e("MWException", "Error while connecting to mw");
                Log.e("MWException", ex.getMessage());

                text = ex.getMessage();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress)
        {

        }

        @Override
        protected void onPostExecute(Long result) {
            if(text.length() > 0) {
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
            //Generate ListView from SQLite Database
            displayListView();
        }
    }

    private class InsertString extends AsyncTask<URL, Integer, Long>
    {
        protected Long doInBackground(URL... urls){
            SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SETTINGS, MODE_PRIVATE);
            try {
                //break on empty hostname
                String hostname = sharedPreferences.getString(MainActivity.MWPAGE, "");
                if(hostname.isEmpty()){
                    throw new EmptyStackException();
                }
                String apiUrl = sharedPreferences.getString(MainActivity.MWPAGE, "") + "/api.php";
                AbstractHttpClient httpClient = new DefaultHttpClient();

                ConnectivityManager connMgr = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    MWApi api = new MWApi(apiUrl, httpClient) ;
                    //write page example
                    MWApi.RequestBuilder rb3 = api.action("edit");
                    rb3.param("title", titlePage);
                    rb3.param("section", "new");
                    rb3.param("appendtext", shareContent);
                    rb3.param("token", api.getEditToken());//use csrftoken
                    ApiResult ar3 = rb3.post();
                    Log.e("MWApi", ar3.getNode("/api/edit").toString());
                    Log.e("MWApi", "write Passed");

                    text = "Data shared";
                } else {
                    // display error
                    Log.e("MWApi", "error with connection");
                    text = "error with Internet connection";
                }

            }catch(IOException ex){
                Log.e("MWException", "Error while connecting to mw");
                Log.e("MWException", ex.getMessage());

                text = ex.getMessage();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress)
        {

        }

        @Override
        protected void onPostExecute(Long result) {
            if(text.length() > 0) {
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }else{
                Toast.makeText(getApplicationContext(),
                        titlePage + " updated", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Update UI to reflect text being shared
        }
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            // Update UI to reflect image being shared
        }
    }

    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            // Update UI to reflect multiple images being shared
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, MWSettings.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
