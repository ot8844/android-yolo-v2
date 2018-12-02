package org.tensorflow.yolo.view;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.tensorflow.yolo.R;
import org.tensorflow.yolo.model.UserInfoDTO;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserFragment extends android.support.v4.app.Fragment {
    private UserInfoDTO user;

    public UserFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    public void addUser(UserInfoDTO user) {
        this.user = user;
    }
}
