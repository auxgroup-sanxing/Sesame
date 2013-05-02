package com.sanxing.adp.eclipse;

import java.io.File;
import java.io.FileNotFoundException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

public class ADPServiceProjectBuilder {
	public void buildAll(String engineServicePath) throws Exception {
		File curPath = new File(engineServicePath);

		for (File file : curPath.listFiles()) {
			if (file.getName().equals("build.xml")) {
				buildProject(file.getAbsolutePath());
			}
			if (file.isDirectory())
				buildAll(file.getAbsolutePath());
		}
	}

	public void buildProject(String buildFileName, String target)
			throws Exception {
		File buildFile = new File(buildFileName);
		Project p = new Project();
		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(2);
		p.addBuildListener(consoleLogger);

		p.fireBuildStarted();
		p.init();
		ProjectHelper helper = ProjectHelper.getProjectHelper();
		helper.parse(p, buildFile);
		p.executeTarget(target);
		p.fireBuildFinished(null);
	}

	public void buildProject(String buildFileName) throws Exception {
		buildProject(buildFileName, "build");
	}

	public static void main(String[] args) throws FileNotFoundException {
		File buildFile = new File(
				"G:\\sesame-1.0\\work\\containers\\studio\\webapp\\workarea\\httpTest\\engine\\caculateInterest\\Adp_caculateInterest\\build.xml");
		Project p = new Project();

		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);

		consoleLogger.setMessageOutputLevel(2);
		p.addBuildListener(consoleLogger);
		try {
			p.fireBuildStarted();
			p.init();
			ProjectHelper helper = ProjectHelper.getProjectHelper();
			helper.parse(p, buildFile);
			p.executeTarget("checkstyle");
			p.fireBuildFinished(null);
		} catch (BuildException e) {
			e.printStackTrace();
			p.fireBuildFinished(e);
		}
	}
}