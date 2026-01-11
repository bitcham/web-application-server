package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest request = new HttpRequest(in);
            DataOutputStream dos = new DataOutputStream(out);

            String method = request.getMethod();

            if ("GET".equals(method)) {
                handleGet(request, dos);
            } else if ("POST".equals(method)) {
                handlePost(request, dos);
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void handleGet(HttpRequest request, DataOutputStream dos) throws IOException {
        String path = request.getPath();
        byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    private void handlePost(HttpRequest request, DataOutputStream dos) {
        String path = request.getPath();

        if ("/user/create".equals(path)) {
            User user = new User(
                    request.getParam("userId"),
                    request.getParam("password"),
                    request.getParam("name"),
                    request.getParam("email")
            );

            DataBase.addUser(user);

            log.debug("User created: {}", user);

            response302Header(dos, "/index.html");
            responseBody(dos, new byte[]{});
            return;
        }

        if("/user/login".equals(path)) {
            String userId = request.getParam("userId");
            String password = request.getParam("password");

            User user = DataBase.findUserById(userId);

            if(user != null && user.getPassword().equals(password)) {
                try {
                    byte[] body = Files.readAllBytes(new File("./webapp/index.html").toPath());
                    response200WithCookie(dos, body, "logined=true");
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            } else{
                String location = "/user/login_failed.html";
                response302WithCookie(dos, location, "logined=false");
                responseBody(dos, new byte[]{});
            }
        }
    }

    private void response200WithCookie(DataOutputStream dos, byte[] body, String cookie) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK\r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + body.length + "\r\n");
            dos.writeBytes("Set-Cookie: " + cookie + "\r\n");
            dos.writeBytes("\r\n");  // 헤더 끝
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302WithCookie(DataOutputStream dos, String location, String cookie) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found\r\n");
            dos.writeBytes("Location: " + location + "\r\n");
            dos.writeBytes("Set-Cookie: " + cookie + "\r\n");
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String location) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found\r\n");
            dos.writeBytes("Location: " + location + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
