package com.example.zhaoguoqian.materialdesign;

import java.io.Serializable;

public class Edge implements Serializable {
    private static final long serialVersionUID = 1L;

    public String node1;
    public String node2;
    public int level;
    public int difficulty;
    public int connected;

    public Edge(final String node1, final String node2, final int level, final int difficulty, final int connected) {
        this.node1 = node1;
        this.node2 = node2;
        this.level = level;
        this.difficulty = difficulty;
        this.connected= connected;
    }
    public String getSource(){return node1;}
    public String getTarget(){return node2;}
}