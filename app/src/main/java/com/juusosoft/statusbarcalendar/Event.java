/**
 * 
 */
package com.juusosoft.statusbarcalendar;

import java.util.Calendar;

/**
 * @author Juuso Valkeejärvi
 * @version 13.6.2014
 */
public class Event {

	private Calendar start = Calendar.getInstance();
	private Calendar end = Calendar.getInstance();
	private String title;
	private int color;
	private boolean allDay;

	/**
	 * Constructor for event
	 * @param start start calendar
	 * @param end end calendar
	 * @param title title and location
	 * @param color event color
	 * @param allDay true for allday event, otherwise false
	 */
	public Event(Calendar start, Calendar end, String title, int color,
			boolean allDay) {
		this.start.setTime(start.getTime());
		this.end.setTime(end.getTime());
		this.title = title;
		this.color = color;
		this.allDay = allDay;
	}

	/**
	 * Constructor without allday parameter, defaults to false
	 * @param start start calendar
	 * @param end end calendar
	 * @param title title and location
	 * @param color event color
	 */
	public Event(Calendar start, Calendar end, String title, int color) {
		this(start, end, title, color, false);
	}

	/**
	 * @return event color code
	 */
	public int getColor() {
		return this.color;
	}
	
	
	/**
	 * @return end calendar
	 */
	public Calendar getEndCal() {
		return this.end;
	}
	

	/**
	 * @return start calendar
	 */
	public Calendar getStartCal() {
		return this.start;
	}
	
	
	/**
	 * @return event text
	 */
	public String getTitle(){
		return this.title;
	}
	
	
	
	/**
	 * @return true if notification is allday, otherwise false
	 */
	public boolean isAllDay(){
		return this.allDay;
	}

}
