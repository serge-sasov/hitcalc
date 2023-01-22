package com.example.hitcalc.ui.turns_and_rounds.sub_fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.hitcalc.storage.GameStorage;

public class RoundCompletionNestedAdapter extends FragmentStatePagerAdapter {
    //keep the backup of the configuration in the file
    private GameStorage mGameStorage;

    //Selected formation to be activated in the turn
    private String mFormationTitle;

    public RoundCompletionNestedAdapter(@NonNull FragmentManager fm, GameStorage gameStorage) {
        super(fm);
        mGameStorage = gameStorage;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return new RoundCompletionNestedFragment(mGameStorage, position);
    }

    @Override
    public int getCount() {
        return 2;
    }
}