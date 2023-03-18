/*******************************************************************************
 * Copyright (C) 2023 Tarana Wireless, Inc. All Rights Reserved.
 ******************************************************************************/

package com.suraj.practice.service.config;

import com.suraj.practice.service.constant.EndpointPath;
import com.suraj.practice.service.resource.v1.EventsDetailsPublisherResource;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.config.SwaggerConfigLocator;
import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.jaxrs.listing.AcceptHeaderApiListingResource;
import io.swagger.jaxrs.listing.ApiListingResource;
import java.util.Collections;
import javax.annotation.PostConstruct;
import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * Jersey configuration.
 *
 * @author suraj kumar
 */
@Configuration
@ApplicationPath(EndpointPath.EDPS_API_BASE_PATH)
public class JerseyConfig extends ResourceConfig {

  @Autowired
  public JerseyConfig() {
    final BeanConfig swaggerConfig = new BeanConfig();
    swaggerConfig.setBasePath(EndpointPath.EDPS_API_BASE_PATH);
    swaggerConfig.setResourcePackage("com.tarana.tcc.events.details.publisher.service.resource");
    swaggerConfig.setPrettyPrint(true);
    swaggerConfig.setScan(true);

    SwaggerConfigLocator.getInstance().putConfig(SwaggerContextService.CONFIG_ID_DEFAULT,
        swaggerConfig);
    // inbuilt
    register(MultiPartFeature.class);

    // service resource
    register(EventsDetailsPublisherResource.class);

    // swagger resource
    register(ApiListingResource.class);
    register(AcceptHeaderApiListingResource.class);

    setProperties(
        Collections.singletonMap("jersey.config.server.response.setStatusOverSendError", true));
  }

  @PostConstruct
  public void setUp() {
  }

}
