/*******************************************************************************
 * Copyright (C) 2023 Tarana Wireless, Inc. All Rights Reserved.
 ******************************************************************************/

package com.suraj.practice.service.config;

import org.gitlab4j.api.GitLabApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Git lab configuration.
 *
 * @author suraj kumar
 */
@Configuration
public class GitLabConfig {

  @Value("${gitlab.api.endpoint}")
  private String gitLabApiEndpoint;

  @Value("${gitlab.api.private-token}")
  private String gitLabApiToken;

  @Bean
  public GitLabApi gitLabApi() {
    return new GitLabApi(gitLabApiEndpoint, gitLabApiToken);
  }
}