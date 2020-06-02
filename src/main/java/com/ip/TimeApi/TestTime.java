package com.ip.TimeApi;

import java.time.LocalTime;

public class TestTime {
public static void main(String[] args) {
	LocalTime time = LocalTime.now();
	int hour = time.getHour();
	int min = time.getMinute();
	int sec = time.getSecond();
	
	System.out.println("Today Time Is "+hour+":"+min+":"+sec);
	
}
}
