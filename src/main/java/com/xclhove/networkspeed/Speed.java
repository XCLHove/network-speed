package com.xclhove.networkspeed;

public class Speed {
    private String unit;
    private float size;
    
    public Speed(float size, String unit) {
        this.unit = unit;
        this.size = size;
    }
    
    /**
     * 获取网速单位
     * @return 网速单位
     */
    public String getUnit() {
        return unit;
    }
    
    /**
     * 获取网速值
     * @return 网速值，单位为B/s，即每秒下载或上传的字节数。
     */
    public float getSize() {
        return this.size;
    }
    
    /**
     * toString方法
     * @return 网速字符串，如：1.00 MB/s，1.00 KB/s，1.00 B/s。
     */
    @Override
    public String toString() {
        Speed speed = this;
        while (speed.nextUnitLevel() != null);
        return String.format("%.2f", speed.size) + " " + speed.unit;
    }
    
    public Speed nextUnitLevel() {
        if (size < 1024) {
            return null;
        }
        
        size /= 1024;
        
        if (unit.matches("^B.*$")) {
            unit = unit.replace("B", "KB");
        }
        else if (unit.matches("^KB.*$")) {
            unit = unit.replace("KB", "MB");
        }
        else if (unit.matches("^MB.*$")) {
            unit = unit.replace("MB", "GB");
        }
        else if (unit.matches("^GB.*$")) {
            unit = unit.replace("GB", "TB");
        }
        else if (unit.matches("^TB.*$")) {
            unit = unit.replace("TB", "PB");
        }
        else if (unit.matches("^PB.*$")) {
            unit = unit.replace("PB", "EB");
        }
        else if (unit.matches("^EB.*$")) {
            unit = unit.replace("EB", "ZB");
        }
        
        return this;
    }
}