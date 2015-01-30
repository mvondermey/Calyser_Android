package com.wuala.websocket.httpserver;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class HttpZipHandler implements HttpRequestHandler {

    /**
     * 1M=1024*1024B
     */
    private static final int BUFFER_LENGTH = 1048576;

    private String webRoot;

    public HttpZipHandler(final String webRoot) {
        this.webRoot = webRoot;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response,
                       HttpContext context) throws HttpException, IOException {
        String target = request.getRequestLine().getUri();
        target = target.substring(0,
                target.length() - WebServer.SUFFIX_ZIP.length());
        final File file = new File(this.webRoot, target);
        HttpEntity entity = new EntityTemplate(new ContentProducer() {
            @Override
            public void writeTo(OutputStream outstream) throws IOException {
                zip(file, outstream);
            }
        });
        response.setStatusCode(HttpStatus.SC_OK);
        response.setHeader("Content-Type", "application/octet-stream");
        response.addHeader("Content-Disposition", "attachment;filename=" + file.getName() + ".zip");
        response.addHeader("Location", target);
        response.setEntity(entity);
    }

    /**
     * zip stream
     *
     * @param inputFile
     * @param outstream
     * @throws java.io.IOException
     */
    private void zip(File inputFile, OutputStream outstream) throws IOException {
        ZipOutputStream zos = null;
        try {
            // create zip stream
            zos = new ZipOutputStream(outstream);
            zip(zos, inputFile, inputFile.getName());
        } catch (IOException e) {
            throw e;
        }
        try {
            if (null != zos) {
                zos.close();
            }
        } catch (IOException e) {
        }
    }

    /**
     * zip file
     */
    private void zip(ZipOutputStream zos, File file, String base)
            throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            zos.putNextEntry(new ZipEntry(base + "/"));
            base = base.length() == 0 ? "" : base + "/";
            if (null != files && files.length > 0) {
                for (File f : files) {
                    zip(zos, f, base + f.getName());
                }
            }
        } else {
            zos.putNextEntry(new ZipEntry(base));
            FileInputStream fis = new FileInputStream(file);
            int count;
            byte[] buffer = new byte[BUFFER_LENGTH];

            while ((count = fis.read(buffer)) != -1) {
                zos.write(buffer, 0, count);
                zos.flush();
            }
            fis.close();
        }
    }

}
