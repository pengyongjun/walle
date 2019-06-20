package com.amwalle.walle.util;

import java.util.List;

public class JSONNode {
    private String nodeName;

    private String nodePath;

    private int level;

    private String dataType;

    private Object data;

    private List<JSONNode> children;

    private String schemaId;

    // This property is used for fulfill schema from definition
    private String reference;

    private boolean isSchemaRequired = true;

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

    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public boolean isSchemaRequired() {
        return isSchemaRequired;
    }

    public void setSchemaRequired(boolean schemaRequired) {
        isSchemaRequired = schemaRequired;
    }
}
