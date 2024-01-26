package com.xclhove.networkspeed;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            NetworkSpeedMonitor monitor = new NetworkSpeedMonitor();
            if (args.length > 0) {
                monitor.setNetworkName(args[0]);
            }
            monitor.setVisible(true);
        });
    }
}