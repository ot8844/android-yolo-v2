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
import android.widget.Toast;

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
    private Button next, save;
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
        save = (Button) findViewById(R.id.user_save);
        fbHelper = new FirebaseHelper();

        if (getIntent() != null && getIntent().getBooleanExtra("view_only", false)) {
            title.setText(R.string.view_user_info);

            name.setText(getIntent().getStringExtra("name"));
            job.setText(getIntent().getStringExtra("job"));
            email.setText(getIntent().getStringExtra("email"));

            if (getIntent().getBooleanExtra("is_me", false)) {
                UserInfoDTO dto = fbHelper.getUser(getIntent().getStringExtra("user_id"));
                if (dto != null) {
                    name.setText(dto.getName());
                    job.setText(dto.getJob());
                    email.setText(dto.getEmail());
                }

                save.setVisibility(View.VISIBLE);
                save.setOnClickListener(v -> {
                    save();
                    Toast.makeText(this, "정보가 갱신되었습니다.", Toast.LENGTH_SHORT).show();
                });
                next.setVisibility(View.INVISIBLE);
            } else {
                save.setVisibility(View.INVISIBLE);
                next.setVisibility(View.INVISIBLE);
                name.setEnabled(false);
                job.setEnabled(false);
                email.setEnabled(false);
            }
            return;
        }

        next.setOnClickListener(v -> {
            save();
            Intent intent = new Intent(this, StatusActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void save() {
        if (name.getText().toString().equals("")
                || job.getText().toString().equals("")
                || email.getText().toString().equals(""))
            return;

        String userId = getIntent().getStringExtra("user_id");
        String nameStr = name.getText().toString();
        String jobStr = job.getText().toString();
        String emailStr = email.getText().toString();

        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs
                .edit()
                .putBoolean(SAVED, true)
                .putString(USER_ID, userId)
                .putString(USER_NAME, nameStr)
                .putString(USER_JOB, jobStr)
                .putString(USER_EMAIL, emailStr)
                .apply();
        UserInfoDTO userInfoDTO = new UserInfoDTO(nameStr, jobStr, emailStr);
        fbHelper.saveUser(userId, userInfoDTO);
    }

    @Override
    public void onBackPressed() {
        if (getIntent() != null && getIntent().getBooleanExtra("view_only", false)) {
            startActivity(new Intent(this, ClassifierActivity.class));
            finish();
        } else {
            super.onBackPressed();
        }
    }
}