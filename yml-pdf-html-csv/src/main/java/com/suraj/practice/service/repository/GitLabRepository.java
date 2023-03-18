/*******************************************************************************
 * Copyright (C) 2023 Tarana Wireless, Inc. All Rights Reserved.
 ******************************************************************************/

package com.suraj.practice.service.repository;

import java.io.InputStream;
import javax.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.springframework.stereotype.Component;

/**
 * Entry point to interact with gitlab.
 *
 * @author suraj kumar
 */
@Component
@Log4j2
public class GitLabRepository {

  @Resource
  private GitLabApi gitLabApi;

  public InputStream getFileRawData(final long projectId, final String branchName,
      final String filePath) throws GitLabApiException {
    try {
      return gitLabApi.getRepositoryFileApi()
          .getRawFile(projectId, branchName, filePath);
    } catch (final Exception e) {
      log.error("An error occurred while fetching (file={}, projectId={}, branch={})", filePath,
          projectId, branchName, e);
      throw e;
    }
  }

}