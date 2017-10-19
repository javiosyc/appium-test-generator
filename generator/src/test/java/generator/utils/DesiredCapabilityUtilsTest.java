package generator.utils;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class DesiredCapabilityUtilsTest {

	@Test
	public void isIOSMobileCapability() {

		DesiredCapabilityUtils.show();

		System.out.println(DesiredCapabilityUtils.isIOSMobileCapability("noReset"));

		System.out.println(DesiredCapabilityUtils.isIOSMobileCapability("shouldUseSingletonTestManager"));
	}

}
