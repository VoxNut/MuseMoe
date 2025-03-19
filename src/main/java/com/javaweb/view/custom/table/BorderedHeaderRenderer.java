package com.javaweb.view.custom.table;

import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import com.javaweb.constant.AppConstant;
import com.javaweb.utils.FontUtil;

public class BorderedHeaderRenderer extends DefaultTableCellRenderer {
    public BorderedHeaderRenderer() {
        setHorizontalAlignment(SwingConstants.CENTER);
        setBackground(AppConstant.NAVBAR_BACKGROUND_COLOR); // Set background color
        setForeground(AppConstant.BUTTON_TEXT_COLOR); // Set text color
        setFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 16)); // Set font to bold
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        ((JComponent) c).setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, AppConstant.BORDER_COLOR)); // Set the border for the
        // header
        c.setFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 16)); // Ensure the font is set to bold
        return c;
    }
}
