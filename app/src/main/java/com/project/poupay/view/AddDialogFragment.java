package com.project.poupay.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.project.poupay.R;


public class AddDialogFragment extends DialogFragment {


    public AddDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtém a referência à View dialog
        View dialogView = getDialog().getWindow().getDecorView();
        //// Define o background do layout dialog
        dialogView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.dialog_rounded_bg));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_dialog, container, false);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        int width = getResources().getDimensionPixelSize(R.dimen.dialog_width);
        getDialog().getWindow().setLayout(width,
                WindowManager.LayoutParams.WRAP_CONTENT
        );
    }
}