package com.amwalle.walle.util;

import java.util.List;

public class JSONNode {
    private String nodeName;

    private String nodePath;

    private int level;

    private String dataType;

    private Object data;

    private List<JSONNode> children;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodePath() {
        return nodePath;
    }

    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public List<JSONNode> getChildren() {
        return children;
    }

    public void setChildren(List<JSONNode> children) {
        this.children = children;
    }
}
