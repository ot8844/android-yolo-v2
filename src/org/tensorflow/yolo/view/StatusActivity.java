package org.tensorflow.yolo.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.TextView;

import org.tensorflow.yolo.R;
import org.tensorflow.yolo.util.FirebaseHelper;

public class StatusActivity extends Activity {

    private Button discoverBtn;
    private Button editProfileBtn;
    private Button userListBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        TextView titleTextView = (TextView) findViewById(R.id.title);
        titleTextView.setText(Html.fromHtml(getString(R.string.login_title)));

        discoverBtn = (Button) findViewById(R.id.discover);
        editProfileBtn = (Button) findViewById(R.id.edit_info);
        userListBtn = (Button) findViewById(R.id.user_list);

        discoverBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClassifierActivity.class);
            intent.putExtra("user_id", FirebaseHelper.MY_USER_ID);
            startActivity(intent);
        });

        editProfileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditUserInfoActivity.class);
            intent.putExtra("user_id", FirebaseHelper.MY_USER_ID);
            intent.putExtra("is_me", true);
            startActivity(intent);
        });

        userListBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, UserListActivity.class));
        });
    }
}
