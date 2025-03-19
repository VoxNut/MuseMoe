package com.javaweb.utils;

import com.javaweb.constant.AppConstant;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public class ImageUtil {
    public static String selectAndResizeImage(JComponent parentComponent, JLabel imageLabel, String relativePathBase, int width, int height) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setPreferredSize(AppConstant.FILE_CHOOSER_SIZE);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        fileChooser.setCurrentDirectory(new File("src/main/java/com/javaweb/view/imgs"));

        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG", "PNG"));

        int result = fileChooser.showOpenDialog(parentComponent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String absolutePath = selectedFile.getAbsolutePath();
            String relativePath = new File(relativePathBase).toURI().relativize(new File(absolutePath).toURI()).getPath();
            String desiredPath = relativePathBase + "/" + relativePath;
            ImageIcon imageIcon = GuiUtil.createImageIcon(desiredPath, width, height);
            imageLabel.setIcon(imageIcon);
            return desiredPath;
        }
        return null;
    }

}
