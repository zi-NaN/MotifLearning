package com.example.zhaoguoqian.materialdesign;

import java.io.Serializable;

public class Node implements Serializable {
    private static final long serialVersionUID = 4990064484446119372L;

    public String innerID;
    public String label;
    public int level;

    public Node(final String innerID, final String label, final int level) {
        this.innerID = innerID;
        this.label = label;
        this.level = level;
    }
}