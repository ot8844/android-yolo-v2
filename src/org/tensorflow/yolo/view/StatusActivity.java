package org.tensorflow.yolo.view;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import org.tensorflow.yolo.R;
import org.tensorflow.yolo.util.FirebaseHelper;

public class StatusActivity extends Activity {

    private Button discoverBtn;
    private Button editProfileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        discoverBtn = (Button) findViewById(R.id.discover);
        editProfileBtn = (Button) findViewById(R.id.edit_info);

        discoverBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClassifierActivity.class);
            intent.putExtra("user_id", FirebaseHelper.MY_USER_ID);
            startActivity(intent);
        });

        editProfileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditUserInfoActivity.class);
            intent.putExtra("user_id", FirebaseHelper.MY_USER_ID);
            intent.putExtra("view_only", true);
            intent.putExtra("is_me", true);
            startActivity(intent);
        });
    }
}
