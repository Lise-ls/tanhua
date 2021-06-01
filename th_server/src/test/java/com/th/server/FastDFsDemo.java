package com.th.server;

/*
 * @author Lise
 * @date 2021年05月30日 21:15
 * @program: th
 */

import com.th.server.util.FastDFSClientUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FastDFsDemo {

    @Autowired
    private FastDFSClientUtil fastDFSClientUtil;


    @Value("${fdfs.web-server-url}")
    private String fileServerUrl;

    //上传图片
    @Test
    public void upload() throws IOException {
        String path="C:\\Users\\Lise\\Desktop\\123.jpg";
        File file=new File(path);
        MultipartFile multipartFile=new CommonsMultipartFile(createFileItem(file));
        String fileId = fastDFSClientUtil.uploadFile(multipartFile);
        System.out.println(fileServerUrl+fileId);
    }


    //file转MultipartFile
    private FileItem createFileItem(File file) {
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        FileItem item = factory.createItem("textField", "text/plain", true, file.getName());
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        try {
            FileInputStream fis = new FileInputStream(file);
            OutputStream os = item.getOutputStream();
            while ((bytesRead = fis.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return item;

    }
}
