package com.dsh105.nexus.server.handlers;

import com.dsh105.nexus.server.NexusServer;
import com.dsh105.nexus.server.debug.Debugger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.simple.JSONObject;

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

            // if (request.getMethod().equalsIgnoreCase("GET"))
            //     return;

             request.setCharacterEncoding("UTF-8");
             response.setHeader("Connection", "close");
             baseRequest.setHandled(true);
             response.setStatus(200);
             response.setContentType("application/json");

             String userAgent = request.getHeader("User-Agent"); // Should be "Nexus"

             Debugger.getInstance().log(7, "Received a Request: " + baseRequest.getPathInfo());

             JSONObject json = new JSONObject();
             json.put("user-agent", userAgent);
             response.getWriter().println(json.toJSONString());;

        } catch (Exception e) {
            // Swallow it
            e.printStackTrace();
        }
    }
}
