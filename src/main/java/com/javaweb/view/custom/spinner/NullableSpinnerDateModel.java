package com.javaweb.view.custom.spinner;

import javax.swing.*;

public class NullableSpinnerDateModel extends SpinnerDateModel {
    private boolean isEmpty = false;

    @Override
    public Object getValue() {
        return isEmpty ? null : super.getValue();
    }

    @Override
    public void setValue(Object value) {
        if (value == null) {
            isEmpty = true;
            fireStateChanged();
        } else {
            isEmpty = false;
            super.setValue(value);
        }
    }

    public void setEmpty() {
        isEmpty = true;
        fireStateChanged();
    }
}
