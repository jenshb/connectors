/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 *       under one or more contributor license agreements. Licensed under a proprietary license.
 *       See the License.txt file for more information. You may not use this file
 *       except in compliance with the proprietary license.
 */
package io.camunda.connector.textract;

import static io.camunda.connector.textract.model.TextractRequestData.WRONG_NOTIFICATION_VALUES_MSG;
import static io.camunda.connector.textract.util.TextractTestUtils.ASYNC_EXECUTION_JSON_WITH_ROLE_ARN_AND_WITHOUT_SNS_TOPIC;
import static io.camunda.connector.textract.util.TextractTestUtils.ASYNC_EXECUTION_JSON_WITH_SNS_TOPIC_AND_WITHOUT_ROLE_ARN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.amazonaws.services.textract.model.AnalyzeDocumentResult;
import com.amazonaws.services.textract.model.Block;
import com.amazonaws.services.textract.model.GetDocumentAnalysisResult;
import com.amazonaws.services.textract.model.StartDocumentAnalysisResult;
import io.camunda.connector.api.error.ConnectorInputException;
import io.camunda.connector.test.outbound.OutboundConnectorContextBuilder;
import io.camunda.connector.textract.caller.AsyncTextractCaller;
import io.camunda.connector.textract.caller.PollingTextractCalller;
import io.camunda.connector.textract.caller.SyncTextractCaller;
import io.camunda.connector.textract.util.TextractTestUtils;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TextractConnectorFunctionTest {

  @Mock private SyncTextractCaller syncCaller;
  @Mock private PollingTextractCalller pollingCaller;
  @Mock private AsyncTextractCaller asyncCaller;

  @InjectMocks private TextractConnectorFunction textractConnectorFunction;

  @Test
  void executeSyncReq() throws Exception {
    var outBounderContext =
        OutboundConnectorContextBuilder.create()
            .secret("ACCESS_KEY", TextractTestUtils.ACTUAL_ACCESS_KEY)
            .secret("SECRET_KEY", TextractTestUtils.ACTUAL_SECRET_KEY)
            .variables(TextractTestUtils.SYNC_EXECUTION_JSON)
            .build();

    AnalyzeDocumentResult analyzeDocumentResult = new AnalyzeDocumentResult();
    analyzeDocumentResult.setBlocks(
        List.of(new Block().withText("text1"), new Block().withText("text1")));
    when(syncCaller.call(any(), any())).thenReturn(analyzeDocumentResult);

    final Set<String> result = (Set<String>) textractConnectorFunction.execute(outBounderContext);
    assertThat(result).containsOnly(analyzeDocumentResult.getBlocks().getFirst().getText());
  }

  @Test
  void executeAsyncReq() throws Exception {
    var outBounderContext =
        OutboundConnectorContextBuilder.create()
            .secret("ACCESS_KEY", TextractTestUtils.ACTUAL_ACCESS_KEY)
            .secret("SECRET_KEY", TextractTestUtils.ACTUAL_SECRET_KEY)
            .variables(TextractTestUtils.ASYNC_EXECUTION_JSON)
            .build();

    final StartDocumentAnalysisResult startDocAnalysisResult =
        new StartDocumentAnalysisResult().withJobId("jobId");

    when(asyncCaller.call(any(), any())).thenReturn(startDocAnalysisResult);

    final String gobId = (String) textractConnectorFunction.execute(outBounderContext);
    assertThat(gobId).isEqualTo(startDocAnalysisResult.getJobId());
  }

  @Test
  void executePollingReq() throws Exception {
    var outBounderContext =
        OutboundConnectorContextBuilder.create()
            .secret("ACCESS_KEY", TextractTestUtils.ACTUAL_ACCESS_KEY)
            .secret("SECRET_KEY", TextractTestUtils.ACTUAL_SECRET_KEY)
            .variables(TextractTestUtils.POLLING_EXECUTION_JSON)
            .build();

    final GetDocumentAnalysisResult getDocAnalysisResult = new GetDocumentAnalysisResult();
    getDocAnalysisResult.setBlocks(
        List.of(new Block().withText("text1"), new Block().withText("text1")));
    when(pollingCaller.call(any(), any())).thenReturn(getDocAnalysisResult);

    final Set<String> result = (Set<String>) textractConnectorFunction.execute(outBounderContext);
    assertThat(result).containsOnly(getDocAnalysisResult.getBlocks().getFirst().getText());
  }

  @Test
  void executeAsyncReqWithS3PrefixAndWithoutS3Bucket() {
    //todo
    OutboundConnectorContextBuilder builder = OutboundConnectorContextBuilder.create()
            .secret("ACCESS_KEY", TextractTestUtils.ACTUAL_ACCESS_KEY)
            .secret("SECRET_KEY", TextractTestUtils.ACTUAL_SECRET_KEY);
    OutboundConnectorContextBuilder.TestConnectorContext outBounderContext = builder.variables(TextractTestUtils.ASYNC_EXECUTION_JSON_WITHOUT_S3_BUCKET_OUTPUT)
            .build();

    Exception exception =
        assertThrows(
                ConnectorInputException.class,
            () -> textractConnectorFunction.execute(outBounderContext));

//    assertThat(exception.getMessage()).isEqualTo(WRONG_OUTPUT_VALUES_MSG);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        ASYNC_EXECUTION_JSON_WITH_ROLE_ARN_AND_WITHOUT_SNS_TOPIC,
        ASYNC_EXECUTION_JSON_WITH_SNS_TOPIC_AND_WITHOUT_ROLE_ARN
      })
  void executeAsyncReqWithWrongNotificationData(String input) {
    var outBounderContext =
        OutboundConnectorContextBuilder.create()
            .secret("ACCESS_KEY", TextractTestUtils.ACTUAL_ACCESS_KEY)
            .secret("SECRET_KEY", TextractTestUtils.ACTUAL_SECRET_KEY)
            .variables(input)
            .build();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> textractConnectorFunction.execute(outBounderContext));

    assertThat(exception.getMessage()).isEqualTo(WRONG_NOTIFICATION_VALUES_MSG);
  }
}
