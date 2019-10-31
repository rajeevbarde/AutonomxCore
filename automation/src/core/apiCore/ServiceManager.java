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

	
	public static void TestRunner(ServiceObject serviceObject) throws Exception {

		// setup api driver
		new AbstractDriverTestNG().setupApiDriver(serviceObject);
		runInterface(serviceObject);
	}
	
	public static void runInterface(ServiceObject serviceObject) throws Exception {
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
		
		// setup before class driver
		DriverObject driver = new DriverObject().withDriverType(DriverType.API);
		String csvFileName = ApiTestDriver.getTestClass(serviceObject);
		new AbstractDriverTestNG().setupWebDriver(csvFileName + TestObject.BEFORE_CLASS_PREFIX, driver);
				
		// run tests in csv files
		runServiceTestFileWithoutDataProvider(csvTestPath, beforeCsvFile, serviceObject.getTcName());
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
		
		// setup after class driver
		DriverObject driver = new DriverObject().withDriverType(DriverType.API);
		String csvFileName = ApiTestDriver.getTestClass(serviceObject);
		new AbstractDriverTestNG().setupWebDriver(csvFileName + TestObject.AFTER_CLASS_PREFIX, driver);
		
		// run tests in csv files
		runServiceTestFileWithoutDataProvider(csvTestPath, afterCsvFile, serviceObject.getTcName());
	}
	
	/**
	 * runs before suite
	 * @param serviceObject
	 * @throws Exception 
	 */
	public static void runBeforeSuite() throws Exception {
		
		// run all tests in csv file
		String csvTestPath = PropertiesReader.getLocalRootPath()
				+ Config.getValue(TEST_BASE_PATH) + BEFORE_SUITE_DIR + File.separator;
		
		String beforeSuiteFile =  Config.getValue(TEST_BASE_BEFORE_SUITE);	
		
		// return if before suite is not set
		if(StringUtils.isBlank(beforeSuiteFile)) return;
		
		// return if before suite is not set
		if(StringUtils.isBlank(beforeSuiteFile)) return;
		
		// run tests in csv files
		runServiceTestFileWithoutDataProvider(csvTestPath, beforeSuiteFile, "");
	}
	
	
	/**
	 * runs after suite
	 * @param serviceObject
	 * @throws Exception 
	 */
	public static void runAfterSuite() throws Exception {
		
		// run all tests in csv file
		String csvTestPath = PropertiesReader.getLocalRootPath()
				+ Config.getValue(TEST_BASE_PATH) + AFTER_SUITE_DIR + File.separator;
		
		String afterSuiteFile =  Config.getValue(TEST_BASE_AFTER_SUITE);
		
		// return if after suite is not set
		if(StringUtils.isBlank(afterSuiteFile)) return;
		
		// return if before suite is not set
		if(StringUtils.isBlank(afterSuiteFile)) return;
		
		// run tests in csv files
		runServiceTestFileWithoutDataProvider(csvTestPath, afterSuiteFile, "");
	}
	
	/**
	 * run csv test without data provider
	 * used for before/after class/suite
	 * @param csvTestPath
	 * @param file
	 * @param parentFileName : name of the class before/after class is running for
	 * @throws Exception
	 */
	public static void runServiceTestFileWithoutDataProvider(String csvTestPath, String file, String parentFileName) throws Exception {
		
		// set test id to be prefixed by the csv it is being used for. eg. before/after class
		String updateName = StringUtils.EMPTY;
		if(StringUtils.isNotBlank(parentFileName))
			 updateName = ApiTestDriver.getTestClass(parentFileName) + "_" + ApiTestDriver.getTestClass(file);
		else
			updateName = file;
		
		// map test list and run through the service runner
		List<String[]> testList = CsvReader.getCsvTestListForTestRunner(csvTestPath, file);
		List<Object[]> updateList = CsvReader.updateCsvFileFromFile(testList, updateName, "");
		for(Object[] dataRow : updateList) {
			ServiceObject serviceObject = CsvReader.mapToServiceObject(dataRow);
			TestRunner(serviceObject);
		}
	}
}