package com.sanxing.sesame.sharelib;

import java.io.File;

public interface ShareLibCallback
{
    public abstract void onInstall( File installationDir );

    public abstract void onDispose( File installationDir );
}