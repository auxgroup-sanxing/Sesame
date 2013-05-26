package com.sanxing.sesame.wssecurity.commons;

import javax.xml.namespace.QName;

public abstract interface Constants
{
    public static final QName SOAP_CLIENT_FAULT_CODE =
        new QName( "http://schemas.xmlsoap.org/soap/envelope/", "Client" );

    public static final QName SOAP_SERVER_FAULT_CODE =
        new QName( "http://schemas.xmlsoap.org/soap/envelope/", "Server" );

    public static final String WS_SECURITY_NS_URI =
        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

    public static final String WS_SECURITY_PREF_NS_PREFIX = "wsse";

    public static final String WS_SECURITY_SECURITY_TAG = "Security";

    public static final QName WS_SECURITY_SECURITY_QNAME = new QName(
        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security" );

    public static final String WS_UTIL_NS_URI =
        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    public static final String WS_UTIL_PREF_NS_PREFIX = "wsu";

    public static final String WS_UTIL_ID_ATTR_NAME = "Id";

    public static final String WS_SECURITY_USERNAMETOKEN_TAG = "UsernameToken";

    public static final String WS_SECURITY_USERNAME_TAG = "Username";

    public static final String WS_SECURITY_PASSWORD_TAG = "Password";

    public static final String WS_SECURITY_PASSWORD_TYPE_ATTR = "Type";

    public static final String WS_SECURITY_TEXT_PASSWORD_TYPE =
        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd#PasswordText";

    public static final String WS_SECURITY_DIGEST_PASSWORD_TYPE =
        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd#PasswordDigest";

    public static final String WS_SECURITY_NONCE_TAG = "Nonce";

    public static final String WS_SECURITY_CREATED_TIME_TAG = "Created";

    public static final String WS_SECURITY_BINARY_TOKEN_TAG = "BinarySecurityToken";

    public static final String WS_SECURITY_VALUE_TYPE_ATTR = "ValueType";

    public static final String WS_SECURITY_ENCODING_TYPE_ATTR = "EncodingType";

    public static final String WS_SECURITY_BASE64_ENCODING_TYPE =
        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary";

    public static final String WS_SECURITY_TOKEN_REF_TAG = "SecurityTokenReference";

    public static final String WS_SECURITY_REF_TAG = "Reference";

    public static final String WS_SECURITY_REF_URI_ATTR = "URI";

    public static final QName WS_SECURITY_INVALID_SECURITY_FAULT_CODE = new QName(
        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "InvalidSecurity" );

    public static final QName WS_SECURITY_FAILED_AUTH_FAULT_CODE = new QName(
        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "FailedAuthentication" );

    public static final QName WS_SECURITY_UNSUPPORTED_ALGO_FAULT_CODE = new QName(
        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "UnsupportedAlgorithm" );

    public static final String WS_XML_SECURITY_SIGN_TAG = "Signature";

    public static final String SAML_ASSERTION_NS_URI = "urn:oasis:names:tc:SAML:1.0:assertion";

    public static final String SAML_ASSERTION_TAG = "Assertion";

    public static final String WS_ADDRESSING_NS_URI = "http://schemas.xmlsoap.org/ws/2004/08/addressing";

    public static final String WS_ADDRESSING_PREF_NS_PREFIX = "wsa";

    public static final String WS_ADDRESSING_TO_TAG = "To";

    public static final String WS_ADDRESSING_ACTION_TAG = "Action";

    public static final QName WS_ADDRESSING_TO_QNAME = new QName( "http://schemas.xmlsoap.org/ws/2004/08/addressing",
        "To" );

    public static final QName WS_ADDRESSING_ACTION_QNAME = new QName(
        "http://schemas.xmlsoap.org/ws/2004/08/addressing", "Action" );

    public static final String TO_ADDRESS_MSG_CONTEXT_PROPERTY = "mc_to_address";

    public static final String DEST_ACTION_URI_MSG_CONTEXT_PROPERTY = "mc_dest_action";

    public static final String HTTP_SOAP_ACTION_HEADER = "SOAPAction";

    public static final String OUR_SECURITY_COP_URI = "http://manning.com/xmlns/samples/soasecimpl/cop";

    public static final String USERNAME_MSG_CONTEXT_PROPERTY = "mc_username";

    public static final String PASSWORD_MSG_CONTEXT_PROPERTY = "mc_password";

    public static final String PASSWORD_TYPE_MSG_CONTEXT_PROPERTY = "mc_password_type";

    public static final String NONCE_MSG_CONTEXT_PROPERTY = "mc_nonce";

    public static final String CREATION_TIME_STR_MSG_CONTEXT_PROPERTY = "mc_creation_time_as_string";

    public static final String GSS_TOKEN_MSG_CONTEXT_PROPERTY = "mc_gss_token";

    public static final String AUTHENTICATED_SUBJECT_MSG_CONTEXT_PROPERTY = "mc_authenticatedSubject";

    public static final String AUTHENTICATION_METHOD_MSG_CONTEXT_PROPERTY = "mc_authentication_method";

    public static final String SERVICE_GSS_NAME_MSG_CONTEXT_PROPERTY = "mc_kerberosNameOfTargetService";

    public static final String KERBEROS5_OID = "1.2.840.113554.1.2.2";

    public static final String WS_SECURITY_KERBEROS5_BASE_URI =
        "http://www.docs.oasis-open.org/wss/2004/07/oasis-000000-wss-kerberos-token-profile-1.0";

    public static final String WS_SECURITY_KERBEROS5_AP_REQ_TYPE =
        "http://www.docs.oasis-open.org/wss/2004/07/oasis-000000-wss-kerberos-token-profile-1.0#Kerberosv5_AP_REQ";

    public static final String WS_SECURITY_X509_PROFILE_BASE_URI =
        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

    public static final String WS_SECURITY_X509V3_CERT_TOKEN_TYPE =
        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3";

    public static final String WS_SECURITY_X509V3_CERT_CHAIN_TOKEN_TYPE =
        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd#X509PKIPathv1";

    public static final String X509_CERTIFICATE_TYPE = "X.509";

    public static final String CERTIFICATION_CHAIN_ENCODING = "PkiPath";
}