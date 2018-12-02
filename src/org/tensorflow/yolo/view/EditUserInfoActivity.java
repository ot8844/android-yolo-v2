package org.tensorflow.yolo.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.yolo.R;
import org.tensorflow.yolo.model.UserInfoDTO;
import org.tensorflow.yolo.util.FirebaseHelper;

import java.util.HashMap;
import java.util.Map;

public class EditUserInfoActivity extends Activity {
    public static final String PREFS = "PREFS";
    public static final String SAVED = "SAVED";
    public static final String USER_ID = "USER_ID";
    public static final String USER_NAME = "USER_NAME";
    public static final String USER_JOB = "USER_JOB";
    public static final String USER_EMAIL = "USER_EMAIL";

    public static boolean fromDiscover = false;
    public static UserInfoDTO dto;

    private TextView nameText;
    private EditText nameEdit;
    private View nameView;

    private TextView emailText;
    private EditText emailEdit;
    private View emailView;

    private TextView majorText;
    private EditText majorEdit;
    private View majorView;

    private TextView jobText;
    private EditText jobEdit;
    private View jobView;

    private TextView historyText;
    private EditText historyEdit;
    private View historyView;

    private Button cancel, save;
    private View divider;
    private FirebaseHelper fbHelper;

    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_userinfo);
        initViews();
        fbHelper = FirebaseHelper.getInstance();

        if (getIntent() != null && !getIntent().getBooleanExtra("sign_up", false)) {
            nameEdit.setText(getIntent().getStringExtra("name"));
            emailEdit.setText(getIntent().getStringExtra("email"));
            majorEdit.setText(getIntent().getStringExtra("major"));
            jobEdit.setText(getIntent().getStringExtra("job"));
            historyEdit.setText(getIntent().getStringExtra("history"));

            if(dto != null) {
                nameEdit.setText(dto.getName());
                emailEdit.setText(dto.getEmail());
                majorEdit.setText(dto.getMajor());
                jobEdit.setText(dto.getJob());
                historyEdit.setText(dto.getHistory());
            }

            if (getIntent().getBooleanExtra("is_me", false)) {
                UserInfoDTO dto = fbHelper.getUser(getIntent().getStringExtra("user_id"));
                if (dto != null) {
                    nameEdit.setText(dto.getName());
                    emailEdit.setText(dto.getEmail());
                    majorEdit.setText(dto.getMajor());
                    jobEdit.setText(dto.getJob());
                    historyEdit.setText(dto.getHistory());
                }

                save.setText("Save");
                save.setOnClickListener(v -> {
                    if (save()) {
                        Toast.makeText(this, "Your profile is updated.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                if (fromDiscover) {
                    title = getIntent().getStringExtra("title");
                    String userId = fbHelper.getUserKey(title);
                    UserInfoDTO userInfoDTO = fbHelper.getUserBySticker(title);

                    save.setText("Add");
                    save.setOnClickListener(v -> {
                        addToMyList(userId, userInfoDTO);
                        Toast.makeText(this, "User is added to your list.", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    save.setText("Delete");
                    save.setOnClickListener(v -> {
                        if (dto != null) {
                            deleteFromMyList(dto);
                            Toast.makeText(this, "User is deleted from your list.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                nameEdit.setEnabled(false);
                emailEdit.setEnabled(false);
                majorEdit.setEnabled(false);
                jobEdit.setEnabled(false);
                historyEdit.setEnabled(false);
            }
        } else {
            save.setOnClickListener(v -> {
                if (save()) {
                    startActivity(new Intent(this, StatusActivity.class));
                    finish();
                }
            });
        }

        cancel.setOnClickListener(v -> onBackPressed());
    }

    private void addToMyList(String userKey, UserInfoDTO userInfoDTO) {
        fbHelper.saveUserToMyList(userKey, userInfoDTO);
    }

    private void deleteFromMyList(UserInfoDTO userInfoDTO) {
        HashMap<String, UserInfoDTO> map = fbHelper.getUsers();
        for (Map.Entry<String, UserInfoDTO> entry : map.entrySet()) {
            if (userInfoDTO.equals(entry.getValue())) {
                fbHelper.removeUserFromMyList(entry.getKey());
                return;
            }
        }
    }

    private void initViews() {
        nameText = (TextView) findViewById(R.id.name_text);
        nameEdit = (EditText) findViewById(R.id.name_input);
        nameView = findViewById(R.id.name_line);

        emailText = (TextView) findViewById(R.id.email_text);
        emailEdit = (EditText) findViewById(R.id.email_input);
        emailView = findViewById(R.id.email_line);

        majorText = (TextView) findViewById(R.id.major_text);
        majorEdit = (EditText) findViewById(R.id.major_input);
        majorView = findViewById(R.id.major_line);

        jobText = (TextView) findViewById(R.id.job_text);
        jobEdit = (EditText) findViewById(R.id.job_input);
        jobView = findViewById(R.id.job_line);

        historyText = (TextView) findViewById(R.id.history_text);
        historyEdit = (EditText) findViewById(R.id.history_input);
        historyView = findViewById(R.id.history_line);

        nameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (nameEdit.getText().length() != 0) {
                    nameText.setTextColor(getColor(R.color.purple_4));
                    nameView.setBackgroundColor(getColor(R.color.purple_4));
                } else {
                    nameText.setTextColor(getColor(R.color.normal_gray));
                    nameView.setBackgroundColor(getColor(R.color.normal_gray));
                }
            }
        });

        emailEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (emailEdit.getText().length() != 0) {
                    emailText.setTextColor(getColor(R.color.purple_4));
                    emailView.setBackgroundColor(getColor(R.color.purple_4));
                } else {
                    emailText.setTextColor(getColor(R.color.normal_gray));
                    emailView.setBackgroundColor(getColor(R.color.normal_gray));
                }
            }
        });

        majorEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (majorEdit.getText().length() != 0) {
                    majorText.setTextColor(getColor(R.color.purple_4));
                    majorView.setBackgroundColor(getColor(R.color.purple_4));
                } else {
                    majorText.setTextColor(getColor(R.color.normal_gray));
                    majorView.setBackgroundColor(getColor(R.color.normal_gray));
                }
            }
        });

        jobEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (jobEdit.getText().length() != 0) {
                    jobText.setTextColor(getColor(R.color.purple_4));
                    jobView.setBackgroundColor(getColor(R.color.purple_4));
                } else {
                    jobText.setTextColor(getColor(R.color.normal_gray));
                    jobView.setBackgroundColor(getColor(R.color.normal_gray));
                }
            }
        });

        historyEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (historyEdit.getText().length() != 0) {
                    historyText.setTextColor(getColor(R.color.purple_4));
                    historyView.setBackgroundColor(getColor(R.color.purple_4));
                } else {
                    historyText.setTextColor(getColor(R.color.normal_gray));
                    historyView.setBackgroundColor(getColor(R.color.normal_gray));
                }
            }
        });


        save = (Button) findViewById(R.id.user_save);
        cancel = (Button) findViewById(R.id.user_cancel);
        divider = findViewById(R.id.divider);
    }

    private boolean save() {
        if (nameEdit.getText().toString().equals("")
                || emailEdit.getText().toString().equals("")
                || jobEdit.getText().toString().equals("")
                || majorEdit.getText().toString().equals("")
                || historyEdit.getText().toString().equals("")) {
            return false;
        }

        String userId = getIntent().getStringExtra("user_id");
        String nameStr = nameEdit.getText().toString();
        String emailStr = emailEdit.getText().toString();
        String jobStr = jobEdit.getText().toString();
        String majorStr = majorEdit.getText().toString();
        String historyStr = historyEdit.getText().toString();

        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs
                .edit()
                .putBoolean(SAVED, true)
                .putString(USER_ID, userId)
                .putString(USER_NAME, nameStr)
                .putString(USER_JOB, jobStr)
                .putString(USER_EMAIL, emailStr)
                .apply();
        UserInfoDTO userInfoDTO = new UserInfoDTO(nameStr, emailStr, majorStr, jobStr, historyStr);
        fbHelper.saveUser(userId, userInfoDTO);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (fromDiscover) {
            startActivity(new Intent(this, ClassifierActivity.class));
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        fromDiscover = false;
        super.onDestroy();
    }
}