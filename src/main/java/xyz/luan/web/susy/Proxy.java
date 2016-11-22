package xyz.luan.web.susy;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class Proxy extends HttpServlet {

    private static final String SUZY = "https://susy.ic.unicamp.br:9999";

    private static final Map<String, Request> results = new HashMap<>();

    private static class Request {
        String data, contentType;

        public Request(URLConnection c) {
            this.data = readAll(c);
            this.contentType = c.getHeaderField("Content-Type");
        }

        public void response(HttpServletResponse resp) throws IOException {
            resp.setContentType(contentType);
            resp.getWriter().println(data);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = SUZY + request.getRequestURI() + (request.getQueryString() == null ? "" : "?" + request.getQueryString());
        Request req = getRequest(path);
        req.response(response);
    }

    private Request getRequest(String path) throws IOException {
        if (results.containsKey(path)) {
            return results.get(path);
        }
        return fetchRequest(path);
    }

    private Request fetchRequest(String path) throws IOException {
        URL url = new URL(path);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setConnectTimeout(30000);
        return new Request(urlConnection);
    }

    private static String readAll(URLConnection urlConnection) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"))) {
            String lines = "", line;

            while ((line = reader.readLine()) != null) {
                lines += line + "\n";
            }
            return lines;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
