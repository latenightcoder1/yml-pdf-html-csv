/*******************************************************************************
 * Copyright (C) 2023 Tarana Wireless, Inc. All Rights Reserved.
 ******************************************************************************/

package com.suraj.practice.service.component;

import com.suraj.practice.service.config.EventSourceConfig;
import com.suraj.practice.service.config.EventSourceConfig.ServiceEventsDetails;
import com.suraj.practice.service.constant.DisplayTableConstants;
import com.suraj.practice.service.constant.EventSourceConstants;
import com.suraj.practice.service.repository.GitLabRepository;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

/**
 * Event details publish component.
 *
 * @author suraj kumar
 */
@Component
@Log4j2
public class EventDetailsPublishComponent {

  @Resource
  private EventSourceConfig eventSourceConfig;

  @Resource
  private GitLabRepository gitLabRepository;

  public Map<String, Map<String, String>> getEventDetails() {
    //additional details
    final Map<String, String> eventAdditionalDetails = getAdditionalDetailsTemplateDetails();
    //internal events
    final Set<String> internalEvents = getInternalEvents();
    //categorized events
    final Map<String, String> eventsCategoryMap = getEventsCategoryMap();
    //event details
    return getEventDetails(eventAdditionalDetails,
        internalEvents, eventsCategoryMap);
  }

  private Map<String, Map<String, String>> getEventDetails(
      final Map<String, String> eventAdditionalDetails, final Set<String> internalEvents,
      final Map<String, String> eventsCategoryMap) {
    final List<ServiceEventsDetails> ServiceEventsDetailsList = eventSourceConfig.getServiceEventsDetailsList();
    final Map<String, Map<String, String>> eventsDetails = new HashMap<>();
    for (final ServiceEventsDetails serviceEventsDetails : ServiceEventsDetailsList) {
      final List<String> allLines = getFileDetails(serviceEventsDetails);
      final String serviceName = serviceEventsDetails.getFilePath().split(EventSourceConstants.SLASH)[0];
      final Map<String, List<String>> eventsRawDetails = allLines.stream()
          .filter(line -> line.startsWith(EventSourceConstants.EVENT_CONFIGURATION_PREFIX))
          .map(line -> line.substring(EventSourceConstants.EVENT_CONFIGURATION_PREFIX.length()))
          .collect(Collectors.groupingBy(a -> a.split(EventSourceConstants.DOT)[0]));
      for (final Map.Entry<String, List<String>> entry : eventsRawDetails.entrySet()) {
        final Map<String, String> map = entry.getValue().stream().map(a -> a.split(
                EventSourceConstants.EQUALS))
            .collect(Collectors.toMap(a -> a[0].split(EventSourceConstants.DOT)[1], a -> a[1]));
        map.put(DisplayTableConstants.SERVICE_NAME, serviceName);
        map.put(DisplayTableConstants.ADDITIONAL_DETAILS, eventAdditionalDetails.get(map.get(
            DisplayTableConstants.NAME)));
        map.put(DisplayTableConstants.CATEGORY, eventsCategoryMap.getOrDefault(map.get(
            DisplayTableConstants.NAME), "OTHER"));
        map.put(DisplayTableConstants.IS_INTERNAL, String.valueOf(internalEvents.contains(map.get(
            DisplayTableConstants.NAME))));
        eventsDetails.put(entry.getKey(), map);
      }
    }
    return eventsDetails;
  }

  private List<String> getFileDetails(final ServiceEventsDetails serviceEventsDetails) {
    try {
      final InputStream inputStream = gitLabRepository.getFileRawData(
          serviceEventsDetails.getProjectId(),
          serviceEventsDetails.getBranch(),
          serviceEventsDetails.getFilePath());
      return inputStreamToList(inputStream);
    } catch (final Exception e) {
      log.error("An error occurred while reading data ", e);
    }
    return new ArrayList<>();
  }


  private Map<String, String> getEventsCategoryMap() {
    final Map<String, String> eventsCategoryDetails = new HashMap<>();
    try {
      final InputStream inputStream = gitLabRepository.getFileRawData(
          eventSourceConfig.getCategoryDetails().getProjectId(),
          eventSourceConfig.getCategoryDetails().getBranch(),
          eventSourceConfig.getCategoryDetails().getFilePath());
      final List<String> allLines = inputStreamToList(inputStream);
      for (final String s : allLines) {
        if (s.startsWith(EventSourceConstants.EVENTS_CATEGORY_PREFIX)) {
          final String categoryDetails = s.substring(EventSourceConstants.EVENTS_CATEGORY_PREFIX.length());
          final String[] tokens = categoryDetails.split(EventSourceConstants.EQUALS);
          final String[] events = tokens[1].split(EventSourceConstants.COMMA);
          for (final String event : events) {
            eventsCategoryDetails.put(event, tokens[0]);
          }
        }
      }
    } catch (final Exception e) {
      log.error("An error occurred while getting category details", e);
    }
    return eventsCategoryDetails;
  }

  private Set<String> getInternalEvents() {
    try {
      final InputStream inputStream = gitLabRepository.getFileRawData(
          eventSourceConfig.getInternalEventsDetails().getProjectId(),
          eventSourceConfig.getInternalEventsDetails().getBranch(),
          eventSourceConfig.getInternalEventsDetails().getFilePath());
      final List<String> allLines = inputStreamToList(inputStream);
      for (final String s : allLines) {
        if (s.startsWith(EventSourceConstants.INTERNAL_EVENTS_PREFIX)) {
          final String internalEventsString = s.substring(
              EventSourceConstants.INTERNAL_EVENTS_PREFIX.length());
          final String[] internalEventsArray = internalEventsString.split(EventSourceConstants.COMMA);
          return new HashSet<>(Arrays.asList(internalEventsArray));
        }
      }
    } catch (final Exception e) {
      log.error("An error occurred while getting internal events details", e);
    }
    return new HashSet<>();
  }

  private Map<String, String> getAdditionalDetailsTemplateDetails() {
    try {
      final InputStream inputStream = gitLabRepository.getFileRawData(
          eventSourceConfig.getAdditionalDetails().getProjectId(),
          eventSourceConfig.getAdditionalDetails().getBranch(),
          eventSourceConfig.getAdditionalDetails().getFilePath());
      final Yaml yaml = new Yaml();
      final List<Map<String, Object>> data = ((Map<String, Map<String, Map<String, List<Map<String, Object>>>>>) yaml.load(
          inputStream)).get("events").get("details").get("templates");
      Map<String, String> additionalData = data.stream().collect(
          Collectors.toMap(a -> (String) a.get("eventName"),
              a -> (String) ((Map<String, String>) ((List<Object>) a.get("additionalDetails")).get(
                  0)).get("templateString")));
      return additionalData;
    } catch (final Exception e) {
      log.error("An error occurred while getting additional details", e);
      return new HashMap<>();

    }
  }

  public List<String> inputStreamToList(final InputStream inputStream) throws Exception {
    final List<String> lines = new ArrayList<>();
    final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,
        StandardCharsets.UTF_8));
    String line;
    while ((line = reader.readLine()) != null) {
      lines.add(line);
    }
    reader.close();
    return lines;
  }
}