package com.xing.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.xing.cartnumberview.CartNumberView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CartNumberView cartNumberView = (CartNumberView) findViewById(R.id.cartNumberView);
        cartNumberView.setOnNumberChangedListener(new CartNumberView.OnNumberChangedListener() {
            @Override
            public void onNumberChanged(int number) {
                Toast.makeText(MainActivity.this, "number = " + number, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
