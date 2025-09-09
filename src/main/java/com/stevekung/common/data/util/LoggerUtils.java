package com.stevekung.common.data.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtils {
    public static Logger getLogger() {
        // Dynamically get caller class name
        return LoggerFactory.getLogger(Thread.currentThread().getStackTrace()[2].getClassName());
    }
}
