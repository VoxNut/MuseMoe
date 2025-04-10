package com.javaweb.view.mini_musicplayer.panel;

import javax.swing.*;
import java.awt.*;

public interface ThemeablePanel {
    void applyTheme(Color backgroundColor, Color textColor, Color accentColor);

    JDialog createStyledDialog(Frame owner, String title);

}
