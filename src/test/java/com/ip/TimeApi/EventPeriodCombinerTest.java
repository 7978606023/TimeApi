package com.ip.TimeApi;


import java.time.*;
import java.util.NavigableSet;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


public class EventPeriodCombinerTest {

	private Clock clock;
	private Calendar calendar;
	private ZonedDateTime startZDateTime;
	private LocalDate startLocalDate;

	@BeforeTest
	public void setup() {
		calendar = new Calendar();
		clock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
		startZDateTime = ZonedDateTime.now(clock);
		startLocalDate = LocalDate.from(startZDateTime);
	}

	@Test
	public void testNoWorkPeriods() {
		calendar.addEvent(Event.of(startZDateTime, startZDateTime.plusHours(1),""));

		NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

		Assert.assertTrue(combined.isEmpty());
	}

	@Test
	public void testSingleWorkPeriod() {
		WorkPeriod p1 = new WorkPeriod(startLocalDate.atTime(1, 0), startLocalDate.atTime(2, 0));
		calendar.addWorkPeriod(p1);

		NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

		Assert.assertEquals(1, combined.size());
		WorkPeriod p2 = combined.first();
		Assert.assertEquals(p2.getStartTime(), p1.getStartTime());
		Assert.assertEquals(p2.getEndTime(), p2.getEndTime());
	}

	@Test
	public void testNoOverlapPeriodFirst() {
		calendar.addEvent(Event.of(startZDateTime.withHour(3), startZDateTime.withHour(4),""));
		WorkPeriod period = new WorkPeriod(startLocalDate.atTime(1, 0), startLocalDate.atTime(2, 0));
		calendar.addWorkPeriod(period);

		NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

		Assert.assertEquals(1, combined.size());
		WorkPeriod p = combined.first();
		Assert.assertEquals(period.getStartTime(), p.getStartTime());
		Assert.assertEquals(period.getEndTime(), p.getEndTime());
	}

	@Test
	public void testNoOverlapEventFirst() {
		calendar.addEvent(Event.of(startZDateTime.withHour(1), startZDateTime.withHour(2),""));
		WorkPeriod period = new WorkPeriod(startLocalDate.atTime(3, 0), startLocalDate.atTime(4, 0));
		calendar.addWorkPeriod(period);

		NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

		Assert.assertEquals(1, combined.size());
		WorkPeriod p = combined.first();
		Assert.assertEquals(period.getStartTime(), p.getStartTime());
		Assert.assertEquals(period.getEndTime(), p.getEndTime());
	}

	@Test
	public void testSimpleOverlapPeriodFirst() {
		Event event = Event.of(startZDateTime.withHour(2), startZDateTime.withHour(4),"");
		calendar.addEvent(event);
		WorkPeriod period = new WorkPeriod(startLocalDate.atTime(1, 0), startLocalDate.atTime(3, 0));
		calendar.addWorkPeriod(period);

		NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

		Assert.assertEquals(1, combined.size());
		WorkPeriod p = combined.first();
		Assert.assertEquals(period.getStartTime(), p.getStartTime());
		Assert.assertEquals(startZDateTime.withHour(2).toLocalDateTime(), p.getEndTime());
	}

	@Test
	public void testSimpleOverlapEventFirst() {
		calendar.addEvent(Event.of(startZDateTime.withHour(1), startZDateTime.withHour(3),""));
		calendar.addWorkPeriod(new WorkPeriod(startLocalDate.atTime(2, 0), startLocalDate.atTime(4, 0)));

		NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

		Assert.assertEquals(1, combined.size());
		WorkPeriod p = combined.first();
		Assert.assertEquals(startZDateTime.withHour(3).toLocalDateTime(), p.getStartTime());
		Assert.assertEquals(startLocalDate.atTime(4, 0), p.getEndTime());
	}

	@Test
	public void testPeriodSurroundsEvent() {
		Event event = Event.of(startZDateTime.withHour(2), startZDateTime.withHour(3),"");
		calendar.addEvent(event);
		WorkPeriod period = new WorkPeriod(startLocalDate.atTime(1, 0), startLocalDate.atTime(4, 0));
		calendar.addWorkPeriod(period);

		NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

		Assert.assertEquals(2, combined.size());
		WorkPeriod p = combined.pollFirst();
		Assert.assertEquals(startLocalDate.atTime(1, 0), p.getStartTime());
		Assert.assertEquals(startZDateTime.withHour(2).toLocalDateTime(), p.getEndTime());
		p = combined.pollFirst();
		Assert.assertEquals(startZDateTime.withHour(3).toLocalDateTime(), p.getStartTime());
		Assert.assertEquals(startLocalDate.atTime(4, 0), p.getEndTime());
	}

	@Test
	public void testEventSurroundsPeriod() {
		calendar.addEvent(Event.of(startZDateTime.withHour(1), startZDateTime.withHour(4),""));
		calendar.addWorkPeriod(new WorkPeriod(startLocalDate.atTime(2, 0), startLocalDate.atTime(3, 0)));

		NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

		Assert.assertTrue(combined.isEmpty());
	}

	@Test
	public void testSimultaneousStartEventLonger() {
		calendar.addEvent(Event.of(startZDateTime, startZDateTime.withHour(3),""));
		calendar.addWorkPeriod(new WorkPeriod(startLocalDate.atStartOfDay(), startLocalDate.atTime(3, 0)));

		NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

		Assert.assertTrue(combined.isEmpty());
	}

	@Test
	public void testSimultaneousStartPeriodLonger() {
		Event event = Event.of(startZDateTime.withHour(1), startZDateTime.withHour(3),"");
		calendar.addEvent(event);
		WorkPeriod period = new WorkPeriod(startLocalDate.atTime(1, 0), startLocalDate.atTime(4, 0));
		calendar.addWorkPeriod(period);

		NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

		Assert.assertEquals(1, combined.size());
		WorkPeriod p = combined.pollFirst();
		Assert.assertEquals(startZDateTime.withHour(3).toLocalDateTime(), p.getStartTime());
		Assert.assertEquals(period.getEndTime(), p.getEndTime());
	}

	@Test
	public void testSimultaneousEndEventLonger() {
		calendar.addEvent(Event.of(startZDateTime.withHour(1), startZDateTime.withHour(4),""));
		calendar.addWorkPeriod(new WorkPeriod(startLocalDate.atTime(2, 0), startLocalDate.atTime(4, 0)));

		NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

		Assert.assertTrue(combined.isEmpty());
	}

	@Test
	public void testSimultaneousEndPeriodLonger() {
		Event event = Event.of(startZDateTime.withHour(2), startZDateTime.withHour(4),"");
		calendar.addEvent(event);
		calendar.addWorkPeriod(new WorkPeriod(startLocalDate.atTime(1, 0), startLocalDate.atTime(4, 0)));

		NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

		Assert.assertEquals(1, combined.size());
		WorkPeriod p = combined.pollFirst();
		Assert.assertEquals(startLocalDate.atTime(1, 0), p.getStartTime());
		Assert.assertEquals(startZDateTime.withHour(2).toLocalDateTime(), p.getEndTime());
	}

	@Test
	public void testAbuttingPeriodFirst() {
		calendar.addEvent(Event.of(startZDateTime.withHour(2), startZDateTime.withHour(3),""));
		calendar.addWorkPeriod(new WorkPeriod(startLocalDate.atTime(1, 0), startLocalDate.atTime(2, 0)));

		NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

		Assert.assertEquals(1, combined.size());
		WorkPeriod p = combined.pollFirst();
		Assert.assertEquals(startLocalDate.atTime(1, 0), p.getStartTime());
		Assert.assertEquals(startLocalDate.atTime(2, 0), p.getEndTime());
	}

	@Test
	public void testAbuttingEventFirst() {
		calendar.addEvent(Event.of(startZDateTime.withHour(1), startZDateTime.withHour(2),""));
		calendar.addWorkPeriod(new WorkPeriod(startLocalDate.atTime(2, 0), startLocalDate.atTime(3, 0)));

		NavigableSet<WorkPeriod> combined = calendar.overwritePeriodsByEvents(clock.getZone());

		Assert.assertEquals(1, combined.size());
		WorkPeriod p = combined.pollFirst();
		Assert.assertEquals(startLocalDate.atTime(2, 0), p.getStartTime());
		Assert.assertEquals(startLocalDate.atTime(3, 0), p.getEndTime());
	}
}
