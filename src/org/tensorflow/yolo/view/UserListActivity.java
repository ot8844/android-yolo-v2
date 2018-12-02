package org.tensorflow.yolo.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;

import org.tensorflow.yolo.R;
import org.tensorflow.yolo.model.UserInfoDTO;

public class UserListActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        UserListFragment userListFragment = new UserListFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, userListFragment)
                .commit();

    }

    public void clickUser(UserInfoDTO user) {
        UserFragment userFragment = new UserFragment();
        userFragment.addUser(user);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, userFragment)
                .addToBackStack(null)
                .commit();
    }
}
