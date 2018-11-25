package org.tensorflow.yolo.view;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.tensorflow.yolo.R;

public class UserActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        TextView textView = (TextView) findViewById(R.id.text);
        textView.setText(getIntent().getStringExtra("title"));
    }
}
