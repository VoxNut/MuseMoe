package com.javaweb.view.custom.spinner;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class SpinnerEditor extends AbstractCellEditor implements TableCellEditor {
    JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));

    @Override
    public Object getCellEditorValue() {
        return spinner.getValue();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        spinner.setValue(value);
        return spinner;
    }
}