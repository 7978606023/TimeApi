package com.ip.TimeApi;

import java.time.LocalDate;

public class TestDate {

	public static void main(String[] args) {
		LocalDate date = LocalDate.now();
		int day = date.getDayOfMonth();
		int month = date.getDayOfMonth();
		int year = date.getYear();
		System.out.println("Today Is "+day+"/"+month+"/"+year);

	}

}
