/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package org.sanxing.sesame.core;

/**
 * The enum of possible operation modes in which Sesame Application may reside.
 * 
 * @author cstamas
 */
public enum OperationMode
{
    /**
     * Sesame is in standalone mode.
     */
    STANDALONE,

    /**
     * Sesame is config slave.
     */
    CONFIGURATION_SLAVE;
}
