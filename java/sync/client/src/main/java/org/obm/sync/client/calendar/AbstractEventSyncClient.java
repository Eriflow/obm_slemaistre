package org.obm.sync.client.calendar;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.obm.sync.NotAllowedException;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventAlreadyExistException;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.base.Category;
import org.obm.sync.base.KeyList;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.CalendarItemsParser;
import org.obm.sync.calendar.CalendarItemsWriter;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventParticipationState;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.calendar.SyncRange;
import org.obm.sync.client.impl.AbstractClientImpl;
import org.obm.sync.client.impl.SyncClientException;
import org.obm.sync.items.EventChanges;
import org.obm.sync.locators.Locator;
import org.obm.sync.services.ICalendar;
import org.obm.sync.utils.DOMUtils;
import org.obm.sync.utils.DateHelper;
import org.w3c.dom.Document;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public abstract class AbstractEventSyncClient extends AbstractClientImpl implements ICalendar {

	private CalendarItemsParser respParser;
	private CalendarItemsWriter ciw;
	private String type;
	private final Locator locator;

	public AbstractEventSyncClient(String type, SyncClientException syncClientException, Locator locator) {
		super(syncClientException);
		this.locator = locator;
		respParser = new CalendarItemsParser();
		this.type = type;
		ciw = new CalendarItemsWriter();
	}

	@Override
	public EventObmId createEvent(AccessToken token, String calendar, Event event, boolean notification) throws ServerFault, EventAlreadyExistException {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		try {
			params.put("event", ciw.getEventString(event));
		} catch (TransformerException e) {
			throw new IllegalArgumentException(e);
		}
		params.put("notification", String.valueOf(notification));
		Document doc = execute(token, type + "/createEvent", params);
		exceptionFactory.checkEventAlreadyExistException(doc);
		return new EventObmId(DOMUtils.getElementText(doc.getDocumentElement(), "value"));
	}

	@Override
	public Event getEventFromId(AccessToken token, String calendar, EventObmId id) throws ServerFault, EventNotFoundException {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("id", id.serializeToString());
		Document doc = execute(token, type + "/getEventFromId", params);
		exceptionFactory.checkEventNotFoundException(doc);
		return respParser.parseEvent(doc.getDocumentElement());
	}

	@Override
	public KeyList getEventTwinKeys(AccessToken token, String calendar, Event event) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		try {
			params.put("event", ciw.getEventString(event));
		} catch (TransformerException e) {
			throw new IllegalArgumentException(e);
		}

		Document doc = execute(token, type + "/getEventTwinKeys", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseKeyList(doc);
	}

	@Override
	public EventChanges getSyncWithSortedChanges(AccessToken token,
			String calendar, Date lastSync) throws ServerFault {
		return getSync(token, calendar, lastSync, null, "getSyncWithSortedChanges");
	}
	
	@Override
	public EventChanges getSync(AccessToken token, String calendar,
			Date lastSync) throws ServerFault {
		return getSync(token, calendar, lastSync, null, "getSync");
	}
	
	@Override
	public EventChanges getSyncInRange(AccessToken token, String calendar, Date lastSync,
			SyncRange syncRange) throws ServerFault {
		return getSync(token, calendar, lastSync, syncRange, "getSyncInRange");
	}
	
	private EventChanges getSync(AccessToken token, String calendar,
			Date lastSync, SyncRange syncRange, String methodName) throws ServerFault {
		if (logger.isDebugEnabled()) {
			logger.debug("getSync(" + token.getSessionId() + ", " + calendar
					+ ", " + lastSync + ")");
		}
		Multimap<String, String> params = ArrayListMultimap.create();
		setToken(params, token);
		params.put("calendar", calendar);
		if (lastSync != null) {
			params.put("lastSync", DateHelper.asString(lastSync));
		} else {
			params.put("lastSync", "0");
		}
		if(syncRange != null){
			if(syncRange.getAfter() != null){
				params.put("syncRangeAfter", DateHelper.asString(syncRange.getAfter()));
			}
			if(syncRange.getBefore() != null){
				params.put("syncRangeBefore", DateHelper.asString(syncRange.getBefore()));
			}
		}

		Document doc = execute(token, type + "/" + methodName, params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseChanges(doc);
	}
	
	@Override
	public EventChanges getSyncEventDate(AccessToken token, String calendar, Date lastSync) throws ServerFault {
		if (logger.isDebugEnabled()) {
			logger.debug("getSyncEventDate(" + token.getSessionId() + ", " + calendar
					+ ", " + lastSync + ")");
		}
		Multimap<String, String> params = ArrayListMultimap.create();
		setToken(params, token);
		params.put("calendar", calendar);
		if (lastSync != null) {
			params.put("lastSync", DateHelper.asString(lastSync));
		} else {
			params.put("lastSync", "0");
		}

		Document doc = execute(token, type + "/getSyncEventDate", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseChanges(doc);
	}

	@Override
	public String getUserEmail(AccessToken token) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		Document doc = execute(token, type + "/getUserEmail", params);
		exceptionFactory.checkServerFaultException(doc);
		return DOMUtils.getElementText(doc.getDocumentElement(), "value");
	}

	@Override
	public CalendarInfo[] listCalendars(AccessToken token) throws ServerFault {
		Multimap<String, String> params = ArrayListMultimap.create();
		setToken(params, token);
		Document doc = execute(token, type + "/listCalendars", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseInfos(doc);
	}

	@Override
	public Event modifyEvent(AccessToken token, String calendar, Event event,
			boolean updateAttendees, boolean notification) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		try {
			params.put("event", ciw.getEventString(event));
		} catch (TransformerException e) {
			throw new IllegalArgumentException(e);
		}
		params.put("updateAttendees", "" + updateAttendees);
		params.put("notification", String.valueOf(notification));
		Document doc = execute(token, type + "/modifyEvent", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseEvent(doc.getDocumentElement());
	}

	@Override
	public void removeEventById(AccessToken token, String calendar, EventObmId uid, int sequence, boolean notification) 
			throws ServerFault, EventNotFoundException, NotAllowedException {
		
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("id", uid.serializeToString());
		params.put("sequence", String.valueOf(sequence));
		params.put("notification", String.valueOf(notification));
		Document doc = execute(token, type + "/removeEvent", params);
		exceptionFactory.checkRemoveEventException(doc);
	}
	
	@Override
	public Event removeEventByExtId(AccessToken token, String calendar, EventExtId extId, int sequence, boolean notification)
			throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("extId", extId.serializeToString());
		params.put("sequence", String.valueOf(sequence));
		params.put("notification", String.valueOf(notification));
		Document doc = execute(token, type + "/removeEventByExtId", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseEvent(doc.getDocumentElement());
	}

	@Override
	public KeyList getRefusedKeys(AccessToken token, String calendar, Date since) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		if (since != null) {
			params.put("since", DateHelper.asString(since));
		} else {
			params.put("since", "0");
		}

		Document doc = execute(token, type + "/getRefusedKeys", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseKeyList(doc);
	}

	@Override
	public List<Category> listCategories(AccessToken at) throws ServerFault {
		List<Category> ret = new LinkedList<Category>();
		Multimap<String, String> params = initParams(at);
		Document doc = execute(at, type + "/listCategories", params);
		exceptionFactory.checkServerFaultException(doc);
		ret.addAll(respParser.parseCategories(doc.getDocumentElement()));
		return ret;
	}

	@Override
	public Event getEventFromExtId(AccessToken token, String calendar, EventExtId extId) throws ServerFault, EventNotFoundException {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("extId", extId.serializeToString());
		Document doc = execute(token, type + "/getEventFromExtId", params);
		exceptionFactory.checkEventNotFoundException(doc);
		if (doc.getDocumentElement().getNodeName().equals("event")) {
			return respParser.parseEvent(doc.getDocumentElement());
		}
		logger.warn("event " + extId + " not found.");
		return null;
	}

	@Override
	public EventObmId getEventObmIdFromExtId(AccessToken token, String calendar, EventExtId extId) throws ServerFault, EventNotFoundException {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("extId", extId.serializeToString());
		Document doc = execute(token, type + "/getEventObmIdFromExtId", params);
		exceptionFactory.checkEventNotFoundException(doc);
		String value = DOMUtils.getElementText(doc.getDocumentElement(), "value");
		return new EventObmId(value);
	}

	@Override
	public List<Event> getListEventsFromIntervalDate(AccessToken token,
			String calendar, Date start, Date end) throws ServerFault {
		List<Event> ret = new LinkedList<Event>();
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("start", Long.toString(start.getTime()));
		params.put("end", Long.toString(end.getTime()));

		Document doc = execute(token, type + "/getListEventsFromIntervalDate", params);
		exceptionFactory.checkServerFaultException(doc);
		ret.addAll(respParser.parseListEvents(doc.getDocumentElement()));
		return ret;
	}

	@Override
	public List<Event> getAllEvents(AccessToken token, String calendar,
			EventType eventType) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("eventType", eventType.name());

		Document doc = execute(token, type + "/getAllEvents", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseListEvents(doc.getDocumentElement());
	}

	@Override
	public List<EventTimeUpdate> getEventTimeUpdateNotRefusedFromIntervalDate(
			AccessToken token, String calendar, Date start, Date end)
			throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("start", Long.toString(start.getTime()));
		if (end != null) {
			params.put("end", Long.toString(end.getTime()));
		}
		Document doc = execute(token, type + "/getEventTimeUpdateNotRefusedFromIntervalDate", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseListEventTimeUpdate(doc.getDocumentElement());
	}

	@Override
	public String parseEvent(AccessToken token, Event event) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		try {
			params.put("event", ciw.getEventString(event));
		} catch (TransformerException e) {
			throw new IllegalArgumentException(e);
		}

		Document doc = execute(token, type + "/parseEvent", params);
		exceptionFactory.checkServerFaultException(doc);
		return DOMUtils.getElementText(doc.getDocumentElement(), "value");
	}

	@Override
	public String parseEvents(AccessToken token, List<Event> events)
			throws ServerFault {
		Multimap<String, String> params = initParams(token);
		try {
			params.put("events", ciw.getListEventString(events));
		} catch (TransformerException e) {
			throw new IllegalArgumentException(e);
		}
		Document doc = execute(token, type + "/parseEvents", params);
		exceptionFactory.checkServerFaultException(doc);
		return DOMUtils.getElementText(doc.getDocumentElement(), "value");
	}

	@Override
	public List<Event> parseICS(AccessToken token, String ics)
			throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("ics", ics);
		Document doc = execute(token, type + "/parseICS", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseListEvents(doc.getDocumentElement());
	}

	@Override
	public FreeBusyRequest parseICSFreeBusy(AccessToken token, String ics) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("ics", ics);
		Document doc = execute(token, type + "/parseICSFreeBusy", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseFreeBusyRequest(doc.getDocumentElement());
	}

	@Override
	public List<EventParticipationState> getEventParticipationStateWithAlertFromIntervalDate(
			AccessToken token, String calendar, Date start, Date end) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("start", Long.toString(start.getTime()));
		params.put("end", Long.toString(end.getTime()));

		Document doc = execute(token, type
				+ "/getEventParticipationStateWithAlertFromIntervalDate",
				params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseListEventParticipationState(doc
				.getDocumentElement());
	}

	@Override
	public Date getLastUpdate(AccessToken token, String calendar) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		Document doc = execute(token, type + "/getLastUpdate", params);
		exceptionFactory.checkServerFaultException(doc);
		String date = DOMUtils.getElementText(doc.getDocumentElement(), "value");
		return new Date(new Long(date));
	}

	@Override
	public boolean isWritableCalendar(AccessToken token, String calendar) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		Document doc = execute(token, type + "/isWritableCalendar", params);
		exceptionFactory.checkServerFaultException(doc);
		return "true".equalsIgnoreCase(DOMUtils.getElementText(doc
				.getDocumentElement(), "value"));
	}

	@Override
	public List<FreeBusy> getFreeBusy(AccessToken token, FreeBusyRequest fbr) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("freebusyrequest", ciw.getFreeBusyRequestString(fbr));
		Document doc = execute(token, type + "/getFreeBusy", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseListFreeBusy(doc.getDocumentElement());
	}

	@Override
	public String parseFreeBusyToICS(AccessToken token, FreeBusy fb) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("freebusy", ciw.getFreeBusyString(fb));

		Document doc = execute(token, type + "/parseFreeBusyToICS", params);
		exceptionFactory.checkServerFaultException(doc);
		String ret = DOMUtils.getElementText(doc.getDocumentElement(), "value");
		return ret;
	}
	
	@Override
	public boolean changeParticipationState(AccessToken token, String calendar,
			EventExtId extId, ParticipationState participationState, 
			int sequence, boolean notification) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("extId", extId.serializeToString());
		params.put("state", participationState.toString());
		params.put("sequence", String.valueOf(sequence));
		params.put("notification", String.valueOf(notification));
		Document doc = execute(token, type + "/changeParticipationState", params);
		exceptionFactory.checkServerFaultException(doc);
		return Boolean.valueOf(DOMUtils.getElementText(doc.getDocumentElement(), "value"));
	}
	
	@Override
	public int importICalendar(final AccessToken token, final String calendar, final String ics) throws ServerFault {
		final Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("ics", ics);
		
		final Document doc = execute(token, type + "/importICalendar", params);
		exceptionFactory.checkServerFaultException(doc);
		return Integer.valueOf(DOMUtils.getElementText(doc.getDocumentElement(), "value"));
	}
	
	@Override
	public void purge(final AccessToken token, final String calendar) throws ServerFault {
		final Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		final Document doc = execute(token, type + "/purge", params);
		exceptionFactory.checkServerFaultException(doc);
	}

	@Override
	public CalendarInfo[] getCalendarMetadata(AccessToken token,
			String[] calendars) throws ServerFault {
		final Multimap<String, String> params = initParams(token);
		for (String calendar : calendars)
			params.put("calendar", calendar);
		final Document doc = execute(token, type + "/getCalendarMetadata", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseInfos(doc);
	}
	
	@Override
	protected Locator getLocator() {
		return locator;
	}
	
}
