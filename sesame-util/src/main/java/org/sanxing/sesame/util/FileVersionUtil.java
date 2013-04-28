package org.sanxing.sesame.util;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FileVersionUtil {
	private static final Logger LOG = LoggerFactory.getLogger(FileVersionUtil.class);
	private static final String VERSION_PREFIX = "version_";
	private static final String[] RESERVED = { "version_" };

	public static int getLatestVersionNumber(File rootDirectory) {
		int result = -1;
		if (isVersioned(rootDirectory)) {
			File[] files = rootDirectory.listFiles();
			for (int i = 0; i < files.length; ++i) {
				int version = getVersionNumber(files[i].getName());
				if (version > result) {
					result = version;
				}
			}
		}
		return result;
	}

	public static File getLatestVersionDirectory(File rootDirectory) {
		File result = null;
		int highestVersion = -1;
		if ((rootDirectory != null) && (isVersioned(rootDirectory))) {
			File[] files = rootDirectory.listFiles();
			for (int i = 0; i < files.length; ++i) {
				int version = getVersionNumber(files[i].getName());
				if (version > highestVersion) {
					highestVersion = version;
					result = files[i];
				}
			}
		}
		return result;
	}

	public static File createNewVersionDirectory(File rootDirectory)
			throws IOException {
		File result = getNewVersionDirectory(rootDirectory);
		if (!(FileUtil.buildDirectory(result))) {
			throw new IOException("Failed to build version directory: "
					+ result);
		}
		return result;
	}

	public static File getNewVersionDirectory(File rootDirectory)
			throws IOException {
		File result = null;
		if (FileUtil.buildDirectory(rootDirectory)) {
			String versionDirectoryName = "version_";
			if (isVersioned(rootDirectory)) {
				int versionNumber = getLatestVersionNumber(rootDirectory);
				versionNumber = (versionNumber > 0) ? versionNumber + 1 : 1;
				versionDirectoryName = versionDirectoryName + versionNumber;
			} else {
				versionDirectoryName = versionDirectoryName + 1;
			}
			result = FileUtil.getDirectoryPath(rootDirectory,
					versionDirectoryName);
		} else {
			throw new IOException("Cannot build parent directory: "
					+ rootDirectory);
		}
		return result;
	}

	public static void initializeVersionDirectory(File rootDirectory)
			throws IOException {
		if (!(isVersioned(rootDirectory))) {
			File newRoot = createNewVersionDirectory(rootDirectory);
			File[] files = rootDirectory.listFiles();
			for (int i = 0; i < files.length; ++i)
				if (!(isReserved(files[i].getName()))) {
					LOG.info(rootDirectory.getPath()
							+ ": moving non-versioned file "
							+ files[i].getName() + " to " + newRoot.getName());
					File moveTo = FileUtil.getDirectoryPath(newRoot,
							files[i].getName());
					FileUtil.moveFile(files[i], moveTo);
				}
		}
	}

	private static boolean isVersioned(File rootDirectory) {
		boolean result = false;
		if ((rootDirectory.exists()) && (rootDirectory.isDirectory())) {
			File[] files = rootDirectory.listFiles();
			result = (files == null) || (files.length == 0);
			if (!(result)) {
				for (int i = 0; i < files.length; ++i) {
					if (isReserved(files[i].getName())) {
						result = true;
						break;
					}
				}
			}
		}
		return result;
	}

	private static boolean isReserved(String name) {
		boolean result = false;
		if (name != null) {
			for (int i = 0; i < RESERVED.length; ++i) {
				if (name.startsWith(RESERVED[i])) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	private static int getVersionNumber(String name) {
		int result = -1;
		if ((name != null) && (name.startsWith("version_"))) {
			String number = name.substring("version_".length());
			result = Integer.parseInt(number);
		}
		return result;
	}
}