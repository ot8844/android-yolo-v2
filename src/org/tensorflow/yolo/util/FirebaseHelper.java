package org.tensorflow.yolo.util;

import android.graphics.Matrix;
import android.media.Image;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.tensorflow.yolo.model.UserInfoDTO;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for manipulating images.
 * Modified by Zoltan Szabo
 * URL: https://github.com/szaza/android-yolo-v2
 **/
public class FirebaseHelper {
    // This value is 2 ^ 18 - 1, and is used to clamp the RGB values before their ranges
    // are normalized to eight bits.
    public static String MY_USER_ID;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    private HashMap<String, UserInfoDTO> users = new HashMap<String, UserInfoDTO>();
    private HashMap<String, String> sticker_to_user = new HashMap<String, String>();
    private List<UserInfoDTO> savedUserList = new ArrayList<>();

    private static class LazyHolder {
        static final FirebaseHelper INSTANCE = new FirebaseHelper();
    }

    public static FirebaseHelper getInstance() {
        return LazyHolder.INSTANCE;
    }

    private FirebaseHelper() {
        ChildEventListener userListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                UserInfoDTO user_info = dataSnapshot.getValue(UserInfoDTO.class);
                users.put(dataSnapshot.getKey(), user_info);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                UserInfoDTO user_info = dataSnapshot.getValue(UserInfoDTO.class);
                users.put(dataSnapshot.getKey(), user_info);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                users.remove(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        ChildEventListener stickerListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                sticker_to_user.put(dataSnapshot.getKey(), dataSnapshot.getValue().toString());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                sticker_to_user.put(dataSnapshot.getKey(), dataSnapshot.getValue().toString());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                sticker_to_user.remove(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        ChildEventListener savedUserListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                savedUserList.add((UserInfoDTO) dataSnapshot.getValue());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                savedUserList.remove(dataSnapshot.getValue());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        databaseReference.child("users").addChildEventListener(userListener);
        databaseReference.child("stickers").addChildEventListener(stickerListener);
        databaseReference.child("saved_users").addChildEventListener(savedUserListener);
    }

    public int userSize() {
        return users.size();
    }

    public void saveUser(String userKey, UserInfoDTO userInfo) {
        databaseReference.child("users").child(userKey).setValue(userInfo); // 데이터 푸쉬
    }

    public void saveUserToMyList(String userKey, UserInfoDTO userInfoDTO) {
        databaseReference.child("saved_users").child(userKey).setValue(userInfoDTO);
    }

    public void removeUserFromMyList(String userKey) {
        databaseReference.child("saved_users").child(userKey).removeValue();
    }

    public List<UserInfoDTO> getSavedUserList() {
        return savedUserList;
    }

    public String getUserKey(String sticker) {
        return sticker_to_user.get(sticker);
    }

    public UserInfoDTO getUser(String userKey) {
        Log.d("jack_debug2", "sticker title: " + userKey);
        Log.d("jack_debug2", "st size: " + sticker_to_user.size());
        Log.d("jack_debug2", "user_id : " + sticker_to_user.get(userKey));
        for (Map.Entry<String, String> entry : sticker_to_user.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Log.d("jack_debug2", "stickers: " + key + " value: " + value);

        }
        for (Map.Entry<String, UserInfoDTO> entry : users.entrySet()) {
            String key = entry.getKey();
            UserInfoDTO value = entry.getValue();
            Log.d("jack_debug2", "stickers: " + key + " value: " + value.getName());

        }
        return users.get(userKey);
    }

    public UserInfoDTO getUserBySticker(String sticker) {
        String key = sticker_to_user.get(sticker);
        if (key == null) {
            return null;
        }
        return users.get(sticker_to_user.get(sticker));
    }
}