package org.aperteworkflow.cmis.widget;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ContentStream;

/**
 * Document content stream 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class DocumentContentStream implements ContentStream {

	private byte[] bytes;
	private String MIMEType;
	private String filename;
	
	public DocumentContentStream(byte[] bytes, String MIMEType, String filename)
	{
		this.bytes = bytes;
		this.MIMEType = MIMEType;
		this.filename = filename;
	}

	@Override
	public long getLength() {
		return bytes.length;
	}

	@Override
	public BigInteger getBigLength() {
		return BigInteger.valueOf(bytes.length);
	}

	@Override
	public String getMimeType() {
		return MIMEType;
	}

	@Override
	public String getFileName() {
		return filename;
	}

	@Override
	public InputStream getStream() {
		return new ByteArrayInputStream(bytes);
	}

	@Override
	public List<CmisExtensionElement> getExtensions() {
		return null;
	}

	@Override
	public void setExtensions(List<CmisExtensionElement> extensions) {

	}

}
