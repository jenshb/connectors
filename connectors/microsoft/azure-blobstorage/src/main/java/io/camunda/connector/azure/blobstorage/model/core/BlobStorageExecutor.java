/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a proprietary license.
 * See the License.txt file for more information. You may not use this file
 * except in compliance with the proprietary license.
 */
package io.camunda.connector.azure.blobstorage.model.core;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import io.camunda.connector.azure.blobstorage.model.request.BlobStorageAction;
import io.camunda.connector.azure.blobstorage.model.request.BlobStorageRequest;
import io.camunda.connector.azure.blobstorage.model.request.DeleteObject;
import io.camunda.connector.azure.blobstorage.model.request.DownloadObject;
import io.camunda.connector.azure.blobstorage.model.request.UploadObject;
import io.camunda.connector.azure.blobstorage.model.response.DeleteResponse;
import io.camunda.connector.azure.blobstorage.model.response.DownloadResponse;
import io.camunda.connector.azure.blobstorage.model.response.UploadResponse;
import io.camunda.document.Document;
import io.camunda.document.store.DocumentCreationRequest;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlobStorageExecutor {

  private static final Logger log = LoggerFactory.getLogger(BlobStorageExecutor.class);
  private final BlobContainerClient blobContainerClient;
  private final BlobClient blobClient;
  private final Function<DocumentCreationRequest, Document> createDocument;

  public BlobStorageExecutor(BlobContainerClient blobContainerClient, BlobClient blobClient, Function<DocumentCreationRequest, Document> createDocument) {
    this.blobContainerClient = blobContainerClient;
    this.blobClient = blobClient;
    this.createDocument = createDocument;
  }

  public static BlobStorageExecutor create(
      BlobStorageRequest blobStorageRequest, Function<DocumentCreationRequest, Document> createDocument) {
    return null; // TODO
//        new BlobStorageExecutor(
////        S3Client.builder()
////            .credentialsProvider(CredentialsProviderSupportV2.credentialsProvider(blobStorageRequest))
////            .region(Region.of(blobStorageRequest.getConfiguration().region()))
////            .build(),
//        createDocument);
  }

  public Object execute(BlobStorageAction blobStorageAction) {
    return switch (blobStorageAction) {
      case DeleteObject deleteObject -> delete(deleteObject);
      case DownloadObject downloadObject -> download(downloadObject);
      case UploadObject uploadObject -> upload(uploadObject);
    };
  }

  private UploadResponse upload(UploadObject uploadObject) {
    Long contentLength = uploadObject.document().metadata().getSize();
    String contentType = uploadObject.document().metadata().getContentType();

    return null;
//    PutObjectRequest putObjectRequest =
//        PutObjectRequest.builder()
//            .bucket(uploadObject.bucket())
//            .key(
//                Optional.ofNullable(uploadObject.key())
//                    .orElse(uploadObject.document().metadata().getFileName()))
//            .contentLength(contentLength)
//            .contentType(contentType)
//            .build();
//
//    this.s3Client.putObject(
//        putObjectRequest,
//        RequestBody.fromInputStream(uploadObject.document().asInputStream(), contentLength));
//
//    return new UploadResponse(
//        uploadObject.bucket(),
//        uploadObject.key(),
//        String.format("https://%s.s3.amazonaws.com/%s", uploadObject.bucket(), uploadObject.key()));
  }

  private DownloadResponse download(DownloadObject downloadObject) {
    return null;
//    GetObjectRequest getObjectRequest =
//        GetObjectRequest.builder()
//            .bucket(downloadObject.bucket())
//            .key(downloadObject.key())
//            .build();
//
//    ResponseInputStream<GetObjectResponse> getObjectResponse =
//        this.s3Client.getObject(getObjectRequest);
//
//    if (!downloadObject.asFile()) {
//      try {
//        return retrieveResponseWithContent(
//            downloadObject.bucket(), downloadObject.key(), getObjectResponse);
//      } catch (IOException e) {
//        log.error("An error occurred while trying to read and parse the downloaded file", e);
//        throw new RuntimeException(e);
//      }
//    } else {
//      return this.createDocument
//          .andThen(
//              document ->
//                  new DownloadResponse(
//                      downloadObject.bucket(),
//                      downloadObject.key(),
//                      new Element.DocumentContent(document)))
//          .apply(
//              DocumentCreationRequest.from(getObjectResponse)
//                  .contentType(getObjectResponse.response().contentType())
//                  .fileName(downloadObject.key())
//                  .build());
//    }
  }

//  private DownloadResponse retrieveResponseWithContent(
//      String bucket, String key, ResponseInputStream<GetObjectResponse> responseResponseInputStream)
//      throws IOException {
//    byte[] rawBytes = responseResponseInputStream.readAllBytes();
//    return switch (responseResponseInputStream.response().contentType()) {
//      case "text/plain" ->
//          new DownloadResponse(
//              bucket, key, new Element.StringContent(new String(rawBytes, StandardCharsets.UTF_8)));
//      case "application/json" ->
//          new DownloadResponse(
//              bucket, key, new Element.JsonContent(new ObjectMapper().readTree(rawBytes)));
//      default ->
//          new DownloadResponse(
//              bucket, key, new Element.StringContent(Base64.getEncoder().encodeToString(rawBytes)));
//    };
//  }

  private DeleteResponse delete(DeleteObject deleteObject) {
    return null;
//    DeleteObjectRequest deleteObjectRequest =
//        DeleteObjectRequest.builder().bucket(deleteObject.bucket()).key(deleteObject.key()).build();
//
//    this.s3Client.deleteObject(deleteObjectRequest);
//    return new DeleteResponse(deleteObject.bucket(), deleteObject.key());
  }
}
