/**
 * 
 */
package com.juusosoft.statusbarcalendar;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * @author Juuso Valkeejärvi
 * @version 18.6.2014
 */
public class Day {
	
	private Calendar cal;
	private ArrayList<Event> events;
	
	
	/**
	 * Constructor for day object
	 * @param year year of the day
	 * @param month month of the day
	 * @param day date of the day
	 */
	public Day(int year, int month, int day){
		this.events = new ArrayList<Event>();
		this.cal = Calendar.getInstance();
		this.cal.clear();
		this.cal.set(year, month, day);
	}
	
	
	/** 
	 * Add an event for day
	 * @param event event to add
	 */
	public void addEvent(Event event){
		this.events.add(event);
	}
	
	
	/**
	 * @return list of events for the day
	 */
	public ArrayList<Event> getEvents(){
		return this.events;
	}
	
	
	
	/**
	 * @return event count of the day
	 */
	public int getEventCount(){
		return this.events.size();
	}
	
	/**
	 * @return year of the day
	 */
	public int getYear(){
		return this.cal.get(Calendar.YEAR);
	}
	
	
	/**
	 * @return month of the day
	 */
	public int getMonth(){
		return this.cal.get(Calendar.MONTH);
	}
	
	
	/**
	 * @return date of the day
	 */
	public int getDay(){
		return this.cal.get(Calendar.DATE);
	}
	
	
	
	/**
	 * @return days calendar object
	 */
	public Calendar getCal(){
		return this.cal;
	}
}
