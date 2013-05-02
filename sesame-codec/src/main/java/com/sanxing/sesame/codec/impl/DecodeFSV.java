package com.sanxing.sesame.codec.impl;

import com.sanxing.sesame.binding.codec.BinarySource;
import com.sanxing.sesame.binding.codec.Decoder;
import com.sanxing.sesame.binding.codec.FormatException;
import com.sanxing.sesame.binding.codec.XMLResult;
import com.sanxing.sesame.codec.impl.validate.ValidateFFormat;
import com.sanxing.sesame.codec.impl.validate.ValidateField;
import com.sanxing.sesame.codec.impl.validate.ValidateVFormat;
import com.sanxing.sesame.codec.util.CodecUtil;
import com.sanxing.sesame.codec.util.HexBinary;
import com.sanxing.sesame.codec.util.RidFillBlank;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.transform.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAppInfo;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.transform.JDOMSource;
import org.jdom2.xpath.XPath;
import org.w3c.dom.NodeList;

public class DecodeFSV implements Decoder {
	private static Logger LOG = LoggerFactory.getLogger(DecodeFSV.class);
	private Map<String, Integer> partTypeMap = new HashMap();

	private List<String> typeList = new ArrayList();

	public DecodeFSV() {
		this.partTypeMap.put("F", new Integer(0));
		this.partTypeMap.put("S", new Integer(1));
		this.partTypeMap.put("V", new Integer(2));
		this.typeList.add("int");
		this.typeList.add("unsignedInt");
		this.typeList.add("short");
		this.typeList.add("unsignedShort");
		this.typeList.add("long");
		this.typeList.add("unsignedLong");
		this.typeList.add("float");
		this.typeList.add("double");
		this.typeList.add("hexBinary");
		this.typeList.add("decimal");
		this.typeList.add("byte");
	}

	public org.jdom2.Element decode(byte[] message, int length, String charset,
			XmlSchemaElement schemaElement, XmlSchema schema)
			throws FormatException {
		if (length > message.length)
			throw new FormatException("decode,parameter length:[" + length
					+ "] bigger then real buffer length:[" + message.length
					+ "] error!");
		ByteBuffer msgBuf = ByteBuffer.wrap(message);

		msgBuf.limit(length);
		org.jdom2.Element root = null;
		try {
			if (schemaElement == null)
				throw new FormatException("schema is null!");
			XmlSchemaType xsdType = schemaElement.getSchemaType();
			if (!(xsdType instanceof XmlSchemaComplexType)) {
				throw new FormatException(
						"in xsdDoc,can not find the child element:[complexType]");
			}
			root = new org.jdom2.Element(schemaElement.getName());

			Iterator elements = CodecUtil.getElements(xsdType);
			decodeMessage(msgBuf, elements, charset, root, schema);
			if (msgBuf.position() != msgBuf.limit())
				throw new FormatException("decode,parameter [length:"
						+ msgBuf.limit()
						+ "] bigger then the real need [length:"
						+ msgBuf.position() + "]");
		} catch (FormatException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new FormatException(e.getMessage(), e);
		}
		return root;
	}

	public boolean skipElement(String elementName) {
		return false;
	}

	protected void decodeMessage(ByteBuffer message, Iterator<?> elements,
			String charset, org.jdom2.Element elementMessage, XmlSchema schema)
			throws FormatException {
		try {
			while (elements.hasNext()) {
				XmlSchemaElement element = (XmlSchemaElement) elements.next();
				String elementName = element.getName();
				if (skipElement(elementName)) {
					continue;
				}
				LOG.debug("elementName=" + elementName);

				org.jdom2.Element childOfElementMessage = new org.jdom2.Element(
						elementName);

				XmlSchemaType xsType = null;
				if (element.getRefName() != null) {
					if (schema.getElementByName(element.getRefName()) == null)
						throw new FormatException(
								"can not get element from schema by name:["
										+ element.getRefName() + "]!");
					element = schema.getElementByName(element.getRefName());
				}
				xsType = element.getSchemaType();

				String type;
				org.w3c.dom.Element format;
				String kind;
				if (xsType instanceof XmlSchemaSimpleType) {
					type = xsType.getName();

					format = CodecUtil.getXSDFromat(element, elementName);

					kind = format.getAttribute("kind");
					ValidateField.validateKind(elementName, kind);
					switch (((Integer) this.partTypeMap.get(kind)).intValue()) {
					case 0:
						ValidateFFormat vFFormat = new ValidateFFormat(element,
								format);

						if (("string".equals(type)) || ("decimal".equals(type))
								|| ("hexBinary".equals(type))) {
							int len = vFFormat.getLen();

							int blank = vFFormat.getBlank();

							String align = vFFormat.getAlign();
							decodeNonNumber(message, charset,
									childOfElementMessage, type, len, blank,
									align);
						} else {
							if (!(this.typeList.contains(type))) {
								throw new FormatException("in xsdDoc,element:["
										+ elementName + "], unsupported type:["
										+ type + "]");
							}

							String endian = vFFormat.getEndian();

							decodeNumber(message, childOfElementMessage,
									endian, type);
						}
						elementMessage.addContent(childOfElementMessage);
						break;
					case 1:
						if ("string".equals(type)) {
							decodeSeparator(message, childOfElementMessage,
									format, charset, elementName);
							elementMessage.addContent(childOfElementMessage);
						} else {
							throw new FormatException("element:["
									+ element.getName() + "],type is:[" + type
									+ "] error! unsupported type!");
						}
						break;
					case 2:
						ValidateVFormat vVFormat = new ValidateVFormat(element,
								format);
						if (("string".equals(type))
								|| ("hexBinary".equals(type)))
							decodeVField(message, charset, elementMessage,
									element.getName(), childOfElementMessage,
									type, vVFormat);
						else
							throw new FormatException("element:["
									+ element.getName() + "],type is:[" + type
									+ "] error! unsupported type!");
						break;
					}
				}
				if ((xsType instanceof XmlSchemaComplexType)
						&& (element.getSchemaType().getName() != null)) {
					elementMessage.addContent(childOfElementMessage);
					decodeMessage(message,
							CodecUtil.getElements(element.getSchemaType()),
							charset, childOfElementMessage, schema);
				} else {
					if ((!(xsType instanceof XmlSchemaComplexType))
							|| (element.getSchemaType().getName() != null))
						continue;
					try {
						org.w3c.dom.Element occurs = null;
						if (element.getAnnotation() != null) {
							XmlSchemaObjectCollection annColl = element
									.getAnnotation().getItems();

							for (Iterator it = annColl.getIterator(); it
									.hasNext();) {
								Object o = it.next();
								if (o instanceof XmlSchemaAppInfo) {
									XmlSchemaAppInfo appInfo = (XmlSchemaAppInfo) o;
									NodeList list = appInfo.getMarkup();
									occurs = CodecUtil.getChildEleOfAppinfo(
											list, "occurs");
								}

							}

						}

						XmlSchemaType loopComplexType = element.getSchemaType();
						if (!(loopComplexType instanceof XmlSchemaComplexType))
							throw new FormatException(
									"in xsdDoc,can not find the child element:[complexType]");
						int loopNum = 1;
						if (occurs != null) {
							String path = occurs.getAttribute("ref");
							if (("".equals(path)) || (path == null))
								throw new FormatException(
										"element:["
												+ element.getName()
												+ "],element occurs,do not define the attribute[ref] or it has no value!");
							org.jdom2.Element repeat_num = (org.jdom2.Element) XPath
									.selectSingleNode(elementMessage, path);

							if (repeat_num == null)
								throw new FormatException("element:["
										+ element.getName()
										+ "],can not find the element by path["
										+ path + "]!");
							loopNum = Integer.parseInt((repeat_num.getText()
									.equals("")) ? "0" : repeat_num.getText());
							if (loopNum < 0)
								throw new FormatException(
										"the repeat element ["
												+ repeat_num.getName()
												+ "]'s value is invalid");
							if (loopNum == 0) {
								org.jdom2.Element detail = new org.jdom2.Element(
										elementName);
								elementMessage.addContent(detail);
							}
						}
						for (int i = 0; i < loopNum; ++i) {
							org.jdom2.Element detail = new org.jdom2.Element(
									elementName);
							elementMessage.addContent(detail);
							decodeMessage(message,
									CodecUtil.getElements(loopComplexType),
									charset, detail, schema);
						}
					} catch (JDOMException e) {
						throw new FormatException(e.getMessage(), e);
					}
				}
			}
		} catch (FormatException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new FormatException(e.getMessage(), e);
		}
	}

	public void decodeVField(ByteBuffer message, String charset,
			org.jdom2.Element elementMessage, String elementName,
			org.jdom2.Element childOfElementMessage, String type,
			ValidateVFormat vVFormat) throws FormatException {
		int headLength = vVFormat.getHeadLength();

		int headBlank = vVFormat.getHeadBlank();

		String headAlign = vVFormat.getHeadAlign();

		int headRadix = vVFormat.getHeadRadix();

		org.jdom2.Element tempElement = new org.jdom2.Element(elementName);
		decodeHead(message, charset, tempElement, headRadix, headLength,
				headBlank, headAlign);
		String lengthString = tempElement.getValue();
		if (lengthString != null) {
			if (lengthString.length() < 1) {
				childOfElementMessage.addContent(new String(""));
			} else {
				int length = 0;
				if (headRadix != 2) {
					length = Integer.parseInt(lengthString, headRadix);
				} else
					length = Integer.parseInt(lengthString, 16);

				byte[] temp = new byte[length];
				message.get(temp);
				try {
					String value = null;
					if ("string".equals(type))
						value = new String(temp, charset);
					else {
						value = HexBinary.encode(temp);
					}
					childOfElementMessage.addContent(value);
				} catch (UnsupportedEncodingException e) {
					throw new FormatException(e.getMessage(), e);
				}
			}
			elementMessage.addContent(childOfElementMessage);
		} else {
			throw new FormatException(" element:[" + elementName
					+ "],can not get the message length");
		}
	}

	private ByteBuffer getNumberBuf(ByteBuffer message, String endian, int len) {
		byte[] temp = new byte[len];
		message.get(temp, 0, len);
		ByteBuffer buf = ByteBuffer.allocate(len);
		setByteOrder(endian, buf);
		buf.put(temp);
		buf.flip();

		return buf;
	}

	private ByteBuffer getUnsignedNumberBuf(ByteBuffer message, String endian,
			int len) {
		byte[] buf = new byte[len];
		message.get(buf, 0, len);
		int longBufLen = 0;
		if (len == 4)
			longBufLen = 8;
		if (len == 2) {
			longBufLen = 4;
		}
		byte[] longBuf = new byte[longBufLen];

		ByteBuffer tempBuf = ByteBuffer.allocate(longBufLen);
		setByteOrder(endian, tempBuf);

		if ("big".equals(endian))
			System.arraycopy(buf, 0, longBuf, len, len);
		else {
			System.arraycopy(buf, 0, longBuf, 0, len);
		}
		tempBuf.put(longBuf);
		tempBuf.flip();
		return tempBuf;
	}

	private void setByteOrder(String endian, ByteBuffer buf) {
		if ("big".equals(endian))
			buf.order(ByteOrder.BIG_ENDIAN);
		else
			buf.order(ByteOrder.LITTLE_ENDIAN);
	}

	private void decodeSeparator(ByteBuffer message,
			org.jdom2.Element childOfElementMessage, org.w3c.dom.Element format,
			String charset, String elementName) throws FormatException {
		boolean flage = true;
		try {
			String hexSeparator = format.getAttribute("separator");
			if (("".equals(hexSeparator)) || (hexSeparator == null))
				throw new FormatException(
						"element:["
								+ elementName
								+ "],attribute [separator] do not define or it has no value!");
			int separatorLength = hexSeparator.getBytes(charset).length;

			String hexLimit = format.getAttribute("limit");
			int hexLimitLength = 0;
			int oldPos = message.position();
			message.position(oldPos);

			if ((hexLimit != null) && (!("".equals(hexLimit)))) {
				hexLimitLength = hexLimit.getBytes().length;
				byte[] limitBuf = new byte[hexLimitLength];
				message.get(limitBuf);
				if (!(hexLimit.equals(new String(limitBuf, charset))))
					throw new FormatException("element:[" + elementName
							+ "],can not find begin limit error!");
				if ((hexLimitLength == separatorLength)
						&& (hexLimit.equals(hexSeparator))) {
					throw new FormatException("element:[" + elementName
							+ "],limit can not equals separator");
				}

				for (int pos = message.position(); message.position() < message
						.limit(); message.position(++pos)) {
					if (message.position() + separatorLength > message.limit())
						break;
					byte[] temp = new byte[separatorLength];
					message.get(temp);

					if ((!(hexSeparator.equals(new String(temp, charset))))
							|| (message.position() - oldPos - hexLimitLength
									- separatorLength < hexLimitLength))
						continue;
					byte[] limitTempend = new byte[hexLimitLength];
					message.position(message.position() - hexLimitLength
							- separatorLength);
					message.get(limitTempend);

					if (hexLimit.equals(new String(limitTempend, charset))) {
						int messageLen = message.position() - oldPos
								- (2 * hexLimitLength);
						byte[] messageBuf = new byte[messageLen];
						message.position(oldPos + hexLimitLength);
						message.get(messageBuf);
						childOfElementMessage.addContent(new String(messageBuf,
								charset));
						message.position(message.position() + hexLimitLength
								+ separatorLength);
						flage = false;
						break;
					}

				}

			} else {
				for (int pos = message.position(); message.position() < message
						.limit(); message.position(++pos)) {
					if (message.position() + separatorLength > message.limit())
						break;
					byte[] temp = new byte[separatorLength];
					message.get(temp, 0, separatorLength);

					if (!(hexSeparator.equals(new String(temp, charset))))
						continue;
					int newPos = message.position();
					int messageLen = newPos - oldPos - separatorLength;
					byte[] messageBuf = new byte[messageLen];
					message.position(oldPos);
					message.get(messageBuf, 0, messageLen);
					message.position(newPos);
					childOfElementMessage.addContent(new String(messageBuf,
							charset));
					flage = false;
					break;
				}

			}

			if (flage) {
				message.position(message.limit());
				if ((hexLimit != null) && (!("".equals(hexLimit)))) {
					byte[] limitBuf = new byte[hexLimitLength];
					message.position(message.position() - hexLimitLength);
					message.get(limitBuf, 0, limitBuf.length);
					if (!(hexLimit.equals(new String(limitBuf, charset)))) {
						throw new FormatException("element:[" + elementName
								+ "],can not find end limit error!");
					}
					byte[] remainBuf = new byte[message.position() - oldPos
							- (2 * hexLimitLength)];
					message.position(oldPos + hexLimitLength);
					message.get(remainBuf, 0, remainBuf.length);
					childOfElementMessage.addContent(new String(remainBuf,
							charset));
					message.position(message.position() + hexLimitLength);
				} else {
					byte[] remainBuf = new byte[message.position() - oldPos];
					message.position(oldPos);
					message.get(remainBuf, 0, remainBuf.length);
					childOfElementMessage.addContent(new String(remainBuf,
							charset));
				}
			}
		} catch (FormatException e) {
			throw e;
		} catch (Exception e) {
			throw new FormatException(e.getMessage(), e);
		}
	}

	private void decodeNumber(ByteBuffer message,
			org.jdom2.Element childOfElementMessage, String endian, String type)
			throws FormatException {
		if (type.equals("int")) {
			int len = 4;
			ByteBuffer buf = null;
			buf = getNumberBuf(message, endian, len);
			childOfElementMessage.addContent("" + buf.getInt());
		} else if (type.equals("unsignedInt")) {
			int len = 4;
			ByteBuffer buf = null;
			buf = getUnsignedNumberBuf(message, endian, len);
			childOfElementMessage.addContent("" + buf.getLong());
		} else if (type.equals("short")) {
			int len = 2;
			ByteBuffer buf = null;
			buf = getNumberBuf(message, endian, len);
			childOfElementMessage.addContent("" + buf.getShort());
		} else if (type.equals("unsignedShort")) {
			int len = 2;
			ByteBuffer buf = null;
			try {
				buf = getUnsignedNumberBuf(message, endian, len);
				childOfElementMessage.addContent("" + buf.getInt());
			} catch (Exception e) {
				throw new FormatException(e.getMessage(), e);
			}
		} else if (type.equals("long")) {
			int len = 8;
			ByteBuffer buf = null;
			try {
				buf = getNumberBuf(message, endian, len);
				childOfElementMessage.addContent("" + buf.getLong());
			} catch (Exception e) {
				throw new FormatException(e.getMessage(), e);
			}
		} else if (type.equals("unsignedLong")) {
			int len = 8;
			ByteBuffer buf = null;
			try {
				buf = getNumberBuf(message, endian, len);
				long longTemp = buf.getLong();
				BigInteger tempValue = BigInteger.valueOf(longTemp);
				if (longTemp < 0L) {
					BigInteger bi = BigInteger.valueOf(-9223372036854775808L);
					BigInteger bi2 = new BigInteger("-2");
					bi = bi.multiply(bi2);
					tempValue = bi.add(tempValue);
				}

				childOfElementMessage.addContent(tempValue.toString());
			} catch (Exception e) {
				throw new FormatException(e.getMessage(), e);
			}
		} else if (type.equals("float")) {
			int len = 4;
			ByteBuffer buf = null;
			try {
				buf = getNumberBuf(message, endian, len);
				childOfElementMessage.addContent("" + buf.getFloat());
			} catch (Exception e) {
				throw new FormatException(e.getMessage(), e);
			}
		} else if (type.equals("double")) {
			int len = 8;
			ByteBuffer buf = null;
			try {
				buf = getNumberBuf(message, endian, len);
				childOfElementMessage.addContent("" + buf.getDouble());
			} catch (Exception e) {
				throw new FormatException(e.getMessage(), e);
			}
		} else if (type.equals("byte")) {
			int len = 1;
			ByteBuffer buf = null;
			try {
				buf = getNumberBuf(message, endian, len);
				childOfElementMessage.addContent("" + buf.get());
			} catch (Exception e) {
				throw new FormatException(e.getMessage(), e);
			}
		} else {
			throw new FormatException("type is:[" + type
					+ "] error! unsupported type!");
		}
	}

	public void decodeHead(ByteBuffer message, String charset,
			org.jdom2.Element childOfElementMessage, int headRadix,
			int headLength, int headBlank, String headAlign)
			throws FormatException {
		byte[] temp = new byte[headLength];
		message.get(temp);
		String type = "";
		if (headRadix != 2)
			type = "string";
		else {
			type = "hexBinary";
		}
		if ("L".equals(headAlign))
			childOfElementMessage.addContent(RidFillBlank.ridRightBlank(temp,
					headBlank, type, charset));
		else
			childOfElementMessage.addContent(RidFillBlank.ridLeftBlank(temp,
					headBlank, type, charset));
	}

	private void decodeNonNumber(ByteBuffer message, String charset,
			org.jdom2.Element childOfElementMessage, String type, int length,
			int blank, String align) throws FormatException {
		byte[] temp = new byte[length];
		message.get(temp);
		if ("L".equals(align))
			childOfElementMessage.addContent(RidFillBlank.ridRightBlank(temp,
					blank, type, charset));
		else
			childOfElementMessage.addContent(RidFillBlank.ridLeftBlank(temp,
					blank, type, charset));
	}

	public void destroy() {
	}

	public void init(String workspaceRoot) {
	}

	public void decode(BinarySource inputSource, XMLResult output)
			throws FormatException {
		XmlSchema schema = inputSource.getXMLSchema();
		InputStream stream = inputSource.getInputStream();
		String charset = inputSource.getEncoding();
		if (schema == null) {
			throw new FormatException(
					"In decodeFSV,can not find schema in BinarySource!");
		}
		XmlSchemaElement schemaEl = inputSource.getRootElement();
		try {
			int length = stream.available();
			byte[] bytes = new byte[length];
			stream.read(bytes);
			org.jdom2.Element element = decode(bytes, length, charset, schemaEl,
					schema);
			element.detach();

			output.setDocument(new Document(element));
		} catch (IOException e) {
			throw new FormatException(e.getMessage(), e);
		}
	}

	public Source decode(InputStream input, XmlSchema schema, String rootName,
			String charset) throws FormatException {
		if (schema == null) {
			throw new FormatException(
					"In decodeFSV,the parameter [XmlSchema] is null!");
		}
		XmlSchemaElement schemaEl = schema.getElementByName(rootName);
		try {
			int length = input.available();
			byte[] bytes = new byte[length];
			input.read(bytes);
			org.jdom2.Element element = decode(bytes, length, charset, schemaEl,
					schema);
			element.detach();

			return new JDOMSource(new Document(element));
		} catch (IOException e) {
			throw new FormatException(e.getMessage(), e);
		}
	}
}