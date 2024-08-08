/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 *       under one or more contributor license agreements. Licensed under a proprietary license.
 *       See the License.txt file for more information. You may not use this file
 *       except in compliance with the proprietary license.
 */
package io.camunda.connector.textract;

import static io.camunda.connector.textract.suppliers.util.AmazonTextractClientUtil.getAsyncTextractClient;
import static io.camunda.connector.textract.suppliers.util.AmazonTextractClientUtil.getSyncTextractClient;

import com.amazonaws.services.textract.model.Block;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.generator.java.annotation.ElementTemplate;
import io.camunda.connector.textract.caller.AsyncTextractCaller;
import io.camunda.connector.textract.caller.PollingTextractCalller;
import io.camunda.connector.textract.caller.SyncTextractCaller;
import io.camunda.connector.textract.model.TextractRequest;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OutboundConnector(
    name = "AWS Textract",
    inputVariables = {"authentication", "configuration", "input"},
    type = "io.camunda:aws-textract:1")
@ElementTemplate(
    id = "io.camunda.connectors.AWSTEXTRACT.v1",
    name = "AWS Textract Outbound Connector",
    description =
        "Automatically extract printed text, handwriting, layout elements, and data from any document",
    inputDataClass = TextractRequest.class,
    version = 1,
    propertyGroups = {
      @ElementTemplate.PropertyGroup(id = "authentication", label = "Authentication"),
      @ElementTemplate.PropertyGroup(id = "configuration", label = "Configuration"),
      @ElementTemplate.PropertyGroup(id = "input", label = "Configure input")
    },
    documentationRef =
        "https://docs.camunda.io/docs/next/components/connectors/out-of-the-box-connectors/amazon-textract/",
    icon = "icon.svg")
public class TextractConnectorFunction implements OutboundConnectorFunction {

  private static final Logger LOGGER = LoggerFactory.getLogger(TextractConnectorFunction.class);

  private final SyncTextractCaller syncTextractCaller;

  private final PollingTextractCalller pollingTextractCaller;

  private final AsyncTextractCaller asyncTextractCaller;

  public TextractConnectorFunction() {
    this.syncTextractCaller = new SyncTextractCaller();
    this.pollingTextractCaller = new PollingTextractCalller();
    this.asyncTextractCaller = new AsyncTextractCaller();
  }

  public TextractConnectorFunction(
      SyncTextractCaller syncTextractCaller,
      PollingTextractCalller pollingTextractCaller,
      AsyncTextractCaller asyncTextractCaller) {
    this.syncTextractCaller = syncTextractCaller;
    this.pollingTextractCaller = pollingTextractCaller;
    this.asyncTextractCaller = asyncTextractCaller;
  }

  @Override
  public Object execute(OutboundConnectorContext context) throws Exception {
    TextractRequest request;
    try {
      request = context.bindVariables(TextractRequest.class);
    } catch (Exception ex) {
      LOGGER.warn("Invalid data provided: {}", ex.getCause().getCause().getMessage());
      throw new IllegalArgumentException(ex.getCause().getCause().getMessage());
    }

    return switch (request.getInput().executionType()) {
      case SYNC -> executeSync(request);
      case POLLING -> executePolling(request);
      case ASYNC -> executeAsync(request);
    };
  }

  private String executeAsync(final TextractRequest request) {
    return asyncTextractCaller.call(request.getInput(), getAsyncTextractClient(request)).getJobId();
  }

  private Set<String> executePolling(final TextractRequest request) throws Exception {
    final var analysisResult =
        pollingTextractCaller.call(request.getInput(), getAsyncTextractClient(request));
    return fetchText(analysisResult.getBlocks());
  }

  private Set<String> executeSync(final TextractRequest request) {
    final var docResult =
        syncTextractCaller.call(request.getInput(), getSyncTextractClient(request));
    return fetchText(docResult.getBlocks());
  }

  private Set<String> fetchText(final List<Block> blocks) {
    return blocks.stream().map(Block::getText).filter(Objects::nonNull).collect(Collectors.toSet());
  }
}
