package com.amwalle.walle.controller;

import com.amwalle.walle.util.PDFConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;

@RestController
public class BookConverterController {
    private static final Logger logger = LoggerFactory.getLogger(BookConverterController.class);

    @RequestMapping(value = "/book-converter", method = RequestMethod.POST)
    public void handleUpload(HttpServletRequest request, HttpServletResponse response) throws IOException {
        MultiValueMap<String, MultipartFile> multiFileMap = ((StandardMultipartHttpServletRequest) request).getMultiFileMap();
        MultipartFile multipartFile = multiFileMap.getFirst("upload");

        assert multipartFile != null;
        File resource = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        logger.info("处理的文件： " + multipartFile.getOriginalFilename());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream(), StandardCharsets.UTF_8))) {
            BufferedWriter outWriter = new BufferedWriter(new FileWriter(resource));
            String line = reader.readLine();
            while (line != null) {
                outWriter.write(line);
                outWriter.write("\n");
                line = reader.readLine();
            }
            outWriter.flush();
        }

        String fileName = multipartFile.getOriginalFilename();
        assert fileName != null;
        fileName = fileName.substring(0, fileName.lastIndexOf("."));
        String outputFileName = URLEncoder.encode(fileName + "1" + ".html", "UTF-8");

        response.setContentType("application/x-download");
        response.setCharacterEncoding("UTF-8");
        response.addHeader("Content-Disposition", "attachment;filename=" + outputFileName);
        ServletOutputStream out = response.getOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);

        if (!multipartFile.getOriginalFilename().endsWith(".html")) {
            logger.info("文件不是HTML格式！");
            writer.write("请上传HTML文件");
            writer.flush();
            return;
        }

        File outFile = PDFConverter.convert(resource);
        try (Scanner scanner = new Scanner(outFile, "UTF-8");) {
            while (scanner.hasNextLine()) {
                writer.write(scanner.nextLine());
                writer.write("\n");
            }
        } catch (Exception e) {
            logger.info("处理出错");
            writer.write("处理出错了！");
            writer.write(e.getMessage());
            writer.flush();
            outFile.deleteOnExit();
            return;
        }

        writer.flush();
        outFile.deleteOnExit();
        logger.info("文件处理成功！");
    }
}
