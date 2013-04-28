/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package org.sanxing.sesame.core;

/**
 * The enum of possible states in which Sesame Application may reside.
 * 
 * @author ShangjieZhou
 */
public enum SystemState
{
    /**
     * Sesame is in process of starting. Should not be bothered until it is RUNNING.
     */
    STARTING,

    /**
     * Sesame is running and is healthy. It is fully functional.
     */
    STARTED,

    /**
     * Sesame tried to start up, but is failed due to broken user configuration. It is nonfunctional.
     */
    BROKEN_CONFIGURATION,

    /**
     * Sesame tried to start up, but is failed due to some unexpected IO error. It is nonfunctional.
     */
    BROKEN_IO,

    /**
     * Sesame is being shutdown.
     */
    STOPPING,

    /**
     * Sesame is shut down.
     */
    STOPPED;
}
