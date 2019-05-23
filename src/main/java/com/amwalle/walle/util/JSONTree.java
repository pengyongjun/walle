package com.amwalle.walle.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class JSONTree {

    public static JSONNode createJSONTree(Object nodeData, String nodeName, String nodePath, int level) {
        if (nodeData == null) {
            return null;
        }

        JSONNode node = new JSONNode();
        node.setNodeName(nodeName);
        node.setNodePath(nodePath);
        node.setLevel(level);
        node.setData(nodeData);

        List<JSONNode> childrenList = new LinkedList<>();

        if (nodeData instanceof JSONObject) {
            node.setDataType("Object");

            JSONObject jsonObject = (JSONObject) nodeData;
            Set<String> keySet = jsonObject.keySet();

            level++;
            for (String key : keySet) {
                JSONNode childNode = createJSONTree(jsonObject.get(key), key, nodePath + "/" + key, level);
                childrenList.add(childNode);
            }

            node.setChildren(childrenList);
        } else if (nodeData instanceof JSONArray) {
            node.setDataType("Array");

            JSONArray jsonArray = (JSONArray) nodeData;

            for (int index = 0, length = jsonArray.length(); index < length; index++) {
                // Array 下面的内容，不是节点，不能按照节点来处理
                JSONNode childNode = createJSONTree(jsonArray.get(index), nodeName, nodePath + "[" + index + "]", level);
                childrenList.add(childNode);
            }

            node.setChildren(childrenList);
        } else {
            node.setChildren(null);
            node.setDataType(nodeData.getClass().getName());
        }

        return node;
    }

    public static List<JSONNode> levelTraversal(JSONNode jsonNode) {
        Queue<JSONNode> queue = new ConcurrentLinkedQueue<>();
        queue.add(jsonNode);

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

    public static void main(String[] args) {
        String data = "{\n" +
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
                "                    \"voucherId\": \"8475223900988\"\n" +
                "                }\n" +
                "            ]\n" +
                "        }";

        JSONObject jsonObject = new JSONObject(data);
        JSONNode root = JSONTree.createJSONTree(jsonObject, "root", "#", 0);
        List<JSONNode> list = JSONTree.levelTraversal(root);
        for (JSONNode jsonNode : list) {
            System.out.println(jsonNode.getNodePath() + "--" + jsonNode.getLevel());
        }
    }
}
