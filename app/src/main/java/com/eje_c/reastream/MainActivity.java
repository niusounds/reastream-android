package com.eje_c.reastream;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

@SuppressWarnings("ConstantConditions")
public class MainActivity extends AppCompatActivity {
    private ReaStream reaStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        reaStream = new ReaStream();

        findViewById(R.id.rec).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (reaStream.isSending()) {
                    reaStream.stopSending();
                } else {
                    reaStream.startSending();
                }
            }
        });

        findViewById(R.id.receive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reaStream.isReceiving()) {
                    reaStream.stopReceiving();
                } else {
                    reaStream.startReveiving();
                }
            }
        });


    }

    @Override
    protected void onDestroy() {
        reaStream.close();
        super.onDestroy();
    }
}
