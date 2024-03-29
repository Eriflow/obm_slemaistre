/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.push.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.MessageBuilder;
import org.apache.james.mime4j.dom.MessageWriter;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.dom.field.ContentTypeField;
import org.apache.james.mime4j.message.BasicBodyFactory;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.MessageImpl;
import org.apache.james.mime4j.message.MessageServiceFactoryImpl;
import org.apache.james.mime4j.message.MultipartImpl;
import org.apache.james.mime4j.storage.Storage;
import org.apache.james.mime4j.storage.StorageBodyFactory;
import org.apache.james.mime4j.storage.StorageOutputStream;
import org.apache.james.mime4j.storage.StorageProvider;
import org.apache.james.mime4j.util.MimeUtil;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Mime4jUtils {

	private MessageBuilder messageBuilder;
	private MessageWriter messageWriter;

	public Mime4jUtils() {
		MessageServiceFactoryImpl messageServiceFactory = new MessageServiceFactoryImpl();
		messageBuilder = messageServiceFactory.newMessageBuilder();
		messageWriter = messageServiceFactory.newMessageWriter();
	}

	public MessageImpl createMessage() {
		return new MessageImpl();
	}

	public Message parseMessage(byte[] data) throws MimeException, IOException {
		ByteArrayInputStream stream = new ByteArrayInputStream(data);
		return parseMessage(stream);
	}
	
	public Message parseMessage(InputStream in) throws MimeException, IOException {
		return messageBuilder.parseMessage(in);
	}
	
	public Multipart createMultipartMixed() {
		return messageBuilder.newMultipart(MimeContentType.MULTIPART_MIXED.getSubType());
	}
	
	public Multipart createMultipartAlternative() {
		return new MultipartImpl(MimeContentType.MULTIPART_ALTERNATIVE.getSubType());
	}

	public BodyPart bodyToBodyPart(Body body, String mimeType) {
		BodyPart bodyPart = new BodyPart();
		bodyPart.setBody(body,mimeType);
		return bodyPart;
	}
	
	public int getAttachmentCount(Multipart multi){
		int attachments = 0;
		for (Entity part: multi.getBodyParts()) {
			if (isAttachmentEntity(part)) {
				attachments++;
			}
		}
		return attachments;
	}

	private boolean isAttachmentEntity(Entity part) {
		return "attachment".equalsIgnoreCase(part.getDispositionType());
	}
	
	public List<String> getAttachmentContentTypeList(Multipart multipart) {
		List<String> list = Lists.newArrayList();
		for (Entity part: multipart.getBodyParts()) {
			if (isAttachmentEntity(part)) {
				list.add(part.getMimeType());
			}
		}
		return list;
	}	
	
	public Map<String,String> getContentTypeHeaderParams(Charset charset) {
		Map<String,String> params = Maps.newHashMap();
		params.put(ContentTypeField.PARAM_CHARSET, charset.name());
		return params;
	}
	
	public Map<String,String> getContentTypeHeaderMultipartParams(Charset charset) {
		Map<String,String> params = getContentTypeHeaderParams(charset); 
		String boundary = MimeUtil.createUniqueBoundary();
		params.put(ContentTypeField.PARAM_BOUNDARY, boundary);
		return params;
	}

	public boolean isMessagePlainText(Message msg) {
		return msg.getMimeType().equalsIgnoreCase(MimeContentType.TEXT_PLAIN.getContentType());
	}

	public boolean isMessageHtmlText(Message msg) {
		return msg.getMimeType().equalsIgnoreCase(MimeContentType.TEXT_HTML.getContentType());
	}

	public boolean isMessageMultipartMixed(Message msg) {
		if (msg.isMultipart()){
			Multipart multipart = (Multipart)msg.getBody();
			return multipart.getSubType().equalsIgnoreCase(MimeContentType.MULTIPART_MIXED.getSubType());
		}
		return false;
	}

	public Entity getFirstTextPlainPart(Multipart multipart) {
		for (Entity entity: multipart.getBodyParts()) {
			if (entity.getMimeType().equalsIgnoreCase(MimeContentType.TEXT_PLAIN.getContentType())) {
				return entity;
			}
		}
		return null;
	}
	
	public Entity getFirstTextHTMLPart(Multipart multipart) {
		for (Entity entity: multipart.getBodyParts()) {
			if (entity.getMimeType().equalsIgnoreCase(MimeContentType.TEXT_HTML.getContentType())) {
				return entity;
			}
		}
		return null;
	}

	public void attach(Multipart multipart, InputStream in,
			String fileName, String mimeType) throws FileNotFoundException,
			IOException {
		StorageBodyFactory bodyFactory = new StorageBodyFactory();
		BodyPart attach = createBinaryPart(bodyFactory, in, mimeType, fileName);
		multipart.addBodyPart(attach);
	}

	/**
	 * Creates a text part from the specified string.
	 */
	public BodyPart createTextPart(String text, String subtype) throws UnsupportedEncodingException {
		TextBody body = createBody(text);
		// Create a text/plain body part
		BodyPart bodyPart = new BodyPart();
		bodyPart.setText(body, subtype);
		bodyPart.setContentTransferEncoding("quoted-printable");

		return bodyPart;
	}

	public TextBody createBody(String text) throws UnsupportedEncodingException {
		BasicBodyFactory bodyFactory = new BasicBodyFactory();
		// Use UTF-8 to encode the specified text
		TextBody body = bodyFactory.textBody(text, "UTF-8");
		return body;
	}
	
	public InputStream toInputStream(Body message) throws IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		messageWriter.writeBody(message, out);
		message.dispose();
		return new ByteArrayInputStream(out.toByteArray());
	}

	public String toString(Body message) throws IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		messageWriter.writeBody(message, out);
		message.dispose();
		return new String(out.toByteArray(), Charsets.UTF_8);
	}
	
	private BodyPart createBinaryPart(StorageBodyFactory bodyFactory,
			InputStream in, String mimeType, String fileName)
			throws IOException {
		// Create a binary message body from the stream
		StorageProvider storageProvider = bodyFactory.getStorageProvider();
		Storage storage = storeStream(storageProvider, in);
		BinaryBody body = bodyFactory.binaryBody(storage);

		// Create a body part with the correct MIME-type and transfer encoding
		BodyPart bodyPart = new BodyPart();
		bodyPart.setBody(body, mimeType);
		if (!mimeType.endsWith("/rfc822")) {
			bodyPart.setContentTransferEncoding("base64");
		}
		// Specify a filename in the Content-Disposition header (implicitly sets
		// the disposition type to "attachment")
		bodyPart.setFilename(fileName);

		return bodyPart;
	}

	/**
	 * Stores the specified stream in a Storage object.
	 */
	private Storage storeStream(StorageProvider storageProvider,
			InputStream in) throws IOException {
		// An output stream that is capable of building a Storage object.
		StorageOutputStream out = storageProvider.createStorageOutputStream();

		FileUtils.transfer(in, out, false);

		// Implicitly closes the output stream and returns the data that has
		// been written to it.
		return out.toStorage();
	}
}
