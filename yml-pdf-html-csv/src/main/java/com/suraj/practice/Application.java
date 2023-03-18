/*******************************************************************************
 * Copyright (C) 2023 Tarana Wireless, Inc. All Rights Reserved.
 ******************************************************************************/

package com.suraj.practice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Entry point of spring boot application.
 *
 * @author suraj kumar
 */
@SpringBootApplication
@EnableSwagger2
public class Application {

  /**
   * main method - starts the tomcat server.
   *
   * @param args arguments to spring application.
   */
  public static void main(final String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
