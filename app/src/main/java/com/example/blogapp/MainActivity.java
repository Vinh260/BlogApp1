package com.example.blogapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                    isFirstTime();
//                SharedPreferences userPref = getApplicationContext().getSharedPreferences("user",Context.MODE_PRIVATE);
//                boolean isLoggedIn = userPref.getBoolean("isLoggedIn",false);
//
////                if (isLoggedIn){
////                    startActivity(new Intent(MainActivity.this,HomeActivity.class));
////                    finish();
////                }
////
////                else {
////                    isFirstTime();
////                }
            }
        },1000);
    }

    private void isFirstTime() {

        SharedPreferences preferences = getApplication().getSharedPreferences("onBoard", Context.MODE_PRIVATE);
        boolean isFirstTime = preferences.getBoolean("isFirstTime",true);
        //default value true
        if (isFirstTime){
            // if its true then its first time and we will change it false
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isFirstTime",false);
            editor.apply();

            // start Onboard activity
            startActivity(new Intent(MainActivity.this,OnBoardActivity.class));
            finish();
        }
        else{
            //start Auth Activity
            startActivity(new Intent(MainActivity.this,AuthActivity.class));
            finish();
        }
    }


}