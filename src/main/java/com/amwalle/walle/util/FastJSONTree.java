package com.amwalle.walle.util;

import com.alibaba.fastjson.*;
import com.alibaba.fastjson.parser.Feature;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FastJSONTree {

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

            for (int index = 0, length = jsonArray.size(); index < length; index++) {
                // Array 元素，不是单个节点；所以将元素下一级的孩子链表整个作为 Array 的孩子链表
                JSONNode childNode = createJSONTree(jsonArray.get(index), nodeName, nodePath + "[" + index + "]", level);
                if (childNode.getChildren() != null) {
                    childrenList.addAll(childNode.getChildren());
                }
            }

            node.setChildren(childrenList);
        } else {
            node.setChildren(null);
            node.setDataType(nodeData.getClass().getName());
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

            List<JSONNode> children = node.getChildren();
            if (children == null || children.size() == 0) {
                continue;
            }

            for (int index = children.size() - 1; index >= 0; index--) {
                stack.push(children.get(index));
            }
        }

        return nodeList;
    }

    public static void main(String[] args) {
        String data = "{\n" +
                "\t\"root\": {\n" +
                "        \"__expiredDate\": \"2019-03-27T03:00:00.000+08:00\",\n" +
                "        \"__startDate\": \"2019-03-27T00:00:00.000+08:00\",\n" +
                "        \"categoryId\": \"\",\n" +
                "        \"id\": \"5889257138\",\n" +
                "        \"itemList\": [\n" +
                "            {\n" +
                "                \"id\": \"8477823502701\",\n" +
                "                \"voucherId\": \"8477823502701\",\n" +
                "                \"aaa\": \"test\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"id\": \"8388402518266\",\n" +
                "                \"voucherId\": \"8388402518266\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"id\": \"8475223900988\",\n" +
                "                \"voucherId\": \"8475223900988\"\n" +
                "            }\n" +
                "        ]\n" +
                "\t}\n" +
                "}";

        JSONObject jsonObject = JSONObject.parseObject(data, JSONObject.class, Feature.OrderedField);

        System.out.println(jsonObject.toString());

        JSONNode root = FastJSONTree.createJSONTree(jsonObject, "root", "#", 0);

        List<JSONNode> list = FastJSONTree.depthFirstTraversal(root);
        for (JSONNode jsonNode : list) {
            System.out.println(jsonNode.getNodePath() + "--" + jsonNode.getLevel() + "--" + jsonNode.getDataType() + "--" + jsonNode.getData().toString());
        }
    }
}
