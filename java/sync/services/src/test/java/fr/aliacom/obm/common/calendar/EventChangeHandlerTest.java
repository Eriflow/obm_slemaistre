package fr.aliacom.obm.common.calendar;

import static fr.aliacom.obm.ToolBox.getDefaultObmDomain;
import static fr.aliacom.obm.ToolBox.getDefaultObmUser;
import static fr.aliacom.obm.ToolBox.getDefaultProducer;
import static fr.aliacom.obm.ToolBox.getDefaultSettings;
import static fr.aliacom.obm.ToolBox.getDefaultSettingsService;
import static fr.aliacom.obm.common.calendar.EventChangeHandlerTestsTools.after;
import static fr.aliacom.obm.common.calendar.EventChangeHandlerTestsTools.compareCollections;
import static fr.aliacom.obm.common.calendar.EventChangeHandlerTestsTools.createRequiredAttendee;
import static fr.aliacom.obm.common.calendar.EventChangeHandlerTestsTools.createRequiredAttendees;
import static fr.aliacom.obm.common.calendar.EventChangeHandlerTestsTools.longAfter;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.jms.JMSException;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.server.mailer.EventChangeMailer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.linagora.obm.sync.Producer;

import fr.aliacom.obm.common.calendar.EventChangeHandler.AttendeeStateValue;
import fr.aliacom.obm.common.setting.SettingsService;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserService;
import fr.aliacom.obm.common.user.UserSettings;
import fr.aliacom.obm.utils.HelperService;
import fr.aliacom.obm.utils.Ical4jHelper;

@RunWith(Suite.class)
@SuiteClasses({
	EventChangeHandlerTest.UpdateTests.class, 
	EventChangeHandlerTest.CreateTests.class, 
	EventChangeHandlerTest.DeleteTests.class, 
	EventChangeHandlerTest.UpdateParticipationTests.class,
	EventChangeHandlerTest.ComputeAttendeesDiffsTests.class})

public class EventChangeHandlerTest {
	
	private static final String ICS_DATA_ADD = "ics data add attendee";
	private static final String ICS_DATA_REMOVE = "ics data remove attendee";
	private static final String ICS_DATA_UPDATE = "ics data update attendee";
	private static final String ICS_DATA_REPLY = "ics data reply attendee";
	
	private static final Locale LOCALE = Locale.FRENCH;
	private static final TimeZone TIMEZONE = TimeZone.getTimeZone("Europe/Paris");
	
	private static EventChangeHandler newEventChangeHandler(
			EventChangeMailer mailer, SettingsService settingsService, UserService userService,
			Producer producer, Ical4jHelper ical4jHelper) {
		
		return new EventChangeHandler(mailer, settingsService, userService, producer, ical4jHelper);
	}
	
	private static EventChangeHandler updateEventChangeHandler(EventChangeMailer mailer, Ical4jHelper ical4jHelper) throws JMSException  {
		UserService userService = EasyMock.createMock(UserService.class);
		SettingsService settingsService = getDefaultSettingsService();
		Producer producer = getDefaultProducer();
		producer.write(ICS_DATA_ADD);
		producer.write(ICS_DATA_REMOVE);
		producer.write(ICS_DATA_UPDATE);
		EasyMock.replay(userService, settingsService);
		return newEventChangeHandler(mailer, settingsService, userService, producer, ical4jHelper);
	}
	
	private static AccessToken getMockAccessToken()
	{
		AccessToken token = new AccessToken(2, 1, "unitTest-origin");
		token.setDomain("unitTest-domain");
		token.setEmail("unitTest-email");
		return token;
	}
	
	public static class CreateTests extends AbstractEventChangeHandlerTest {
		
		@Override
		protected void processEvent(EventChangeHandler eventChangeHandler, Event event, ObmUser obmUser) {
			eventChangeHandler.create(obmUser, event, true, getMockAccessToken());
		}
		
		@Override
		public void testDefaultEvent() {
			super.testDefaultEvent();
		}
		
		@Override
		public void testEventInThePast() {
			super.testEventInThePast();
		}
		
		@Override
		public void testNoAttendee() {
			super.testNoAttendee();
		}
		
		@Override
		public void testOnlyOwnerIsAttendee() {
			super.testOnlyOwnerIsAttendee();
		}
		
		@Test
		public void testAcceptedAttendee() {
			super.testAcceptedAttendee();
		}
		
		@Test
		public void testNeedActionAttendee() {
			super.testNeedActionAttendee();
		}
		
		@Test
		public void testDeclinedAttendee() {
			super.testDeclinedAttendee();
		}
		
		@Test
		public void testObmUserIsNotOwner() {
			super.testObmUserIsNotOwner();
		}
		
		@Override
		protected EventChangeMailer expectationAcceptedAttendees(
				Attendee attendeeAccepted, Event event, ObmUser obmUser) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyAcceptedNewUsers(eq(obmUser), compareCollections(ImmutableList.of(attendeeAccepted)), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE), anyObject(AccessToken.class));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}

		@Override
		protected EventChangeMailer expectationNeedActionAttendees(
				Attendee attendeeNeedAction, String icsData, Event event, ObmUser obmUser) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			String ics = ical4jHelper.buildIcsInvitationRequest(obmUser, event);
			mailer.notifyNeedActionNewUsers(eq(obmUser), compareCollections(ImmutableList.of(attendeeNeedAction)), 
					anyObject(Event.class), eq(LOCALE), eq(TIMEZONE), eq(ics), anyObject(AccessToken.class));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}

		@Override
		protected EventChangeMailer expectationDeclinedAttendees(
				Attendee attendeeDeclined, Event event, ObmUser obmUser) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			replay(mailer);
			return mailer;
		}
		
		@Test
		public void testTwoAttendee() {
			super.testTwoAttendee();
		}
		
		@Override
		protected EventChangeMailer expectationTwoAttendees(Attendee attendeeAccepted, Attendee attendeeNotAccepted, 
				Event event, ObmUser obmUser) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			String ics = ical4jHelper.buildIcsInvitationRequest(obmUser, event);
			mailer.notifyAcceptedNewUsers(eq(obmUser), compareCollections(ImmutableList.of(attendeeAccepted)), eq(event), eq(LOCALE), eq(TIMEZONE), anyObject(AccessToken.class));
			expectLastCall().once();
			mailer.notifyNeedActionNewUsers(eq(obmUser), compareCollections(ImmutableList.of(attendeeNotAccepted)), 
					eq(event), eq(LOCALE), eq(TIMEZONE), eq(ics), anyObject(AccessToken.class));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}
		
		@Test
		public void testSameAttendeeTwice() {
			super.testSameAttendeeTwice();
		}
		
		@Override
		protected EventChangeMailer expectationSameAttendeeTwice(Attendee attendee, Event event, ObmUser obmUser) {
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			String ics = ical4jHelper.buildIcsInvitationRequest(obmUser, event);
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyNeedActionNewUsers(eq(obmUser), compareCollections(ImmutableList.of(attendee)), 
					anyObject(Event.class), eq(LOCALE), eq(TIMEZONE), eq(ics), EasyMock.anyObject(AccessToken.class));
			EasyMock.expectLastCall().once();
			EasyMock.replay(mailer);
			return mailer;
		}

		@Test
		public void testManyAttendees() {
			super.testManyAttendees();
		}
		
		@Override
		protected EventChangeMailer expectationManyAttendee(List<Attendee> needActionAttendees, List<Attendee> accpetedAttendees,
				Event event, ObmUser obmUser) {
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			String ics = ical4jHelper.buildIcsInvitationRequest(obmUser, event);
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyAcceptedNewUsers(eq(obmUser), compareCollections(accpetedAttendees), eq(event), eq(LOCALE), eq(TIMEZONE), EasyMock.anyObject(AccessToken.class));
			expectLastCall().once();
			mailer.notifyNeedActionNewUsers(eq(obmUser), compareCollections(needActionAttendees), 
					eq(event), eq(LOCALE), eq(TIMEZONE), eq(ics), EasyMock.anyObject(AccessToken.class));
			expectLastCall().once();
			replay(mailer);
			return mailer;

		}

		@Override
		protected EventChangeMailer expectationObmUserIsNotOwner(ObmUser synchronizer, Attendee owner) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			List<Attendee> ownerAsList = new ArrayList<Attendee>();
			ownerAsList.add(owner);
			mailer.notifyAcceptedNewUsers(eq(synchronizer), eq(ownerAsList), EasyMock.anyObject(Event.class), eq(LOCALE), eq(TIMEZONE), EasyMock.anyObject(AccessToken.class));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}
	}
	
	public static class DeleteTests extends AbstractEventChangeHandlerTest {
		
		@Override
		protected void processEvent(EventChangeHandler eventChangeHandler, Event event, ObmUser obmUser) {
			eventChangeHandler.delete(obmUser, event, true, getMockAccessToken());
		}
		
		@Test
		public void testDefaultEvent() {
			super.testDefaultEvent();
		}
		
		@Test
		public void testNoAttendee() {
			super.testNoAttendee();
		}
		
		@Test
		public void testOnlyOwnerIsAttendee() {
			super.testOnlyOwnerIsAttendee();
		}
		
		@Test
		public void testEventInThePast() {
			super.testEventInThePast();
		}
		
		@Test
		public void testAcceptedAttendee() {
			super.testAcceptedAttendee();
		}
		
		@Test
		public void testNeedActionAttendee() {
			super.testNeedActionAttendee();
		}
		
		@Test
		public void testDeclinedAttendee() {
			super.testDeclinedAttendee();
		}

		@Test
		public void testObmUserIsNotOwner() {
			super.testObmUserIsNotOwner();
		}
		
		@Override
		protected EventChangeMailer expectationAcceptedAttendees(Attendee attendeeAccepted, Event event, ObmUser obmUser) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			String ics = ical4jHelper.buildIcsInvitationCancel(obmUser, event);
			mailer.notifyRemovedUsers(eq(obmUser), compareCollections(ImmutableList.of(attendeeAccepted)), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE), eq(ics), EasyMock.anyObject(AccessToken.class));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}

		@Override
		protected EventChangeMailer expectationNeedActionAttendees(
				Attendee attendeeNeedAction, String icsData, Event event, ObmUser obmUser) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			String ics = ical4jHelper.buildIcsInvitationCancel(obmUser, event);
			mailer.notifyRemovedUsers(eq(obmUser), compareCollections(ImmutableList.of(attendeeNeedAction)), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE), eq(ics), anyObject(AccessToken.class));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}

		@Override
		protected EventChangeMailer expectationDeclinedAttendees(Attendee attendeeDeclined, Event event, ObmUser obmUser) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			replay(mailer);
			return mailer;
		}
		
		@Test
		public void testTwoAttendee() {
			super.testTwoAttendee();
		}
		
		@Override
		protected EventChangeMailer expectationTwoAttendees( Attendee attendeeAccepted, Attendee attendeeNotAccepted, Event event,
				ObmUser obmUser) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			String ics = ical4jHelper.buildIcsInvitationCancel(obmUser, event);
			mailer.notifyRemovedUsers(eq(obmUser), compareCollections(ImmutableList.of(attendeeNotAccepted, attendeeAccepted )), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE), eq(ics), anyObject(AccessToken.class));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}
		
		@Test
		public void testSameAttendeeTwice() {
			super.testSameAttendeeTwice();
		}
		
		@Override
		protected EventChangeMailer expectationSameAttendeeTwice(Attendee attendee, Event event, ObmUser obmUser) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			String ics = ical4jHelper.buildIcsInvitationCancel(obmUser, event);
			mailer.notifyRemovedUsers(eq(obmUser), compareCollections(ImmutableList.of(attendee)), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE), eq(ics), anyObject(AccessToken.class));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}
		
		@Test
		public void testManyAttendees() {
			super.testManyAttendees();
		}
		
		@Override
		protected EventChangeMailer expectationManyAttendee(List<Attendee> needActionAttendees, List<Attendee> accpetedAttendees,
				Event event, ObmUser obmUser) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			List<Attendee> atts = Lists.newArrayList();
			atts.addAll(needActionAttendees);
			atts.addAll(accpetedAttendees);
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			String ics = ical4jHelper.buildIcsInvitationCancel(obmUser, event);
			mailer.notifyRemovedUsers(eq(obmUser), compareCollections(atts), anyObject(Event.class), eq(LOCALE), eq(TIMEZONE), eq(ics), anyObject(AccessToken.class));
			expectLastCall().once();
			replay(mailer);
			return mailer;
		}
		
		@Override
		protected EventChangeMailer expectationObmUserIsNotOwner(ObmUser synchronizer, Attendee owner) {
			EventChangeMailer mailer = createMock(EventChangeMailer.class);

			mailer.notifyOwnerRemovedEvent(eq(synchronizer), eq(owner), EasyMock.anyObject(Event.class), eq(LOCALE), eq(TIMEZONE), anyObject(AccessToken.class));
			EasyMock.expectLastCall().once();
			EasyMock.replay(mailer);
			return mailer;
		}
		
	}

	public static class UpdateTests {

		@Test
		public void testDefaultEventNoChange() throws JMSException {
			Event event = new Event();
			event.setDate(after());
			ObmUser defaultObmUser = getDefaultObmUser();
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, event)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(defaultObmUser, event)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, event)).andReturn(ICS_DATA_UPDATE);
			
			HelperService helper = createMock(HelperService.class);
			
			replay(mailer, ical4jHelper, helper);
			
			EventChangeHandler eventChangeHandler = updateEventChangeHandler(mailer, ical4jHelper);
			eventChangeHandler.update(defaultObmUser, event, event, true, false, getMockAccessToken());
			verify(mailer, ical4jHelper);
		}

		@Test
		public void testDefaultEventDateChangeZeroUser() throws JMSException {
			Event event = new Event();
			event.setDate(after());
			Event eventAfter = new Event();
			eventAfter.setDate(longAfter());
			ObmUser defaultObmUser = getDefaultObmUser();
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, eventAfter)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(defaultObmUser, eventAfter)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, eventAfter)).andReturn(ICS_DATA_UPDATE);
			replay(mailer, ical4jHelper);
			
			EventChangeHandler eventChangeHandler = updateEventChangeHandler(mailer, ical4jHelper);
			eventChangeHandler.update(defaultObmUser, event, eventAfter, true, true, getMockAccessToken());
			verify(mailer, ical4jHelper);
		}
		
		@Test
		public void testDefaultEventDateChangeOneNeedActionUser() throws JMSException {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.NEEDSACTION);
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);
			Event currentEvent = new Event();
			currentEvent.setDate(longAfter());
			currentEvent.addAttendee(attendee);
			ObmUser defaultObmUser = getDefaultObmUser();

			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, currentEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(defaultObmUser, currentEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, currentEvent)).andReturn(ICS_DATA_UPDATE);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyNeedActionUpdateUsers(eq(defaultObmUser), compareCollections(ImmutableList.of(attendee)), 
					eq(previousEvent), eq(currentEvent), eq(LOCALE), eq(TIMEZONE), eq(ICS_DATA_UPDATE), anyObject(AccessToken.class));
			expectLastCall().once();
			
			replay(mailer, ical4jHelper);
			
			EventChangeHandler eventChangeHandler = updateEventChangeHandler(mailer, ical4jHelper);
			eventChangeHandler.update(defaultObmUser, previousEvent, currentEvent, true, true, getMockAccessToken());
			
			verify(mailer, ical4jHelper);
		}
		
		@Test
		public void testDefaultEventDateChangeOneAcceptedUser() throws JMSException {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.ACCEPTED);
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);
			Event currentEvent = new Event();
			currentEvent.setDate(longAfter());
			currentEvent.addAttendee(attendee);
			ObmUser defaultObmUser = getDefaultObmUser();
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, currentEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(defaultObmUser, currentEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, currentEvent)).andReturn(ICS_DATA_UPDATE);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyAcceptedUpdateUsers(eq(defaultObmUser), compareCollections(ImmutableList.of(attendee)), 
					eq(previousEvent), eq(currentEvent), eq(LOCALE), eq(TIMEZONE), eq(ICS_DATA_UPDATE), anyObject(AccessToken.class));
			
			expectLastCall().once();
			replay(mailer, ical4jHelper);
			
			EventChangeHandler eventChangeHandler = updateEventChangeHandler(mailer, ical4jHelper);
			eventChangeHandler.update(defaultObmUser, previousEvent, currentEvent, true, true, getMockAccessToken());
			
			verify(mailer, ical4jHelper);
		}
		
		@Test
		public void testDefaultEventNoChangeOneNeedActionUser() throws JMSException {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.NEEDSACTION);
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);
			ObmUser defaultObmUser = getDefaultObmUser();
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, previousEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(defaultObmUser, previousEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, previousEvent)).andReturn(ICS_DATA_UPDATE);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			replay(mailer, ical4jHelper);
			
			EventChangeHandler eventChangeHandler = updateEventChangeHandler(mailer, ical4jHelper);
			eventChangeHandler.update(defaultObmUser, previousEvent, previousEvent, true, false, getMockAccessToken());
			verify(mailer, ical4jHelper);
		}
		
		@Test
		public void testDefaultEventNoChangeOneAcceptedUser() throws JMSException {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.ACCEPTED);
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);
			ObmUser defaultObmUser = getDefaultObmUser();
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, previousEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(defaultObmUser, previousEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, previousEvent)).andReturn(ICS_DATA_UPDATE);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			replay(mailer, ical4jHelper);
			
			EventChangeHandler eventChangeHandler = updateEventChangeHandler(mailer, ical4jHelper);
			eventChangeHandler.update(defaultObmUser, previousEvent, previousEvent, true, false, getMockAccessToken());
			verify(mailer, ical4jHelper);
		}
		
		@Test
		public void testDefaultEventAddOneNeedActionUser() throws JMSException {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.NEEDSACTION);
			Attendee addedAttendee = createRequiredAttendee("addedeAttendee@test", ParticipationState.NEEDSACTION);
			
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);

			Event currentEvent = new Event();
			currentEvent.setDate(after());
			currentEvent.addAttendee(attendee);
			currentEvent.addAttendee(addedAttendee);
			ObmUser defaultObmUser = getDefaultObmUser();
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, currentEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(defaultObmUser, currentEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, currentEvent)).andReturn(ICS_DATA_UPDATE);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyNeedActionNewUsers(eq(defaultObmUser), compareCollections(ImmutableList.of(addedAttendee)), 
					eq(currentEvent), eq(LOCALE), eq(TIMEZONE), eq(ICS_DATA_ADD), anyObject(AccessToken.class));
			expectLastCall().once();

			replay(mailer, ical4jHelper);

			EventChangeHandler eventChangeHandler = updateEventChangeHandler(mailer, ical4jHelper);
			eventChangeHandler.update(defaultObmUser, previousEvent, currentEvent, true, false, getMockAccessToken());
			
			verify(mailer, ical4jHelper);
		}
		
		@Test
		public void testDefaultEventAddOneAcceptedUser() throws JMSException {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.ACCEPTED);
			Attendee addedAttendee = createRequiredAttendee("addedeAttendee@test", ParticipationState.ACCEPTED);
			
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);

			Event currentEvent = new Event();
			currentEvent.setDate(after());
			currentEvent.addAttendee(attendee);
			currentEvent.addAttendee(addedAttendee);
			ObmUser defaultObmUser = getDefaultObmUser();
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, currentEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(defaultObmUser, currentEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, currentEvent)).andReturn(ICS_DATA_UPDATE);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyAcceptedNewUsers(eq(defaultObmUser), compareCollections(ImmutableList.of(addedAttendee)), eq(currentEvent), eq(LOCALE), eq(TIMEZONE), anyObject(AccessToken.class));
			expectLastCall().once();

			replay(mailer, ical4jHelper);
			
			EventChangeHandler eventChangeHandler = updateEventChangeHandler(mailer, ical4jHelper);
			eventChangeHandler.update(defaultObmUser, previousEvent, currentEvent, true, false, getMockAccessToken());
			
			verify(mailer, ical4jHelper);
		}

		@Test
		public void testDefaultEventDateChangeOneAcceptedUserWithCanWriteOnCalendar() throws JMSException {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.ACCEPTED);
			attendee.setCanWriteOnCalendar(true);
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);
			Event currentEvent = new Event();
			currentEvent.setDate(longAfter());
			currentEvent.addAttendee(attendee);
			ObmUser defaultObmUser = getDefaultObmUser();
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, currentEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(defaultObmUser, currentEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, currentEvent)).andReturn(ICS_DATA_UPDATE);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyAcceptedUpdateUsersCanWriteOnCalendar(eq(defaultObmUser), compareCollections(ImmutableList.of(attendee)), 
					eq(previousEvent), eq(currentEvent), eq(LOCALE), eq(TIMEZONE), anyObject(AccessToken.class));
			
			expectLastCall().once();
			replay(mailer, ical4jHelper);

			EventChangeHandler eventChangeHandler = updateEventChangeHandler(mailer, ical4jHelper);
			eventChangeHandler.update(defaultObmUser, previousEvent, currentEvent, true, true, getMockAccessToken());

			verify(mailer, ical4jHelper);
		}

		public void testUserIsNotEventOwnerAddOneAcceptedUser() throws JMSException {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.ACCEPTED);
			Attendee addedAttendee = createRequiredAttendee("addedeAttendee@test", ParticipationState.ACCEPTED);

			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);
			previousEvent.setOwnerEmail("attendee1@test");

			Event currentEvent = new Event();
			currentEvent.setDate(after());
			currentEvent.addAttendee(attendee);
			currentEvent.addAttendee(addedAttendee);
			currentEvent.setOwnerEmail("attendee1@test");
			ObmUser defaultObmUser = getDefaultObmUser();
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, currentEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(defaultObmUser, currentEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, currentEvent)).andReturn(ICS_DATA_UPDATE);

			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyAcceptedNewUsers(eq(defaultObmUser), compareCollections(ImmutableList.of(addedAttendee)), eq(currentEvent), eq(LOCALE), eq(TIMEZONE), anyObject(AccessToken.class));

			expectLastCall().once();

			replay(mailer, ical4jHelper);

			EventChangeHandler eventChangeHandler = updateEventChangeHandler(mailer, ical4jHelper);
			eventChangeHandler.update(defaultObmUser, previousEvent, currentEvent, true, false, getMockAccessToken());

			verify(mailer, ical4jHelper);
		}

		@Test
		public void testUserIsNotEventOwnerDefaultEventDateChangeOneAcceptedUser() throws JMSException {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.ACCEPTED);
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);
			previousEvent.setOwnerEmail("attendee1@test");
			Event currentEvent = new Event();
			currentEvent.setDate(longAfter());
			currentEvent.addAttendee(attendee);
			currentEvent.setOwnerEmail("attendee1@test");
			ObmUser defaultObmUser = getDefaultObmUser();
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, currentEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(defaultObmUser, currentEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(defaultObmUser, currentEvent)).andReturn(ICS_DATA_UPDATE);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyOwnerUpdate(eq(defaultObmUser), eq(attendee), eq(previousEvent), eq(currentEvent), eq(LOCALE), eq(TIMEZONE), anyObject(AccessToken.class));
			replay(mailer, ical4jHelper);

			EventChangeHandler eventChangeHandler = updateEventChangeHandler(mailer, ical4jHelper);
			eventChangeHandler.update(defaultObmUser, previousEvent, currentEvent, true, true, getMockAccessToken());

			verify(mailer, ical4jHelper);
		}
		
	}

	public static class UpdateParticipationTests {
		
		@Test
		public void testParticipationChangeWithOwnerExpectingEmails() {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.NEEDSACTION);
			Attendee organizer = createRequiredAttendee("organizer@test", ParticipationState.ACCEPTED);
			organizer.setOrganizer(true);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			
			Event event = new Event();
			event.setDate(after());
			event.addAttendee(attendee);
			event.addAttendee(organizer);
			
			ObmUser attendeeUser = new ObmUser();
			attendeeUser.setEmail(attendee.getEmail());
			attendeeUser.setDomain(getDefaultObmDomain());
			
			ObmUser organizerUser = new ObmUser();
			attendeeUser.setEmail(attendee.getEmail());
			attendeeUser.setDomain(getDefaultObmDomain());
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationReply(event, attendeeUser)).andReturn(ICS_DATA_REPLY);
			
			mailer.notifyUpdateParticipationState(
					eq(event), eq(organizer), eq(attendeeUser),
					eq(ParticipationState.ACCEPTED), eq(LOCALE), eq(TIMEZONE), eq(ICS_DATA_REPLY), anyObject(AccessToken.class));
			expectLastCall().once();
			
			UserService userService = EasyMock.createMock(UserService.class);
			userService.getUserFromLogin(organizer.getEmail(), attendeeUser.getDomain().getName());
			EasyMock.expectLastCall().andReturn(organizerUser).once();

			UserSettings settings = getDefaultSettings();
			EasyMock.expect(settings.expectParticipationEmailNotification()).andReturn(true).once();
			
			SettingsService settingsService = EasyMock.createMock(SettingsService.class);
			settingsService.getSettings(eq(organizerUser));
			EasyMock.expectLastCall().andReturn(settings).once();
			settingsService.getSettings(eq(attendeeUser));
			EasyMock.expectLastCall().andReturn(settings).once();
			Producer producer = getDefaultProducer();
			
			EasyMock.replay(userService, settingsService, settings, mailer, ical4jHelper);
			
			EventChangeHandler handler = newEventChangeHandler(mailer, settingsService, userService, producer, ical4jHelper);
			handler.updateParticipationState(event, attendeeUser, ParticipationState.ACCEPTED, true, getMockAccessToken());
			
			verify(userService, settingsService, settings, mailer, ical4jHelper);
		}
		
		@Test
		public void testParticipationChangeWithExternalOrganizer() {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.NEEDSACTION);
			Attendee organizer = createRequiredAttendee("organizer@test", ParticipationState.ACCEPTED);
			organizer.setOrganizer(true);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			
			Event event = new Event();
			event.setDate(after());
			event.addAttendee(attendee);
			event.addAttendee(organizer);
			
			ObmUser attendeeUser = new ObmUser();
			attendeeUser.setEmail(attendee.getEmail());
			attendeeUser.setDomain(getDefaultObmDomain());
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationReply(event, attendeeUser)).andReturn(ICS_DATA_REPLY);
			
			mailer.notifyUpdateParticipationState(
					eq(event), eq(organizer), eq(attendeeUser),
					eq(ParticipationState.ACCEPTED), eq(LOCALE), eq(TIMEZONE), eq(ICS_DATA_REPLY), anyObject(AccessToken.class));
			expectLastCall().once();
			
			UserService userService = EasyMock.createMock(UserService.class);
			userService.getUserFromLogin(organizer.getEmail(), attendeeUser.getDomain().getName());
			EasyMock.expectLastCall().andReturn(null).once();

			UserSettings settings = getDefaultSettings();
			
			SettingsService settingsService = EasyMock.createMock(SettingsService.class);
			settingsService.getSettings(eq(attendeeUser));
			EasyMock.expectLastCall().andReturn(settings).once();
			Producer producer = getDefaultProducer();
			
			EasyMock.replay(userService, settingsService, settings, mailer, ical4jHelper);
			
			EventChangeHandler handler = newEventChangeHandler(mailer, settingsService, userService, producer, ical4jHelper);
			handler.updateParticipationState(event, attendeeUser, ParticipationState.ACCEPTED, true, getMockAccessToken());
			
			verify(userService, settingsService, settings, mailer, ical4jHelper);
		}

		@Test
		public void testParticipationChangeWithOwnerNotExpectingEmails() {
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.NEEDSACTION);
			Attendee organizer = createRequiredAttendee("organizer@test", ParticipationState.ACCEPTED);
			organizer.setOrganizer(true);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			
			Event event = new Event();
			event.setDate(after());
			event.addAttendee(attendee);
			event.addAttendee(organizer);
			
			ObmUser attendeeUser = new ObmUser();
			attendeeUser.setEmail(attendee.getEmail());
			attendeeUser.setDomain(getDefaultObmDomain());
			
			ObmUser organizerUser = new ObmUser();
			attendeeUser.setEmail(attendee.getEmail());
			attendeeUser.setDomain(getDefaultObmDomain());
			
			UserService userService = EasyMock.createMock(UserService.class);
			userService.getUserFromLogin(organizer.getEmail(), attendeeUser.getDomain().getName());
			EasyMock.expectLastCall().andReturn(organizerUser).once();

			UserSettings settings = getDefaultSettings();
			EasyMock.expect(settings.expectParticipationEmailNotification()).andReturn(false).once();
			
			SettingsService settingsService = EasyMock.createMock(SettingsService.class);
			settingsService.getSettings(eq(organizerUser));
			EasyMock.expectLastCall().andReturn(settings).once();
			
			Producer producer = getDefaultProducer();
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.replay(userService, settingsService, settings, mailer);
			
			EventChangeHandler handler = newEventChangeHandler(mailer, settingsService, userService, producer, ical4jHelper);
			handler.updateParticipationState(event, attendeeUser, ParticipationState.ACCEPTED, true, getMockAccessToken());
			verify(userService, settingsService, settings, mailer);
		}
	}

	public static class ComputeAttendeesDiffsTests {

		@Test
		public void testComputeUpdateNotificationDiffWithNoKeptAttendees() {
			final String addedUserMail = "new@testing.org";
			final String removedUserMail = "old@testing.org";

			List<Attendee> previousAtts = ImmutableList.of(createRequiredAttendee(removedUserMail, ParticipationState.ACCEPTED));
			List<Attendee> currentAtts = ImmutableList.of(createRequiredAttendee(addedUserMail, ParticipationState.NEEDSACTION));

			Event previous = new Event();
			Event current = new Event();

			EventChangeHandler eventChangeHandler = new EventChangeHandler(
					null, null, null, null, null);

			previous.setAttendees(previousAtts);
			current.setAttendees(currentAtts);

			Map<AttendeeStateValue, Set<Attendee>> groups = eventChangeHandler
					.computeUpdateNotificationGroups(previous, current);

			Assert.assertTrue(groups.get(AttendeeStateValue.KEPT).isEmpty());
			Assert.assertEquals(removedUserMail, groups.get(AttendeeStateValue.REMOVED).iterator().next().getEmail());
			Assert.assertEquals(addedUserMail, groups.get(AttendeeStateValue.ADDED).iterator().next().getEmail());
			Assert.assertEquals(ParticipationState.ACCEPTED, groups.get(AttendeeStateValue.REMOVED).iterator().next().getState());
			Assert.assertEquals(ParticipationState.NEEDSACTION,	groups.get(AttendeeStateValue.ADDED).iterator().next().getState());
		}

		@Test
		public void testComputeUpdateNotificationSimpleDiff() {
			final String addedUserMail = "user2@testing.org";
			final String keptUserMail = "user1@testing.org";
			final String removedUserMail = "user0@testing.org";
			List<Attendee> previousAtts = createRequiredAttendees("user", "@testing.org", ParticipationState.ACCEPTED, 0, 2);
			List<Attendee> currentAtts = createRequiredAttendees("user", "@testing.org", ParticipationState.NEEDSACTION, 1, 2);
			Event previous = new Event();
			Event current = new Event();
			EventChangeHandler eventChangeHandler = new EventChangeHandler(null, null, null, null, null);

			previous.setAttendees(previousAtts);
			current.setAttendees(currentAtts);

			Map<AttendeeStateValue, Set<Attendee>> groups =
				eventChangeHandler.computeUpdateNotificationGroups(previous, current);

			Assert.assertEquals(keptUserMail, groups.get(AttendeeStateValue.KEPT).iterator().next().getEmail());
			Assert.assertEquals(removedUserMail, groups.get(AttendeeStateValue.REMOVED).iterator().next().getEmail());
			Assert.assertEquals(addedUserMail, groups.get(AttendeeStateValue.ADDED).iterator().next().getEmail());
			Assert.assertEquals(ParticipationState.NEEDSACTION, groups.get(AttendeeStateValue.KEPT).iterator().next().getState());
			Assert.assertEquals(ParticipationState.ACCEPTED, groups.get(AttendeeStateValue.REMOVED).iterator().next().getState());
			Assert.assertEquals(ParticipationState.NEEDSACTION, groups.get(AttendeeStateValue.ADDED).iterator().next().getState());
		}

		@Test
		public void testComputeUpdateNotificationGroupsWhereAttendeesComeFrom() {
			final String email = "myEmail";
			EventChangeHandler eventChangeHandler = new EventChangeHandler(null, null, null, null, null);

			Attendee attendee1 = new Attendee();
			attendee1.setEmail(email);
			Event event1 = new Event();
			event1.setAttendees(ImmutableList.of(attendee1));

			Attendee attendee2 = new Attendee();
			attendee2.setEmail(email);
			Event event2 = new Event();
			event2.setAttendees(ImmutableList.of(attendee2));

			Map<AttendeeStateValue, Set<Attendee>> groups =
					eventChangeHandler.computeUpdateNotificationGroups(event1, event2);
			Set<Attendee> actual = groups.get(AttendeeStateValue.KEPT);

			Assert.assertSame(attendee2, actual.iterator().next());
		}

		@Test
		public void testComputeUpdateNotificationGroupsConcurrentModificationBug() {
			final String email = "myEmail";
			EventChangeHandler eventChangeHandler = new EventChangeHandler(null, null, null, null, null);

			Attendee attendee1 = new Attendee();
			attendee1.setEmail(email);
			Event event1 = new Event();
			event1.setAttendees(ImmutableList.of(attendee1));

			Attendee attendee2 = new Attendee();
			attendee2.setEmail(email);
			Attendee attendee3 = new Attendee();
			attendee3.setEmail("another email");
			Attendee attendee4 = new Attendee();
			attendee4.setEmail("yet another email");
			Event event2 = new Event();
			event2.setAttendees(ImmutableList.of(attendee2, attendee3, attendee4));

			Map<AttendeeStateValue, Set<Attendee>> groups =
					eventChangeHandler.computeUpdateNotificationGroups(event1, event2);
			Set<Attendee> actual = groups.get(AttendeeStateValue.KEPT);

			Assert.assertSame(attendee2, actual.iterator().next());
		}
	}
}
