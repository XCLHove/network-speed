# 网速显示（仅限Linux）

# network-speed

## 介绍
在Linux系统通过悬浮窗显示实时网速的小工具。

仅在Ubuntu22.04上测试通过。

## 使用
1. 需要安装`ifstat`和`openjdk`才能使用。
   ```shell
   sudo apt install ifstat openjdk-8-jdk -y
   ```
2. 如何不能正常使用，请尝试运行下面的**核心命令**以检查输出是否正常。
3. 运行`network-speed.sh`脚本即可启动程序。

## 功能

1. 双击悬浮窗关闭程序。
2. 右键悬浮窗打开设置。

## 核心命令

1. 获取网速
    ```shell
    ifstat -q -i eth0 1 1
    ```
   
   输出样例：
   ```
   user@ubuntu:~$  ifstat -q -i eth0 1 1
          eth0       
    KB/s in  KB/s out
        0.00      0.00
   ```

2. 列出网络
    ```shell
    ip addr show | grep -E '^[0-9]+: ' | awk -F': ' '{print $2}'
    ```
   
   输出样例：
   ```
   user@ubuntu:~$ ip addr show | grep -E '^[0-9]+: ' | awk -F': ' '{print $2}'
   eth0
   lo
   enp8s0f1
   wlp0s20f3
   docker0
   veth54db585@if6
   vetha61af39@if8
   veth1439fae@if10
   ```