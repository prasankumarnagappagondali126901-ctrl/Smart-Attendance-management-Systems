package com.smartatt.desktop.ui;

import javax.swing.*;
import java.awt.*;

public final class UIUtils {
    public static void applyAppTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        // Optional: increase default font size slightly for readability
        Font base = new JLabel().getFont().deriveFont(13f);
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        for (Object k : defaults.keySet()) {
            if (k != null && defaults.get(k) instanceof Font) {
                UIManager.put(k, base);
            }
        }
    }
}
