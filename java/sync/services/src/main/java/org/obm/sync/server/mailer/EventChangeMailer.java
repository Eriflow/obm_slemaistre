package org.obm.sync.server.mailer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.server.template.ITemplateLoader;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import fr.aliacom.obm.common.MailService;
import fr.aliacom.obm.common.calendar.EventMail;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.services.constant.ConstantService;
import freemarker.template.SimpleDate;
import freemarker.template.Template;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateException;

public class EventChangeMailer extends AbstractMailer {

	private final String baseUrl;
	
	@Inject
	/* package */ EventChangeMailer(MailService mailService, ConstantService constantService, ITemplateLoader templateLoader) {
		super(mailService, constantService, templateLoader);
		this.baseUrl = constantService.getObmUIBaseUrl();
	}
	
	public void notifyNeedActionNewUsers(final ObmUser user, Collection<Attendee> attendee, Event event, 
			Locale locale, TimeZone timezone, String ics, AccessToken token) throws NotificationException {
		
		try {
			EventMail mail = 
				new EventMail(
						extractSenderAddress(user), 
						event.getAttendees(), 
						newUserTitle(event.getOwnerDisplayName(), event.getTitle(), locale), 
						inviteNewUserBodyTxt(event, locale, timezone),
						inviteNewUserBodyHtml(event, locale, timezone),
						ics, "REQUEST");
			sendNotificationMessageToAttendees(attendee, mail, token);
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}
	
	public void notifyAcceptedNewUsers(final ObmUser synchronizer, Collection<Attendee> attendee, Event event, Locale locale, TimeZone timezone, AccessToken token) throws NotificationException {
		try {
			EventMail mail = 
				new EventMail(
						extractSenderAddress(synchronizer), 
						event.getAttendees(), 
						newUserTitle(event.getOwnerDisplayName(), event.getTitle(), locale),
						notifyNewUserBodyTxt(event, locale, timezone),
						notifyNewUserBodyHtml(event, locale, timezone));
			sendNotificationMessageToAttendees(attendee, mail, token);
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}

	public void notifyRemovedUsers(final ObmUser synchronizer, Collection<Attendee> attendees, Event event, 
			Locale locale, final TimeZone timezone, String ics, AccessToken token) throws NotificationException {
		
		try {
			EventMail mail = 
				new EventMail(
						extractSenderAddress(synchronizer),
						event.getAttendees(), 
						removedUserTitle(event.getOwnerDisplayName(), event.getTitle(), locale), 
						removedUserBodyTxt(event, locale, timezone),
						removedUserBodyHtml(event, locale, timezone), 
						ics, "CANCEL");
			sendNotificationMessageToAttendees(attendees, mail, token);
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}

	public void notifyNeedActionUpdateUsers(final ObmUser synchronizer, Collection<Attendee> attendees, 
			Event previous, Event current, Locale locale,
			TimeZone timezone, String ics, AccessToken token) throws NotificationException {
		try {
			EventMail mail = 
				new EventMail(
						extractSenderAddress(synchronizer),
						current.getAttendees(), 
						updateUserTitle(current.getOwnerDisplayName(), current.getTitle(), locale), 
						inviteUpdateUserBodyTxt(previous, current, locale, timezone),
						inviteUpdateUserBodyHtml(previous, current, locale, timezone), 
						ics, "REQUEST");
			sendNotificationMessageToAttendees(attendees, mail, token);
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}
	
	public void notifyAcceptedUpdateUsers(final ObmUser synchronizer, Collection<Attendee> attendees, Event previous, Event current, Locale locale, 
			TimeZone timezone, String ics, AccessToken token) throws NotificationException {

		try {
			EventMail mail = getNotifyUpdateUserEventMail(synchronizer, previous, current, locale, timezone, ics);
			sendNotificationMessageToAttendees(attendees, mail, token);
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}

	public void notifyAcceptedUpdateUsersCanWriteOnCalendar(final ObmUser synchronizer, Collection<Attendee> attendees, Event previous, Event current, 
			Locale locale, TimeZone timezone, AccessToken token) throws NotificationException {
		notifyAcceptedUpdateUsers(synchronizer, attendees, previous, current, locale, timezone, null, token);
	}
	
	private EventMail getNotifyUpdateUserEventMail(final ObmUser synchronizer, Event previous, Event current, Locale locale, TimeZone timezone, String ics) 
			throws UnsupportedEncodingException, IOException, TemplateException {
		
		EventMail eventMail = new EventMail(
				extractSenderAddress(synchronizer),
				current.getAttendees(), 
				updateUserTitle(current.getOwnerDisplayName(), current.getTitle(), locale), 
				notifyUpdateUserBodyTxt(previous, current, locale, timezone),
				notifyUpdateUserBodyHtml(previous, current, locale, timezone));
		if (ics != null) {
			eventMail.setIcsContent(ics);
			eventMail.setIcsMethod("REQUEST");
		}
		return eventMail;
	}

	public void notifyOwnerUpdate(final ObmUser synchronizer, Attendee owner, Event previous, Event current, Locale locale,
			TimeZone timezone, AccessToken token) {
		try {
			EventMail mail = 
				new EventMail(
						extractSenderAddress(synchronizer),
						current.getAttendees(), 
						updateUserTitle(current.getOwnerDisplayName(), current.getTitle(), locale), 
						notifyUpdateUserBodyTxt(previous, current, locale, timezone),
						notifyUpdateUserBodyHtml(previous, current, locale, timezone));
			sendNotificationMessageToAttendee(owner, mail, token);
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}
	
	public void notifyUpdateParticipationState(final Event event, final Attendee organizer, final ObmUser obmUser, 
			final ParticipationState newState, final Locale locale, final TimeZone timezone, String ics, AccessToken token) {
	
		try {
			final EventMail mail = 
				new EventMail(
						extractSenderAddress(obmUser),
						event.getAttendees(), 
						updateParticipationStateTitle(event.getTitle(), locale), 
						updateParticipationStateBodyTxt(event, obmUser, newState, locale, timezone),
						updateParticipationStateBodyHtml(event, obmUser, newState, locale, timezone),
						ics, "REPLY"
						);
			sendNotificationMessageToOrganizer(organizer, mail, token);
			
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}
	

	public void notifyOwnerRemovedEvent(final ObmUser synchronizer, Attendee owner, Event event, Locale locale, TimeZone timezone, AccessToken token) {
		try {
			EventMail mail = 
				new EventMail(
						extractSenderAddress(synchronizer), 
						event.getAttendees(), 
						removedUserTitle(event.getOwnerDisplayName(), event.getTitle(), locale),
						removedUserBodyTxt(event, locale, timezone),
						removedUserBodyHtml(event, locale, timezone));
			sendNotificationMessageToAttendee(owner, mail, token);
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}


	private InternetAddress extractSenderAddress(final ObmUser user)
	throws UnsupportedEncodingException {
		return new InternetAddress(user.getEmail(), user.getDisplayName());
	}
	
	private List<InternetAddress> convertAttendeesToAddresses(Collection<Attendee> attendees) throws UnsupportedEncodingException {
		List<InternetAddress> internetAddresses = Lists.newArrayList();
		for (Attendee at: attendees) {
			internetAddresses.add(convertAttendeeToAddresse(at));
		}
		return internetAddresses;
	}
	
	private InternetAddress convertAttendeeToAddresse(Attendee attendee) throws UnsupportedEncodingException {
		return new InternetAddress(attendee.getEmail(), attendee.getDisplayName());
	}
	
	private void sendNotificationMessageToAttendee(Attendee attendee, EventMail mail, AccessToken token) throws NotificationException {
		try {
			InternetAddress add = convertAttendeeToAddresse(attendee);
			sendNotificationMessage(mail, add, token);
		} catch (MessagingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		}
	}
	
	private void sendNotificationMessageToAttendees(Collection<Attendee> attendees, EventMail mail, AccessToken token) throws NotificationException {
		try {
			List<InternetAddress> adds = convertAttendeesToAddresses(attendees);
			sendNotificationMessage(mail, adds, token);
		} catch (MessagingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		}
	}
	
	private void sendNotificationMessageToOrganizer(Attendee organizer, EventMail mail, AccessToken token) throws NotificationException {
		try {
			InternetAddress adds = convertAttendeeToAddresse(organizer);
			sendNotificationMessage(mail, ImmutableList.of(adds), token);
		} catch (MessagingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		}
	}

	private void sendNotificationMessage(EventMail mail, List<InternetAddress>  addresses, AccessToken token) throws MessagingException, IOException{
		MimeMessage mimeMail = mail.buildMimeMail(session);
		mailService.sendMessage(addresses, mimeMail, token);
	}
	
	private void sendNotificationMessage(EventMail mail, InternetAddress address, AccessToken token) throws MessagingException, IOException{
		MimeMessage mimeMail = mail.buildMimeMail(session);
		mailService.sendMessage(address, mimeMail, token);
	}
	
	private String participationState(ParticipationState state, Locale locale){
		if(ParticipationState.ACCEPTED.equals(state)){
			return getMessages(locale).participationStateAccepted();
		} else {
			return getMessages(locale).participationStateDeclined();
		}
	}

	private String newUserTitle(String owner, String title, Locale locale) {
		return getMessages(locale).newEventTitle(owner, title);
	}
	
	private String updateParticipationStateTitle(String title, Locale locale) {
		return getMessages(locale).updateParticipationStateTitle(title);
	}
	
	private String notifyNewUserBodyTxt(Event event, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		return applyEventOnTemplate("EventNoticePlain.tpl", event, locale, timezone);
	}
	
	private String notifyNewUserBodyHtml(Event event, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		return applyEventOnTemplate("EventNoticeHtml.tpl", event, locale, timezone);
	}
	
	private String inviteNewUserBodyTxt(Event event, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		return applyEventOnTemplate("EventInvitationPlain.tpl", event, locale, timezone);
	}
	
	private String inviteNewUserBodyHtml(Event event, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		return applyEventOnTemplate("EventInvitationHtml.tpl", event, locale, timezone);
	}

	private String updateParticipationStateBodyTxt(Event event,
			final ObmUser attendeeUpdated, ParticipationState newState, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		return applyUpdateParticipationStateOnTemplate("ParticipationStateChangePlain.tpl", event, attendeeUpdated, newState, locale, timezone);
	}
	
	private String updateParticipationStateBodyHtml(Event event,
			final ObmUser attendeeUpdated, ParticipationState newState, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		return applyUpdateParticipationStateOnTemplate("ParticipationStateChangeHtml.tpl", event, attendeeUpdated, newState, locale, timezone);
	}
	

	private String removedUserBodyTxt(Event event, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		return applyEventOnTemplate("EventCancelPlain.tpl", event, locale, timezone);
	}
	
	private String removedUserBodyHtml(Event event, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		return applyEventOnTemplate("EventCancelHtml.tpl", event, locale, timezone);
	}
	
	private String inviteUpdateUserBodyTxt(Event oldEvent, Event newEvent, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		return applyEventUpdateOnTemplate("EventUpdateInvitationPlain.tpl", oldEvent, newEvent, locale, timezone);
	}
	
	private String inviteUpdateUserBodyHtml(Event oldEvent, Event newEvent, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		return applyEventUpdateOnTemplate("EventUpdateInvitationHtml.tpl", oldEvent, newEvent, locale, timezone);
	}
	
	private String notifyUpdateUserBodyTxt(Event oldEvent, Event newEvent, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		return applyEventUpdateOnTemplate("EventUpdateNoticePlain.tpl", oldEvent, newEvent, locale, timezone);
	}
	
	private String notifyUpdateUserBodyHtml(Event oldEvent, Event newEvent, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		return applyEventUpdateOnTemplate("EventUpdateNoticeHtml.tpl", oldEvent, newEvent, locale, timezone);
	}
	
	private String applyEventUpdateOnTemplate(String templateName, Event oldEvent, Event newEvent, Locale locale, TimeZone timezone) 
		throws TemplateException, IOException {
		
		Builder<Object, Object> builder = buildEventUpdateDatamodel(oldEvent, newEvent);
		ImmutableMap<Object, Object> datamodel = defineTechnicalData(builder, newEvent).build();
		Template template = templateLoader.getTemplate(templateName, locale, timezone);
		return applyTemplate(datamodel, template);
	}

	private String applyEventOnTemplate(String templateName, Event event, Locale locale, TimeZone timezone)
			throws IOException, TemplateException {
		Builder<Object, Object> builder = buildEventDatamodel(event);
		ImmutableMap<Object, Object> datamodel = defineTechnicalData(builder, event).build();
		Template template = templateLoader.getTemplate(templateName, locale, timezone);
		return applyTemplate(datamodel, template);
	}
	
	private String applyUpdateParticipationStateOnTemplate(String templateName,
			Event event, final ObmUser attendeeUpdated, ParticipationState newState,  Locale locale, TimeZone timezone) throws IOException, TemplateException {
		Builder<Object, Object> builder = buildUpdateParticipationStateDatamodel(event, attendeeUpdated, participationState(newState, locale));
		Template template = templateLoader.getTemplate(templateName, locale, timezone);
		return applyTemplate(builder.build(), template);
	}

	private Builder<Object, Object> defineTechnicalData(Builder<Object, Object> builder, Event event) {
		return builder
			.put("host", this.baseUrl)
			.put("calendarId", event.getObmId().serializeToString());
	}

	private Builder<Object, Object> buildEventDatamodel(Event event) {
		Builder<Object, Object> datamodel = ImmutableMap.builder()
			.put("start", new SimpleDate(event.getDate(), TemplateDateModel.DATETIME))
			.put("end", new SimpleDate(event.getEndDate(), TemplateDateModel.DATETIME))
			.put("subject", Strings.nullToEmpty(event.getTitle()))
			.put("location", Strings.nullToEmpty(event.getLocation()))
			.put("author", Strings.nullToEmpty(event.getOwnerDisplayName()));
		return datamodel;
	}
	
	private Builder<Object, Object> buildEventUpdateDatamodel(Event oldEvent, Event newEvent) {
		Builder<Object, Object> datamodel = ImmutableMap.builder()
			.put("old", buildEventDatamodel(oldEvent).build())
			.put("new", buildEventDatamodel(newEvent).build());
		return datamodel;
	}
	
	private Builder<Object, Object> buildUpdateParticipationStateDatamodel(
			Event event, final ObmUser attendeeUpdated, String state) {
				Builder<Object, Object> datamodel = ImmutableMap.builder()
			.put("user", attendeeUpdated.getDisplayName())
			.put("participationState", state)
			.put("subject", Strings.nullToEmpty(event.getTitle()))
			.put("start", new SimpleDate(event.getDate(), TemplateDateModel.DATE));
		return datamodel;
	}
	
	private String removedUserTitle(String owner, String title, Locale locale) {
		return getMessages(locale).canceledEventTitle(owner, title);
	}

	private String updateUserTitle(String owner, String title, Locale locale) {
		return getMessages(locale).updatedEventTitle(owner, title);
	}

	/* package */ void setMailService(MailService mailService) {
		this.mailService = mailService;
	}
}
