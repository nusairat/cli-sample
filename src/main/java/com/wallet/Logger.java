package com.wallet;

import org.slf4j.LoggerFactory;

public interface Logger {

    default public org.slf4j.Logger logger() {
        return LoggerFactory.getLogger(getClass());
    }

    default public void trace(String message) {
        logger().trace(message);
    }

    default public void debug(String message) {
        logger().debug(message);
    }

    default public void info(String message) {
        logger().info(message);
    }
    default public void info(String message, Object...args) {
        logger().info(message, args);
    }

    default public void warn(String message) {
        logger().info(message);
    }

    default public void warn(String message, Object...args) {
        logger().warn(message, args);
    }

    default public void error(String message) {
        logger().error(message);
    }

    default public void error(String message, Throwable t) {
        logger().error(message, t);
    }

    default public void error(String message, Object...s) {
        logger().error(message, s);
    }
}