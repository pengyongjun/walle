package com.amwalle.walle.util;

import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PDFConverter {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入要转换的文件路径： ");
        String filePath = scanner.nextLine();
        System.out.println(filePath);
        convert(filePath);
    }

    public enum HeaderLevel {
        Part("H1", ".*第.*部分.*"),
        Chapter("H2", "(.*第.*章.*)|(.*[1-9].*)");

        private String headerLevel;
        private String expression;

        HeaderLevel(String headerLevel, String expression) {
            this.headerLevel = headerLevel;
            this.expression = expression;
        }

        public static String getHeaderLevel(String input) {
            if (StringUtils.isEmpty(input) || (!input.contains("</a>") && !input.contains("<p class=\"calibre1\">"))) {
                return null;
            }

            String dummy = preHandleHeader(input);

            for (HeaderLevel level : HeaderLevel.values()) {
                Pattern pattern = Pattern.compile(level.expression);
                Matcher matcher = pattern.matcher(dummy);

                if (matcher.find()) {
                    return level.headerLevel;
                }
            }

            return null;
        }

        private static String preHandleHeader(String input) {
            String dummy = input;
            if (input.contains("</a>") && input.contains("</p>")) {
                dummy = dummy.substring(dummy.indexOf("</a>") + 4, dummy.indexOf("</p>"));
            } else if (input.contains("<p class=\"calibre1\">") && input.contains("</p>")) {
                dummy = dummy.substring(dummy.indexOf("<p class=\"calibre1\">") + 20, dummy.indexOf("</p>"));
            }

            dummy = dummy.replaceAll("<b class=\"calibre3\"> </b>", "");
            dummy = dummy.replaceAll("<b class=\"calibre3\">", "");
            dummy = dummy.replaceAll(" </b>", "");
            dummy = dummy.replaceAll("</b>", "");
            dummy = removeSpace(dummy);
            return dummy;
        }

        public String getHeaderLevel() {
            return headerLevel;
        }

        public void setHeaderLevel(String headerLevel) {
            this.headerLevel = headerLevel;
        }

        public String getExpression() {
            return expression;
        }

        public void setExpression(String expression) {
            this.expression = expression;
        }
    }

    public static void convert(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.isFile() || !file.canRead() || !file.getName().endsWith(".html")) {
            System.out.println("请输入正确的html文件路径，并保证文件可读！");
        }

        String title = file.getName();
        title = title.replace(".html", "");

        File outFile = new File(filePath.replace(".html", "1.html"));
        if (outFile.exists()) {
            outFile.delete();
            outFile.createNewFile();
        }

        try (Scanner scanner = new Scanner(file, "UTF-8"); BufferedWriter outWriter = new BufferedWriter(new FileWriter(outFile))) {
            Set<String> catalog = new HashSet<>();

            StringBuffer output = new StringBuffer();
            boolean isIndentationNeeded = false;
            int count = 0;
            while (scanner.hasNextLine()) {
                count++;
                // 有些文件太大，需要在中间存一下文件
                if (count >= 10000) {
                    outWriter.append(output);
                    count = 0;
                    output.delete(0, output.length());
                }

                String line = scanner.nextLine();

                // 目录前面的内容（封面、版权等）
                if (catalog.isEmpty() && !line.startsWith("<p class=\"calibre1\"><a href=\"")) {
                    output.append(line).append("\n");
                    continue;
                }

                // 处理目录：目录数据存set用于后续增加标题格式
                if (line.endsWith("</a></p>")) {
                    if (catalog.isEmpty()) {
                        output.delete(output.length() - 1, output.length());
                        String header = output.toString();
                        String catalogTitle = header.substring(header.lastIndexOf("\n") + 1);
                        // 格式化目录标题，增加目录前的分页
                        if (catalogTitle.contains("目录") || catalogTitle.contains("Contents")) {
                            catalogTitle = "<h1>" + catalogTitle + "</h1>" + "\n";
                            output.delete(header.lastIndexOf("\n") + 1, output.length());
                            output.append(addPagination(title));
                            output.append(catalogTitle);
                        } else {
                            output.append("\n");
                            output.append(addPagination(title));
                        }
                    }

                    line = line.replaceAll("part0000\\.html", "");
                    output.append(line).append("\n");

                    String temp = line.substring(line.lastIndexOf("\">") + 2, line.indexOf("</a></p>"));
                    temp = removeSpace(temp);
                    catalog.add(temp);

                    continue;
                }

                if (StringUtils.isEmpty(line)) {
                    output.append("\n");
                    continue;
                }

                // 处理章节标题
                if (isLineATitle(line, catalog)) {
                    isIndentationNeeded = true;

                    String titleLevel = HeaderLevel.getHeaderLevel(line);
                    if (HeaderLevel.Part.headerLevel.equals(titleLevel)) {
                        output.append(addPagination(title));
                        output.append("<h1>").append(line).append("</h1>").append("\n");
                        continue;
                    }

                    if (HeaderLevel.Chapter.headerLevel.equals(titleLevel)) {
                        output.append(addPagination(title));
                        output.append("<h2>").append(line).append("</h2>").append("\n");
                        continue;
                    }

                    output.append("<h3>").append(line).append("</h3>").append("\n");
                    continue;
                }

                // 首行缩进
                if (isIndentationNeeded) {
                    line = line.replace("<p class=\"calibre1\">", "<p class=\"calibre1\" style=\"text-indent:2em;\">");
                    line = line.replace("</p>", "");
                    output.append(line).append("\n");
                    isIndentationNeeded = false;
                    continue;
                }

                // 文本内容分段
                if (isParagraphEnd(line)) {
                    isIndentationNeeded = true;
                    line = line.replace("<p class=\"calibre1\">", "");
                    line = line.replace("</p>", "");
                    output.append(line).append("\n");
                    continue;
                }

                // 普通行处理：去掉换行
                if (line.startsWith("<p class=\"calibre1\">") && line.endsWith("</p>")) {
                    line = line.replace("<p class=\"calibre1\">", "");
                    line = line.replace("</p>", "");
                    output.append(line);
                    continue;
                }

                output.append(line).append("\n");
            }
            outWriter.append(output);

            System.out.println("********************************************");
            System.out.println("Done! 生成的新文件路径： ");
            System.out.println(outFile.getPath());
        } catch (FileNotFoundException e) {
            System.out.println("File convert failed!");
            e.printStackTrace();
        }
    }

    private static String removeSpace(String input) {
        String result = input.trim().replaceAll("\\.", "").replaceAll("·", "");
        result = result.replaceAll("\\s*", "").replaceAll("\\u00a0", "").replaceAll((char) 12288 + "", "");

        return result;
    }

    private static String addPagination(String title) {
        return "\n" +
                "</body>\n" +
                "</html>\n" +
                "<html xml:lang=\"zh-cn\" xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                "<head>\n" +
                "<title>" + title + "</title>\n" +
                "  <link rel=\"stylesheet\" type=\"text/css\" href=\"../styles/0002.css\"/>\n" +
                "  <link rel=\"stylesheet\" type=\"text/css\" href=\"../styles/0001.css\"/>\n" +
                "</head>\n" +
                "<body style=\"font-family:songti;\">\n" +
                "\n";
    }

    private static boolean isLineATitle(String input, Set<String> catalog) {
        if (StringUtils.isEmpty(input) || (!input.contains("</a>") && !input.contains("<p class=\"calibre1\">"))) {
            return false;
        }

        String dummy = HeaderLevel.preHandleHeader(input);

        for (String catalogEntry : catalog) {
            if (StringUtils.isEmpty(catalogEntry)) {
                continue;
            }

            if (catalogEntry.equals(dummy)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isParagraphEnd(String input) {
        Pattern pattern = Pattern.compile(".*(\\w</p>)$");
        Matcher matcher = pattern.matcher(input);
        if (matcher.matches()) {
            return false;
        }

        // 句子结尾的标点符号：./!/?/……/"」 （英文+中文）
        Pattern pattern1 = Pattern.compile(".*(([\\.|!|\\?|\"|\\u3002|\\uff01|\\uff1f|\\u2026|\\u201d|\\u300d])|(\\. ))</p>$");
        Matcher matcher1 = pattern1.matcher(input);
        return matcher1.matches();
    }
}
