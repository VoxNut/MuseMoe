package com.javaweb.utils;

import com.javaweb.constant.AppConstant;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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

    public static BufferedImage loadImageFromFile(String fileUrl) throws IOException {
        File imageFile = new File(fileUrl);
        if (!imageFile.exists()) {
            throw new IOException("File not found: " + fileUrl);
        }
        return ImageIO.read(imageFile);
    }

}
