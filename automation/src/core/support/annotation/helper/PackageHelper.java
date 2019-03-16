package core.support.annotation.helper;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.Element;

public class PackageHelper {
	
	public static String MODULE_MANAGER_CLASS = "ModuleManager";
	public static String PANEL_MANAGER_CLASS = "PanelManager";
	public static String ROOT_PATH = "moduleManager";
	public static String MODULE_CLASS = "ModuleBase";

	/**
	 * gets module name. eg. module.android.LoginPanel with return android
	 * 
	 * @param element
	 * @return
	 */
	public static String getModuleName(Element element) {
		String sourceClass = element.asType().toString();
		String module = sourceClass.split("\\.")[1];
		return module;
	}

	public static String getPackagePath(Element element) {
		String sourceClass = element.asType().toString();
		String packagePath = ROOT_PATH + "." + sourceClass.split("\\.")[0] + "." + sourceClass.split("\\.")[1];
		return packagePath;
	}

	/**
	 * path: module.android.panel
	 * 
	 * @param path
	 * @return "module"
	 */
	public static String getRootPath(String path) {
		return path.split("\\.")[0];
	}

	public static String getAppName(Element element) {
		String sourceClass = element.asType().toString();
		String appName = sourceClass.split("\\.")[1];
		return appName;
	}

	/**
	 * gets the full path of the first module eg. module.android.panel
	 * 
	 * @param panelMap
	 * @return
	 */
	public static String getFirstModuleFullPath(Map<String, List<Element>> panelMap) {
		String sourceClass = "";
		for (Entry<String, List<Element>> entry : panelMap.entrySet()) {

			Element firstElement = entry.getValue().get(0);
			sourceClass = firstElement.asType().toString();
			break;
		}
		return sourceClass;
	}

}