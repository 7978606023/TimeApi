package com.ip.TimeApi;

import java.io.ObjectInputStream.GetField;
import java.time.LocalDate;
import java.time.LocalTime;

public class Time {

	public static void main(String[] args) {
		LocalTime time = LocalTime.now();
		System.out.println("Now Time Is: "+time);
	}
}
