package org.tensorflow.yolo.view;

import android.app.Activity;
import android.content.ContentQueryMap;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.tensorflow.yolo.R;
import org.tensorflow.yolo.model.UserInfoDTO;
import org.tensorflow.yolo.util.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;


public class UserListFragment extends android.support.v4.app.Fragment {
    private FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
    private UserListAdapter adapter;

    public UserListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new UserListAdapter(getActivity(), firebaseHelper.getSavedUserList());
        listView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    private static class UserListAdapter extends BaseAdapter {
        private List<UserInfoDTO> userInfoDTOList;
        private LayoutInflater inflater;
        private UserListActivity userListActivity;

        public UserListAdapter(Activity activity, List<UserInfoDTO> userList) {
            this.inflater = LayoutInflater.from(activity);
            this.userInfoDTOList = userList;
            this.userListActivity = (UserListActivity) activity;
        }

        @Override
        public int getCount() {
            return userInfoDTOList.size();
        }

        @Override
        public Object getItem(int position) {
            return userInfoDTOList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_user_item, null);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.user_name);
                holder.job = (TextView) convertView.findViewById(R.id.user_job);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            UserInfoDTO user = (UserInfoDTO) getItem(position);
            holder.name.setText(user.getName());
            holder.job.setText(user.getJob());
            convertView.setOnClickListener(v -> {
                Intent intent = new Intent(userListActivity, EditUserInfoActivity.class);
                EditUserInfoActivity.dto = user;
                userListActivity.startActivity(intent);
            });
            return convertView;
        }

        static class ViewHolder {
            TextView name, job;
        }
    }
}
