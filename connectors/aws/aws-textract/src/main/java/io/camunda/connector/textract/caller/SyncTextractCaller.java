/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 *       under one or more contributor license agreements. Licensed under a proprietary license.
 *       See the License.txt file for more information. You may not use this file
 *       except in compliance with the proprietary license.
 */
package io.camunda.connector.textract.caller;

import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.model.AnalyzeDocumentRequest;
import com.amazonaws.services.textract.model.AnalyzeDocumentResult;
import com.amazonaws.services.textract.model.Document;
import com.amazonaws.services.textract.model.S3Object;
import io.camunda.connector.textract.model.TextractRequestData;

public class SyncTextractCaller implements TextractCaller<AnalyzeDocumentResult> {

  @Override
  public AnalyzeDocumentResult call(
      TextractRequestData requestData, AmazonTextract textractClient) {
    final S3Object s3Obj = this.prepareS3Obj(requestData);
    final Document document = new Document().withS3Object(s3Obj);

    final AnalyzeDocumentRequest analyzeDocumentRequest =
        new AnalyzeDocumentRequest()
            .withFeatureTypes(prepareFeatureTypes(requestData))
            .withDocument(document);

    return textractClient.analyzeDocument(analyzeDocumentRequest);
  }
}
