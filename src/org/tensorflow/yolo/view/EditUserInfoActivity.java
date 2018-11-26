package org.tensorflow.yolo.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.tensorflow.yolo.R;
import org.tensorflow.yolo.model.UserInfoDTO;

import java.util.Random;

public class EditUserInfoActivity extends Activity {
    public static final String PREFS = "PREFS";
    public static final String SAVED = "SAVED";
    public static final String USER_ID = "USER_ID";

    private EditText name, job, email;
    private Button next;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_userinfo);

        name = (EditText) findViewById(R.id.name_input);
        job = (EditText) findViewById(R.id.job_input);
        email = (EditText) findViewById(R.id.email_input);
        next = (Button) findViewById(R.id.user_next);

        next.setOnClickListener(v -> {
            if (name.getText().toString().equals("")
                    || job.getText().toString().equals("")
                    || email.getText().toString().equals(""))
                return;

            String randomId = String.valueOf(new Random().nextInt(1000) + 1);

            SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            prefs
                    .edit()
                    .putBoolean(SAVED, true)
                    .putString(USER_ID, randomId)
                    .apply();


            String nameStr = name.getText().toString();
            String jobStr = job.getText().toString();
            String emailStr = email.getText().toString();

            UserInfoDTO userInfoDTO = new UserInfoDTO(nameStr, jobStr, emailStr);
            databaseReference.child("users").child(randomId).push().setValue(userInfoDTO); // 데이터 푸쉬
            startActivity(new Intent(this, ClassifierActivity.class));
        });
    }
}