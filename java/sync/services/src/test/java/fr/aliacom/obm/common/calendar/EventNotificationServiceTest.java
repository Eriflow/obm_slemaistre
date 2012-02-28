/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package fr.aliacom.obm.common.calendar;

import static fr.aliacom.obm.ToolBox.getDefaultObmDomain;
import static fr.aliacom.obm.ToolBox.getDefaultObmUser;
import static fr.aliacom.obm.ToolBox.getDefaultSettings;
import static fr.aliacom.obm.ToolBox.getDefaultSettingsService;
import static fr.aliacom.obm.common.calendar.EventNotificationServiceTestTools.after;
import static fr.aliacom.obm.common.calendar.EventNotificationServiceTestTools.compareCollections;
import static fr.aliacom.obm.common.calendar.EventNotificationServiceTestTools.createRequiredAttendee;
import static fr.aliacom.obm.common.calendar.EventNotificationServiceTestTools.createRequiredAttendees;
import static fr.aliacom.obm.common.calendar.EventNotificationServiceTestTools.longAfter;
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

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.obm.icalendar.ICalendarFactory;
import org.obm.icalendar.Ical4jHelper;
import org.obm.icalendar.Ical4jUser;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.server.mailer.EventChangeMailer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.calendar.EventNotificationServiceImpl.AttendeeStateValue;
import fr.aliacom.obm.common.setting.SettingsService;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserService;
import fr.aliacom.obm.common.user.UserSettings;
import fr.aliacom.obm.utils.HelperService;

@RunWith(Suite.class)
@SuiteClasses({
	EventNotificationServiceTest.UpdateTests.class, 
	EventNotificationServiceTest.CreateTests.class, 
	EventNotificationServiceTest.DeleteTests.class, 
	EventNotificationServiceTest.UpdateParticipationTests.class,
	EventNotificationServiceTest.ComputeAttendeesDiffsTests.class})

public class EventNotificationServiceTest {
	
	private static final String ICS_DATA_ADD = "ics data add attendee";
	private static final String ICS_DATA_REMOVE = "ics data remove attendee";
	private static final String ICS_DATA_UPDATE = "ics data update attendee";
	private static final String ICS_DATA_REPLY = "ics data reply attendee";
	
	private static final Locale LOCALE = Locale.FRENCH;
	private static final TimeZone TIMEZONE = TimeZone.getTimeZone("Europe/Paris");
	
	private static EventNotificationService newEventNotificationServiceImpl(
			EventChangeMailer mailer, SettingsService settingsService, UserService userService,
			Ical4jHelper ical4jHelper, ICalendarFactory calendarFactory) {
		
		return new EventNotificationServiceImpl(mailer, settingsService, userService, ical4jHelper, calendarFactory);
	}
	
	private static EventNotificationService getEventNotificationServiceToNotifyUpdatedEvent(EventChangeMailer mailer, Ical4jHelper ical4jHelper, 
			ICalendarFactory calendarFactory, UserService userService)  {
		
		SettingsService settingsService = getDefaultSettingsService();
		EasyMock.replay(settingsService);
		return newEventNotificationServiceImpl(mailer, settingsService, userService, ical4jHelper, calendarFactory);
	}
	
	public static class CreateTests extends AbstractEventNotificationServiceTest {
		
		@Override
		protected void processEvent(EventNotificationService eventNotificationService, Event event, ObmUser obmUser, AccessToken accessToken) {
			eventNotificationService.notifyCreatedEvent(event, accessToken);
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
			String ics = ical4jHelper.buildIcsInvitationRequest(ToolBox.getIcal4jUser(obmUser), event);
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
			String ics = ical4jHelper.buildIcsInvitationRequest(ToolBox.getIcal4jUser(obmUser), event);
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
			String ics = ical4jHelper.buildIcsInvitationRequest(ToolBox.getIcal4jUser(obmUser), event);
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
			String ics = ical4jHelper.buildIcsInvitationRequest(ToolBox.getIcal4jUser(obmUser), event);
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
	
	public static class DeleteTests extends AbstractEventNotificationServiceTest {
		
		@Override
		protected void processEvent(EventNotificationService eventNotificationService, Event event, ObmUser obmUser, AccessToken accessToken) {
			eventNotificationService.notifyDeletedEvent(event, accessToken);
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
			String ics = ical4jHelper.buildIcsInvitationCancel(ToolBox.getIcal4jUser(obmUser), event);
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
			String ics = ical4jHelper.buildIcsInvitationCancel(ToolBox.getIcal4jUser(obmUser), event);
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
			String ics = ical4jHelper.buildIcsInvitationCancel(ToolBox.getIcal4jUser(obmUser), event);
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
			String ics = ical4jHelper.buildIcsInvitationCancel(ToolBox.getIcal4jUser(obmUser), event);
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
			String ics = ical4jHelper.buildIcsInvitationCancel(ToolBox.getIcal4jUser(obmUser), event);
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
		public void testDefaultEventNoChange() {
			Ical4jUser ical4jUser = ToolBox.getIcal4jUser();
			ObmUser defaultObmUser = getDefaultObmUser();

			Event event = new Event();
			event.setDate(after());
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(ical4jUser, event)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(ical4jUser, event)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(ical4jUser, event)).andReturn(ICS_DATA_UPDATE);
			
			HelperService helper = createMock(HelperService.class);
			
			ICalendarFactory calendarFactory = createMock(ICalendarFactory.class);
			EasyMock.expect(calendarFactory.createIcal4jUserFromObmUser(defaultObmUser)).andReturn(ical4jUser).anyTimes();
			
			AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
			
			UserService userService = EasyMock.createMock(UserService.class);
			EasyMock.expect(userService.getUserFromAccessToken(accessToken)).andReturn(defaultObmUser);
			
			replay(mailer, ical4jHelper, helper, calendarFactory, userService);
			
			EventNotificationService eventNotificationService = getEventNotificationServiceToNotifyUpdatedEvent(mailer, ical4jHelper, calendarFactory, userService);
			eventNotificationService.notifyUpdatedEvent(event, event, accessToken);
			verify(mailer, ical4jHelper);
		}

		@Test
		public void testDefaultEventDateChangeZeroUser() {
			Ical4jUser ical4jUser = ToolBox.getIcal4jUser();
			ObmUser defaultObmUser = getDefaultObmUser();
			
			Event event = new Event();
			event.setDate(after());
			Event eventAfter = new Event();
			eventAfter.setDate(longAfter());
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(ical4jUser, eventAfter)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(ical4jUser, eventAfter)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(ical4jUser, eventAfter)).andReturn(ICS_DATA_UPDATE);
			
			ICalendarFactory calendarFactory = createMock(ICalendarFactory.class);
			EasyMock.expect(calendarFactory.createIcal4jUserFromObmUser(defaultObmUser)).andReturn(ical4jUser).anyTimes();
			
			AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
			
			UserService userService = EasyMock.createMock(UserService.class);
			EasyMock.expect(userService.getUserFromAccessToken(accessToken)).andReturn(defaultObmUser);
			
			replay(mailer, ical4jHelper, calendarFactory, userService);
			
			EventNotificationService eventNotificationService = getEventNotificationServiceToNotifyUpdatedEvent(mailer, ical4jHelper, calendarFactory, userService);
			eventNotificationService.notifyUpdatedEvent(event, eventAfter, accessToken);
			verify(mailer, ical4jHelper);
		}
		
		@Test
		public void testDefaultEventDateChangeOneNeedActionUser() {
			Ical4jUser ical4jUser = ToolBox.getIcal4jUser();
			ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
			
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.NEEDSACTION);
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);
			previousEvent.setSequence(0);
			
			Event currentEvent = new Event();
			currentEvent.setDate(longAfter());
			currentEvent.addAttendee(attendee);
			currentEvent.setSequence(1);
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(ical4jUser, currentEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(ical4jUser, currentEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(ical4jUser, currentEvent)).andReturn(ICS_DATA_UPDATE);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyNeedActionUpdateUsers(eq(defaultObmUser), compareCollections(ImmutableList.of(attendee)), 
					eq(previousEvent), eq(currentEvent), eq(LOCALE), eq(TIMEZONE), eq(ICS_DATA_UPDATE), anyObject(AccessToken.class));
			expectLastCall().once();
			
			ICalendarFactory calendarFactory = createMock(ICalendarFactory.class);
			EasyMock.expect(calendarFactory.createIcal4jUserFromObmUser(defaultObmUser)).andReturn(ical4jUser).anyTimes();
			
			AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
			
			UserService userService = EasyMock.createMock(UserService.class);
			EasyMock.expect(userService.getUserFromAccessToken(accessToken)).andReturn(defaultObmUser);
			
			replay(mailer, ical4jHelper, calendarFactory, userService);
			
			EventNotificationService eventNotificationService = getEventNotificationServiceToNotifyUpdatedEvent(mailer, ical4jHelper, calendarFactory, userService);
			eventNotificationService.notifyUpdatedEvent(previousEvent, currentEvent, accessToken);
			
			verify(mailer, ical4jHelper);
		}
		
		@Test
		public void testDefaultEventDateChangeOneAcceptedUser() {
			Ical4jUser ical4jUser = ToolBox.getIcal4jUser();
			ObmUser defaultObmUser = getDefaultObmUser();

			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.ACCEPTED);
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);
			previousEvent.setSequence(0);
			
			Event currentEvent = new Event();
			currentEvent.setDate(longAfter());
			currentEvent.addAttendee(attendee);
			currentEvent.setSequence(1);
			
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(ical4jUser, currentEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(ical4jUser, currentEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(ical4jUser, currentEvent)).andReturn(ICS_DATA_UPDATE);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyAcceptedUpdateUsers(eq(defaultObmUser), compareCollections(ImmutableList.of(attendee)), 
					eq(previousEvent), eq(currentEvent), eq(LOCALE), eq(TIMEZONE), eq(ICS_DATA_UPDATE), anyObject(AccessToken.class));
			
			expectLastCall().once();
			
			ICalendarFactory calendarFactory = createMock(ICalendarFactory.class);
			EasyMock.expect(calendarFactory.createIcal4jUserFromObmUser(defaultObmUser)).andReturn(ical4jUser).anyTimes();
			
			AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
			
			UserService userService = EasyMock.createMock(UserService.class);
			EasyMock.expect(userService.getUserFromAccessToken(accessToken)).andReturn(defaultObmUser);
			
			replay(mailer, ical4jHelper, calendarFactory, userService);
			
			EventNotificationService eventNotificationService = getEventNotificationServiceToNotifyUpdatedEvent(mailer, ical4jHelper, calendarFactory, userService);
			eventNotificationService.notifyUpdatedEvent(previousEvent, currentEvent, accessToken);
			
			verify(mailer, ical4jHelper);
		}
		
		@Test
		public void testDefaultEventNoChangeOneNeedActionUser() {
			Ical4jUser ical4jUser = ToolBox.getIcal4jUser();
			ObmUser defaultObmUser = getDefaultObmUser();
			
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.NEEDSACTION);
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(ical4jUser, previousEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(ical4jUser, previousEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(ical4jUser, previousEvent)).andReturn(ICS_DATA_UPDATE);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			
			ICalendarFactory calendarFactory = createMock(ICalendarFactory.class);
			EasyMock.expect(calendarFactory.createIcal4jUserFromObmUser(defaultObmUser)).andReturn(ical4jUser).anyTimes();
			
			AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
			
			UserService userService = EasyMock.createMock(UserService.class);
			EasyMock.expect(userService.getUserFromAccessToken(accessToken)).andReturn(defaultObmUser);
			
			replay(mailer, ical4jHelper, calendarFactory, userService);
			
			EventNotificationService eventNotificationService = getEventNotificationServiceToNotifyUpdatedEvent(mailer, ical4jHelper, calendarFactory, userService);
			eventNotificationService.notifyUpdatedEvent(previousEvent, previousEvent, accessToken);
			verify(mailer, ical4jHelper);
		}
		
		@Test
		public void testDefaultEventNoChangeOneAcceptedUser() {
			Ical4jUser ical4jUser = ToolBox.getIcal4jUser();
			ObmUser defaultObmUser = getDefaultObmUser();
			
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.ACCEPTED);
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(ical4jUser, previousEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(ical4jUser, previousEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(ical4jUser, previousEvent)).andReturn(ICS_DATA_UPDATE);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			
			ICalendarFactory calendarFactory = createMock(ICalendarFactory.class);
			EasyMock.expect(calendarFactory.createIcal4jUserFromObmUser(defaultObmUser)).andReturn(ical4jUser).anyTimes();
			
			AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
			
			UserService userService = EasyMock.createMock(UserService.class);
			EasyMock.expect(userService.getUserFromAccessToken(accessToken)).andReturn(defaultObmUser);
			
			replay(mailer, ical4jHelper, calendarFactory, userService);
			
			EventNotificationService eventNotificationService = getEventNotificationServiceToNotifyUpdatedEvent(mailer, ical4jHelper, calendarFactory, userService);
			eventNotificationService.notifyUpdatedEvent(previousEvent, previousEvent, accessToken);
			verify(mailer, ical4jHelper);
		}
		
		@Test
		public void testDefaultEventAddOneNeedActionUser() {
			Ical4jUser ical4jUser = ToolBox.getIcal4jUser();
			ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
			
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.NEEDSACTION);
			Attendee addedAttendee = createRequiredAttendee("addedeAttendee@test", ParticipationState.NEEDSACTION);
			
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);

			Event currentEvent = new Event();
			currentEvent.setDate(after());
			currentEvent.addAttendee(attendee);
			currentEvent.addAttendee(addedAttendee);
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(ical4jUser, currentEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(ical4jUser, currentEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(ical4jUser, currentEvent)).andReturn(ICS_DATA_UPDATE);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyNeedActionNewUsers(eq(defaultObmUser), compareCollections(ImmutableList.of(addedAttendee)), 
					eq(currentEvent), eq(LOCALE), eq(TIMEZONE), eq(ICS_DATA_ADD), anyObject(AccessToken.class));
			expectLastCall().once();

			ICalendarFactory calendarFactory = createMock(ICalendarFactory.class);
			EasyMock.expect(calendarFactory.createIcal4jUserFromObmUser(defaultObmUser)).andReturn(ical4jUser).anyTimes();
			
			AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
			
			UserService userService = EasyMock.createMock(UserService.class);
			EasyMock.expect(userService.getUserFromAccessToken(accessToken)).andReturn(defaultObmUser);
			
			replay(mailer, ical4jHelper, calendarFactory, userService);

			EventNotificationService eventNotificationService = getEventNotificationServiceToNotifyUpdatedEvent(mailer, ical4jHelper, calendarFactory, userService);
			eventNotificationService.notifyUpdatedEvent(previousEvent, currentEvent, accessToken);
			
			verify(mailer, ical4jHelper);
		}
		
		@Test
		public void testDefaultEventAddOneAcceptedUser() {
			Ical4jUser ical4jUser = ToolBox.getIcal4jUser();
			ObmUser defaultObmUser = getDefaultObmUser();
			
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.ACCEPTED);
			Attendee addedAttendee = createRequiredAttendee("addedeAttendee@test", ParticipationState.ACCEPTED);
			
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);

			Event currentEvent = new Event();
			currentEvent.setDate(after());
			currentEvent.addAttendee(attendee);
			currentEvent.addAttendee(addedAttendee);
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(ical4jUser, currentEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(ical4jUser, currentEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(ical4jUser, currentEvent)).andReturn(ICS_DATA_UPDATE);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyAcceptedNewUsers(eq(defaultObmUser), compareCollections(ImmutableList.of(addedAttendee)), eq(currentEvent), eq(LOCALE), eq(TIMEZONE), anyObject(AccessToken.class));
			expectLastCall().once();

			ICalendarFactory calendarFactory = createMock(ICalendarFactory.class);
			EasyMock.expect(calendarFactory.createIcal4jUserFromObmUser(defaultObmUser)).andReturn(ical4jUser).anyTimes();
			
			AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
			
			UserService userService = EasyMock.createMock(UserService.class);
			EasyMock.expect(userService.getUserFromAccessToken(accessToken)).andReturn(defaultObmUser);
			
			replay(mailer, ical4jHelper, calendarFactory, userService);
			
			EventNotificationService eventNotificationService = getEventNotificationServiceToNotifyUpdatedEvent(mailer, ical4jHelper, calendarFactory, userService);
			eventNotificationService.notifyUpdatedEvent(previousEvent, currentEvent, accessToken);
			
			verify(mailer, ical4jHelper);
		}

		@Test
		public void testDefaultEventDateChangeOneAcceptedUserWithCanWriteOnCalendar() {
			Ical4jUser ical4jUser = ToolBox.getIcal4jUser();
			ObmUser defaultObmUser = getDefaultObmUser();
			
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.ACCEPTED);
			attendee.setCanWriteOnCalendar(true);
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);
			previousEvent.setSequence(0);
			
			Event currentEvent = new Event();
			currentEvent.setDate(longAfter());
			currentEvent.addAttendee(attendee);
			currentEvent.setSequence(1);
			
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(ical4jUser, currentEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(ical4jUser, currentEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(ical4jUser, currentEvent)).andReturn(ICS_DATA_UPDATE);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyAcceptedUpdateUsersCanWriteOnCalendar(eq(defaultObmUser), compareCollections(ImmutableList.of(attendee)), 
					eq(previousEvent), eq(currentEvent), eq(LOCALE), eq(TIMEZONE), anyObject(AccessToken.class));
			
			expectLastCall().once();
			
			ICalendarFactory calendarFactory = createMock(ICalendarFactory.class);
			EasyMock.expect(calendarFactory.createIcal4jUserFromObmUser(defaultObmUser)).andReturn(ical4jUser).anyTimes();

			AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
			
			UserService userService = EasyMock.createMock(UserService.class);
			EasyMock.expect(userService.getUserFromAccessToken(accessToken)).andReturn(defaultObmUser);
			
			replay(mailer, ical4jHelper, calendarFactory, userService);

			EventNotificationService eventNotificationService = getEventNotificationServiceToNotifyUpdatedEvent(mailer, ical4jHelper, calendarFactory, userService);
			eventNotificationService.notifyUpdatedEvent(previousEvent, currentEvent, accessToken);

			verify(mailer, ical4jHelper);
		}

		public void testUserIsNotEventOwnerAddOneAcceptedUser() {
			Ical4jUser ical4jUser = ToolBox.getIcal4jUser();
			ObmUser defaultObmUser = getDefaultObmUser();
			
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
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(ical4jUser, currentEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(ical4jUser, currentEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(ical4jUser, currentEvent)).andReturn(ICS_DATA_UPDATE);

			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyAcceptedNewUsers(eq(defaultObmUser), compareCollections(ImmutableList.of(addedAttendee)), eq(currentEvent), eq(LOCALE), eq(TIMEZONE), anyObject(AccessToken.class));

			expectLastCall().once();

			ICalendarFactory calendarFactory = createMock(ICalendarFactory.class);
			EasyMock.expect(calendarFactory.createIcal4jUserFromObmUser(defaultObmUser)).andReturn(ical4jUser).anyTimes();
			
			AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
			
			UserService userService = EasyMock.createMock(UserService.class);
			EasyMock.expect(userService.getUserFromAccessToken(accessToken)).andReturn(defaultObmUser);
			
			replay(mailer, ical4jHelper, calendarFactory, userService);

			EventNotificationService eventNotificationService = getEventNotificationServiceToNotifyUpdatedEvent(mailer, ical4jHelper, calendarFactory, userService);
			eventNotificationService.notifyUpdatedEvent(previousEvent, currentEvent, accessToken);

			verify(mailer, ical4jHelper);
		}

		@Test
		public void testUserIsNotEventOwnerDefaultEventDateChangeOneAcceptedUser() {
			Ical4jUser ical4jUser = ToolBox.getIcal4jUser();
			ObmUser defaultObmUser = getDefaultObmUser();
			
			Attendee attendee = createRequiredAttendee("attendee1@test", ParticipationState.ACCEPTED);
			Event previousEvent = new Event();
			previousEvent.setDate(after());
			previousEvent.addAttendee(attendee);
			previousEvent.setOwnerEmail("attendee1@test");
			previousEvent.setSequence(0);
			
			Event currentEvent = new Event();
			currentEvent.setDate(longAfter());
			currentEvent.addAttendee(attendee);
			currentEvent.setOwnerEmail("attendee1@test");
			currentEvent.setSequence(1);
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(ical4jUser, currentEvent)).andReturn(ICS_DATA_ADD);
			EasyMock.expect(ical4jHelper.buildIcsInvitationCancel(ical4jUser, currentEvent)).andReturn(ICS_DATA_REMOVE);
			EasyMock.expect(ical4jHelper.buildIcsInvitationRequest(ical4jUser, currentEvent)).andReturn(ICS_DATA_UPDATE);
			
			EventChangeMailer mailer = createMock(EventChangeMailer.class);
			mailer.notifyOwnerUpdate(eq(defaultObmUser), eq(attendee), eq(previousEvent), eq(currentEvent), eq(LOCALE), eq(TIMEZONE), anyObject(AccessToken.class));

			ICalendarFactory calendarFactory = createMock(ICalendarFactory.class);
			EasyMock.expect(calendarFactory.createIcal4jUserFromObmUser(defaultObmUser)).andReturn(ical4jUser).anyTimes();
			
			AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
			
			UserService userService = EasyMock.createMock(UserService.class);
			EasyMock.expect(userService.getUserFromAccessToken(accessToken)).andReturn(defaultObmUser);
			
			replay(mailer, ical4jHelper, calendarFactory, userService);

			EventNotificationService eventNotificationService = getEventNotificationServiceToNotifyUpdatedEvent(mailer, ical4jHelper, calendarFactory, userService);
			eventNotificationService.notifyUpdatedEvent(previousEvent, currentEvent, accessToken);

			verify(mailer, ical4jHelper);
		}
		
	}

	public static class UpdateParticipationTests {
		
		@Test
		public void testParticipationChangeWithOwnerExpectingEmails() {
			ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
			Ical4jUser ical4jUser = ToolBox.getIcal4jUser();

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
			EasyMock.expect(ical4jHelper.buildIcsInvitationReply(event, ical4jUser)).andReturn(ICS_DATA_REPLY);
			
			mailer.notifyUpdateParticipationState(
					eq(event), eq(organizer), eq(attendeeUser),
					eq(ParticipationState.ACCEPTED), eq(LOCALE), eq(TIMEZONE), eq(ICS_DATA_REPLY), anyObject(AccessToken.class));
			expectLastCall().once();
			
			AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());
			
			UserService userService = EasyMock.createMock(UserService.class);
			EasyMock.expect(userService.getUserFromAccessToken(accessToken)).andReturn(defaultObmUser);

			ObmUser organizerUser = new ObmUser();
			EasyMock.expect(userService.getUserFromLogin(organizer.getEmail(), defaultObmUser.getDomain().getName()))
				.andReturn(organizerUser).once();

			UserSettings settings = getDefaultSettings();
			EasyMock.expect(settings.expectParticipationEmailNotification()).andReturn(true).once();
			
			SettingsService settingsService = EasyMock.createMock(SettingsService.class);
			settingsService.getSettings(eq(organizerUser));
			EasyMock.expectLastCall().andReturn(settings).once();
			settingsService.getSettings(eq(attendeeUser));
			EasyMock.expectLastCall().andReturn(settings).once();
			
			ICalendarFactory calendarFactory = createMock(ICalendarFactory.class);
			EasyMock.expect(calendarFactory.createIcal4jUserFromObmUser(defaultObmUser)).andReturn(ical4jUser);
			
			EasyMock.replay(userService, settingsService, settings, mailer, ical4jHelper, calendarFactory);
			
			EventNotificationService eventNotificationService = newEventNotificationServiceImpl(mailer, settingsService, userService, ical4jHelper, calendarFactory);
			eventNotificationService.notifyUpdatedParticipationStateAttendees(event, attendeeUser, ParticipationState.ACCEPTED, accessToken);
			
			verify(userService, settingsService, settings, mailer, ical4jHelper);
		}
		
		@Test
		public void testParticipationChangeWithExternalOrganizer() {
			ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
			Ical4jUser ical4jUser = ToolBox.getIcal4jUser();
			
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
			EasyMock.expect(ical4jHelper.buildIcsInvitationReply(event, ical4jUser)).andReturn(ICS_DATA_REPLY);
			
			mailer.notifyUpdateParticipationState(
					eq(event), eq(organizer), eq(attendeeUser),
					eq(ParticipationState.ACCEPTED), eq(LOCALE), eq(TIMEZONE), eq(ICS_DATA_REPLY), anyObject(AccessToken.class));
			expectLastCall().once();
			
			AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());

			UserService userService = EasyMock.createMock(UserService.class);
			EasyMock.expect(userService.getUserFromAccessToken(accessToken)).andReturn(defaultObmUser);

			EasyMock.expect(userService.getUserFromLogin(organizer.getEmail(), defaultObmUser.getDomain().getName()))
				.andReturn(null).once();

			UserSettings settings = getDefaultSettings();
			
			SettingsService settingsService = EasyMock.createMock(SettingsService.class);
			settingsService.getSettings(eq(attendeeUser));
			EasyMock.expectLastCall().andReturn(settings).once();
			
			ICalendarFactory calendarFactory = createMock(ICalendarFactory.class);
			EasyMock.expect(calendarFactory.createIcal4jUserFromObmUser(defaultObmUser)).andReturn(ical4jUser);
			
			EasyMock.replay(userService, settingsService, settings, mailer, ical4jHelper, calendarFactory);
			
			EventNotificationService eventNotificationService = newEventNotificationServiceImpl(mailer, settingsService, userService, ical4jHelper, calendarFactory);
			eventNotificationService.notifyUpdatedParticipationStateAttendees(event, attendeeUser, ParticipationState.ACCEPTED, accessToken);
			
			verify(userService, settingsService, settings, mailer, ical4jHelper);
		}

		@Test
		public void testParticipationChangeWithOwnerNotExpectingEmails() {
			ObmUser defaultObmUser = ToolBox.getDefaultObmUser();
			Ical4jUser ical4jUser = ToolBox.getIcal4jUser();
			
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
			
			AccessToken accessToken = ToolBox.mockAccessToken(defaultObmUser.getLogin(), defaultObmUser.getDomain());

			UserService userService = EasyMock.createMock(UserService.class);
			EasyMock.expect(userService.getUserFromAccessToken(accessToken)).andReturn(defaultObmUser);
			userService.getUserFromLogin(organizer.getEmail(), attendeeUser.getDomain().getName());
			EasyMock.expectLastCall().andReturn(organizerUser).once();

			UserSettings settings = getDefaultSettings();
			EasyMock.expect(settings.expectParticipationEmailNotification()).andReturn(false).once();
			
			SettingsService settingsService = EasyMock.createMock(SettingsService.class);
			settingsService.getSettings(eq(organizerUser));
			EasyMock.expectLastCall().andReturn(settings).once();
			
			Ical4jHelper ical4jHelper = createMock(Ical4jHelper.class);
			
			ICalendarFactory calendarFactory = createMock(ICalendarFactory.class);
			EasyMock.expect(calendarFactory.createIcal4jUserFromObmUser(defaultObmUser)).andReturn(ical4jUser);
			
			
			EasyMock.replay(userService, settingsService, settings, mailer, calendarFactory);
			
			EventNotificationService eventNotificationService = newEventNotificationServiceImpl(mailer, settingsService, userService, ical4jHelper, calendarFactory);
			eventNotificationService.notifyUpdatedParticipationStateAttendees(event, attendeeUser, ParticipationState.ACCEPTED, accessToken);
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

			EventNotificationServiceImpl eventNotificationService = new EventNotificationServiceImpl(
					null, null, null, null, null);

			previous.setAttendees(previousAtts);
			current.setAttendees(currentAtts);

			Map<AttendeeStateValue, Set<Attendee>> groups = eventNotificationService
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
			EventNotificationServiceImpl eventNotificationService = new EventNotificationServiceImpl(
					null, null, null, null, null);

			previous.setAttendees(previousAtts);
			current.setAttendees(currentAtts);

			Map<AttendeeStateValue, Set<Attendee>> groups =
				eventNotificationService.computeUpdateNotificationGroups(previous, current);

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
			EventNotificationServiceImpl eventNotificationService = new EventNotificationServiceImpl(
					null, null, null, null, null);

			Attendee attendee1 = new Attendee();
			attendee1.setEmail(email);
			Event event1 = new Event();
			event1.setAttendees(ImmutableList.of(attendee1));

			Attendee attendee2 = new Attendee();
			attendee2.setEmail(email);
			Event event2 = new Event();
			event2.setAttendees(ImmutableList.of(attendee2));

			Map<AttendeeStateValue, Set<Attendee>> groups =
					eventNotificationService.computeUpdateNotificationGroups(event1, event2);
			Set<Attendee> actual = groups.get(AttendeeStateValue.KEPT);

			Assert.assertSame(attendee2, actual.iterator().next());
		}

		@Test
		public void testComputeUpdateNotificationGroupsConcurrentModificationBug() {
			final String email = "myEmail";
			EventNotificationServiceImpl eventNotificationService = new EventNotificationServiceImpl(
					null, null, null, null, null);

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
					eventNotificationService.computeUpdateNotificationGroups(event1, event2);
			Set<Attendee> actual = groups.get(AttendeeStateValue.KEPT);

			Assert.assertSame(attendee2, actual.iterator().next());
		}
		
	}
	
}
