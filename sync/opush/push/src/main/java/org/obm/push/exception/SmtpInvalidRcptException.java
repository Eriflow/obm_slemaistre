package org.obm.push.exception;

import java.util.Map;
import java.util.Map.Entry;

import org.obm.push.store.ActiveSyncException;

/**
 * 
 * @author adrienp
 * 
 */
public class SmtpInvalidRcptException extends ActiveSyncException {
	
	private static final long serialVersionUID = -4332866043885420096L;
	private Map<String, Throwable> rcpt;
	
	public SmtpInvalidRcptException(Map<String, Throwable> rcpt, String message, Throwable cause) {
		super(message, cause);
		this.rcpt = rcpt;
	}

	public SmtpInvalidRcptException(Map<String, Throwable> undeliveredRcpt) {
		this(undeliveredRcpt, "", null);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for(Entry<String, Throwable> t : rcpt.entrySet()){
			b.append(t.getKey());
			b.append(": ");
			b.append(t);
			b.append("\r\n");
		}
		return b.toString();
	}

	
	
}
