package gov.nih.nci.coppa.webservices.security;

/**
 * Basic username/password credentials.
 * @author Jason Aliyetti <jason.aliyetti@semanticbits.com>
 */
public class BasicAuthInfo {

    private String username;
    private String password;

    /**
     * Basic constructor.
     * @param username The username.
     * @param password The password.
     */
    public BasicAuthInfo(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     *
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     * @param username The username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     *
     * @return The password.
     */
    public String getPassword() {
        return password;
    }

    /**
     *
     * @param password The password.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
