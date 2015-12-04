package com.perso.antonleb.projetandroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by antonleb on 26/11/2015.
 */
public class ConnectActivity extends AppCompatActivity {

    public static String ARG_CONNECT_USERNAME = "username";

    private static String SHARED_PREFERENCES = "com.perso.antonleb.projetandroid.ConnectActivity";
    private static String ARG_SHARED_CONNECT_USERNAME = "username_login";

    private EditText username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        final CoordinatorLayout snackbarLayout = (CoordinatorLayout) findViewById(R.id.snackbar_connect_text);

        SharedPreferences prefs = this.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String user_login = prefs.getString(ARG_SHARED_CONNECT_USERNAME, "");

        username = (EditText) findViewById(R.id.edit_connect_username);
        username.setText(user_login);
        username.setSelection(user_login.length());
        Button connect = (Button) findViewById(R.id.button_connect);

        username.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)
                        || (actionId == EditorInfo.IME_ACTION_GO) || (actionId == EditorInfo.IME_ACTION_NEXT)
                        || (actionId == EditorInfo.IME_ACTION_SEND)) {

                    if (username.getText().toString().equals("")){
                        Snackbar snackbar = Snackbar.make(snackbarLayout, "The username cannot be empty", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    } else {
                        toMain(username.getText().toString());
                    }
                }
                return false;
            }
        });

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (username.getText().toString().equals("")){
                    Snackbar snackbar = Snackbar.make(snackbarLayout, "The username cannot be empty", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                } else {
                    toMain(username.getText().toString());
                }
            }
        });
    }

    private void toMain(String username){
        SharedPreferences prefs = this.getSharedPreferences(ConnectActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().putString(ConnectActivity.ARG_SHARED_CONNECT_USERNAME, username).apply();

        Intent toMain = new Intent(ConnectActivity.this, MainActivity.class);
        toMain.putExtra(ARG_CONNECT_USERNAME, username);
        ConnectActivity.this.startActivity(toMain);
    }


}
