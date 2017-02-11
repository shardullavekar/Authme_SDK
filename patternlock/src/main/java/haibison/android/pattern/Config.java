package haibison.android.pattern;

/**
 * Created by shardullavekar on 07/08/16.
 */
public class Config {
    public static final String HOST = "http://authme.io";
    public static final String WEB_URL = HOST + "";
    public static final String SERVER_URL = HOST + ":3000/";
    public static final String XMPP_HOST = "authme.io";
    public static final String XMPP_HOST_IP = "authme.io";

    private static String api_key;
    private static String emailId;

    public String getApi_key() {
        return api_key;
    }

    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }
}
