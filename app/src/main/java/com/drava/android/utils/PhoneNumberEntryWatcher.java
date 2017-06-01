package com.drava.android.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.lang.ref.WeakReference;

public class PhoneNumberEntryWatcher implements TextWatcher {
    private boolean isInAfterTextChanged = false;
    /** Prefix to insert */
    private final String prefix;
    /** Prefix to insert length */
    private final int prefixLength;
    /** Weak reference to parent text edit */
    private final WeakReference<EditText> parentEdit;


    public PhoneNumberEntryWatcher(final String prefix, final EditText parentEdit) {
        this.prefix = prefix;
        this.prefixLength = (prefix == null ? 0 : prefix.length());
        this.parentEdit = new WeakReference<EditText>(parentEdit);
    }

    @Override
    public synchronized void afterTextChanged(final Editable text) {
        if (!this.isInAfterTextChanged) {
            this.isInAfterTextChanged = true;

            if (text.length() <= this.prefixLength) {
                text.clear();
                text.insert(0, this.prefix);

                final EditText parent = this.parentEdit.get();

                if (null != parent) {
                    parent.setSelection(this.prefixLength);
                }
            }
            else {
                if (!this.prefix.equals(text
                        .subSequence(0, this.prefixLength).toString())) {
                    text.clear();
                    text.insert(0, this.prefix);
                }

                final String withoutSpaces
                        = text.toString().replaceAll(" ", "");

                text.clear();
                text.insert(0, withoutSpaces);
            }

            // now delete all spaces
            this.isInAfterTextChanged = false;
        }
    }

    @Override
    public void beforeTextChanged(final CharSequence s,
                                  final int start,
                                  final int count,
                                  final int after) {
        // nothing to do here
    }

    @Override
    public void onTextChanged(final CharSequence s,
                              final int start,
                              final int before,
                              final int count) {
        // nothing to do here
    }
}
