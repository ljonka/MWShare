package de.rosapavian.mwshare;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

/**
 * Created by leonid on 25.11.16.
 */

public class MWSettings extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SharedPreferences sharedPreferences = getPreferences(
                MODE_PRIVATE);
        //restore data in view elements
        EditText pageUrl = (EditText)findViewById(R.id.editText3);
        EditText username = (EditText)findViewById(R.id.editText4);
        EditText password = (EditText)findViewById(R.id.editText5);
        pageUrl.setText(sharedPreferences.getString(MainActivity.MWPAGE, ""));
        username.setText(sharedPreferences.getString(MainActivity.MWUSER, ""));
        password.setText(sharedPreferences.getString(MainActivity.MWPASS, ""));
    }

    public void onClickClose(View view){
        SharedPreferences sharedPreferences = getPreferences(
                MODE_PRIVATE);
        EditText mPageURI, mUsername, mPassword;
        mPageURI = (EditText)findViewById(R.id.editText3);
        mUsername = (EditText)findViewById(R.id.editText4);
        mPassword = (EditText)findViewById(R.id.editText5);

        SharedPreferences.Editor editor =
                getPreferences(MODE_PRIVATE).edit();
        editor.putString(MainActivity.MWPAGE, mPageURI.getText().toString());
        editor.putString(MainActivity.MWUSER, mUsername.getText().toString());
        editor.putString(MainActivity.MWPASS, mPassword.getText().toString());
        editor.putBoolean(MainActivity.MWCHECKED, true);
        editor.commit();
        finish();
    }

    public void onClickCancel(View view){
        finish();
    }
}
