package com.hongdacode.chatroom;

import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabAdaptor extends FragmentPagerAdapter {


    public TabAdaptor(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                ConversationsFragment conversationsFragment = new ConversationsFragment();
                return conversationsFragment;

            case 1:
                GroupsFragment groupsFragment = new GroupsFragment();
                return groupsFragment;

            case 2:
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;

            case 3:
                FriendRequestFragment requestFragment = new FriendRequestFragment();
                return requestFragment;

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 4;
    }

//    @Nullable
//    @Override
//    public CharSequence getPageTitle(int position) {
//
//        switch (position){
//            case 0:
//                return "Conversations";
//
//            case 1:
//                return "Groups";
//
//            case 2:
//                return "Contacts";
//
//            default:
//                return null;
//        }
//    }
}
