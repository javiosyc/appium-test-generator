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

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import generator.annotation.NoResetSetting;
import generator.annotation.NoResetSettingRule;
import generator.annotation.TestingAccount;
import generator.annotation.UserLoginTestRule;
import generator.utils.CommandUtils;
import io.appium.java_client.MobileElement;
import io.appium.java_client.TouchAction;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import models.AccountInfo;
import models.CommonMethod;
import models.CommonUtilClass;
import models.Feature;
import models.Scenario;
import models.Step;

public class AppiumTestGenerator {

	private Map<String, AccountInfo> accountInfos;
	private Map<String, MethodSpec> defaultMethodSpec = new HashMap<>();

	private String defaultUtilPackage = "module";

	private Map<String, Object> desiredCapabilities;

	private final String driverName = "driver";

	private Map<String, Object> driverProperties;

	private List<Feature> features;

	private String heightField = "height";
	private String implicitlyWaitSecName = "implicitlyWaitSec";

	private List<JavaFile> javaFiles = new ArrayList<>();

	private String outputDir = "examples/test";
	private String packageName = "sauce_appium_junit";
	private String passwordField = "password";
	private String pidField = "pid";
	private String userNameField = "userName";
	private String userRule = "userRule";
	private Map<String, CommonMethod> utilMethodsMapper = new HashMap<>();

	private List<CommonUtilClass> utils;
	private String widthField = "width";

	public AppiumTestGenerator(ExcelReader reader) {

		Map<String, Object> data = reader.getData();

		features = (List<Feature>) data.get("script");
		desiredCapabilities = ((Map<String, Map<String, Object>>) data.get("settings")).get("desiredCapabilities");
		driverProperties = ((Map<String, Map<String, Object>>) data.get("settings")).get("driverProperties");

		List<AccountInfo> accounts = (List<AccountInfo>) data.get("account");
		accountInfos = new HashMap<>();
		for (AccountInfo acc : accounts) {
			accountInfos.put(acc.getType(), acc);
		}

		utils = (List<CommonUtilClass>) data.get("commonStep");
		utils.forEach(util -> {
			util.getMethods().forEach(method -> {
				utilMethodsMapper.put(method.getDesc(), method);
			});
		});
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

	public void generate() throws IOException {

		generateUtilsClass();

		generateTestClass();
	}

	public Builder generateDefaultTestMethod(String testMethodName) {
		return MethodSpec.methodBuilder(testMethodName).addModifiers(Modifier.PUBLIC).returns(void.class)
				.addAnnotation(Test.class);
	}

	public MethodSpec generateScenariosMethod(Scenario scenario) {

		Builder methodBuilder = generateDefaultTestMethod(scenario.getName());

		methodBuilder.addJavadoc(scenario.getDesc() + "\n");

		for (Step step : scenario.getSteps()) {

			methodBuilder.addComment("$L $L $L $L ", step.getGherkinType(), step.getDesc(), step.getCommand().getType(),
					step.getCommand().getParams());

			String commandType = step.getCommand().getType();
			String desc = step.getDesc();
			List<Object> params = step.getCommand().getParams();
			if ("Then".equals(step.getGherkinType())) {

				if ("ByName".equals(commandType) || "ByXPath".equals(commandType)) {

					String methodName = StringUtils.lowerCase(commandType.substring(2));

					if (params.isEmpty()) {
						continue;
					}

					String element = (String) params.get(0);

					methodBuilder.addCode("$T<$T> expectedElements = $L.findElements($T.$L(\"$L\"));\n", List.class,
							MobileElement.class, driverName, By.class, methodName, element);

					methodBuilder.addCode("assertTrue(expectedElements.size() >0);\n");

				}

			} else if ("ByName".equals(commandType) || "ByXPath".equals(commandType)) {
				String methodName = StringUtils.lowerCase(commandType.substring(2));
				if (params.isEmpty()) {
					continue;
				}
				if (params.size() <= 1)
					continue;

				String action = (String) params.get(1);

				if ("click".equals(action)) {
					appendClickCode(methodBuilder, params, methodName);
				} else if ("sendKeys".equals(action)) {
					appendSendKeyCode(methodBuilder, params, methodName);
				}
			} else if (commandType.startsWith("TouchAction_")) {
				appendTouchActionCode(methodBuilder, commandType);
			} else if (commandType.startsWith("Waiting")) {
				appendWaitingCode(methodBuilder, commandType, params);
			} else if ("CheckAlert".equals(commandType)) {
				appendCheckAlertCode(methodBuilder, commandType, params);
			}

			else if (utilMethodsMapper.containsKey(desc)) {

				CommonMethod clazz = utilMethodsMapper.get(desc);

				ClassName utilClass = ClassName.get(
						packageName + "." + defaultUtilPackage + "." + clazz.getPackageName(), clazz.getClassName());

				methodBuilder.addCode("$T.$L($L,$L,$L,$L,$L);\n", utilClass, clazz.getName(), driverName, userNameField,
						passwordField, pidField, implicitlyWaitSecName);

				AnnotationSpec annotationSpec = AnnotationSpec.builder(NoResetSetting.class)
						.addMember("noReset", "$L", clazz.isNoReset()).build();

				methodBuilder.addAnnotation(annotationSpec);

			} else if (accountInfos.containsKey(desc)) {

				AccountInfo acc = accountInfos.get(desc);

				AnnotationSpec annotationSpec = AnnotationSpec.builder(TestingAccount.class)
						.addMember("userName", "$S", acc.getUserName()).addMember("password", "$S", acc.getPassword())
						.addMember("pid", "$S", acc.getPid()).build();
				methodBuilder.addAnnotation(annotationSpec);
			}
		}

		return methodBuilder.build();
	}

	public void generateSetUpAndTearDownMethod() {
		defaultMethodSpec.put("setUp", generateSetUpMethod());
		defaultMethodSpec.put("tearDown", generateTearDownMethod());
	}

	public MethodSpec generateSetUpMethod() {

		Builder methodBuilder = MethodSpec.methodBuilder("setUp").addModifiers(Modifier.PUBLIC).returns(void.class)
				.addJavadoc(getKuaiKuai()).addAnnotation(Before.class).addException(MalformedURLException.class);

		methodBuilder.addCode(generateDesiredCapabilities("capabilities", desiredCapabilities));

		methodBuilder.addCode(generateDriver(driverName, "capabilities", driverProperties));

		methodBuilder.addCode(generateUserCode());

		return methodBuilder.build();
	}

	public String getKuaiKuai() {

		String msg = "<pre>\n"
				+ "               ░░░░░░░░░ ░░                                        ░░░░▒▒░░░░░     ░░░░\n"
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
				+ "</pre>\n";
		return msg;

	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
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

	private void addFieldForTest(TypeSpec.Builder classBuilder) {
		classBuilder.addField(IOSDriver.class, driverName, Modifier.PRIVATE);

		FieldSpec fieldSpec = FieldSpec.builder(NoResetSettingRule.class, "rule").addAnnotation(Rule.class)
				.addModifiers(Modifier.PUBLIC).initializer("new $T()", NoResetSettingRule.class).build();
		classBuilder.addField(fieldSpec);

		FieldSpec memberFieldSpec = FieldSpec.builder(UserLoginTestRule.class, userRule).addAnnotation(Rule.class)
				.addModifiers(Modifier.PUBLIC).initializer("new $T()", UserLoginTestRule.class).build();
		classBuilder.addField(memberFieldSpec);

		classBuilder.addField(String.class, userNameField, Modifier.PRIVATE);
		classBuilder.addField(String.class, pidField, Modifier.PRIVATE);
		classBuilder.addField(String.class, passwordField, Modifier.PRIVATE);

		classBuilder.addField(Integer.class, widthField, Modifier.PRIVATE);
		classBuilder.addField(Integer.class, heightField, Modifier.PRIVATE);

		classBuilder.addField(TypeName.LONG, implicitlyWaitSecName, Modifier.PRIVATE);
	}

	private void appendCheckAlertCode(Builder methodBuilder, String commandType, List<Object> params) {
		String elementName = String.valueOf(params.get(0));
		methodBuilder.addCode("$T.presenceClick($L,2L,$S,$L );\n", CommandUtils.class, driverName, elementName,
				implicitlyWaitSecName);
	}

	private void appendClickCode(Builder methodBuilder, List<Object> params, String methodName) {
		String element = (String) params.get(0);
		methodBuilder.addCode("$L.findElement($T.$L(\"$L\")).click();\n", driverName, By.class, methodName, element);
	}

	private void appendSendKeyCode(Builder methodBuilder, List<Object> params, String methodName) {
		String element = String.valueOf(params.get(0));

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

		if (value.startsWith("${") && value.endsWith("}")) {
			methodBuilder.addCode("$L.findElement($T.$L(\"$L\")).sendKeys($L);\n", driverName, By.class, methodName,
					element, value.substring(2, value.length() - 1));
		} else {
			methodBuilder.addCode("$L.findElement($T.$L(\"$L\")).sendKeys($L);\n", driverName, By.class, methodName,
					element, "\"" + value + "\"");
		}
	}

	private void appendTouchActionCode(Builder methodBuilder, String commandType) {
		String distance = commandType.replaceAll("TouchAction_", "");

		double distanceDouble = Double.parseDouble(distance);

		int times = new Double(distanceDouble / 0.5).intValue();

		for (int i = 0; i < times; i++) {
			methodBuilder.addCode(
					"(new $T($L)).press( ($L/2), $L -25).moveTo(0, (-1) * $L / 2 ).release().perform();\n",
					TouchAction.class, driverName, widthField, heightField, heightField);
		}
	}

	private void appendWaitingCode(Builder methodBuilder, String commandType, List<Object> params) {
		String secString = StringUtils.trim(commandType.replaceAll("Waiting", ""));
		int seconds;
		if (StringUtils.isBlank(secString)) {
			if (params.size() == 0) {
				return;
			} else {
				seconds = Double.valueOf(String.valueOf(params.get(0))).intValue();
			}
		} else {
			seconds = Integer.parseInt(StringUtils.trim(StringUtils.strip(StringUtils.strip(secString, "_"), "s")));
		}
		methodBuilder.addCode("try { \n	$T.sleep($L* 1000);\n}catch($T e) { e.printStackTrace();\n}\n", Thread.class,
				seconds, InterruptedException.class);
	}

	private CodeBlock generateDesiredCapabilities(String variable, Map<String, Object> preoperties) {

		CodeBlock.Builder builder = CodeBlock.builder();

		builder.add("$T $L = new $T();\n", DesiredCapabilities.class, variable, DesiredCapabilities.class);

		for (Map.Entry<String, Object> entry : preoperties.entrySet()) {

			String property = entry.getKey();

			if ("noReset".equals(property)) {

				builder.beginControlFlow("if ($L.isNoReset()!=null)", "rule");

				builder.add("$L.setCapability(\"$L\", $L.isNoReset());\n", variable, entry.getKey(), "rule");

				builder.endControlFlow();

			} else if (property.equals(MobileCapabilityType.PLATFORM_VERSION)) {
				String version = (String) entry.getValue();
				version = version.replaceAll("[^\\.0123456789]", "");
				builder.add("$L.setCapability(\"$L\",\"$L\");\n", variable, entry.getKey(), version);
			} else {
				builder.add("$L.setCapability(\"$L\",\"$L\");\n", variable, entry.getKey(), entry.getValue());
			}
		}

		return builder.build();
	}

	private CodeBlock generateDriver(String driverName, String variable, Map<String, Object> properties) {

		CodeBlock.Builder builder = CodeBlock.builder();

		String url = (String) properties.getOrDefault("appiumUrl", "http://127.0.0.1:4723/wd/hub");
		int implicitlyWaitSec = (int) properties.get("implicitlyWait");

		builder.add("$L= new $T<$T>(new $T(\"$L\"), $L);\n", driverName, IOSDriver.class, MobileElement.class,
				URL.class, url, variable);

		builder.add("$L= $L;\n", implicitlyWaitSecName, implicitlyWaitSec);

		builder.add("$L.manage().timeouts().implicitlyWait($L ,$T.SECONDS);\n", driverName, implicitlyWaitSec,
				TimeUnit.class);

		builder.add("$L = $L.manage().window().getSize().getWidth();\n", widthField, driverName);
		builder.add("$L = $L.manage().window().getSize().getHeight();\n", heightField, driverName);

		return builder.build();
	}

	private MethodSpec generateTearDownMethod() {
		Builder methodBuilder = MethodSpec.methodBuilder("tearDown").addModifiers(Modifier.PUBLIC).returns(void.class)
				.addAnnotation(After.class);

		return methodBuilder.build();
	}

	private void generateTestClass() {

		generateSetUpAndTearDownMethod();

		for (Feature feature : features) {

			String className = StringUtils.endsWith(feature.getName(), "Test") ? feature.getName()
					: feature.getName() + "Test";

			TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC);

			addFieldForTest(classBuilder);

			List<MethodSpec> methods = new ArrayList<>();
			methods.add(defaultMethodSpec.get("setUp"));
			methods.add(defaultMethodSpec.get("tearDown"));

			classBuilder.addMethods(methods);

			for (Scenario s : feature.getScenarios()) {
				classBuilder.addMethod(generateScenariosMethod(s));
			}

			TypeSpec typeSpec = classBuilder.build();

			javaFiles.add(JavaFile.builder(packageName + "." + feature.getPackageName(), typeSpec)
					.addStaticImport(org.junit.Assert.class, "*").build());
		}
	}

	private CodeBlock generateUserCode() {
		CodeBlock.Builder builder = CodeBlock.builder();

		builder.beginControlFlow("if($L.getHasUser())", userRule);
		builder.add("$L=$L.getUserName();\n", userNameField, userRule);
		builder.add("$L=$L.getPid();\n", pidField, userRule);
		builder.add("$L=$L.getPassword();\n", passwordField, userRule);
		builder.endControlFlow();

		return builder.build();
	}

	/**
	 * 
	 * @param method
	 * @return
	 */
	private MethodSpec generateUtilMethod(CommonMethod method) {

		Builder methodBuilder = MethodSpec.methodBuilder(method.getName()).addModifiers(Modifier.PUBLIC)
				.addModifiers(Modifier.STATIC).addParameter(IOSDriver.class, driverName)
				.addParameter(String.class, userNameField).addParameter(String.class, passwordField)
				.addParameter(String.class, pidField).addParameter(TypeName.LONG, implicitlyWaitSecName)
				.returns(void.class).addJavadoc(method.getDesc() + "\n@param " + driverName
						+ "\n@param userName\n@param password\n@param pid\n@return\n");

		for (Step step : method.getSteps()) {
			methodBuilder.addComment("$L $L $L ", step.getDesc(), step.getCommand().getType(),
					step.getCommand().getParams());

			String commandType = step.getCommand().getType();

			List<Object> params = step.getCommand().getParams();

			if ("ByName".equals(commandType) || "ByXPath".equals(commandType)) {

				String methodName = StringUtils.lowerCase(commandType.substring(2));
				if (params.isEmpty()) {
					continue;
				}
				if (params.size() <= 1)
					continue;

				String action = (String) params.get(1);

				if ("click".equals(action)) {
					appendClickCode(methodBuilder, params, methodName);
				} else if ("sendKeys".equals(action)) {
					appendSendKeyCode(methodBuilder, params, methodName);
				}
			} else if (commandType.startsWith("TouchAction_")) {
				appendTouchActionCode(methodBuilder, commandType);
			} else if (commandType.startsWith("Waiting")) {
				appendWaitingCode(methodBuilder, commandType, params);
			} else if ("CheckAlert".equals(commandType)) {
				appendCheckAlertCode(methodBuilder, commandType, params);
			}
		}

		return methodBuilder.build();
	}

	private void generateUtilsClass() {

		for (CommonUtilClass utilClass : utils) {

			String className = utilClass.getName();
			TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC);

			for (CommonMethod method : utilClass.getMethods()) {
				classBuilder.addMethod(generateUtilMethod(method));
			}

			TypeSpec typeSpec = classBuilder.addJavadoc(utilClass.getDesc() + "\n").build();

			javaFiles.add(JavaFile
					.builder(packageName + "." + defaultUtilPackage + "." + utilClass.getPackageName(), typeSpec)
					.build());
		}

	}
}
