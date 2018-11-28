package org.tensorflow.yolo.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.tensorflow.yolo.R;
import org.tensorflow.yolo.model.UserInfoDTO;
import org.tensorflow.yolo.util.FirebaseHelper;

import java.util.Random;

public class EditUserInfoActivity extends Activity {
    public static final String PREFS = "PREFS";
    public static final String SAVED = "SAVED";
    public static final String USER_ID = "USER_ID";
    public static final String USER_NAME = "USER_NAME";
    public static final String USER_JOB = "USER_JOB";
    public static final String USER_EMAIL = "USER_EMAIL";

    private TextView title;
    private EditText name, job, email;
    private Button next;
    private FirebaseHelper fbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_userinfo);
        title = (TextView) findViewById(R.id.title);
        name = (EditText) findViewById(R.id.name_input);
        job = (EditText) findViewById(R.id.job_input);
        email = (EditText) findViewById(R.id.email_input);
        next = (Button) findViewById(R.id.user_next);

        fbHelper = new FirebaseHelper();

        if (getIntent().getBooleanExtra("from_classifier", false)) {
            name.setText(getIntent().getStringExtra("name"));
            job.setText(getIntent().getStringExtra("job"));
            email.setText(getIntent().getStringExtra("email"));
        }

        if (getIntent() != null && getIntent().getBooleanExtra("view_only", false)) {
            title.setText("개인 정보 조회");

//            SharedPreferences next = (Button) findViewById(R.id.user_next);prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
//            name.setText(prefs.getString(USER_NAME, "USER_NAME"));
//            job.setText(prefs.getString(USER_JOB, "USER_JOB"));
//            email.setText(prefs.getString(USER_EMAIL, "USER_EMAIL"));
            name.setEnabled(false);
            job.setEnabled(false);
            email.setEnabled(false);
            next.setVisibility(View.INVISIBLE);
            return;
        }

        next.setOnClickListener(v -> {
            if (name.getText().toString().equals("")
                    || job.getText().toString().equals("")
                    || email.getText().toString().equals(""))
                return;

            String randomId = getIntent().getStringExtra("user_id");
            String nameStr = name.getText().toString();
            String jobStr = job.getText().toString();
            String emailStr = email.getText().toString();

            SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            prefs
                    .edit()
                    .putBoolean(SAVED, true)
                    .putString(USER_ID, randomId)
                    .putString(USER_NAME, nameStr)
                    .putString(USER_JOB, jobStr)
                    .putString(USER_EMAIL, emailStr)
                    .apply();
            UserInfoDTO userInfoDTO = new UserInfoDTO(nameStr, jobStr, emailStr);
            fbHelper.saveUser(randomId, userInfoDTO);
            startActivity(new Intent(this, ClassifierActivity.class));
        });
    }

    @Override
    public void onBackPressed() {
        if (getIntent() != null && getIntent().getBooleanExtra("from_classifier", false)) {
            startActivity(new Intent(this, ClassifierActivity.class));
            finish();
        } else {
            super.onBackPressed();
        }
    }
}