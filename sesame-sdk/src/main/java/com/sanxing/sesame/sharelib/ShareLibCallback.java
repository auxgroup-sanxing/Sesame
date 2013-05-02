package com.sanxing.sesame.sharelib;

import java.io.File;

public abstract interface ShareLibCallback {
	public abstract void onInstall(File paramFile);

	public abstract void onDispose(File paramFile);
}