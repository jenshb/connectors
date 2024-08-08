/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 *       under one or more contributor license agreements. Licensed under a proprietary license.
 *       See the License.txt file for more information. You may not use this file
 *       except in compliance with the proprietary license.
 */
package io.camunda.connector.textract.caller;

import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.model.NotificationChannel;
import com.amazonaws.services.textract.model.OutputConfig;
import com.amazonaws.services.textract.model.StartDocumentAnalysisRequest;
import com.amazonaws.services.textract.model.StartDocumentAnalysisResult;
import io.camunda.connector.textract.model.TextractRequestData;
import org.apache.commons.lang3.StringUtils;

public class AsyncTextractCaller implements TextractCaller<StartDocumentAnalysisResult> {
  @Override
  public StartDocumentAnalysisResult call(
      TextractRequestData requestData, AmazonTextract textractClient) {
    final StartDocumentAnalysisRequest startDocumentAnalysisRequest =
        new StartDocumentAnalysisRequest()
            .withFeatureTypes(prepareFeatureTypes(requestData))
            .withDocumentLocation(prepareDocumentLocation(requestData));

    startDocumentAnalysisRequest.withClientRequestToken(requestData.clientRequestToken());
    startDocumentAnalysisRequest.withJobTag(requestData.jobTag());
    startDocumentAnalysisRequest.withKMSKeyId(requestData.kmsKeyId());

    prepareNotification(startDocumentAnalysisRequest, requestData);
    prepareOutput(startDocumentAnalysisRequest, requestData);

    return textractClient.startDocumentAnalysis(startDocumentAnalysisRequest);
  }

  /*
  case 1:
  when NotificationChannel value is null
  the result of textractClient.startDocumentAnalysis(startDocumentAnalysisRequest) is SUCCESSFUL
  case 2:
  when NotificationChannel value is not equal to null but its fields are not set
  the result of textractClient.startDocumentAnalysis(startDocumentAnalysisRequest) is FAIL
   */
  private void prepareNotification(
      StartDocumentAnalysisRequest startDocumentAnalysisRequest, TextractRequestData requestData) {
    String roleArn = requestData.notificationChannelRoleArn();
    String snsTopic = requestData.notificationChannelSnsTopicArn();
    if (StringUtils.isNoneEmpty(roleArn, snsTopic)) {
      NotificationChannel notificationChannel =
          new NotificationChannel().withSNSTopicArn(snsTopic).withRoleArn(roleArn);

      startDocumentAnalysisRequest.withNotificationChannel(notificationChannel);
    }
  }

  private void prepareOutput(
      StartDocumentAnalysisRequest startDocumentAnalysisRequest, TextractRequestData requestData) {
    String s3Bucket = requestData.outputConfigS3Bucket();
    String s3Prefix = requestData.outputConfigS3Prefix();
    if (StringUtils.isNoneEmpty(s3Bucket)) {
      OutputConfig outputConfig = new OutputConfig().withS3Bucket(s3Bucket).withS3Prefix(s3Prefix);

      startDocumentAnalysisRequest.withOutputConfig(outputConfig);
    }
  }
}
