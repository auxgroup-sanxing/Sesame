package com.sanxing.sesame.wtc;

import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.codec.FormatException;
import com.sanxing.sesame.binding.codec.XMLResult;
import com.sanxing.sesame.codec.impl.DecodeFSV;
import com.sanxing.sesame.codec.impl.validate.ValidateVFormat;
import com.sanxing.sesame.codec.util.CodecUtil;
import com.sanxing.sesame.codec.util.HexBinary;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class WTCDecoder extends DecodeFSV
{
  private static final Logger LOG = LoggerFactory.getLogger(WTCDecoder.class);

  private String output(Element element) {
    XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    return outputter.outputString(element);
  }

  public void decode(BinarySource inputSource, XMLResult output)
    throws FormatException
  {
    XmlSchema schema = inputSource.getXMLSchema();
    InputStream stream = inputSource.getInputStream();
    String charset = inputSource.getEncoding();
    if (schema == null) {
      LOG.error("In WTCDecoder,can not find schema in BinarySource!");
      throw new FormatException(
        "In WTCDecoder,can not find schema in BinarySource!");
    }
    XmlSchemaElement schemaEl = inputSource.getRootElement();
    try
    {
      int total = stream.available();
      int headLen = 107;
      byte[] head = new byte[headLen];
      stream.read(head);
      Element headEle = decodeHead(head, headLen, charset);

      int length = total - headLen;
      if (length <= 0) {
        throw new FormatException(
          "In WTCDecoder, not enough bytes can be read.");
      }

      byte[] bytes = new byte[length];
      stream.read(bytes);
      Element element = decode(bytes, length, charset, schemaEl, schema);
      element.addContent(headEle);
      element.detach();

      if (LOG.isInfoEnabled()) {
        LOG.info("WTC:after decode: ");
        LOG.info("\n" + output(element));
      }

      output.setDocument(new Document(element));
    } catch (Exception e) {
      throw new FormatException(e.getMessage(), e);
    }
  }

  public Element decode(byte[] message, int length, String charset, XmlSchemaElement schemaElement, XmlSchema schema) throws FormatException
  {
    if (length > message.length)
      throw new FormatException("decode,parameter length:[" + length + "] bigger then real buffer length:[" + message.length + "] error!");
    ByteBuffer msgBuf = ByteBuffer.wrap(message);

    msgBuf.limit(length);
    Element root = null;
    try
    {
      if (schemaElement == null)
        throw new FormatException("schema is null!");
      XmlSchemaType xsdType = schemaElement.getSchemaType();
      if (!(xsdType instanceof XmlSchemaComplexType)) {
        throw new FormatException("in xsdDoc,can not find the child element:[complexType]");
      }
      root = new Element(schemaElement.getName());

      Iterator elements = CodecUtil.getElements(xsdType);
      decodeMessage(msgBuf, elements, charset, root, schema);
    } catch (FormatException e) {
      e.printStackTrace();
      throw e;
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new FormatException(e.getMessage(), e);
    }
    return root;
  }

  public Element decodeHead(byte[] message, int length, String charset) throws FormatException
  {
    try
    {
      String rootName = "header";
      Element root = new Element(rootName);
      Element from = new Element("channel_from");
      Element to = new Element("channel_to");
      Element pinflag = new Element("pin_flag");
      Element pinseed = new Element("pin_seed");
      byte[] fromBytes = new byte[3];
      byte[] toBytes = new byte[3];
      byte[] pinflagBytes = new byte[1];
      byte[] pinseedBytes = new byte[16];
      System.arraycopy(message, 38, 
        fromBytes, 0, 3);
      System.arraycopy(message, 41, toBytes, 
        0, 3);
      System.arraycopy(message, 45, 
        pinflagBytes, 0, 1);
      System.arraycopy(message, 22, 
        pinseedBytes, 0, 16);
      String fromText = new String(fromBytes, charset);
      String toText = new String(toBytes, charset);
      String pinflagText = new String(pinflagBytes, charset);
      String pinseedText = new String(pinseedBytes, charset);
      if (isNull(fromBytes)) {
        fromText = "";
      }
      if (isNull(toBytes)) {
        toText = "";
      }
      if (isNull(pinflagBytes)) {
        pinflagText = "";
      }
      if (isNull(pinseedBytes)) {
        pinseedText = "";
      }
      from.setText(fromText);
      to.setText(toText);
      pinflag.setText(pinflagText);
      pinseed.setText(pinseedText);
      root.addContent(from);
      root.addContent(to);
      root.addContent(pinflag);
      root.addContent(pinseed);
      return root;
    } catch (UnsupportedEncodingException e) {
      throw new FormatException(e.getMessage(), e);
    }
  }

  public boolean skipElement(String elementName)
  {
    if ((elementName.equalsIgnoreCase("channel_from")) || 
      (elementName.equalsIgnoreCase("channel_to"))) {
      return true;
    }
    return false;
  }

  public void decodeVField(ByteBuffer message, String charset, Element elementMessage, String elementName, Element childOfElementMessage, String type, ValidateVFormat vVFormat)
    throws FormatException
  {
    int headLength = vVFormat.getHeadLength();

    int headBlank = vVFormat.getHeadBlank();

    String headAlign = vVFormat.getHeadAlign();

    int headRadix = vVFormat.getHeadRadix();

    String value = decodeVField(message, charset, elementName, type, 
      headLength, headBlank, headAlign, headRadix);

    childOfElementMessage.addContent(value);
    elementMessage.addContent(childOfElementMessage);
  }

  private String decodeVField(ByteBuffer message, String charset, String elementName, String type, int headLength, int headBlank, String headAlign, int headRadix)
    throws FormatException
  {
    Element tempElement = new Element(elementName);
    decodeHead(message, charset, tempElement, headRadix, headLength, 
      headBlank, headAlign);
    String lengthString = tempElement.getValue();
    String value = "";
    LOG.debug("lengthString=" + lengthString);
    if (lengthString != null) {
      if (lengthString.length() < 1) {
        return "";
      }
      int length = 0;
      if (headRadix != 2)
        length = Integer.parseInt(lengthString, headRadix);
      else {
        length = Integer.parseInt(lengthString, 16);
      }
      LOG.debug("lengthString=" + lengthString + "; length=" + length);
      if (length <= 250) {
        value = getField(message, charset, length, type);
      } else {
        value = getField(message, charset, 
          250, type);
        length -= 250;
        value = value.concat(decodeVField(message, charset, 
          elementName, type, headLength, headBlank, 
          headAlign, headRadix));
      }
    }

    return value;
  }

  private String getField(ByteBuffer message, String charset, int length, String type) throws FormatException
  {
    byte[] temp = new byte[length];
    message.get(temp);
    try
    {
      String value;
      String value;
      if ("string".equals(type))
        value = new String(temp, charset);
      else {
        value = HexBinary.encode(temp);
      }
      LOG.debug("value=" + value);
      return value;
    } catch (UnsupportedEncodingException e) {
      throw new FormatException(e.getMessage(), e);
    }
  }

  public boolean isNull(byte[] bytes) {
    if (bytes == null) {
      return true;
    }
    for (int i = 0; i < bytes.length; i++) {
      if (bytes[i] != 0) {
        return false;
      }
    }
    return true;
  }

  public static void main(String[] args)
  {
    byte[] bytes = { 77, 65, 67, 32, -48, -93, 
      -47, -23, 67, 108, 105, 101, 110, 116, 
      32, 77, 65, 67, -54, -89, -80, 
      -36 };

    String charset = "GBK";
    try {
      System.out.println(new String(bytes, charset));
      byte b = 32;
      System.out.println(Integer.toHexString(b & 0xFF));

      byte[] temp = { 32 };
      int blank = 32;
      if ((blank ^ temp[0]) != 0)
        System.out.println("1");
      else {
        System.out.println("2");
      }
      String result = HexBinary.encode(temp);
      System.out.println(result);
    }
    catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }
}



