package com.javaweb.view.custom.table;

import com.javaweb.constant.AppConstant;
import com.javaweb.utils.FontUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class BorderedTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        if (value instanceof Boolean) {
            JCheckBox checkBox = new JCheckBox();
            checkBox.setSelected((Boolean) value);
            checkBox.setHorizontalAlignment(SwingConstants.CENTER); // Center the checkbox
            checkBox.setBackground(isSelected ? AppConstant.TEXTFIELD_BACKGROUND_COLOR : AppConstant.ACTIVE_BACKGROUND_COLOR);
            checkBox.setForeground(isSelected ? AppConstant.TEXT_COLOR : Color.BLACK);
            return checkBox;
        }

        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (c instanceof JComponent) {
            JComponent jc = (JComponent) c;
            jc.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, AppConstant.BORDER_COLOR));
            jc.setPreferredSize(new Dimension(jc.getPreferredSize().width, table.getRowHeight()));
        }

        // Only check status column for product table which has 5 columns
        if (table.getColumnCount() == 5 && value instanceof String) {
            String status = (String) value;
            if ("Ngừng bán".equals(status) || "Ngừng áp dụng".equals(status)) {
                c.setBackground(AppConstant.DISABLED_BACKGROUND_COLOR);
            } else if ("Sắp áp dụng".equals(status)) {
                c.setBackground(AppConstant.PENDING);
            } else {
                c.setBackground(AppConstant.ACTIVE_BACKGROUND_COLOR);
            }
        } else {
            // For staff table or other tables
            c.setBackground(AppConstant.ACTIVE_BACKGROUND_COLOR);
        }

        int statusColumnIndex = -1;
        // Set background color based on order status
        if ("pendingOrdersTable".equals(table.getName()) || "pendingConfirmationOrdersTable".equals(table.getName()) || "deliveryOrdersTable".equals(table.getName())) {
            // Replace with the actual index of your status column
            statusColumnIndex = 4;

        } else if ("historyOrdersTable".equals(table.getName())) {
            statusColumnIndex = 8;
        }
        if (column == statusColumnIndex) {
            Object statusValue = table.getValueAt(row, statusColumnIndex);

            if (statusValue instanceof String) {
                String status = (String) statusValue;
                switch (status) {
                    case "CHỜ ORDER":
                        c.setBackground(AppConstant.PENDING_ORDER);
                        break;
                    case "CHỜ XÁC NHẬN":
                        c.setBackground(AppConstant.PENDING_CONFIRMATION_ORDER);
                        break;
                    case "HOÀN THÀNH":
                        c.setBackground(AppConstant.COMPLETED_ORDER);
                        break;
                    case "CHỜ NGƯỜI GIAO":
                        c.setBackground(AppConstant.WAITING_FOR_DELIVERY_ORDER);
                        break;
                    case "ĐANG GIAO":
                        c.setBackground(AppConstant.IN_DELIVERY_ORDER);
                        break;
                    default:
                        c.setBackground(AppConstant.ACTIVE_BACKGROUND_COLOR);
                        break;
                }
            } else {
                c.setBackground(AppConstant.ACTIVE_BACKGROUND_COLOR);
            }
        }
        //Set background and text color after click on
        c.setForeground(AppConstant.TABLE_TEXT);
        if (isSelected) {
            c.setForeground(AppConstant.TEXT_COLOR);
            c.setBackground(AppConstant.TEXTFIELD_BACKGROUND_COLOR);
            c.setFont(FontUtil.getJetBrainsMonoFont(Font.BOLD, 18));
        }

        return c;
    }
}