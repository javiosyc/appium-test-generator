package generator.utils;

import java.lang.reflect.Field;

import java.util.Arrays;
import java.util.HashSet;

import java.util.Set;

import io.appium.java_client.remote.IOSMobileCapabilityType;
import io.appium.java_client.remote.MobileCapabilityType;

public class DesiredCapabilityUtils {

	public final static Set<String> iOSMobileCapabilities = new HashSet<>();

	static {
		try {
			getFields(IOSMobileCapabilityType.class);
			getFields(MobileCapabilityType.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void getFields(Class clazz) throws ClassNotFoundException {
		Field[] fields = clazz.getFields();

		Arrays.asList(fields).forEach((field) -> {

			String value = null;
			try {

				value = (String) field.get(clazz);
				iOSMobileCapabilities.add(value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}

		});
	}

	public static boolean isIOSMobileCapability(String name) {
		return iOSMobileCapabilities.contains(name);
	}

	public static void show() {

		for (String name : iOSMobileCapabilities) {
			System.out.println(name);
		}

	}
}
