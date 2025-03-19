package com.javaweb.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.im.InputContext;
import java.util.Locale;

public class LocaleUtil {
    public static void setInputLocale(Component component, Locale locale) {
        if (component instanceof JComponent) {
            InputContext inputContext = component.getInputContext();
            inputContext.selectInputMethod(locale);
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                setInputLocale(child, locale);
            }
        }
    }
}
