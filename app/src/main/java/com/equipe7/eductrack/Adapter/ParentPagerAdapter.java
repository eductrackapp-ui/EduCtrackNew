package com.equipe7.eductrack.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.equipe7.eductrack.TrackModule.PerformanceFragment;
import com.equipe7.eductrack.TrackModule.ProgressFragment;

public class ParentPagerAdapter extends FragmentStateAdapter {

    public ParentPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new PerformanceFragment();
        } else {
            return new ProgressFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Performance + Progress
    }
}
