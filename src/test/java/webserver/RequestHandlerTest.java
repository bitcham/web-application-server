package webserver;

import junit.framework.TestCase;
import org.junit.Test;

public class RequestHandlerTest extends TestCase {

    @Test
    public void testParseRegisterRequest(){
        String requestLine = "GET /user/create?userId=heell&password=1234&name=asdf&email=asdf%40asdf.com HTTP/1.1";
        String[] tokens = requestLine.split(" ");
        assertEquals("GET", tokens[0]);
        assertEquals("/user/create?userId=heell&password=1234&name=asdf&email=asdf%40asdf.com", tokens[1]);
        assertEquals("HTTP/1.1", tokens[2]);

        String pathAndQuery = tokens[1];
        String[] pathQuerySplit = pathAndQuery.split("\\?");
        assertEquals("/user/create", pathQuerySplit[0]);
        assertEquals("userId=heell&password=1234&name=asdf&email=asdf%40asdf.com", pathQuerySplit[1]);

        String queryString = pathQuerySplit[1];
        String[] params = queryString.split("&");
        assertEquals(4, params.length);
        assertEquals("userId=heell", params[0]);
        assertEquals("password=1234", params[1]);
        assertEquals("name=asdf", params[2]);
        assertEquals("email=asdf%40asdf.com", params[3]);
    }

}