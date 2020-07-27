package ru.job4j.tourist;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class MarkListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.host_fragment);
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentById(R.id.content) == null) {
            fm.beginTransaction()
                    .add(R.id.content, loadFrg())
                    .commit();
        }
    }

    public Fragment loadFrg() {
        return new MarkListFragment();
    }
}
