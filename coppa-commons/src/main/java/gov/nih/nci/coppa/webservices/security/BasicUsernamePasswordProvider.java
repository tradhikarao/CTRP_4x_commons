package gov.nih.nci.coppa.webservices.security;

import org.apache.catalina.connector.Request;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 *
 * {@link UsernameTokenAuthenticator} implementation that uses the logic
 * from {@link org.apache.catalina.authenticator.BasicAuthenticator} to parse out the
 * username and password from the request.
 *
 * @author Jason Aliyetti <jason.aliyetti@semanticbits.com>
 */
public class BasicUsernamePasswordProvider implements UsernamePasswordProvider {

    @Override
    public BasicAuthInfo getBasicAuthInfo(Request request) {
        BasicAuthInfo result = null;



        String encodedUsernameToken = StringUtils.substringAfter(request.getHeader("authorization"), "Basic ");

        if (StringUtils.isNotBlank(encodedUsernameToken)) {

            byte[] decodedBytes = Base64.decodeBase64(encodedUsernameToken.getBytes());
            String decodedUsernameToken = new String(decodedBytes, StandardCharsets.UTF_8);

            String[] parts = decodedUsernameToken.split(":");
            result = new BasicAuthInfo(parts[0], parts[1]);
        }



        return result;
    }
}
