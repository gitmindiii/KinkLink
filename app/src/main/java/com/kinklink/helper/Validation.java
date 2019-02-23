package com.kinklink.helper;

import android.widget.EditText;
import android.widget.TextView;

public class Validation {
    private final String NAME_PATTERN = "[a-zA-Z ]+";

    public boolean isTextNull(TextView textView) {
        return !(textView.getText().toString().isEmpty() && textView.getText().toString().equals(""));
    }

    public boolean isEditNull(EditText editText) {
        return !(editText.getText().toString().isEmpty() && editText.getText().toString().equals(""));
    }

    public boolean isEmailValid(EditText editText) {
        String getValue = editText.getText().toString().trim();
        return android.util.Patterns.EMAIL_ADDRESS.matcher(getValue).matches();
    }

    public boolean isPasswordValid(EditText editText) {
        String getValue = editText.getText().toString().trim();
        return getValue.length() >= 8;
    }

    public boolean isPassExceedMax(EditText editText) {
        String getValue = editText.getText().toString().trim();
        return getValue.length() <= 20;
    }

    public boolean isNameValid(EditText editText) {
        return editText.getText().toString().trim().matches(NAME_PATTERN);
    }
}
