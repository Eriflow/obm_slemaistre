package org.obm.sync.calendar;


import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class AllEventAttributesExceptExceptionsEquivalenceTest  {
	
	private Event getMockBeforeTimeUpdateEvent(){
		Event ev = getMockEvent();
		ev.setTimeUpdate(timeUpdateBefore());
		return ev;
	}
	
	private Event getMockAfterTimeUpdateEvent(){
		Event ev = getMockEvent();
		ev.setTimeUpdate(timeUpdateAfter());
		return ev;
	}
	
	private Event getMockBeforeSequenceEvent(){
		Event ev = getMockEvent();
		ev.setSequence(sequenceBefore());
		return ev;
	}
	
	private Event getMockAfterSequenceEvent(){
		Event ev = getMockEvent();
		ev.setSequence(sequenceAfter());
		return ev;
	}
	
	private Event getMockEvent(){
		Event ev = new Event();
		ev.setInternalEvent(true);
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(1295258400000L);
		ev.setDate(cal.getTime());
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
		ev.setTimeUpdate(cal.getTime());
		
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
		ev.setTimeCreate(cal.getTime());
		ev.setCategory("category");
		ev.setExtId(new EventExtId("2bf7db53-8820-4fe5-9a78-acc6d3262149"));
		ev.setTitle("fake rdv");
		ev.setOwner("john@do.fr");
		ev.setDuration(3600);
		ev.setDescription("description");
		ev.setLocation("tlse");
		ev.setOpacity(EventOpacity.OPAQUE);
		ev.setCompletion(new Date(0));
		ev.setPercent(1);
		ev.setPriority(1);
		List<Attendee> la = new LinkedList<Attendee>();
		Attendee at = new Attendee();
		at.setDisplayName("John Do");
		at.setEmail("john@do.fr");
		at.setState(ParticipationState.NEEDSACTION);
		at.setRequired(ParticipationRole.CHAIR);
		at.setOrganizer(true);
		la.add(at);
		at = new Attendee();
		at.setDisplayName("noIn TheDatabase");
		at.setEmail("notin@mydb.com");
		at.setState(ParticipationState.ACCEPTED);
		at.setRequired(ParticipationRole.OPT);
		la.add(at);
		ev.setAttendees(la);
		ev.setAlert(60);
		EventRecurrence er = new EventRecurrence();
		er.setKind(RecurrenceKind.daily);
		er.setFrequence(1);
		er.addException(ev.getDate());
		cal.add(Calendar.MONTH, 1);
		er.addException(cal.getTime());
		er.setEnd(null);
		ev.setRecurrence(er);
		ev.setSequence(3);
		
		return ev;
	}
	
	@Test
	public void testCompareNoChange(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(true, result);
	}
	
	@Test
	public void testCompareAllDay(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.setAllday(!e2.isAllday());
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareAlert(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.setAlert(e1.getAlert() + 10);
boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareAlert2(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.setAlert(e1.getAlert() - 10);
boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareAddAttendees(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		
		Attendee at = new Attendee();
		at.setDisplayName("User Un");
		at.setEmail("uun@mydb.com");
		at.setState(ParticipationState.ACCEPTED);
		at.setRequired(ParticipationRole.OPT);
		e2.getAttendees().add(at);
		
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareDeleteAttendees(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.getAttendees().remove(0);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareDeleteAndAddAttendees(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.getAttendees().remove(0);
		Attendee at = new Attendee();
		at.setDisplayName("User Un");
		at.setEmail("uun@mydb.com");
		at.setState(ParticipationState.ACCEPTED);
		at.setRequired(ParticipationRole.OPT);
		e2.getAttendees().add(at);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareCategory(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.setCategory(e1.getCategory() + "Modif");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEmptyCategory(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.setCategory("");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareCompletionAfter(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		Date completion = new Date(10 + e1.getCompletion().getTime());
		e2.setCompletion(completion);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareCompletionBefore(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		Date completion = new Date(e1.getCompletion().getTime() - 10000);
		e2.setCompletion(completion);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareDescription(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.setDescription(e1.getDescription() + "Mofif");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEmptyDescription(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.setDescription("");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEndDateAfter(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.setDuration(e1.getDuration() + 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEndDateBefore(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.setDuration(e1.getDuration() - 1000);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareOpacity(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.setOpacity(EventOpacity.TRANSPARENT);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testComparePercent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.setPercent(e1.getPercent() + 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareLesserPercent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.setPercent(e1.getPercent() - 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testComparePriority(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.setPriority(e1.getPriority() + 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareLesserPriority(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.setPriority(e1.getPriority() - 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testComparePrivacy(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.setPrivacy(e1.getPrivacy() + 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareTitle(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.setTitle(e1.getTitle()+"Modif");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEmptyTitle(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.setTitle("");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareLocation(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.setLocation(e1.getLocation()+"Modif");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEmptyLocation(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.setLocation("");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareDateBefore(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		Date start = new Date( e1.getDate().getTime() - 1000);
		e2.setDate(start);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareDateAfter(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		Date start = new Date( e1.getDate().getTime() + 1000);
		e2.setDate(start);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareDuration(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.setDuration(e1.getDuration() + 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareLesserDuration(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.setDuration(e1.getDuration() - 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareType(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.setType(EventType.VTODO);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurDays(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.getRecurrence().setDays("1010101");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurEndBefore(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		Date end = new Date( e1.getDate().getTime() - 1000);
		e2.setDate(end);
		e2.getRecurrence().setEnd(end);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurEndAfter(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		Date end = new Date( e1.getDate().getTime() + 1000);
		e2.setDate(end);
		e2.getRecurrence().setEnd(end);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurFrequence(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.getRecurrence().setFrequence(e1.getRecurrence().getFrequence() + 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurLesserFrequence(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.getRecurrence().setFrequence(e1.getRecurrence().getFrequence() - 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurKind(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.getRecurrence().setKind(RecurrenceKind.yearly);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareTestReflexiv(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeSequenceEvent();
		Event e2 = getMockAfterSequenceEvent();
		e2.getRecurrence().setKind(RecurrenceKind.yearly);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareAllDayWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.setAllday(!e2.isAllday());
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareAlertWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.setAlert(e1.getAlert() + 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareAlert2WithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.setAlert(e1.getAlert() - 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareAddAttendeesWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		
		Attendee at = new Attendee();
		at.setDisplayName("User Un");
		at.setEmail("uun@mydb.com");
		at.setState(ParticipationState.ACCEPTED);
		at.setRequired(ParticipationRole.OPT);
		e2.getAttendees().add(at);
		
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareDeleteAttendeesWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.getAttendees().remove(0);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareDeleteAndAddAttendeesWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.getAttendees().remove(0);
		Attendee at = new Attendee();
		at.setDisplayName("User Un");
		at.setEmail("uun@mydb.com");
		at.setState(ParticipationState.ACCEPTED);
		at.setRequired(ParticipationRole.OPT);
		e2.getAttendees().add(at);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareCategoryWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.setCategory(e1.getCategory() + "Modif");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEmptyCategoryWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.setCategory("");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareCompletionAfterWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		Date completion = new Date(10 + e1.getCompletion().getTime());
		e2.setCompletion(completion);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareCompletionBeforeWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		Date completion = new Date(e1.getCompletion().getTime() - 10000);
		e2.setCompletion(completion);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareDescriptionWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.setDescription(e1.getDescription() + "Mofif");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEmptyDescriptionWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.setDescription("");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEndDateAfterWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.setDuration(e1.getDuration() + 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEndDateBeforeWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.setDuration(e1.getDuration() - 1000);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareOpacityWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.setOpacity(EventOpacity.TRANSPARENT);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testComparePercentWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.setPercent(e1.getPercent() + 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareLesserPercentWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.setPercent(e1.getPercent() - 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testComparePriorityWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.setPriority(e1.getPriority() + 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareLesserPriorityWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.setPriority(e1.getPriority() - 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testComparePrivacyWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.setPrivacy(e1.getPrivacy() + 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareTitleWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.setTitle(e1.getTitle()+"Modif");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEmptyTitleWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.setTitle("");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareLocationWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.setLocation(e1.getLocation()+"Modif");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEmptyLocationWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.setLocation("");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareDateBeforeWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		Date start = new Date( e1.getDate().getTime() - 1000);
		e2.setDate(start);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareDateAfterWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		Date start = new Date( e1.getDate().getTime() + 1000);
		e2.setDate(start);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareDurationWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.setDuration(e1.getDuration() + 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareLesserDurationWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.setDuration(e1.getDuration() - 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareTypeWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.setType(EventType.VTODO);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurDaysWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.getRecurrence().setDays("1010101");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurEndBeforeWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		Date end = new Date( e1.getDate().getTime() - 1000);
		e2.setDate(end);
		e2.getRecurrence().setEnd(end);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurEndAfterWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		Date end = new Date( e1.getDate().getTime() + 1000);
		e2.setDate(end);
		e2.getRecurrence().setEnd(end);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurFrequenceWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.getRecurrence().setFrequence(e1.getRecurrence().getFrequence() + 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurLesserFrequenceWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.getRecurrence().setFrequence(e1.getRecurrence().getFrequence() - 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurKindWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.getRecurrence().setKind(RecurrenceKind.yearly);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareTestReflexivWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getMockBeforeTimeUpdateEvent();
		Event e2 = getMockAfterTimeUpdateEvent();
		e2.getRecurrence().setKind(RecurrenceKind.yearly);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	private Date timeUpdateBefore(){
		Calendar cal = Calendar.getInstance();
		cal.set(2011, 9, 1);
		return cal.getTime();
	}
	
	private Date timeUpdateAfter(){
		Calendar cal = Calendar.getInstance();
		cal.set(2011, 10, 1);
		return cal.getTime();
	}
	
	private int sequenceBefore(){
		return 5;
	}
	
	private int sequenceAfter(){
		return 10;
	}
	
}
