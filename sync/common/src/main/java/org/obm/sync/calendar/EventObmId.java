package org.obm.sync.calendar;

import java.io.Serializable;

import com.google.common.base.Objects;

public class EventObmId implements Serializable {

	private final int obmId;

	public EventObmId(String obmId) {
		this(Integer.valueOf(obmId));
	}
	
	public EventObmId(int obmId) {
		this.obmId = obmId;
	}
	
	public int getObmId() {
		return obmId;
	}
	
	public String serializeToString() {
		return String.valueOf(obmId);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EventObmId) {
			EventObmId other = (EventObmId) obj;
			return Objects.equal(obmId, other.obmId);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(obmId);
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("obmId", obmId)
			.toString();
	}
	
}
