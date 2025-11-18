package com.smartatt.desktop;

import com.smartatt.desktop.ui.LoginFrame;
import com.smartatt.desktop.ui.UIUtils;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
            UIUtils.applyAppTheme();
            javax.swing.SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
