package com.javaweb.view.custom.musicplayer;

import javax.swing.*;
import java.awt.*;

public interface ThemeablePanel {
    void applyTheme(Color backgroundColor, Color textColor, Color accentColor);

    JDialog createStyledDialog(Frame owner, String title);

}
