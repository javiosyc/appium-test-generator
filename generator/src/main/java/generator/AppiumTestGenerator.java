package generator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

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

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeSpec;

import io.appium.java_client.MobileElement;
import io.appium.java_client.TouchAction;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import models.AccountInfo;
import models.Feature;
import models.Scenario;
import models.Step;

public class AppiumTestGenerator {

	private final String driverName = "driver";
	private String outputDir = "examples/test";

	private final ExcelReader reader;

	public AppiumTestGenerator(ExcelReader reader) {
		super();
		this.reader = reader;

		desiredCapabilities = reader.getDesiredCapabilities();
		driverProperties = reader.getProperties();

		features = reader.getFeatures();

		accountInfos = new HashMap<>();

		for (AccountInfo acc : reader.getAccounts()) {
			accountInfos.put(acc.getType(), acc);

		}

	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	private String packageName = "sauce_appium_junit";

	private Map<String, Object> desiredCapabilities;

	private Map<String, Object> driverProperties;

	private Map<String, AccountInfo> accountInfos;

	private List<Feature> features;

	private Map<String, MethodSpec> defaultMethodSpec = new HashMap<>();

	private List<JavaFile> javaFiles = new ArrayList<>();

	public void generateDefault() {

		defaultMethodSpec.put("setUp", generateSetUpMethod());
		defaultMethodSpec.put("tearDown", generateTearDownMethod());
	}

	public MethodSpec generateScenariosMethod(Scenario scenario) {

		Builder methodBuilder = generateDefaultTestMethod(scenario.getName());

		
		
		for (Step step : scenario.getSteps()) {

			methodBuilder.addComment("$L $L $L $L ", step.getGherkinType(), step.getDesc(), step.getCommand().getType(),
					step.getCommand().getParams());

			String commandType = step.getCommand().getType();

			List<Object> params = step.getCommand().getParams();

			if ("Then".equals(step.getGherkinType())) {

				if ("ByName".equals(commandType) || "ByXPath".equals(commandType)) {

					String methodName = StringUtils.lowerCase(commandType.substring(2));

					String element = (String) params.get(0);

					methodBuilder.addCode("$T<$T> expectedElements = $L.findElements($T.$L(\"$L\"));\n", List.class,
							MobileElement.class, driverName, By.class, methodName, element);

					methodBuilder.addCode("assertThat(expectedElements.size(), equalTo(1));\n");

				}

			} else if ("ByName".equals(commandType) || "ByXPath".equals(commandType)) {
				String methodName = StringUtils.lowerCase(commandType.substring(2));

				String action = (String) params.get(1);

				if ("click".equals(action)) {
					String element = (String) params.get(0);
					methodBuilder.addCode("$L.findElement($T.$L(\"$L\")).click();\n", driverName, By.class, methodName,
							element);
				} else if ("sendKeys".equals(action)) {
					String element = (String) params.get(0);

					String value = String.valueOf(params.get(2));

					if (value.startsWith("#{") && value.endsWith("}")) {

						String[] tag = value.substring(2, value.length() - 1).split("\\.");
						AccountInfo acc = accountInfos.get(tag[0]);

						String m = tag[1];

						if (acc != null) {
							if (StringUtils.equalsIgnoreCase(m, "password")) {
								value = acc.getPassword();
							} else if (StringUtils.equalsIgnoreCase(m, "pid")) {
								value = acc.getPid();
							} else if (StringUtils.equalsIgnoreCase(m, "userName")) {
								value = acc.getUserName();
							}
						}
					}

					methodBuilder.addCode("$L.findElement($T.name(\"$L\")).sendKeys($L);\n", driverName, By.class,
							element, "\"" + value + "\"");
				}
			} else if ("TouchAction".equals(commandType)) {
				methodBuilder.addCode("(new $T($L)).press(180,500).moveTo(0,-400).release().perform();\n",
						TouchAction.class, driverName);
			} else if ("Waiting".equals(commandType)) {
				Double seconds = (Double) params.get(0);

				methodBuilder.addCode("try { \n	$T.sleep($L* 1000);\n}catch($T e) { e.printStackTrace();\n}\n",
						Thread.class, seconds.intValue(), InterruptedException.class);

			} else if ("".equals(commandType)) {

			}
		}

		return methodBuilder.build();
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

		return methodBuilder.build();
	}

	public void generate() throws IOException {
		generateDefault();
		for (Feature feature : features) {

			List<MethodSpec> methods = new ArrayList<>();
			methods.add(defaultMethodSpec.get("setUp"));
			methods.add(defaultMethodSpec.get("tearDown"));

			String className = StringUtils.endsWith(feature.getName(), "Test") ? feature.getName()
					: feature.getName() + "Test";

			TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC);

			classBuilder.addField(IOSDriver.class, driverName, Modifier.PRIVATE);

			classBuilder.addMethods(methods);

			for (Scenario s : feature.getScenarios()) {
				classBuilder.addMethod(generateScenariosMethod(s));
			}

			TypeSpec typeSpec = classBuilder.build();

			javaFiles.add(JavaFile.builder(packageName + "." + feature.getPackageName(), typeSpec)
					.addStaticImport(org.hamcrest.CoreMatchers.class, "*").addStaticImport(org.junit.Assert.class, "*")
					.build());
		}

	}

	public void writeTo() {
		javaFiles.forEach((javaFile) -> {
			try {
				javaFile.writeTo(new File(outputDir));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private CodeBlock generateDriver(String driverName, String variable, Map<String, Object> properties) {

		CodeBlock.Builder builder = CodeBlock.builder();

		String url = (String) properties.getOrDefault("appiumUrl", "http: 127.0.0.1:4723/wd/hub");
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

	public void addFeatures(List<Feature> features) {
		this.features.addAll(features);
	}

	public Builder generateDefaultTestMethod(String testMethodName) {
		return MethodSpec.methodBuilder(testMethodName).addModifiers(Modifier.PUBLIC).returns(void.class)
				.addJavadoc(getKuaiKuai())
				.addAnnotation(Test.class);
	}

	public String getKuaiKuai() {

		String msg = 
				"\n" +
				"               ░░░░░░░░░ ░░                                        ░░░░▒▒░░░░░     ░░░░\n"
				+ "          ░ ░████▓▓▓▓▓▓▓▓▓███▓░░                                ░██████████████░██▓▓▒▓▓░\n"
				+ "        ░██▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓██▒                   ░ ░      ░███████████████████▓▓▓▓▓▓█\n"
				+ "     ░▒█▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓██░░             ░███░    ░██████▓▓▓▓▓▓▓▓▓███████▓▓▓█▓▓▒░\n"
				+ "    ▒█▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓█░   ░▒▓    ██████░    ███████▓▓▓▓▓▓▓▓▓▓████████████▓\n"
				+ "  ░█▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓█▓  ▒▒▓▓   ███████░░░███████████████████████████░▓█\n"
				+ " ░░█▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓█░       ░████████████████████████████████████░\n"
				+ " ░░█▓▓▓▓▓▓▓▓▓██▓▒░▒▓▓▓▓▓▓▓▓▓▓▓▓▓▓█▓▓░░▓▓▓▓▓█░       ██████████░░░▓█░██░░░░███████████████\n"
				+ " ░█▓▓▓▓▓░░░░ ░░░▒▓██▓▓▓▓▓▓▓░░░░░ ░░▒▓██▓▓▓▓▓▓        ████████▒░░░▓░▒█░░░░░█▓░█████████████░\n"
				+ " ░▓▓▓▓███████ ░████████▓▓████████ ░███████▓▓▓█░       ░███████▓░▒░░▒░░░░░▒█░░░██████████████░   ░▒█\n"
				+ " ░█▓▓▓▓▓▓░░▓▓ ░▓▓░ ░▓▓▓█▓█▓▓▓ ░▓▓ ░▓▓  ▓▓▓█▓▓█▒        ░███████░░░░██░░░░░░░░▒█▒░░░█████████████████\n"
				+ " █▓▓▓▓    ██  ██░ ░░▒▓▓▓▓░   ░██  █▓  ░░█▓▓▓▓▓       ░░░██████░░░▒█░▒░░░██░░░░▒░░░█████████████████\n"
				+ " █▓▓▓▓██  ██  ██░ ░███▓▓▓███ ░██  █▓  ███▓▓▓█▓       ░░░░░▒░░░▓░░░▒▒▒░▒██░░░░░░▒▒█████████████████░\n"
				+ " █▓▓▓▓░░   ░  ██░ ░░ ░▓▓█▒░  ░░▒  █▓░ ░  █▓▓█░       ░░░░░░  ███░░▒░▒▒▒░░░░░▓██████████████████▒\n"
				+ " ▓█▓▓░▓███▓▓  ██▓█▓▓▓█▓▓█▓████▓▓  █▓█▓▓▓██▓▓█░          ░░░░░▒▒▒░░▒░░░░░░▒████████▒█████████░\n"
				+ " ░█▓▓▓▓▓▓▓▓▓  ██▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓  █▓▓▓▓▓▓▓▓█▒             ░▓▓▓▓░░░░▒░░░░▒▒▓▓▓▓░░ ░░███▒░\n"
				+ " ░█▓▓▓▓▓▓▓▓ ░██▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓ ░█▓▓▓▓▓▓▓▓█                  ░▒▒▒▓▒░░░▒▒▒▓▓▓░░░  ░░\n"
				+ " █▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓██                   ▒▒▒▓░▓▒▒▒▒▒▓░ ░░░░ ░░░░░\n"
				+ " ░██▓▓▓▓▓▓▒░░▓░▓▒▓░▓░▓▓▓▓▒▓▓▒▓▓▓▓▓▓▓▓█▒                    ▓▒▒▒▒▓▒▒▒▒▒▒▓     ░░░░░\n"
				+ "  ░░█▓▓▓▓▓░░░▓░▓▓░░▒░▓▓░▓▒▓░▒░▒▓▓▓▓██░                      ▓▓▓▓▓▓▓▓▓▓▓▓░\n"
				+ "    ░░██▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓█▓░                   ░░   ░▓▓▓▓▓ ▓▓▓▓▓▓░\n"
				+ "        ▒██▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓██░                   ░▒▒▒▒▒▒▒▒▓▒░▓    ░░░░▒▓▒▒▒▒▒▒▒▒▓░\n"
				+ "           ░░▓█████▓▓▓█████▒░                     ░▓▒▒▒▒▒▒▒▒▒▒▓▓▓░     ▒▓▓▓▓▒▒▒▒▒▒▒▒▓░░\n"
				+"\n";
		return msg;

	}
}
