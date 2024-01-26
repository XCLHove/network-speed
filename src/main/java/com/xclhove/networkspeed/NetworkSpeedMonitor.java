package com.xclhove.networkspeed;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xclhove
 */
public class NetworkSpeedMonitor extends JFrame {
    private String networkName;
    private final JLabel speedLabel;
    private int initialX;
    private int initialY;
    private long lastClickTime;
    private int refreshFrequency = 500;
    
    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }
    
    public NetworkSpeedMonitor() {
        this.networkName = listNetwork().get(0);
        
        // 设置窗口属性
        setUndecorated(true);
        setLayout(new GridBagLayout());
        setSize(150, 20);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        // 设置窗口背景为半透明
        setOpacity(0.6f);
        
        // 设置窗口圆角
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
        
        // 设置窗口置顶
        setAlwaysOnTop(true);
        
        // 添加鼠标事件监听器
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    showSettingsWindow();
                } else {
                    // 快速双击关闭程序
                    if (System.currentTimeMillis() - lastClickTime < 300) {
                        System.exit(0);
                        return;
                    }
                    
                    // 更新上次点击的时间戳
                    lastClickTime = System.currentTimeMillis();
                    
                    initialX = e.getX();
                    initialY = e.getY();
                }
            }
        });
        
        // 添加鼠标拖动事件监听器，用于移动窗口位置。
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int currentX = getLocation().x + e.getX() - initialX;
                int currentY = getLocation().y + e.getY() - initialY;
                setLocation(currentX, currentY);
            }
        });
        
        // 创建用于显示网速的标签
        speedLabel = new JLabel();
        speedLabel.setFont(new Font("Arial", Font.BOLD, 10));
        add(speedLabel);
        
        // 刷新网速
        refreshSpeed();
        
        // 使用新线程定时刷新网速避免拖动窗口时卡顿
        Thread refreshThread = new Thread(() -> {
            while (true) {
                refreshSpeed();
                try {
                    Thread.sleep(refreshFrequency);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        refreshThread.start();
    }
    
    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(getBackground());
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        super.paint(g2d);
        g2d.dispose();
    }
    
    /**
     * 刷新网速
     */
    private void refreshSpeed() {
        // 显示在标签上
        speedLabel.setText(getSpeed(networkName));
    }
    
    /**
     * 获取一个网络的网速
     * @param networkName 网络名称
     * @return 网速文本，如：↓ 2.5 MB/s ↑ 1.2 MB/s
     */
    private String getSpeed(String networkName) {
        // 执行命令获取网速信息
        List<String> lines = exec("ifstat -q -i " + networkName + " " + (refreshFrequency / 1000f)+ " 1");
        
        // 网速单位
        String uploadUnit = "";
        String downloadUnit = "";
        
        // 网速大小
        float uploadSize = 0f;
        float downloadSize = 0f;
        
        // 解析输出结果，获取接收和发送的网速
        for (String line : lines) {
            String[] parts = line.trim().split("\\s+");
            // 解析网速单位
            if (parts.length == 4) {
                for (int i = 0; i < parts.length; i++) {
                    if ("in".equalsIgnoreCase(parts[i])) {
                        downloadUnit = parts[i - 1];
                    } else if ("out".equalsIgnoreCase(parts[i])) {
                        uploadUnit = parts[i - 1];
                    }
                }
            }
            // 解析网速大小
            else if (parts.length == 2) {
                try {
                    downloadSize = Float.parseFloat(parts[0]);
                } catch (NumberFormatException e) {
                    downloadSize = -1f;
                }
                
                try {
                    uploadSize = Float.parseFloat(parts[1]);
                } catch (NumberFormatException e) {
                    uploadSize = -1f;
                }
            }
        }
        
        // 生成网速文本
        String speedText = "↓ " + new Speed(downloadSize, downloadUnit) + " ↑ " + new Speed(uploadSize, uploadUnit);
        
        return speedText;
    }
    
    /**
     * 列出网络
     * @return 网络名称列表，如：[enp0s3, enp0s8]
     */
    private List<String> listNetwork() {
        List<String> networks = new ArrayList<>();
        
        List<String> lines = exec("ip addr show");
        lines.forEach(line -> {
            // 过滤以数字开头的行，并提取第二个字段
            if (line.matches("^[0-9].*")) {
                String[] fields = line.split("\\s+");
                if (fields.length > 1) {
                    networks.add(fields[1].replace(":", ""));
                }
            }
        });
        
        if (networks.isEmpty()) {
            JOptionPane.showMessageDialog(this, "获取网络名称失败!", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        return networks;
    }
    
    /**
     * 执行命令
     * @param command 要执行的命令
     * @return 输出结果的列表，每行一个元素。如：[line1, line2, ...]
     */
    private List<String> exec(String command) {
        List<String> lines = new ArrayList<>();
        
        // 执行命令
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return lines;
    }
    
    /**
     * 显示设置窗口
     */
    private void showSettingsWindow() {
        SettingsWindow settingsWindow = new SettingsWindow();
        settingsWindow.setLocationRelativeTo(this);
        settingsWindow.setVisible(true);
    }
    
    /**
     * 设置窗口
     */
    private class SettingsWindow extends JFrame {
        private JTextField frequencyTextField;
        private JComboBox<String> networkComboBox;
        
        public SettingsWindow() {
            setTitle("设置");
            setLayout(new GridLayout(3, 2));
            
            JLabel frequencyLabel = new JLabel("刷新频率 (毫秒):");
            frequencyTextField = new JTextField(String.valueOf(refreshFrequency));
            add(frequencyLabel);
            add(frequencyTextField);
            
            JLabel networkLabel = new JLabel("网络:");
            List<String> networkList = listNetwork();
            networkComboBox = new JComboBox<>(networkList.toArray(new String[0]));
            networkComboBox.setSelectedItem(networkName);
            add(networkLabel);
            add(networkComboBox);
            
            add(getCancelButton());
            add(getConfirmButton());
            
            pack();
        }
        
        private JButton getConfirmButton() {
            JButton confirmButton = new JButton("确定");
            confirmButton.addActionListener(e -> {
                String frequencyText = frequencyTextField.getText();
                try {
                    int frequency = Integer.parseInt(frequencyText);
                    refreshFrequency = frequency;
                } catch (NumberFormatException ex) {
                    // 处理非法输入
                    JOptionPane.showMessageDialog(this, "无效的刷新频率!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                networkName = (String) networkComboBox.getSelectedItem();
                dispose();
            });
            return confirmButton;
        }
        
        private JButton getCancelButton() {
            JButton cancelButton = new JButton("取消");
            cancelButton.addActionListener(e -> dispose());
            return cancelButton;
        }
    }
}