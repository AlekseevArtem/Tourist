package ru.job4j.tourist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class SingleChoiceDialogFragment extends DialogFragment {
    private SingleChoiceListener callback;
    private int position;

    public interface SingleChoiceListener{
        void onPositiveSwapMode(int markMode);
        void onNegativeSwapMode();
    }

    public static DialogFragment newInstance(int position) {
        DialogFragment singleChoiceDialog = new SingleChoiceDialogFragment();
        Bundle args = new Bundle();
        args.putInt("current_mode", position);
        singleChoiceDialog.setArguments(args);
        return singleChoiceDialog;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            callback = (SingleChoiceListener) context;
        } catch (Exception e) {
            throw new ClassCastException(getActivity().toString() + " SingleChoiceListener must implemented");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] list = getActivity().getResources().getStringArray(R.array.choice_modes);
        position = getArguments().getInt("current_mode");
        builder.setTitle("Select mode")
                .setSingleChoiceItems(list, position, (dialog, which) -> position = which)
                .setPositiveButton("Ok", (dialog, which) -> callback.onPositiveSwapMode(position))
                .setNegativeButton("Cancel", (dialog, which) -> callback.onNegativeSwapMode());
        return builder.create();
    }
}
