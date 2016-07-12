package com.eje_c.reastream;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.eje_c.libreastream.ReaStream;

import java.net.UnknownHostException;

@SuppressWarnings("ConstantConditions")
public class MainActivity extends AppCompatActivity {
    private ReaStream reaStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        reaStream = new ReaStream();

        EditText identifier = (EditText) findViewById(R.id.input_identifier);
        identifier.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                reaStream.setIdentifier(s.toString());
            }
        });

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup_mode);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_mode_receive:
                        if (reaStream.isSending()) {
                            reaStream.stopSending();
                        }
                        if (!reaStream.isReceiving()) {
                            reaStream.startReveiving();
                        }
                        break;
                    case R.id.radio_mode_send:
                        if (reaStream.isReceiving()) {
                            reaStream.stopReceiving();
                        }
                        if (!reaStream.isSending()) {
                            reaStream.startSending();
                        }
                        break;
                }
            }
        });

        CheckBox enabled = (CheckBox) findViewById(R.id.check_enabled);
        enabled.setChecked(reaStream.isEnabled());
        enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reaStream.setEnabled(isChecked);
            }
        });

        reaStream.startReveiving();

        EditText remoteAddress = (EditText) findViewById(R.id.input_remoteAddress);
        remoteAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    reaStream.setRemoteAddress(s.toString());
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            reaStream.setRemoteAddress(remoteAddress.getText().toString());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        reaStream.close();
        super.onDestroy();
    }
}
