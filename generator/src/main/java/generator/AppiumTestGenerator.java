package generator;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
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
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import generator.mappers.AccountMapper;
import generator.mappers.CommonStepMapper;
import generator.mappers.ScriptMapper;
import generator.mappers.SettingMapper;
import generator.test.annotation.NoResetSetting;
import generator.test.annotation.TestingAccount;
import generator.test.rules.ExceptionRule;
import generator.test.rules.NoResetSettingRule;
import generator.test.rules.UserLoginTestRule;
import generator.test.utils.CommandUtils;
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

	private static final String ACCOUNT_PASSWORD = "password";
	private static final String ACCOUNT_PID = "pid";
	private static final String ACCOUNT_USERNAME = "userName";

	private static final String DEFAULT_PACKAGE = "com.esun.automation";
	private static final String DEFAULT_UTIL_PACKAGE = "module";

	private static String DEVICE_NAME;
	private static final String DRIVER_IMPLICITLY_WAIT_SEC = "implicitlyWaitSec";
	private static final String DRIVER_NAME = "driver";

	private static String DRIVER_PLATEFORM_VERSION;
	private static TypeName DRIVER_TYPE = ParameterizedTypeName.get(IOSDriver.class, MobileElement.class);
	private static final String PHONE_HEIGHT = "height";
	private static final String PHONE_WIDTH = "width";

	private static final String TEST_RULE_EXCEPTION = "exceptionRule";
	private static final String TEST_RULE_USER = "userRule";

	private Map<String, AccountInfo> accountInfos;

	private Map<String, MethodSpec> defaultMethodSpec = new HashMap<>();
	private Map<String, Object> desiredCapabilities;
	private Map<String, Object> driverProperties;
	private List<Feature> features;
	private List<JavaFile> javaFiles = new ArrayList<>();

	private String outputDir = "examples/test";

	private Map<String, CommonMethod> utilMethodsMapper = new HashMap<>();

	private List<CommonUtilClass> utils;

	@SuppressWarnings("unchecked")
	public AppiumTestGenerator(ExcelReader reader) {

		Map<String, Object> data = reader.getData();

		features = (List<Feature>) data.get(ScriptMapper.TYPE);

		desiredCapabilities = ((Map<String, Map<String, Object>>) data.get(SettingMapper.TYPE))
				.get("desiredCapabilities");
		driverProperties = ((Map<String, Map<String, Object>>) data.get(SettingMapper.TYPE)).get("driverProperties");

		List<AccountInfo> accounts = (List<AccountInfo>) data.get(AccountMapper.TYPE);
		accountInfos = new HashMap<>();
		if (accounts != null)
			for (AccountInfo acc : accounts) {
				accountInfos.put(acc.getType(), acc);
			}

		utils = (List<CommonUtilClass>) data.get(CommonStepMapper.TYPE);

		if (utils != null)
			utils.forEach(util -> {
				util.getMethods().forEach(method -> {
					utilMethodsMapper.put(method.getDesc(), method);
				});
			});
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

			addStepComment(methodBuilder, step);

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
							MobileElement.class, DRIVER_NAME, By.class, methodName, element);

					methodBuilder.addCode("assertTrue(expectedElements.size() >0);\n");

				}

			} else if ("ByName".equals(commandType) || "ByXPath".equals(commandType)) {
				appendByNameOrByXPathCode(methodBuilder, commandType, params);
			} else if (commandType.startsWith("TouchAction_")) {
				appendTouchActionCode(methodBuilder, commandType);
			} else if (commandType.startsWith("Waiting")) {
				appendWaitingCode(methodBuilder, commandType, params);
			} else if ("CheckAlert".equals(commandType)) {
				appendCheckAlertCode(methodBuilder, commandType, params);
			} else if ("Picker".equals(commandType)) {
				appendPickerCode(methodBuilder, commandType, params);
			} else if (utilMethodsMapper.containsKey(desc)) {

				CommonMethod clazz = utilMethodsMapper.get(desc);

				ClassName utilClass = ClassName.get(
						DEFAULT_PACKAGE + "." + DEFAULT_UTIL_PACKAGE + "." + clazz.getPackageName(),
						clazz.getClassName());

				methodBuilder.addCode("$T.$L($L,$L,$L,$L,$L);\n", utilClass, clazz.getName(), DRIVER_NAME,
						ACCOUNT_USERNAME, ACCOUNT_PASSWORD, ACCOUNT_PID, DRIVER_IMPLICITLY_WAIT_SEC);

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

		methodBuilder.addCode(generateDriver(DRIVER_NAME, "capabilities", driverProperties));

		methodBuilder.addCode(generateUserCode());

		methodBuilder.addCode(generateSetExceptionRule());

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

	private Builder addDefaultUtilMethodParameter(Builder methodBuilder) {
		methodBuilder.addParameter(DRIVER_TYPE, DRIVER_NAME).addParameter(String.class, ACCOUNT_USERNAME)
				.addParameter(String.class, ACCOUNT_PASSWORD).addParameter(String.class, ACCOUNT_PID)
				.addParameter(TypeName.LONG, DRIVER_IMPLICITLY_WAIT_SEC);

		return methodBuilder;
	}

	private void addFieldForTest(TypeSpec.Builder classBuilder) {

		FieldSpec driverNameSpec = FieldSpec.builder(DRIVER_TYPE, DRIVER_NAME, Modifier.PRIVATE).build();
		classBuilder.addField(driverNameSpec);

		FieldSpec exceptionfieldSpec = FieldSpec.builder(ExceptionRule.class, TEST_RULE_EXCEPTION)
				.addAnnotation(Rule.class).addModifiers(Modifier.PUBLIC).initializer("new $T()", ExceptionRule.class)
				.build();
		classBuilder.addField(exceptionfieldSpec);

		FieldSpec fieldSpec = FieldSpec.builder(NoResetSettingRule.class, "rule").addAnnotation(Rule.class)
				.addModifiers(Modifier.PUBLIC).initializer("new $T()", NoResetSettingRule.class).build();
		classBuilder.addField(fieldSpec);

		FieldSpec memberFieldSpec = FieldSpec.builder(UserLoginTestRule.class, TEST_RULE_USER).addAnnotation(Rule.class)
				.addModifiers(Modifier.PUBLIC).initializer("new $T()", UserLoginTestRule.class).build();
		classBuilder.addField(memberFieldSpec);

		classBuilder.addField(String.class, ACCOUNT_USERNAME, Modifier.PRIVATE);
		classBuilder.addField(String.class, ACCOUNT_PID, Modifier.PRIVATE);
		classBuilder.addField(String.class, ACCOUNT_PASSWORD, Modifier.PRIVATE);

		classBuilder.addField(Integer.class, PHONE_WIDTH, Modifier.PRIVATE);
		classBuilder.addField(Integer.class, PHONE_HEIGHT, Modifier.PRIVATE);

		classBuilder.addField(TypeName.LONG, DRIVER_IMPLICITLY_WAIT_SEC, Modifier.PRIVATE);
	}

	private void addStepComment(Builder methodBuilder, Step step) {

		if (step.getGherkinType() == null) {
			methodBuilder.addComment("$L $L $L", step.getDesc(), step.getCommand().getType(),
					step.getCommand().getParams());
		} else {
			methodBuilder.addComment("$L $L $L $L", step.getGherkinType(), step.getDesc(), step.getCommand().getType(),
					step.getCommand().getParams());
		}
	}

	private void appendByNameOrByXPathCode(Builder methodBuilder, String commandType, List<Object> params) {
		String methodName = StringUtils.lowerCase(commandType.substring(2));
		if (params.isEmpty()) {
			return;
		}
		if (params.size() <= 1)
			return;

		String action = (String) params.get(1);

		if ("click".equals(action)) {
			appendClickCode(methodBuilder, params, methodName);
		} else if ("sendKeys".equals(action)) {
			appendSendKeyCode(methodBuilder, params, methodName);
		} else if ("clear".equals(action)) {
			appendClearCode(methodBuilder, params, methodName);
		}

	}

	private void appendCheckAlertCode(Builder methodBuilder, String commandType, List<Object> params) {
		String elementName = String.valueOf(params.get(0));
		methodBuilder.addCode("$T.presenceClick($L,2L,$S,$L );\n", CommandUtils.class, DRIVER_NAME, elementName,
				DRIVER_IMPLICITLY_WAIT_SEC);
	}

	private void appendClearCode(Builder methodBuilder, List<Object> params, String methodName) {
		String element = (String) params.get(0);
		methodBuilder.addCode("$L.findElement($T.$L(\"$L\")).clear();\n", DRIVER_NAME, By.class, methodName, element);
	}

	private void appendClickCode(Builder methodBuilder, List<Object> params, String methodName) {
		String element = (String) params.get(0);
		methodBuilder.addCode("$L.findElement($T.$L(\"$L\")).click();\n", DRIVER_NAME, By.class, methodName, element);
	}

	private void appendPickerCode(Builder methodBuilder, String commandType, List<Object> params) {

		if (params.isEmpty()) {
			return;
		}

		String value = (String) params.get(0);

		methodBuilder.addCode("$L.findElement(By.xpath(\"//XCUIElementTypePickerWheel\")).setValue($S);\n", DRIVER_NAME,
				value);

		methodBuilder.addCode("$L.findElement(By.name(\"完成\")).click();\n", DRIVER_NAME);
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
			methodBuilder.addCode("$L.findElement($T.$L(\"$L\")).sendKeys($L);\n", DRIVER_NAME, By.class, methodName,
					element, value.substring(2, value.length() - 1));
		} else {
			methodBuilder.addCode("$L.findElement($T.$L(\"$L\")).sendKeys($L);\n", DRIVER_NAME, By.class, methodName,
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
					TouchAction.class, DRIVER_NAME, PHONE_WIDTH, PHONE_HEIGHT, PHONE_HEIGHT);
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
				DRIVER_PLATEFORM_VERSION = StringUtils.replaceAll(version, "\\.", "_");
				builder.add("$L.setCapability(\"$L\",\"$L\");\n", variable, entry.getKey(), version);
			} else {

				if (property.equals(MobileCapabilityType.DEVICE_NAME)) {
					DEVICE_NAME = StringUtils.removeAll(StringUtils.lowerCase(String.valueOf(entry.getValue())), " ");
				}

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

		builder.add("$L= $L;\n", DRIVER_IMPLICITLY_WAIT_SEC, implicitlyWaitSec);

		builder.add("$L.manage().timeouts().implicitlyWait($L ,$T.SECONDS);\n", driverName, implicitlyWaitSec,
				TimeUnit.class);

		builder.add("$L = $L.manage().window().getSize().getWidth();\n", PHONE_WIDTH, driverName);
		builder.add("$L = $L.manage().window().getSize().getHeight();\n", PHONE_HEIGHT, driverName);

		return builder.build();
	}

	private CodeBlock generateSetExceptionRule() {
		CodeBlock.Builder builder = CodeBlock.builder();
		builder.add("$L.setDriver($L);\n", TEST_RULE_EXCEPTION, DRIVER_NAME);
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

			javaFiles.add(JavaFile.builder(getTestClassPackage(feature.getPackageName()), typeSpec)
					.addStaticImport(org.junit.Assert.class, "*").build());
		}
	}

	private CodeBlock generateUserCode() {
		CodeBlock.Builder builder = CodeBlock.builder();

		builder.beginControlFlow("if($L.getHasUser())", TEST_RULE_USER);
		builder.add("$L=$L.getUserName();\n", ACCOUNT_USERNAME, TEST_RULE_USER);
		builder.add("$L=$L.getPid();\n", ACCOUNT_PID, TEST_RULE_USER);
		builder.add("$L=$L.getPassword();\n", ACCOUNT_PASSWORD, TEST_RULE_USER);
		builder.endControlFlow();

		return builder.build();
	}

	/**
	 * 
	 * @param method
	 * @return
	 */
	private MethodSpec generateUtilMethod(CommonMethod method) {

		Builder methodBuilder = MethodSpec.methodBuilder(method.getName()).addModifiers(Modifier.PUBLIC,
				Modifier.STATIC);

		methodBuilder = addDefaultUtilMethodParameter(methodBuilder);

		methodBuilder.returns(void.class).addJavadoc(MessageFormat.format(
				"{0}\n\n@param {1}\n@param {2}\n@param {3}\n@param {4}\n@param {5}\n@return\n", method.getDesc(),
				DRIVER_NAME, ACCOUNT_USERNAME, ACCOUNT_PASSWORD, ACCOUNT_PID, DRIVER_IMPLICITLY_WAIT_SEC));

		for (Step step : method.getSteps()) {

			addStepComment(methodBuilder, step);

			String commandType = step.getCommand().getType();
			List<Object> params = step.getCommand().getParams();

			if ("ByName".equals(commandType) || "ByXPath".equals(commandType)) {
				appendByNameOrByXPathCode(methodBuilder, commandType, params);
			} else if (commandType.startsWith("TouchAction_")) {
				appendTouchActionCode(methodBuilder, commandType);
			} else if (commandType.startsWith("Waiting")) {
				appendWaitingCode(methodBuilder, commandType, params);
			} else if ("CheckAlert".equals(commandType)) {
				appendCheckAlertCode(methodBuilder, commandType, params);
			} else if ("Picker".equals(commandType)) {
				appendPickerCode(methodBuilder, commandType, params);
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
					.builder(DEFAULT_PACKAGE + "." + DEFAULT_UTIL_PACKAGE + "." + utilClass.getPackageName(), typeSpec)
					.build());
		}
	}

	private String getTestClassPackage(String packageName) {
		return DEFAULT_PACKAGE + "." + DEVICE_NAME + ".ios" + DRIVER_PLATEFORM_VERSION + "." + packageName;
	}
}
