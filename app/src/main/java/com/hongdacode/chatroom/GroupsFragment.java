package com.hongdacode.chatroom;

import android.icu.text.Edits;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GroupsFragment extends Fragment {

    private View mFragView;
    private ListView mGroupListView;
    private ArrayAdapter<String> mArrayAdapter;
    private ArrayList<String> mStringList = new ArrayList<>();

    private DatabaseReference mDatabaseGroupRef;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mFragView = inflater.inflate(R.layout.fragment_groups, container, false);

        mDatabaseGroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        loadViews();

        getDisplayGroups();

        return mFragView;

    }

    private void loadViews() {

        mGroupListView = mFragView.findViewById(R.id.group_list_view);
        mArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, mStringList);
        mGroupListView.setAdapter(mArrayAdapter);

    }

    private void getDisplayGroups() {
        mDatabaseGroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Set<String> mSet = new HashSet<>();
                Iterator ite = snapshot.getChildren().iterator();

                while (ite.hasNext()) {
                    mSet.add(((DataSnapshot)ite.next()).getKey());
                }

                mStringList.clear();
                mStringList.addAll(mSet);
                mArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}