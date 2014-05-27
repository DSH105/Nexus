package com.dsh105.nexus.server.handlers;

import com.dsh105.nexus.server.NexusServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RequestHandler extends AbstractHandler {

    private static final Logger logger = LogManager.getLogger("RequestHandler");

    private NexusServer nexusServer;

    public RequestHandler(final NexusServer nexusServer) {
        this.nexusServer = nexusServer;
    }

    @Override
    public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
       try {

           if (request.getMethod().equalsIgnoreCase("GET"))
               return;

           request.setCharacterEncoding("UTF-8");
           response.setHeader("Connection", "close");
           baseRequest.setHandled(true);
           response.setStatus(200);
           response.setContentType("text/plain");

           String userAgent = request.getHeader("User-Agent");

           if (userAgent.equalsIgnoreCase("Nexus")) {

           }

           JSONObject json = (JSONObject) JSONValue.parse("");

       } catch (Exception e) {

       }
    }
}
