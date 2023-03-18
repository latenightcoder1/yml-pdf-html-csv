/*******************************************************************************
 * Copyright (C) 2023 Tarana Wireless, Inc. All Rights Reserved.
 ******************************************************************************/

package com.suraj.practice.service.resource.v1;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.opencsv.CSVWriter;
import com.suraj.practice.service.constant.DisplayTableConstants;
import com.suraj.practice.service.constant.EndpointPath;
import com.suraj.practice.service.component.EventDetailsPublishComponent;
import io.swagger.annotations.Api;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import javax.annotation.Resource;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

//url- http://localhost:8080/api/edp/v1/publish/table >> for html table display
//url- http://localhost:8080/api/edp/v1/publish/table?purpose=download >> for pdf download
//url- http://localhost:8080/api/edp/v1/publish/table?purpose=download&format=csv >> for csv download

/**
 * TCS generated events details.
 *
 * @author suraj kumar
 */
@Api(value = "Events Details Publisher Resource")
@Log4j2
@Service
@Path(EndpointPath.EVENT_DETAILS_PUBLISHER_SERVICE_V1_API)
public class EventsDetailsPublisherResource {

  @Resource
  private EventDetailsPublishComponent eventDetailsPublishComponent;


  @GET
  @Path("/table")
  @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML,
      org.springframework.http.MediaType.APPLICATION_PDF_VALUE})
  public Response generateTable(@QueryParam("purpose")
  @DefaultValue("display") final String purpose, @QueryParam("format")
  @DefaultValue("pdf") final String format) throws IOException {
    final Map<String, Map<String, String>> eventsDetails = eventDetailsPublishComponent.getEventDetails();
    if (purpose.equals("download")) {
      if (format.equals("csv")) {
        final InputStream inputStream = getInputStream(eventsDetails);
        return Response.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=events_" + LocalDateTime.now() + ".csv")
            .entity(inputStream).build();
      } else {
        final String htmlTable = mapToHtmlTable(eventsDetails);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
          final Document document = new Document(PageSize.A2);
          final PdfWriter pdfWriter = PdfWriter.getInstance(document, baos);
          document.open();
          // Parse the HTML string into an iText XMLWorkerHelper object
          final XMLWorkerHelper worker = XMLWorkerHelper.getInstance();
          final InputStream inputStream = new ByteArrayInputStream(htmlTable.getBytes());
          worker.parseXHtml(pdfWriter, document, inputStream);
          document.close();
        } catch (final Exception e) {
          log.error("An exception occurred while getting pdf table", e);
        }
        return Response
            .ok(baos.toByteArray(), org.springframework.http.MediaType.APPLICATION_PDF_VALUE)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=events_" + LocalDateTime.now() + ".pdf")
            .build();
      }
    } else {
      final String htmlTable = mapToHtmlTable(eventsDetails);
      return Response.ok().entity(htmlTable).build();
    }
  }

  public static String mapToHtmlTable(final Map<String, Map<String, String>> eventsDetails) {
    final String[] headers = {DisplayTableConstants.NUMBER, DisplayTableConstants.DISPLAY_NAME, DisplayTableConstants.DESCRIPTION, DisplayTableConstants.ADDITIONAL_DETAILS, DisplayTableConstants.CATEGORY,
        DisplayTableConstants.IS_INTERNAL, DisplayTableConstants.NAME, DisplayTableConstants.CONTEXT, DisplayTableConstants.SERVICE_NAME};
    final StringBuilder html = new StringBuilder();
    html.append(
        "<table style=\"border: 1px solid black; width:100%; background-color: #CCCCFF;\">");
    html.append(DisplayTableConstants.TR_START);
    for (final String header : headers) {
      html.append(
              "<th style=\"border: 1px solid black; color: white; background-color: #4CAF50;\">")
          .append(header).append(DisplayTableConstants.TH_END);
    }
    html.append(DisplayTableConstants.TR_END);
    int count = 1;
    for (final Map.Entry<String, Map<String, String>> entry : eventsDetails.entrySet()) {
      final Map<String, String> row = entry.getValue();
      html.append(DisplayTableConstants.TR_START);
      html.append("<td style=\"border: 1px solid black;\">").append(count++).append(
          DisplayTableConstants.TD_END);
      for (int i = 1; i < headers.length; i++) {
        final String header = headers[i];
        html.append("<td style=\"border: 1px solid black;\">").append(row.get(header))
            .append(DisplayTableConstants.TD_END);
      }
      html.append(DisplayTableConstants.TR_END);
    }
    html.append(DisplayTableConstants.TABLE_END);
    return html.toString();
  }

  private static InputStream getInputStream(final Map<String, Map<String, String>> values)
      throws IOException {
    final String[] headers = {DisplayTableConstants.DISPLAY_NAME, DisplayTableConstants.DESCRIPTION, DisplayTableConstants.ADDITIONAL_DETAILS, DisplayTableConstants.CATEGORY, DisplayTableConstants.IS_INTERNAL,
        DisplayTableConstants.NAME, DisplayTableConstants.CONTEXT, DisplayTableConstants.SERVICE_NAME};
    final StringWriter writer = new StringWriter();
    final CSVWriter csvWriter = new CSVWriter(writer);
    csvWriter.writeNext(headers);
    for (final Map.Entry<String, Map<String, String>> entry : values.entrySet()) {
      final Map<String, String> row = entry.getValue();
      final String[] rowData = {row.get(DisplayTableConstants.DISPLAY_NAME), row.get(
          DisplayTableConstants.DESCRIPTION),
          row.get(DisplayTableConstants.ADDITIONAL_DETAILS), row.get(DisplayTableConstants.CATEGORY), row.get(
          DisplayTableConstants.IS_INTERNAL),
          row.get(DisplayTableConstants.NAME), row.get(DisplayTableConstants.CONTEXT), row.get(
          DisplayTableConstants.SERVICE_NAME)};
      csvWriter.writeNext(rowData);
    }
    csvWriter.close();
    final String csv = writer.toString();

    final byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
    return new ByteArrayInputStream(bytes);
  }

}
