package com.amwalle.walle.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class JSONTree {

    private static HashSet<String> pathSet = new HashSet<>();

    /**
     * This method is used for creating a JSON Tree
     *
     * @param nodeData Node data
     * @param nodeName Node name
     * @param nodePath Node path
     * @param level    Node level
     * @return The node
     */
    public static JSONNode createJSONTree(Object nodeData, String nodeName, String nodePath, int level) {
        JSONNode node = new JSONNode();
        node.setNodeName(nodeName);
        node.setNodePath(nodePath);
        node.setLevel(level);
        node.setData(nodeData);

        if (nodeData == null) {
            node.setDataType(NodeType.Null.getJsonType());
            return node;
        }

        node.setDataType(nodeData.getClass().getName());

        List<JSONNode> childrenList = new LinkedList<>();

        if (nodeData instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) nodeData;
            Set<String> keySet = jsonObject.keySet();

            level++;
            for (String key : keySet) {
                JSONNode childNode = createJSONTree(jsonObject.get(key), key, nodePath + "/" + key, level);
                childrenList.add(childNode);
            }

            node.setChildren(childrenList);
        } else if (nodeData instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) nodeData;


            for (int index = 0, size = jsonArray.size(); index < size; index++) {
                // Array 元素，不是单个节点；所以将元素下一级的孩子链表整个作为 Array 的孩子链表
                JSONNode childNode = createJSONTree(jsonArray.get(index), nodeName, nodePath + "[" + index + "]", level);
                if (childNode.getChildren() != null) {
                    childrenList.addAll(childNode.getChildren());
                }
            }

            node.setChildren(childrenList);
        } else {
            node.setChildren(null);
        }

        return node;
    }

    /**
     * This method is used for creating a JSON tree, in which all path for node is unique.
     *
     * @param nodeData Node data
     * @param nodeName Node name
     * @param nodePath Node path
     * @param level    Node level
     * @return Node
     */
    public static JSONNode createDeduplicateJSONTree(Object nodeData, String nodeName, String nodePath, int level) {
        JSONNode node = new JSONNode();
        node.setNodeName(nodeName);
        node.setNodePath(nodePath);
        node.setLevel(level);
        node.setData(nodeData);

        if (nodeData == null) {
            node.setDataType(NodeType.Null.getJsonType());
            return node;
        }

        node.setDataType(NodeType.getJSONTypeByJavaType(nodeData.getClass().getSimpleName()));

        List<JSONNode> childrenList = new LinkedList<>();

        if (nodeData instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) nodeData;
            Set<String> keySet = jsonObject.keySet();

            level++;
            for (String key : keySet) {
                JSONNode childNode = createDeduplicateJSONTree(jsonObject.get(key), key, nodePath + "/" + key, level);
                if (childNode != null) {
                    childrenList.add(childNode);
                }
            }

            node.setChildren(childrenList);
        } else if (nodeData instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) nodeData;

            JSONNode arrayItem = new JSONNode();
            arrayItem.setNodeName("items");
            arrayItem.setNodePath(nodePath + "/items");
            arrayItem.setLevel(++level);
            arrayItem.setData(nodeData);

            List<JSONNode> arrayChildrenList = new LinkedList<>();

            for (Object arrayData : jsonArray) {
                arrayItem.setDataType(NodeType.getJSONTypeByJavaType(arrayData.getClass().getSimpleName()));
                // Array 元素，不是单个节点；所以将元素下一级的孩子链表整个作为 Array 的孩子链表
                JSONNode childNode = createDeduplicateJSONTree(arrayData, nodeName, nodePath + "/items", level);

                if (childNode != null && childNode.getChildren() != null) {
                    arrayChildrenList.addAll(childNode.getChildren());
                }
            }

            arrayItem.setChildren(arrayChildrenList);
            childrenList.add(arrayItem);

            node.setChildren(childrenList);
        } else {
            // 如果路径已经存在，则舍弃该节点
            if (!pathSet.add(node.getNodePath())) {
                return null;
            }
            node.setChildren(null);
        }

        return node;
    }

    public static List<JSONNode> levelTraversal(JSONNode rootNode) {
        if (rootNode == null) {
            return null;
        }

        Queue<JSONNode> queue = new ConcurrentLinkedQueue<>();
        queue.add(rootNode);

        List<JSONNode> nodeList = new LinkedList<>();

        while (!queue.isEmpty()) {
            JSONNode node = queue.poll();
            nodeList.add(node);

            if (node != null) {
                if (node.getChildren() != null) {
                    queue.addAll(node.getChildren());
                }
            }
        }

        return nodeList;
    }

    public static List<JSONNode> depthFirstTraversal(JSONNode rootNode) {
        if (rootNode == null) {
            return null;
        }

        Stack<JSONNode> stack = new Stack<>();
        stack.push(rootNode);

        List<JSONNode> nodeList = new LinkedList<>();

        while (!stack.isEmpty()) {
            JSONNode node = stack.pop();
            nodeList.add(node);

            if (node == null || node.getChildren() == null) {
                continue;
            }

            List<JSONNode> children = node.getChildren();

            for (int index = children.size() - 1; index >= 0; index--) {
                stack.push(children.get(index));
            }
        }

        return nodeList;
    }

    public static void createJSONSchema(JSONNode jsonNode, JSONObject jsonObject, String id) {
        jsonObject.fluentPut("$id", id);
        jsonObject.fluentPut("type", jsonNode.getDataType());

        List<JSONNode> children = jsonNode.getChildren();

        if (NodeType.Object.getJsonType().equals(jsonNode.getDataType())) {
            id = id + "/properties/";
        } else if (NodeType.Array.getJsonType().equals(jsonNode.getDataType())) {
            id = id + "/";
        }

        JSONObject schema = JSONObject.parseObject("{}", JSONObject.class, Feature.OrderedField);
        if (children != null) {
            for (JSONNode node : children) {
                // TODO 如果该节点不需要 Schema 配置，则舍弃该节点
                if ("".equals(node.getNodeName())) {
                    continue;
                }

                JSONObject childSchema = JSONObject.parseObject("{}", JSONObject.class, Feature.OrderedField);
                createJSONSchema(node, childSchema, id + node.getNodeName());
                schema.fluentPut(node.getNodeName(), childSchema);
            }
        }

        // TODO 在这里区分子节点的类别，然后判断应该加入什么关键字
        if (NodeType.Object.getJsonType().equals(jsonNode.getDataType())) {
            jsonObject.fluentPut("required", "[]");
            jsonObject.fluentPut("properties", schema);
        } else if (NodeType.Array.getJsonType().equals(jsonNode.getDataType())) {
            jsonObject.fluentPut("items", schema.get("items"));
        } else {
            jsonObject.fluentPut("pattern", "^(.*)$");
        }

    }

    public static void main(String[] args) {
        String data = "{\n" +
                "    \"TestNull\": null,\n" +
                "    \"country\": [\n" +
                "        {\n" +
                "            \"A\": \"A\",\n" +
                "            \"B\": \"B\",\n" +
                "            \"C\": \"C\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"C\": \"C\",\n" +
                "            \"D\": \"D\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"E\": \"E\",\n" +
                "            \"F\": {\n" +
                "                \"G\": \"G\"\n" +
                "            }\n" +
                "        }\n" +
                "    ],\n" +
                "    \"fast_open\": false,\n" +
                "    \"local_address\": \"127.0.0.1\",\n" +
                "    \"local_port\": 1080,\n" +
                "    \"method\": \"aes-256-cfb\",\n" +
                "    \"password\": \"pyj1234%\",\n" +
                "    \"server\": \"0.0.0.0\",\n" +
                "    \"server_port\": 8388,\n" +
                "    \"test\": [\n" +
                "        \"hello\",\n" +
                "        \"world\"\n" +
                "    ],\n" +
                "    \"timeout\": 300,\n" +
                "    \"workers\": 1\n" +
                "}";

        JSONObject jsonObject = JSONObject.parseObject(data, JSONObject.class, Feature.OrderedField);

        JSONNode root = JSONTree.createDeduplicateJSONTree(jsonObject, "root", "#", 0);

        JSONObject schema = JSONObject.parseObject("{}", JSONObject.class, Feature.OrderedField);
        schema.fluentPut("definitions", "{}");
        schema.fluentPut("$schema", "http://json-schema.org/draft-07/schema#");
        assert root != null;
        createJSONSchema(root, schema, "");
        System.out.println(schema.toJSONString());

        List<JSONNode> list = JSONTree.depthFirstTraversal(root);

        assert list != null;
        for (JSONNode jsonNode : list) {
            System.out.printf("%" + (jsonNode.getLevel() * 4 + 1) + "s" + "%1$s%2$s%n", " ", jsonNode.getLevel() + "--" + jsonNode.getNodePath() + "--" + jsonNode.getDataType());
        }
    }
}
