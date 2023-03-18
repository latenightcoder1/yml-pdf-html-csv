/*******************************************************************************
 * Copyright (C) 2023 Tarana Wireless, Inc. All Rights Reserved.
 ******************************************************************************/

package com.suraj.practice.service.config;

import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Events source configuration.
 *
 * @author suraj kumar
 */
@Component
@ConfigurationProperties(prefix = "events")
@Data
public class EventSourceConfig {

  private ServiceEventsDetails additionalDetails;

  private ServiceEventsDetails categoryDetails;

  private ServiceEventsDetails internalEventsDetails;

  private List<ServiceEventsDetails> serviceEventsDetailsList;

  @Getter
  @Setter
  public static class ServiceEventsDetails {

    private String filePath;
    private long projectId;
    private String branch = "devel";
  }

}