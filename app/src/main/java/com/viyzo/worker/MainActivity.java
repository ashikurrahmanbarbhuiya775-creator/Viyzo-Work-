package com.viyzo.worker;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView text = new TextView(this);
        text.setText("🤖 Viyzo Worker\n\nApp is running successfully.");
        text.setTextSize(22);
        text.setTextColor(Color.WHITE);
        text.setBackgroundColor(Color.rgb(16, 16, 20));
        text.setPadding(40, 80, 40, 40);

        setContentView(text);
    }
}
