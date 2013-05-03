package com.sanxing.sesame.codec.impl;

import com.sanxing.sesame.binding.codec.BinaryResult;
import com.sanxing.sesame.binding.codec.Encoder;
import com.sanxing.sesame.binding.codec.FormatException;
import com.sanxing.sesame.binding.codec.XMLSource;
import com.sanxing.sesame.codec.impl.validate.ValidateFFormat8583;
import com.sanxing.sesame.codec.impl.validate.ValidateField;
import com.sanxing.sesame.codec.impl.validate.ValidateVFormat8583;
import com.sanxing.sesame.codec.util.BCD;
import com.sanxing.sesame.codec.util.BitMap;
import com.sanxing.sesame.codec.util.CodecUtil;
import com.sanxing.sesame.codec.util.HexBinary;
import com.sanxing.sesame.codec.util.RidFillBlank;
import com.sanxing.sesame.util.JdomUtil;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.Iterator;
import javax.xml.transform.Source;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.jdom.Document;

public class Encode8583 implements Encoder {
	public void encode(org.jdom.Element xmlElement,
			XmlSchemaElement xsdElement, OutputStream output,
			String encodeCharset) throws FormatException {
		if (xmlElement == null)
			throw new FormatException("the parameter encode xml is null");
		if (xsdElement == null)
			throw new FormatException(" the parameter schema is null");
		XmlSchemaType xsdType = xsdElement.getSchemaType();
		if (!(xsdType instanceof XmlSchemaComplexType))
			throw new FormatException(
					"in xsdDoc,can not find the child element:[complexType]");
		encodeMessage(xsdType, xmlElement, output, encodeCharset);
	}

	private void encodeMessage(XmlSchemaType xsdType,
			org.jdom.Element xmlElement, OutputStream output,
			String encodeCharset) throws FormatException {
		try {
			Iterator iterator = CodecUtil.getElements(xsdType);

			Iterator tempIter = CodecUtil.getElements(xsdType);

			BitSet bs = new BitSet(64);
			encodeBitSet(xmlElement, tempIter, bs);

			if (bs.size() == 128) {
				bs.set(0);
			}
			byte[] bitMapBuf = new BitMap().encodeBitMap(bs);
			output.write(bitMapBuf);

			while (iterator.hasNext()) {
				XmlSchemaElement child = (XmlSchemaElement) iterator.next();

				String elementName = child.getName();

				String elementType = child.getSchemaType().getName();
				if ((elementType == null) || ("".equals(elementType))) {
					throw new FormatException(
							"in xsdDoc,elementName:["
									+ elementName
									+ "]have not dedine the attribute[type] or it has no vlaue!");
				}
				String elementValue = xmlElement.getChildText(elementName);

				if (elementValue.length() < 1) {
					continue;
				}

				org.w3c.dom.Element format = CodecUtil.getXSDFromat(child,
						elementName);

				String kind = format.getAttribute("kind");
				ValidateField.validate8583Kind(elementName, kind);

				if ("F".equals(kind)) {
					ValidateFFormat8583 vFFormat = new ValidateFFormat8583(
							elementType, elementName, format);
					encodeFValue(output, encodeCharset, elementType,
							elementValue, elementName, vFFormat);
				} else if ("V".equals(kind)) {
					ValidateVFormat8583 vVFormat = new ValidateVFormat8583(
							elementType, elementName, format);
					encodeVValue(output, encodeCharset, elementType,
							elementValue, elementName, vVFormat);
				}
			}
		} catch (FormatException fe) {
			throw fe;
		} catch (Exception e) {
			throw new FormatException(e.getMessage(), e);
		}
	}

	private void encodeBitSet(org.jdom.Element xmlElement, Iterator tempIter,
			BitSet bs) throws FormatException {
		while (tempIter.hasNext())
			try {
				XmlSchemaElement child = (XmlSchemaElement) tempIter.next();

				String elementName = child.getName();

				String elementValue = xmlElement.getChildText(elementName);
				if (elementValue == null) {
					throw new FormatException("in xsdDoc name is:["
							+ elementName + "],in xml,can not find the element");
				}

				org.w3c.dom.Element format = CodecUtil.getXSDFromat(child,
						elementName);

				int id = ValidateField.validateEId(format.getAttribute("id"),
						elementName);

				if (elementValue.length() > 0) {
					if (bs.get(id - 1))
						throw new FormatException(
								"in,xsdDoc, name is:["
										+ elementName
										+ "],bit map error! maybe two element have same id!");
					bs.set(id - 1);
				}
			} catch (FormatException e) {
				throw e;
			} catch (Exception e) {
				throw new FormatException(e.getMessage(), e);
			}
	}

	protected void encodeVValue(OutputStream output, String encodeCharset,
			String elementType, String elementValue, String elementName,
			ValidateVFormat8583 vVFormat) throws FormatException {
		int headLen = vVFormat.getHeadLen();

		int headCompress = vVFormat.getHeadCompress();

		char headBlank = vVFormat.getHeadBlank();

		String headAlign = vVFormat.getHeadAlign();

		int headRadix = vVFormat.getHeadRadix();

		int compress = vVFormat.getCompress();

		String headValue = "";
		try {
			if ("string".equals(elementType)) {
				headValue = Integer.toString(elementValue.getBytes().length,
						headRadix);
				if (headRadix == 2) {
					headValue = Integer.toString(
							elementValue.getBytes(encodeCharset).length, 16);
					if (headValue.length() % 2 != 0) {
						headValue = "0" + headValue;
					}
					headValue = new String(HexBinary.decode(headValue));
				}
			} else if ("hexBinary".equals(elementType)) {
				if (elementValue.getBytes(encodeCharset).length % 2 != 0) {
					throw new FormatException("element:[" + elementName
							+ "],type:[" + elementType + "],value length is:["
							+ elementValue.getBytes(encodeCharset).length
							+ " ]error! it length must can divide by 2");
				}
				headValue = Integer.toString(
						elementValue.getBytes(encodeCharset).length / 2,
						headRadix);
				if (headRadix == 2) {
					headValue = Integer
							.toString(
									elementValue.getBytes(encodeCharset).length / 2,
									16);
					if (headValue.length() % 2 != 0) {
						headValue = "0" + headValue;
					}
					headValue = new String(HexBinary.decode(headValue));
				}
			} else {
				throw new FormatException("element:[" + elementName
						+ "],kind is [V],type is:[" + elementType + "]error!");
			}
			String realHeadValue = null;
			realHeadValue = RidFillBlank.fillBlank(headValue, headLen,
					headBlank, headAlign, encodeCharset, elementName);

			if (1 == headCompress) {
				output.write(BCD.str2bcd(realHeadValue, headAlign, headBlank));
			} else {
				output.write(realHeadValue.getBytes(encodeCharset));
			}

			if ("string".equals(elementType)) {
				if (1 == compress) {
					output.write(BCD
							.str2bcd(elementValue, headAlign, headBlank));
					return;
				}

				output.write(elementValue.getBytes(encodeCharset));
				return;
			}
			if (!("hexBinary".equals(elementType)))
				return;
			byte[] temp = HexBinary.decode(elementValue);
			output.write(temp);
		} catch (FormatException fe) {
			throw fe;
		} catch (Exception e) {
			new FormatException(e.getMessage(), e);
		}
	}

	protected void encodeFValue(OutputStream output, String encodeCharset,
			String elementType, String elementValue, String elementName,
			ValidateFFormat8583 vFFormat) throws FormatException {
		try {
			if ("string".equals(elementType)) {
				String realValue = null;

				int len = vFFormat.getLen();

				String align = vFFormat.getAlign();

				char blank = vFFormat.getBlank();

				int compress = vFFormat.getCompress();

				realValue = RidFillBlank.fillBlank(elementValue, len, blank,
						align, encodeCharset, elementName);
				if (1 == compress) {
					output.write(BCD.str2bcd(realValue, align, blank));
					return;
				}
				try {
					output.write(realValue.getBytes(encodeCharset));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				return;
			}

			if ("int".equals(elementType)) {
				String endian = vFFormat.getEndian();
				int len = 4;
				ByteBuffer buffer = ByteBuffer.allocate(len);
				CodecUtil.setByteOrder(endian, buffer);
				buffer.putInt(Integer.parseInt(elementValue));
				output.write(buffer.array());
				return;
			}
			if ("hexBinary".equals(elementType)) {
				int len = vFFormat.getLen();

				byte[] temp = HexBinary.decode(elementValue);
				if (temp.length == len) {
					output.write(temp);
					return;
				}
				if (temp.length <= len)
					return;
				throw new FormatException("hexBinary length over-longer!");
			}
			throw new FormatException("element:[" + elementName
					+ "],attribute kind is:[F],attribute type is:["
					+ elementType + "]error!");
		} catch (FormatException fe) {
			throw fe;
		} catch (Exception e) {
			throw new FormatException(e.getMessage(), e);
		}
	}

	public void destroy() {
	}

	public void init(String workspaceRoot) {
	}

	public long encode(Source input, XmlSchema schema, String charset,
			OutputStream output) throws FormatException {
		Document message = JdomUtil.source2JDomDocument(input);
		String elementName = message.getRootElement().getName();

		if (charset == null)
			throw new FormatException("charset not specified");
		if (schema == null)
			throw new FormatException("schema is null");
		if (message == null)
			;
		try {
			XmlSchemaElement schemaEl = schema.getElementByName(elementName);
			encode(message.getRootElement(), schemaEl, output, charset);
			return 0L;
		} catch (FormatException fe) {
			throw fe;
		} catch (Exception e) {
			throw new FormatException(e.getMessage(), e);
		}
	}

	public void encode(XMLSource source, BinaryResult result)
			throws FormatException {
		String charset = result.getEncoding();
		XmlSchema schema = result.getXMLSchema();
		OutputStream output = result.getOutputStream();
		String elementName = result.getElementName();
		try {
			Document message = source.getJDOMDocument();

			if (charset == null)
				throw new FormatException("charset not specified");
			if (schema == null)
				throw new FormatException("schema is null");
			if (message == null) {
				throw new FormatException(
						"can not get doc from parameter [DOMSource]");
			}
			XmlSchemaElement schemaEl = schema.getElementByName(elementName);
			encode(message.getRootElement(), schemaEl, output, charset);
		} catch (FormatException fe) {
			throw fe;
		} catch (Exception e) {
			throw new FormatException(e.getMessage(), e);
		}
	}
}