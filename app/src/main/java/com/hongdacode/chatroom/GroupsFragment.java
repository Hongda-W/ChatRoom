package com.hongdacode.chatroom;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.icu.text.Edits;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

    private StorageReference mStorageReference;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mFragView = inflater.inflate(R.layout.fragment_groups, container, false);

        mDatabaseGroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        mStorageReference = FirebaseStorage.getInstance().getReference();

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
                renameGroup(groupName);
                return true;
            case R.id.option_delete:
                AlertDialog myAlertDialog = DeleteConfirmation(groupName);
                myAlertDialog.show();
                myAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE);
                myAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private AlertDialog DeleteConfirmation(final String groupName) {
        AlertDialog confirmationDialogBox = new AlertDialog.Builder(getActivity())
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete this contact ?")
                .setIcon(android.R.drawable.ic_delete)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        mDatabaseGroupRef.child(groupName).child("StorageID").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    final String storageID = snapshot.getValue().toString();
                                    Log.d("ChatRoom", "Should delete "+storageID);
                                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(storageID);
                                    storageReference.delete(); // The storage reference will not be deleted.
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        mDatabaseGroupRef.child(groupName).removeValue();
                        Toast.makeText(getActivity(), "Group "+groupName+ " deleted.", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })

                .create();
        return confirmationDialogBox;
    }

    private void renameGroup(final String groupName) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity(), R.style.AlertDialog);
        alertBuilder.setTitle("Enter group name: ");

        final EditText groupNameView = new EditText(getActivity());
        groupNameView.setHint(groupName);
        alertBuilder.setView(groupNameView);

        alertBuilder.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String newName = groupNameView.getText().toString();

                if (groupName.equals("")){
                    showErrorDialog("Group name cannot be empty");
                } else {
                    mDatabaseGroupRef.child(groupName).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                 final Object tmpValue = snapshot.getValue();
                                mDatabaseGroupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild(newName)){
                                            showErrorDialog(newName + " already exists!");
                                        } else{
                                            mDatabaseGroupRef.child(newName).setValue(tmpValue);
                                            mDatabaseGroupRef.child(groupName).removeValue();
                                            Toast.makeText(getActivity(), "Renamed "+groupName+" to "+newName, Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });

        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        alertBuilder.show();
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

    private void showErrorDialog(String message){
        new android.app.AlertDialog.Builder(getActivity())
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.stat_notify_error)
                .show();
    }
}