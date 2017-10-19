package generator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.lang.model.element.Modifier;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeSpec;

import io.appium.java_client.MobileElement;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobileCapabilityType;

public class AppiumTestGenerator {

	private final String driverName = "driver";
	private final String outputDir = "examples/test";

	private String className = "HelloWorld";
	private String packageName = "sauce_appium_junit";

	private Map<String, Object> desiredCapabilities = new HashMap<>();

	private Map<String, Object> driverProperties = new HashMap<>();

	private Map<String, MethodSpec> defaultMethodSpec = new HashMap<>();

	public void generateDefault() {

		defaultMethodSpec.put("setUp", generateSetUpMethod());
		defaultMethodSpec.put("tearDown", generateTearDownMethod());
	}

	public MethodSpec generateSetUpMethod() {

		Builder methodBuilder = MethodSpec.methodBuilder("setUp").addModifiers(Modifier.PUBLIC).returns(void.class)
				.addAnnotation(Before.class).addException(MalformedURLException.class);

		methodBuilder.addCode(generateDesiredCapabilities("capabilities", desiredCapabilities));

		methodBuilder.addCode(generateDriver(driverName, "capabilities", driverProperties));

		return methodBuilder.build();
	}

	private MethodSpec generateTearDownMethod() {
		Builder methodBuilder = MethodSpec.methodBuilder("tearDown").addModifiers(Modifier.PUBLIC).returns(void.class)
				.addAnnotation(After.class);

		methodBuilder.addComment("cyndi ggyy");

		return methodBuilder.build();
	}

	public void generate() throws IOException {

		List<MethodSpec> methods = new ArrayList<>();

		methods.add(generateSetUpMethod());

		methods.add(generateTearDownMethod());

		TypeSpec helloWorld = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC).addMethods(methods)
				.addField(IOSDriver.class, driverName, Modifier.PRIVATE).build();

		JavaFile javaFile = JavaFile.builder(packageName, helloWorld).build();

		javaFile.writeTo(new File(outputDir));
	}

	private CodeBlock generateDriver(String driverName, String variable, Map<String, Object> properties) {

		CodeBlock.Builder builder = CodeBlock.builder();

		String url = (String) properties.getOrDefault("appiumUrl", "http://127.0.0.1:4723/wd/hub");
		int implicitlyWaitSec = (int) properties.get("implicitlyWait");

		builder.add("$L= new $T<$T>(new $T(\"$L\"), $L);\n", driverName, IOSDriver.class, MobileElement.class,
				URL.class, url, variable);

		builder.add("$L.manage().timeouts().implicitlyWait($L ,$T.SECONDS);\n", driverName, implicitlyWaitSec,
				TimeUnit.class);

		return builder.build();
	}

	private CodeBlock generateDesiredCapabilities(String variable, Map<String, Object> preoperties) {

		CodeBlock.Builder builder = CodeBlock.builder();

		builder.add("$T $L = new $T();\n", DesiredCapabilities.class, variable, DesiredCapabilities.class);

		for (Map.Entry<String, Object> entry : preoperties.entrySet()) {

			builder.add("$L.setCapability(\"$L\",\"$L\");\n", variable, entry.getKey(), entry.getValue());
		}

		return builder.build();
	}

	public void addDesiredCapabilities(Map<String, Object> desiredCapabilities) {
		for (Map.Entry<String, Object> entry : desiredCapabilities.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();

			if (key.equals(MobileCapabilityType.PLATFORM_VERSION)) {
				String version = (String) value;
				version = version.replaceAll("[^\\.0123456789]", "");
				value = version;
			}

			this.desiredCapabilities.put(entry.getKey(), value);
		}
	}

	public void addDriverProperties(Map<String, Object> driverProperties) {
		for (Map.Entry<String, Object> entry : driverProperties.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();

			this.driverProperties.put(key, value);
		}
	}

}
