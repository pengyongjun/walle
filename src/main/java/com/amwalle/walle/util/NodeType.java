package com.amwalle.walle.util;

public enum NodeType {

    Object("JSONObject", "object"),
    Array("JSONArray", "array"),
    String("String", "string"),
    Integer("Integer", "integer"),
    Long("Long", "integer"),
    BigDecimal("BigDecimal", "number"),
    Boolean("Boolean", "boolean"),
    Null("null", "null");

    private String javaType;
    private String jsonType;

    NodeType(String javaType, String jsonType) {
        this.javaType = javaType;
        this.jsonType = jsonType;
    }

    public static String getJSONTypeByJavaType(String javaType) {
        for (NodeType nodeType : NodeType.values()) {
            if (nodeType.getJavaType().equals(javaType)) {
                return nodeType.jsonType;
            }
        }

        return null;
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public String getJsonType() {
        return jsonType;
    }

    public void setJsonType(String jsonType) {
        this.jsonType = jsonType;
    }
}
