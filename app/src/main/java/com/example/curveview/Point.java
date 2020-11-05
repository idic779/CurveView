package com.example.curveview;

import java.io.Serializable;

public class Point implements Serializable {
    private int x;
    private int xPercent;
    private float yPercent;
    private int y;
    private boolean isServer;
    private boolean showDesc;
    private String desc;
    private int descColor;
    private int lineColor;

    public Point() {
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getxPercent() {
        return xPercent;
    }

    public void setxPercent(int xPercent) {
        this.xPercent = xPercent;
    }

    public float getyPercent() {
        return yPercent;
    }

    public void setyPercent(float yPercent) {
        this.yPercent = yPercent;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isServer() {
        return isServer;
    }

    public void setServer(boolean server) {
        isServer = server;
    }

    public boolean isShowDesc() {
        return showDesc;
    }

    public void setShowDesc(boolean showDesc) {
        this.showDesc = showDesc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getDescColor() {
        return descColor;
    }

    public void setDescColor(int descColor) {
        this.descColor = descColor;
    }

    public int getLineColor() {
        return lineColor;
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }
}
