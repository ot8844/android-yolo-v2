package org.tensorflow.yolo.util;

import android.graphics.Matrix;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
    private FirebaseStorage storage = FirebaseStorage.getInstance("gs://keonilkim-aos.appspot.com");
    private StorageReference storageRef = storage.getReference();

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
                UserInfoDTO user_info = dataSnapshot.getValue(UserInfoDTO.class);
                savedUserList.add(user_info);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                UserInfoDTO user_info = dataSnapshot.getValue(UserInfoDTO.class);
                savedUserList.remove(user_info);
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

    public void saveMyProfile(Uri file, SimpleDraweeView profile) {
        final StorageReference ref = storageRef.child("images/"+MY_USER_ID+".jpg");
        UploadTask uploadTask = ref.putFile(file);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    profile.setImageURI(downloadUri);
                }
            }
        });
    }

    public void setProfileAsync(SimpleDraweeView profile, String userId) {
        storageRef.child("images/"+userId+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                profile.setImageURI(uri);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    public void removeUserFromMyList(String userKey) {
        databaseReference.child("saved_users").child(userKey).removeValue();
    }

    public String getUserId(UserInfoDTO userInfoDTO) {
        for (Map.Entry<String, UserInfoDTO> entry : users.entrySet()) {
            if (userInfoDTO.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public List<UserInfoDTO> getSavedUserList() {
        return savedUserList;
    }

    public HashMap<String, UserInfoDTO> getUsers() {
        return users;
    }

    public String getUserKey(String sticker) {
        return sticker_to_user.get(sticker);
    }

    public UserInfoDTO getUser(String userKey) {
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