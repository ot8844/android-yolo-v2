package org.tensorflow.yolo.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.tensorflow.yolo.R;
import org.tensorflow.yolo.model.ChatDTO;

public class EditUserInfoActivity extends Activity {
    private EditText user_chat, user_edit;
    private Button user_next;
    private ListView chat_list;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_userinfo);

        user_chat = (EditText) findViewById(R.id.user_chat);
        user_edit = (EditText) findViewById(R.id.user_edit);
        user_next = (Button) findViewById(R.id.user_next);
        chat_list = (ListView) findViewById(R.id.chat_list);

        user_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user_edit.getText().toString().equals("") || user_chat.getText().toString().equals(""))
                    return;

                String chat = user_chat.getText().toString();
                String name = user_edit.getText().toString();

                ChatDTO chat_dto = new ChatDTO(name, "hello"); //ChatDTO를 이용하여 데이터를 묶는다.
                databaseReference.child("chat").child(chat).push().setValue(chat_dto); // 데이터 푸쉬
                user_chat.setText(""); //입력창 초기화
                user_edit.setText("");
//                Intent intent = new Intent(EditUserInfoActivity.this, ChatActivity.class);
//                intent.putExtra("chatName", user_chat.getText().toString());
//                intent.putExtra("userName", user_edit.getText().toString());
//                startActivity(intent);
            }
        });
        showChatList();
    }

    private void showChatList() {
        // 리스트 어댑터 생성 및 세팅
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, android.R.id.text1);
        chat_list.setAdapter(adapter);

        // 데이터 받아오기 및 어댑터 데이터 추가 및 삭제 등..리스너 관리
        databaseReference.child("chat").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.e("LOG", "dataSnapshot.getKey() : " + dataSnapshot.getKey());
                adapter.add(dataSnapshot.getKey());
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}