package core.apiCore;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import core.apiCore.driver.ApiTestDriver;
import core.apiCore.helpers.CsvReader;
import core.apiCore.interfaces.Authentication;
import core.apiCore.interfaces.AzureInterface;
import core.apiCore.interfaces.RabbitMqInterface;
import core.apiCore.interfaces.RestApiInterface;
import core.apiCore.interfaces.SqlInterface;
import core.apiCore.interfaces.TestPrepare;
import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.configReader.PropertiesReader;
import core.support.objects.DriverObject;
import core.support.objects.ServiceObject;
import core.support.objects.TestObject;
import core.uiCore.driverProperties.driverType.DriverType;
import core.uiCore.drivers.AbstractDriverTestNG;

public class ServiceManager {
	private static final String TOKEN_GENERATOR = "Authentication";
	public static final String SERVICE_TEST_RUNNER_ID = "ServiceTestRunner"; // matches the name of the service test runner class
	private static final String RESTFULL_API_INTERFACE = "RESTfulAPI";
	private static final String SQL_DB_INTERFACE = "SQLDB";
	private static final String AZURE_INTERFACE = "AZURE";
	private static final String RABBIT_MQ_INTERFACE = "RABBITMQ";
	private static final String TEST_PREPARE_INTERFACE = "TestPrepare";
	
	// test file for before/after class/suite 
	private static final String TEST_BASE_PATH = "api.base.path";
	private static final String TEST_BASE_BEFORE_CLASS = "api.base.before.testfile";
	private static final String TEST_BASE_AFTER_CLASS = "api.base.after.testfile";
	private static final String TEST_BASE_BEFORE_SUITE = "api.base.before.suite";
	private static final String TEST_BASE_AFTER_SUITE = "api.base.after.suite";
	
	// before/after class/suite directories
	private static final String BEFORE_CLASS_DIR = "beforeClass";
	private static final String AFTER_CLASS_DIR = "afterClass";
	private static final String BEFORE_SUITE_DIR = "beforeSuite";
	private static final String AFTER_SUITE_DIR = "afterSuite";

	
	public static void TestRunner(ServiceObject serviceObject)  {

		try {
			// setup api driver
			new AbstractDriverTestNG().setupApiDriver(serviceObject);
			runInterface(serviceObject);
		}catch(Exception e) {
			Helper.assertFalse(e.getMessage());
		}
	}
	
	public static void runInterface(ServiceObject serviceObject) {
		try {
			runCsvInterface(serviceObject);
		} catch (Exception e) {
			Helper.assertFalse(e.getMessage());
		}
	}
	
	public static void runCsvInterface(ServiceObject serviceObject) throws Exception {
		switch (serviceObject.getInterfaceType()) {
		case TOKEN_GENERATOR:
			Authentication.tokenGenerator(serviceObject);
			break;
		case RESTFULL_API_INTERFACE:
			RestApiInterface.RestfullApiInterface(serviceObject);
			break;
		case SQL_DB_INTERFACE:
			SqlInterface.DataBaseInterface(serviceObject);
			break;
		case AZURE_INTERFACE:
			AzureInterface.AzureClientInterface(serviceObject);
			break;
		case RABBIT_MQ_INTERFACE:
			RabbitMqInterface.testRabbitMqInterface(serviceObject);
			break;
		case TEST_PREPARE_INTERFACE:
			TestPrepare.TestPrepareInterface(serviceObject);
			break;
		default:
			Helper.assertFalse("no interface found: " + serviceObject.getInterfaceType() + ". Options:"
					+ "Authentication, RESTfulAPI, SQLDB, RABBITMQ");
			break;
		}
	}
	
	/**
	 * runs before each csv file
	 * @param serviceObject
	 * @throws Exception 
	 */
	public static void runBeforeCsv(ServiceObject serviceObject) throws Exception {
		
		// return if current test index is not 0
		int testIndex = Integer.valueOf(serviceObject.getTcIndex());
		if (!ApiTestDriver.isCsvTestStarted(testIndex)) return;
		
		// run all tests in csv file
		String csvTestPath = PropertiesReader.getLocalRootPath()
				+ Config.getValue(TEST_BASE_PATH) + BEFORE_CLASS_DIR + File.separator;
		
		String beforeCsvFile =  Config.getValue(TEST_BASE_BEFORE_CLASS);	
		
		// return if before csv is not set
		if(StringUtils.isBlank(beforeCsvFile)) return;
		
		String beforeTestName = ApiTestDriver.getTestClass(serviceObject) + TestObject.BEFORE_TEST_FILE_PREFIX + "-" + ApiTestDriver.getTestClass(beforeCsvFile);
		// run tests in csv files
		runServiceTestFileWithoutDataProvider(csvTestPath, beforeCsvFile, beforeTestName, serviceObject.getParent());
	}
	
	/**
	 * runs after each csv file
	 * @param serviceObject
	 * @throws Exception 
	 */
	public static void runAfterCsv(ServiceObject serviceObject) throws Exception {

		// return if current text index in csv = number of tests in test file
		if (!ApiTestDriver.isCsvTestComplete(serviceObject)) return;
		
		// run all tests in csv file
		String csvTestPath = PropertiesReader.getLocalRootPath()
				+ Config.getValue(TEST_BASE_PATH) + AFTER_CLASS_DIR + File.separator;;
		
		String afterCsvFile = Config.getValue(TEST_BASE_AFTER_CLASS);
		
		// return if after csv is not set
		if(StringUtils.isBlank(afterCsvFile)) return;
		
		String afterTestName = ApiTestDriver.getTestClass(serviceObject) + TestObject.AFTER_TEST_FILE_PREFIX + "-" + ApiTestDriver.getTestClass(afterCsvFile);

		// run tests in csv files
		runServiceTestFileWithoutDataProvider(csvTestPath, afterCsvFile, afterTestName, serviceObject.getParent());
	}
	
	/**
	 * runs before suite
	 * @param serviceObject
	 * @throws Exception 
	 */
	public static void runServiceBeforeSuite()  {
		
		// run all tests in csv file
		String csvTestPath = PropertiesReader.getLocalRootPath()
				+ Config.getValue(TEST_BASE_PATH) + BEFORE_SUITE_DIR + File.separator;
		
		String beforeSuiteFile =  Config.getValue(TEST_BASE_BEFORE_SUITE);	
		
		// return if before suite is not set
		if(StringUtils.isBlank(beforeSuiteFile)) return;
		
		String beforeSuiteName = TestObject.SUITE_NAME + TestObject.BEFORE_SUITE_PREFIX + "-" + ApiTestDriver.getTestClass(beforeSuiteFile);

		// run tests in csv files
		runServiceTestFileWithoutDataProvider(csvTestPath, beforeSuiteFile, beforeSuiteName, "");
	}
	
	
	/**
	 * runs after suite
	 * @param serviceObject
	 * @throws Exception 
	 */
	public static void runServiceAfterSuite() {
		
		// run all tests in csv file
		String csvTestPath = PropertiesReader.getLocalRootPath()
				+ Config.getValue(TEST_BASE_PATH) + AFTER_SUITE_DIR + File.separator;
		
		String afterSuiteFile =  Config.getValue(TEST_BASE_AFTER_SUITE);
		
		// return if after suite is not set
		if(StringUtils.isBlank(afterSuiteFile)) return;
		
		String afterSuiteName = TestObject.SUITE_NAME + TestObject.AFTER_SUITE_PREFIX + "-" + ApiTestDriver.getTestClass(afterSuiteFile);
		
		// run tests in csv files
		runServiceTestFileWithoutDataProvider(csvTestPath, afterSuiteFile, afterSuiteName, "");
	}
	
	/**
	 * run csv test without data provider
	 * used for before/after class/suite
	 * @param csvTestPath
	 * @param file
	 * @param parentFileName : name of the class before/after class is running for
	 * @throws Exception
	 */
	public static void runServiceTestFileWithoutDataProvider(String csvTestPath, String file, String testname, String parent) {
		
		// set test id to be prefixed by the csv it is being used for. eg. before/after class
		String updateName = testname;
		
		// map test list and run through the service runner
		List<String[]> testList = CsvReader.getCsvTestListForTestRunner(csvTestPath, file);
		List<Object[]> updateList = CsvReader.updateCsvFileFromFile(testList, updateName, "");
		for(Object[] dataRow : updateList) {
			ServiceObject testServiceObject = CsvReader.mapToServiceObject(dataRow);
			testServiceObject.withParent(parent);
			TestRunner(testServiceObject);
		}
	}
	
	/**
	 * parent object is setup once per csv test file
	 * all service tests will inherit from the parent object
	 * parent object is used to pass data from one test to another
	 * @param serviceObject
	 */
	public static void setupParentObject(ServiceObject serviceObject) {
		// set parent object 
		String csvFileName = ApiTestDriver.getTestClass(serviceObject); 
		String parent = csvFileName + TestObject.PARENT_PREFIX; 
		serviceObject.withParent(parent); 
		
		// setup before class driver
		DriverObject driver = new DriverObject().withDriverType(DriverType.API);
		new AbstractDriverTestNG().setupWebDriver(serviceObject.getParent(), driver);
	}
}