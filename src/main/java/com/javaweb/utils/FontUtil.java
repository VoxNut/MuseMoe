package com.javaweb.utils;

import com.javaweb.constant.AppConstant;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class FontUtil {


    public static void setDefaultFont(String fontPath) {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new File(fontPath)).deriveFont(Font.PLAIN, 20);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);

            UIManager.put("Label.font", font);
            UIManager.put("Button.font", font);
            UIManager.put("TextField.font", font);
            UIManager.put("PasswordField.font", font);
            UIManager.put("TextArea.font", font);
            UIManager.put("ComboBox.font", font);
            UIManager.put("List.font", font);
            UIManager.put("Table.font", font);
            UIManager.put("TableHeader.font", font);
            UIManager.put("TitledBorder.font", font);
            UIManager.put("ToolTip.font", font);
            UIManager.put("Tree.font", font);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    public static Font getJetBrainsMonoFont(int style, float size) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, new File(AppConstant.FONT_PATH)).deriveFont(style, size);
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException("Failed to load JetBrains Mono font", e);
        }
    }

    public static Font getSpotifyFont(int style, float size) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, new File(AppConstant.SPOTIFY_FONT_PATH)).deriveFont(style, size);
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException("Failed to load Circular Spotify font", e);
        }
    }

    public static Font getMonoSpacedFont(int style, int size) {
        return new Font(Font.MONOSPACED, style, size);
    }


}