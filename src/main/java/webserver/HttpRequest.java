package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private String method;
    private String path;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> params = new HashMap<>();
    private String body;

    //http://localhost:8080/user/create?userId=heell&password=1234&name=asdf&email=asdf%40asdf.com
    public HttpRequest(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        String requestLine = br.readLine();
        if (requestLine == null) return;

        String[] tokens = requestLine.split(" ");
        method = tokens[0];
        parsePath(tokens);

        String line;
        while ((line = br.readLine()) != null && !line.isEmpty()) {
            String[] headerParts = line.split(": ", 2);
            headers.put(headerParts[0], headerParts[1]);
        }

        if(getContentLength() > 0) {
            char[] bodyChars = new char[getContentLength()];
            br.read(bodyChars);
            body = new String(bodyChars);
            parseParams(body);
        }
    }

    private void parsePath(String[] tokens) {
        String pathAndQuery = tokens[1];
        String[] pathQuerySplit = pathAndQuery.split("\\?");
        path = pathQuerySplit[0];
    }

    private void parseParams(String body) {
        for (String pair : body.split("&")) {
            String[] keyValue = pair.split("=", 2);
            String key = keyValue[0];
            String value = keyValue.length > 1
                    ? URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8)
                    : "";
            params.put(key, value);
        }
    }



    private int getContentLength() {
        String length = headers.get("Content-Length");
        return length != null ? Integer.parseInt(length) : 0;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHeader(String name) { return headers.get(name); }
    public String getParam(String name) { return params.get(name); }

    public String getBody() {
        return body;
    }
}
