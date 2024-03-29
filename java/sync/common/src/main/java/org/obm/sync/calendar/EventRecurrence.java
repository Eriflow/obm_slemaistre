package org.obm.sync.calendar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.obm.push.utils.collection.Sets;

import com.google.common.base.Equivalence;
import com.google.common.base.Equivalence.Wrapper;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class EventRecurrence {

	private String days;
	private Date end;
	private int frequence;
	private RecurrenceKind kind;
	private List<Date> exceptions;
	private List<Event> eventExceptions;

	public EventRecurrence() {
		exceptions = new LinkedList<Date>();
		eventExceptions = new LinkedList<Event>();
	}

	public String getDays() {
		return days;
	}

	public void setDays(String days) {
		this.days = days;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public int getFrequence() {
		return frequence;
	}

	public void setFrequence(int frequence) {
		this.frequence = frequence;
	}

	public RecurrenceKind getKind() {
		return kind;
	}

	public void setKind(RecurrenceKind kind) {
		this.kind = kind;
	}

	public Date[] getExceptions() {
		return exceptions.toArray(new Date[exceptions.size()]);
	}

	public List<Date> getListExceptions() {
		return exceptions;
	}

	public void setExceptions(Date[] exceptions) {
		this.exceptions = Arrays.asList(exceptions);
	}

	public void addException(Date d) {
		exceptions.add(d);
	}

	public List<Event> getEventExceptions() {
		return eventExceptions;
	}

	public void setEventExceptions(List<Event> eventExceptions) {
		this.eventExceptions = eventExceptions;
	}

	public void addEventException(Event eventException) {
		this.eventExceptions.add(eventException);
	}

	public EventRecurrence clone() {
		EventRecurrence eventRecurrence = new EventRecurrence();
		eventRecurrence.setDays(this.days);
		eventRecurrence.setEnd(this.end);
		List<Event> eventExceptions = new ArrayList<Event>();
		for (Event event: this.eventExceptions) {
			eventExceptions.add(event.clone());
		}
		eventRecurrence.setEventExceptions(eventExceptions);
		eventRecurrence.setExceptions(getExceptions());
		eventRecurrence.setFrequence(this.frequence);
		eventRecurrence.setKind(this.kind);
		return eventRecurrence;
	}

	public boolean hasImportantChanges(EventRecurrence recurrence) {
		if (recurrence == null) {
			return true;
		}
		if ( !(Objects.equal(this.end, recurrence.end)
				&& Objects.equal(this.kind, recurrence.kind)
				&& Objects.equal(this.frequence, recurrence.frequence)
				&& (this.eventExceptions.size() == recurrence.eventExceptions.size())
				&& (this.exceptions.size() == recurrence.exceptions.size())) ) {
			return true;
		}
		Set<Event> difference = Sets.difference(this.getEventExceptions(), recurrence.getEventExceptions(), 
				new ComparatorUsingEventHasImportantChanges());
		return !difference.isEmpty();
	}
	
	public List<Event> getEventExceptionWithChangesExceptedOnException(EventRecurrence recurrence) {
		if (recurrence == null) {
			return ImmutableList.copyOf(this.getEventExceptions());
		}
		Builder<Event> eventExceptionWithChanges = ImmutableList.builder();
		
		final AllEventAttributesExceptExceptionsEquivalence equivalence = new AllEventAttributesExceptExceptionsEquivalence();
		Collection<Wrapper<Event>> recurrenceEquivalenceWrappers = transformToEquivalenceWrapper(recurrence, equivalence);
		
		for(Event exp : eventExceptions){
			Wrapper<Event> r = equivalence.wrap(exp);
			if(!recurrenceEquivalenceWrappers.contains(r)) {
				eventExceptionWithChanges.add(exp);
			}
		}
		return eventExceptionWithChanges.build();
	}
	
	private Collection<Wrapper<Event>> transformToEquivalenceWrapper(EventRecurrence recurrence, 
			final AllEventAttributesExceptExceptionsEquivalence equivalence) {
		return Collections2.transform(recurrence.getEventExceptions(), new Function<Event, Equivalence.Wrapper<Event>>() {
			@Override
			public Equivalence.Wrapper<Event> apply(Event input) {
				Wrapper<Event> wrapper = equivalence.wrap(input);
				return wrapper;
			}
		});
	}

	public Event getEventExceptionWithRecurrenceId(Date recurrenceId) {
		for(Event event : this.eventExceptions){
			if(recurrenceId.equals(event.getRecurrenceId())) {
				return event;
			}
		}
		return null;
	}
	
	@Override
	public final int hashCode() {
		return Objects.hashCode(days, end, frequence, kind, exceptions,
				eventExceptions);
	}

	@Override
	public final boolean equals(Object object){
		if (object instanceof EventRecurrence) {
			EventRecurrence that = (EventRecurrence) object;
			return Objects.equal(this.days, that.days)
				&& Objects.equal(this.end, that.end)
				&& Objects.equal(this.frequence, that.frequence)
				&& Objects.equal(this.kind, that.kind)
				&& Objects.equal(this.exceptions, that.exceptions)
				&& Objects.equal(this.eventExceptions, that.eventExceptions);
		}
		return false;
	}

}