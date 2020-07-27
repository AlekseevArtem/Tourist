package ru.job4j.tourist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

import at.markushi.ui.CircleButton;
import ru.job4j.tourist.store.SQLStore;

public class MarkListFragment extends Fragment {
    private RecyclerView mViewsForMarks;
    private MarkAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mark_list, container, false);
        mViewsForMarks = view.findViewById(R.id.mark_recycler_view);
        mViewsForMarks.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        SQLStore store = SQLStore.getInstance(getActivity());
        List<Mark> marks = store.getMarks();
        if (mAdapter == null) {
            mAdapter = new MarkAdapter(marks);
            mViewsForMarks.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    private class MarkHolder extends RecyclerView.ViewHolder{
        private TextView mLat, mLong, mTitle;
        private CircleButton mMap;
        private Mark mMark;

        public MarkHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_mark, parent,false));
            mLat = itemView.findViewById(R.id.item_mark_latitude);
            mLong = itemView.findViewById(R.id.item_mark_longitude);
            mTitle = itemView.findViewById(R.id.item_mark_title);
            mMap = itemView.findViewById(R.id.item_mark_show);
        }

        public void bind(Mark mark) {
            mMark = mark;
            mLat.setText(String.valueOf(mMark.getLatitude()));
            mLong.setText(String.valueOf(mMark.getLongitude()));
            mTitle.setText(mMark.getTitle());
            mMap.setOnClickListener(this::showMarkOnMap);
        }

        public void showMarkOnMap(View v) {
            Intent intent = new Intent();
            intent.putExtra("mark", mMark.getId());
            Objects.requireNonNull(getActivity()).setResult(Activity.RESULT_OK, intent);
            getActivity().onBackPressed();
        }
    }

    private class MarkAdapter extends RecyclerView.Adapter<MarkHolder> {
        private List<Mark> marks;

        public MarkAdapter(List<Mark> marks) {
            this.marks = marks;
        }

        @NonNull
        @Override
        public MarkHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MarkHolder(LayoutInflater.from(getActivity()), parent);
        }

        @Override
        public void onBindViewHolder(@NonNull MarkHolder holder, int position) {
            holder.bind(marks.get(position));
        }

        @Override
        public int getItemCount() {
            return marks.size();
        }
    }
}
