package org.obm.push.calendar;

import static org.junit.Assert.*;
import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.FactoryConfigurationError;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.IApplicationData;
import org.obm.push.protocol.data.CalendarDecoder;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

public class EventConverterTest {

	private EventConverter eventConverter;
	private CalendarDecoder decoder;

	@Before
	public void init() {
		this.eventConverter = new EventConverter();
		this.decoder = new CalendarDecoder();
	}

	@Test
	public void testAttendeesWithNoOrganizerInNewEventStream() throws SAXException, IOException, FactoryConfigurationError {
		String loginAtDomain = "jribiera@obm.lng.org";
		BackendSession backendSession = buildBackendSession(loginAtDomain);
		
		IApplicationData data = getApplicationData("HTC-Windows-Mobile-6.1-new_event.xml");
		Event event = eventConverter.convertAsInternal(backendSession, data);
		
		Attendee organizer = event.findOrganizer();
		List<Attendee> attendees = listAttendeesWithoutOrganizer(organizer, event);  
		
		assertNotNull(event);
		assertEquals("Windows Mobile 6.1 - HTC", event.getTitle());
		
		checkOrganizer(loginAtDomain, organizer);
		
		assertThat(event.getAttendees()).hasSize(4);
		assertThat(attendees).hasSize(3).excludes(organizer);
		checkAttendeeParticipationState(attendees);
	}
	
	@Test
	public void testAttendeesWithOrganizerEmailInNewEventStream() throws SAXException, IOException, FactoryConfigurationError {
		String loginAtDomain = "jribier@obm.lng.org";
		BackendSession backendSession = buildBackendSession(loginAtDomain);
		
		IApplicationData data = getApplicationData("Galaxy-S-Android-2.3.4-new_event.xml");
		Event event = eventConverter.convertAsInternal(backendSession, data);

		Attendee organizer = event.findOrganizer();
		List<Attendee> attendees = listAttendeesWithoutOrganizer(organizer, event); 
		
		Assert.assertNotNull(event);
		Assert.assertEquals("Android 2.3.4 - Galaxy S", event.getTitle());

		checkOrganizer("jribiera@obm.lng.org", organizer);
		
		assertThat(event.getAttendees()).hasSize(4);
		assertThat(attendees).hasSize(3).excludes(organizer);
		checkAttendeeParticipationState(attendees);
	}

	private List<Attendee> listAttendeesWithoutOrganizer(Attendee organizer, Event event) {
		List<Attendee> attendees = Lists.newArrayList(event.getAttendees());
		attendees.remove(organizer);
		return attendees;
	}
	
	private void checkOrganizer(String loginAtDomain, Attendee organizer) {
		Assert.assertNotNull(organizer);
		Assert.assertEquals(loginAtDomain, organizer.getEmail());
		Assert.assertNull(organizer.getDisplayName());
		Assert.assertEquals(ParticipationState.ACCEPTED, organizer.getState());
		Assert.assertEquals(ParticipationRole.REQ, organizer.getRequired());
		Assert.assertTrue(organizer.isOrganizer());
	}

	private void checkAttendeeParticipationState(List<Attendee> attendeesListWithoutOrganizer) {
		for (Attendee attendee: attendeesListWithoutOrganizer) {
			Assert.assertEquals(ParticipationState.NEEDSACTION, attendee.getState());
			Assert.assertFalse(attendee.isOrganizer());
		}
	}
	
	private IApplicationData getApplicationData(String filename) throws SAXException, IOException, FactoryConfigurationError {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("xml/" + filename); 
		Document document = DOMUtils.parse(inputStream);
		return decoder.decode(document.getDocumentElement()); 
	}
	
	private BackendSession buildBackendSession(String loginAtDomain) {
		BackendSession bs = new BackendSession(new Credentials(loginAtDomain, "test"),
				"Sync", new Device(1, "devType", "devId", new Properties()), new BigDecimal("12.5"));
		return bs;
	}
	
}