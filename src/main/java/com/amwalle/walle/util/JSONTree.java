package com.amwalle.walle.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class JSONTree {

    private static HashSet<String> pathSet = new HashSet<>();
    private static HashMap<String, JSONNode> arrayNode = new HashMap<>();

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

            // 判断节点是否已经存在，如果已经存在，并且 arrayChildrenList 不为空，
            // 那么将 arrayChildrenList 加到已存在节点的 child 中，并舍弃当前正在生成的节点
            if (arrayNode.get(arrayItem.getNodePath()) != null) {
                if (!arrayChildrenList.isEmpty()) {
                    arrayNode.get(arrayItem.getNodePath()).getChildren().addAll(arrayChildrenList);
                }

                return null;
            }

            arrayItem.setChildren(arrayChildrenList);
            childrenList.add(arrayItem);
            node.setChildren(childrenList);

            arrayNode.put(arrayItem.getNodePath(), arrayItem);
        } else {
            // 针对叶子节点，如果路径已经存在，则舍弃该节点
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
            jsonObject.fluentPut("required", new JSONArray());
            jsonObject.fluentPut("properties", schema);
        } else if (NodeType.Array.getJsonType().equals(jsonNode.getDataType())) {
            jsonObject.fluentPut("items", schema.get("items"));
        } else {
            jsonObject.fluentPut("pattern", "^(.*)$");
        }

    }

    public static void main(String[] args) {
        String data = "{\n" +
                "    \"datas\": [\n" +
                "        {\n" +
                "            \"__expiredDate\": \"2019-03-27T03:00:00.000+08:00\",\n" +
                "            \"__startDate\": \"2019-03-27T00:00:00.000+08:00\",\n" +
                "            \"categoryId\": \"\",\n" +
                "            \"id\": \"5889257138\",\n" +
                "            \"itemList\": [\n" +
                "                {\n" +
                "                    \"id\": \"8477823502701\",\n" +
                "                    \"voucherId\": \"8477823502701\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"id\": \"8388402518266\",\n" +
                "                    \"voucherId\": \"8388402518266\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"id\": \"8475223900988\",\n" +
                "                    \"voucherId\": \"8475223900988\",\n" +
                "                    \"test\": \"test\"\n" +
                "                }\n" +
                "            ]\n" +
                "        },\n" +
                "        {\n" +
                "            \"__expiredDate\": \"2019-03-27T06:00:00.000+08:00\",\n" +
                "            \"__startDate\": \"2019-03-27T03:00:00.000+08:00\",\n" +
                "            \"categoryId\": \"\",\n" +
                "            \"id\": \"5889257139\",\n" +
                "            \"itemList\": [\n" +
                "                {\n" +
                "                    \"id\": \"8395001966626\",\n" +
                "                    \"voucherId\": \"8395001966626\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"id\": \"8429604672093\",\n" +
                "                    \"voucherId\": \"8429604672093\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"id\": \"8387403610830\",\n" +
                "                    \"voucherId\": \"8387403610830\",\n" +
                "                    \"hello\": \"world\"\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ],\n" +
                "    \"moduleId\": \"flashVoucher\",\n" +
                "    \"pageId\": \"100556902\",\n" +
                "    \"resourceCode\": \"icms-zebra-100556902-4106311\",\n" +
                "    \"tagId\": \"timeList\"\n" +
                "}";

        JSONObject jsonObject = JSONObject.parseObject(data, JSONObject.class, Feature.OrderedField);

        JSONNode root = JSONTree.createDeduplicateJSONTree(jsonObject, "root", "#", 0);

        JSONObject schema = JSONObject.parseObject("{}", JSONObject.class, Feature.OrderedField);
        schema.fluentPut("definitions", new JSONObject());
        schema.fluentPut("$schema", "http://json-schema.org/draft-07/schema#");
        assert root != null;
        createJSONSchema(root, schema, "");
        schema.put("$id", "http://example.com/root.json");
        System.out.println(JSON.toJSONString(schema, true));

        System.out.println();
        List<JSONNode> list = JSONTree.depthFirstTraversal(root);

        assert list != null;
        for (JSONNode jsonNode : list) {
            System.out.printf("%" + (jsonNode.getLevel() * 4 + 1) + "s" + "%1$s%2$s%n", " ", jsonNode.getLevel() + "--" + jsonNode.getNodePath() + "--" + jsonNode.getDataType());
        }
    }
}
