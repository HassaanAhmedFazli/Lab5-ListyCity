package com.example.listycity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class AddCityFragment extends DialogFragment {

    interface AddCityDialogListener {
        void onCitySaved(int position, City city);
    }

    private AddCityDialogListener listener;

    private static final String ARG_POSITION = "position";
    private static final String ARG_CITY_NAME = "city_name";
    private static final String ARG_PROVINCE_NAME = "province_name";

    private EditText editCityName;
    private EditText editProvinceName;

    private int position = -1;

    public static AddCityFragment newInstanceForAdd() {
        AddCityFragment f = new AddCityFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, -1);
        f.setArguments(b);
        return f;
    }

    public static AddCityFragment newInstanceForEdit(int position, String cityName, String provinceName) {
        AddCityFragment f = new AddCityFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        b.putString(ARG_CITY_NAME, cityName);
        b.putString(ARG_PROVINCE_NAME, provinceName);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AddCityDialogListener) {
            listener = (AddCityDialogListener) context;
        } else {
            throw new RuntimeException(context + " must implement AddCityDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_add_city, null);

        editCityName = view.findViewById(R.id.edit_text_city_text);
        editProvinceName = view.findViewById(R.id.edit_text_province_text);

        Bundle args = getArguments();
        position = (args == null) ? -1 : args.getInt(ARG_POSITION, -1);

        boolean isEdit = position != -1;

        if (isEdit && args != null) {
            editCityName.setText(args.getString(ARG_CITY_NAME, ""));
            editProvinceName.setText(args.getString(ARG_PROVINCE_NAME, ""));
        }

        String title = isEdit ? "Edit city" : "Add a city";
        String positive = isEdit ? "Save" : "Add";

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(view)
                .setTitle(title)
                .setNegativeButton("Cancel", null)
                // We override the click later so it doesn't auto-close on invalid input
                .setPositiveButton(positive, null);

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null) return;

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String cityName = editCityName.getText().toString().trim();
            String provinceName = editProvinceName.getText().toString().trim();

            boolean ok = true;

            if (cityName.isEmpty()) {
                editCityName.setError("City name required");
                ok = false;
            }

            if (provinceName.isEmpty()) {
                editProvinceName.setError("Province required");
                ok = false;
            }

            if (!ok) return;

            // id is null here; MainActivity uses `position` to find the existing docId on edit
            listener.onCitySaved(position, new City(null, cityName, provinceName));
            dialog.dismiss();
        });
    }
}
