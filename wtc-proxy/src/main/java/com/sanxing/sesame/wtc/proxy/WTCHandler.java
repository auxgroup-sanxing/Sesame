/*
 * Copyright (c) 2013 Sanxing Electric, Inc.
 * All Rights Reserved.
 */
package com.sanxing.sesame.wtc.proxy;

/**
 * @author ShangjieZhou
 */
public interface WTCHandler
{
    WTCResponse handle(WTCRequest request);
}
