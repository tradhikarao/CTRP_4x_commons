package gov.nih.nci.coppa.webservices.security;

import org.apache.catalina.connector.Request;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.message.token.UsernameToken;
import org.springframework.mock.web.DelegatingServletInputStream;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Parses username and password from WS-Security SOAP header using WSS4j.
 *
 * This implementation will attempt to parse any request that has a content type of
 * text/xml or application/xml.
 *
 * Since the request body must be read, it will replace the request input stream so that it can be read
 * downstream.
 *
 * CXF chokes on messages that specify mustUnderstand='1' for the WS-Security header because it wants to
 * handle the policy enforcement.  To work around this, this class will manipulate the mustUnderstand attribute.
 * If present, it will ensure that it is set to 0 so that CXF will not throw an exception when it tries to
 * handle the WS-Security header.
 *
 * @author Jason Aliyetti <jason.aliyetti@semanticbits.com>
 */
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.AvoidThrowingRawExceptionTypes" })
public class WsSecurityUsernamePasswordProvider implements UsernamePasswordProvider {
    private static final Set<String> CONTENT_TYPES;

    static {
        CONTENT_TYPES = new HashSet<String>();
        CONTENT_TYPES.add(MediaType.APPLICATION_XML);
        CONTENT_TYPES.add(MediaType.TEXT_XML);
    }

    /**
     * {@inheritDoc}
     *
     * TODO:  Validate plain text password, and not digest.
     */
    @SuppressWarnings("PMD")
    public BasicAuthInfo getBasicAuthInfo(Request request) {

        BasicAuthInfo result = null;
        String contentType = getContentType(request);

        if (contentType != null && CONTENT_TYPES.contains(contentType)) {
            try {
                byte[] payload = IOUtils.toByteArray(request.getInputStream());

                try {
                    Document doc = toDom(payload);
                    Element usernameTokenElement = getUsernameTokenElement(doc);

                    //if the header exists, parse it
                    if (usernameTokenElement != null) {
                        UsernameToken usernameToken = new UsernameToken(usernameTokenElement);

                        //set up the result
                        //only handle it if the password is plain text, otherwise, do not attempt to set it
                        if (WSConstants.PASSWORD_TEXT.equalsIgnoreCase(usernameToken.getPasswordType())) {
                            result = new BasicAuthInfo(usernameToken.getName(), usernameToken.getPassword());

                            //flip the "MustUnderstand" header property if set
                            handleMustUnderstand(usernameTokenElement);
                        }

                    }

                    payload = toBytes(doc);

                } catch (TransformerException e) {
                  throw new RuntimeException(e);
                } catch (ParserConfigurationException e) {
                    throw new RuntimeException(e);
                } catch (SAXException e) {
                    throw new RuntimeException(e);
                } finally {
                    //always make nice and restore the payload
                    request.setInputStream(new DelegatingServletInputStream(new ByteArrayInputStream(payload)));
                }

            } catch (IOException e) {

                //throw it since it means we couldn't read the request
                throw new RuntimeException(e);
            }
        }

        return result;
    }



    private void handleMustUnderstand(Element usernameTokenElement) {
        Attr mustUnderstandAttr
                = ((Element) usernameTokenElement.getParentNode())
                .getAttributeNodeNS(WSConstants.URI_SOAP11_ENV, WSConstants.ATTR_MUST_UNDERSTAND);

        if (mustUnderstandAttr != null) {
            mustUnderstandAttr.setValue(Integer.toString(0));
        }
    }

    private Document toDom(byte[] payload) throws IOException, SAXException, ParserConfigurationException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        DocumentBuilder documentBuilder = dbf.newDocumentBuilder();

        return documentBuilder.parse(new ByteArrayInputStream(payload));
    }

    private byte[] toBytes(Document doc) throws TransformerException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        StreamResult streamResult = new StreamResult(byteArrayOutputStream);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(new DOMSource(doc), streamResult);

        return byteArrayOutputStream.toByteArray();
    }

    private Element getUsernameTokenElement(Document doc) {
        Element result = null;

        NodeList usernameTokenElements
                = doc.getElementsByTagNameNS(WSConstants.WSSE_NS, WSConstants.USERNAME_TOKEN_LN);

        if (usernameTokenElements.getLength() > 0) {
            result = (Element) usernameTokenElements.item(0);
        }

        return result;
    }

    private String getContentType(Request request) {
        String contentType = ((HttpServletRequest) request).getContentType();

        if (StringUtils.contains(contentType, ";")) {
            contentType = StringUtils.substringBefore(contentType, ";"); // NOPMD
        }

        return contentType;
    }


}
