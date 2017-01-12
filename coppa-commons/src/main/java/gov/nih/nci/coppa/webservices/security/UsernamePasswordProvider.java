package gov.nih.nci.coppa.webservices.security;


import org.apache.catalina.connector.Request;

/**
 * @author Jason Aliyetti <jason.aliyetti@semanticbits.com>
 */
public interface UsernamePasswordProvider {

    /**
     * Parses the given request for username and password.
     *
     * @param request The request to parse.
     * @return The {@link BasicAuthInfo} for the request, or null if it is not present.
     */
    BasicAuthInfo getBasicAuthInfo(Request request);
}
