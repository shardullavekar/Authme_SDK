package haibison.android.lockpattern.server;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by parth on 25/4/16.
 */
public class PostRequest {
    String method = "GET";
    String url;
    Map<String, String> formBody;
    Callback callback;
    private String body;

    public PostRequest(String url, String method) {
        this.method = method;
        this.url = url;
    }

    public PostRequest(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getFormBody() {
        return formBody;
    }

    public void setFormBody(Map<String, String> formBody) {
        this.formBody = formBody;
    }

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
