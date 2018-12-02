package org.tensorflow.yolo.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import org.tensorflow.yolo.R;
import org.tensorflow.yolo.util.FirebaseHelper;

public class LoginActivity extends Activity {
    private static final int RC_SIGN_IN = 900;
    // 구글api클라이언트
    private GoogleSignInClient googleSignInClient;
    // 파이어베이스 인증 객체 생성
    private FirebaseAuth firebaseAuth;
    // 구글  로그인 버튼
    private SignInButton buttonGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView titleTextView = (TextView) findViewById(R.id.title);
        titleTextView.setText(Html.fromHtml(getString(R.string.login_title)));

        // 파이어베이스 인증 객체 선언
        firebaseAuth = FirebaseAuth.getInstance();
        buttonGoogle = (SignInButton) findViewById(R.id.btn_googleSignIn);

        // Google 로그인을 앱에 통합
        // GoogleSignInOptions 개체를 구성할 때 requestIdToken을 호출
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        buttonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //FIXME : 이거 주석 바꿔야함 넥서스 테스트 때문에 이렇게 했음.
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
//                Intent intent = new Intent(LoginActivity.this, EditUserInfoActivity.class);
//                startActivity(IntentWithUserId(intent, "123124123"));
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 구글로그인 버튼 응답
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // 구글 로그인 성공
                GoogleSignInAccount acct = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(acct);
                SharedPreferences prefs = getSharedPreferences(EditUserInfoActivity.PREFS, Context.MODE_PRIVATE);
                if (prefs.getBoolean(EditUserInfoActivity.SAVED, false)) {
                    Intent intent = new Intent(LoginActivity.this, StatusActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(LoginActivity.this, EditUserInfoActivity.class);
                    intent.putExtra("sign_up", true);
                    startActivity(IntentWithUserId(intent, acct.getId()));
                }
                finish();
            } catch (ApiException e) {

            }

        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 로그인 성공
                            Toast.makeText(LoginActivity.this, R.string.success_login, Toast.LENGTH_SHORT).show();
                            SharedPreferences prefs = getSharedPreferences(EditUserInfoActivity.PREFS, Context.MODE_PRIVATE);
                            if (prefs.getBoolean(EditUserInfoActivity.SAVED, false)) {
                                Intent intent = new Intent(LoginActivity.this, StatusActivity.class);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(LoginActivity.this, EditUserInfoActivity.class);
                                intent.putExtra("sign_up", true);
                                startActivity(IntentWithUserId(intent, acct.getId()));
                            }
                            finish();
                        } else {
                            // 로그인 실패
                            Toast.makeText(LoginActivity.this, R.string.failed_login, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private Intent IntentWithUserId(Intent intent, String userId) {
        FirebaseHelper.MY_USER_ID = userId;
        return intent.putExtra("user_id", userId);
    }
}
