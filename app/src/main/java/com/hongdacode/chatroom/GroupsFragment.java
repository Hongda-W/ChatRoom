package com.hongdacode.chatroom;

import android.app.Activity;
import android.content.Intent;
import android.icu.text.Edits;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
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


        mGroupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String mGroupName = adapterView.getItemAtPosition(i).toString();

                Intent groupConversationIntent = new Intent(getContext(), GroupConversationActivity.class);
                groupConversationIntent.putExtra("GroupName", mGroupName);
                startActivity(groupConversationIntent);
            }
        });

//        mGroupListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
//                String mGroupName = adapterView.getItemAtPosition(i).toString();
//                Log.d("ChatRoom", "Long clicked "+ mGroupName);
//                return true;
//            }
//        });

        return mFragView;

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.group_list_view) {
            getActivity().getMenuInflater().inflate(R.menu.menu_group_options, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String groupName = (String) mGroupListView.getItemAtPosition(info.position);

        switch (item.getItemId()) {
            case R.id.option_rename:
                Toast.makeText(getActivity(), "Rename "+groupName, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.option_delete:
                Toast.makeText(getActivity(), "Delete "+groupName, Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void loadViews() {

        mGroupListView = mFragView.findViewById(R.id.group_list_view);
        registerForContextMenu(mGroupListView);

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
                Collections.sort(mStringList);
                mArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}