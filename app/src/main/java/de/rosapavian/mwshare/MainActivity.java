package de.rosapavian.mwshare;

import android.content.Context;
import android.content.Intent;
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
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mediawiki.api.ApiResult;
import org.mediawiki.api.MWApi;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Save Settings in key value db
 *
 */
public class MainActivity extends AppCompatActivity {

            public static final String MWPAGE     = "mwpage";     //: uri, use https only, while mobile internet is unsecure
            public static final String MWUSER     = "mwuser";     //: string
            public static final String MWPASS     = "mwpass";     //: password
            public static final String MWCHECKED  = "mwchecked";  //: boolean //did the connection worked already?

    PagesDatabase mDB;
    ListView mListView;

    public String sAllPagesURL = "api.php?action=query&list=allpages";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        new DownloadFilesTask().execute();

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


    private class DownloadFilesTask extends AsyncTask<URL, Integer, Long>
    {
        protected Long doInBackground(URL... urls){
            try {
                String apiUrl = "https://help.bluespice.com/api.php";
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
                    for (ApiResult title : iterator) {
                        Log.e("Title", title.getString("@title"));
                    }

                    //https://help.bluespice.com/api.php?action=query&list=allcategories&acprop=size

                    MWApi.RequestBuilder rb2 = api.action("query");
                    rb2.param("list", "allcategories");
                    ApiResult ar2 = rb2.get();
                    Log.e("MWApi", "Passed");
                    ArrayList<ApiResult> iterator2 = ar2.getNodes("/api/query/allcategories/c");
                    for (ApiResult title : iterator2) {
                        Log.e("Category", title.getString("text()"));
                    }
                } else {
                    // display error
                    Log.e("MWApi", "error with connection");
                }

            }catch(IOException ex){
                Log.e("MWException", "Error while connecting to mw");
                Log.e("MWException", ex.getMessage());
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress)
        {

        }

        @Override
        protected void onPostExecute(Long result) {

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

    private void updateWordList() {
        SimpleCursorAdapter simpleCursorAdapter = new
                SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_1,
                mDB.getTitleList(),
                new String[]{"title"},
                new int[]{android.R.id.text1},
                0);
        mListView.setAdapter(simpleCursorAdapter);
    }
}
