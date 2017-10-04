package generator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.lang.model.element.Modifier;

import org.junit.Before;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeSpec;

import io.appium.java_client.MobileElement;
import io.appium.java_client.ios.IOSDriver;

public class AppiumTestGenerator {

	public static void main(String[] args) throws IOException {

		String driverName = "driver";

		String appPath = "/Users/Hsinyi/Library/Developer/Xcode/DerivedData/EsunMobileBank-gmvukxjyiqhyqhadahjafizronhl/Build/Products/Debug-iphonesimulator/玉山銀行.app";

		String className = "HelloWorld";
		String packageName = "sauce_appium_junit";
		String outputDir = "examples/test";

		String platformVersion = "11.0";
		String deviceName = "iPhone 7";
		Boolean showXcodeLog = true;

		Map<String, Object> properties = new HashMap<>();
		properties.put("deviceName", deviceName);
		properties.put("platformVersion", platformVersion);
		properties.put("app", appPath);
		properties.put("showXcodeLog", showXcodeLog);

		Map<String, Object> driverProperties = new HashMap<>();

		driverProperties.put("appiumUrl", "http://127.0.0.1:4723/wd/hub");
		driverProperties.put("implicitlyWaitSec", 4);

		Builder setUpMethodBuilder = MethodSpec.methodBuilder("setUp").addModifiers(Modifier.PUBLIC).returns(void.class)
				.addAnnotation(Before.class)
				.addException(MalformedURLException.class);
		

		setUpMethodBuilder.addCode(generateDesiredCapabilities("capabilities", properties));

		setUpMethodBuilder.addCode(generateDriver(driverName, "capabilities", driverProperties));

		MethodSpec setUpMethod = setUpMethodBuilder.build();
		TypeSpec helloWorld = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC).addMethod(setUpMethod)
				.addField(IOSDriver.class, driverName, Modifier.PRIVATE).build();

		JavaFile javaFile = JavaFile.builder(packageName, helloWorld).build();

		javaFile.writeTo(new File(outputDir));
	}

	private static CodeBlock generateDriver(String driverName, String variable, Map<String, Object> properties) {

		CodeBlock.Builder builder = CodeBlock.builder();

		String url = (String) properties.get("appiumUrl");
		int implicitlyWaitSec = (int) properties.get("implicitlyWaitSec");

		builder.add("$L= new $T<$T>(new $T(\"$L\"), $L);\n", driverName, IOSDriver.class, MobileElement.class,URL.class, url, variable);

		builder.add("$L.manage().timeouts().implicitlyWait($L ,$T.SECONDS);\n", driverName, implicitlyWaitSec,TimeUnit.class);

		return builder.build();
	}

	private static CodeBlock generateDesiredCapabilities(String variable, Map<String, Object> preoperties) {

		CodeBlock.Builder builder = CodeBlock.builder();

		builder.add("$T $L = new $T();\n", DesiredCapabilities.class, variable, DesiredCapabilities.class);

		for (Map.Entry<String, Object> entry : preoperties.entrySet()) {

			builder.add("$L.setCapability(\"$L\",\"$L\");\n", variable, entry.getKey(), entry.getValue());
		}

		return builder.build();
	}

}
