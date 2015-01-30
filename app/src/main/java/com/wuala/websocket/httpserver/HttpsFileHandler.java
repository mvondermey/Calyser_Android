package com.wuala.websocket.httpserver;

import android.text.TextUtils;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Wang on 11/28/14.
 */
public class HttpsFileHandler extends AbstractHandler {

    private String webRoot;

    public HttpsFileHandler(final String webRoot) {
        this.webRoot = webRoot;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        // add logic

        // open database

        // search database via user id

        // create json / html

        String userID = baseRequest.getParameter("id");
        String tag = baseRequest.getParameter("tag");
        response.setContentType("text/html;charset=utf-8");
        baseRequest.setHandled(true);
        response.setStatus(HttpServletResponse.SC_OK);
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equals("browser")) {
                response.getWriter().println("<html><body><h1>Request successfully，return html</h1></body></html>");
            } else {
                response.getWriter().println("<html><body><h1>404 Error, No Authorize</h1></body></html>");
            }
        } else {
            if (!TextUtils.isEmpty(userID)) {
                response.getWriter().println("<html><body><h1>Return JSON Data</h1></body></html>");
            }
        }
    }
}