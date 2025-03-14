/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a proprietary license.
 * See the License.txt file for more information. You may not use this file
 * except in compliance with the proprietary license.
 */
package io.camunda.connector.box;

import static io.camunda.connector.box.BoxUtil.download;
import static io.camunda.connector.box.BoxUtil.getFile;
import static io.camunda.connector.box.BoxUtil.getFolder;
import static io.camunda.connector.box.BoxUtil.item;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxCCGAPIConnection;
import com.box.sdk.BoxConfig;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.box.sdk.BoxSearch;
import com.box.sdk.BoxSearchParameters;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.box.model.BoxRequest;
import io.camunda.connector.box.model.BoxRequest.Operation.Search.SortDirection;
import io.camunda.connector.box.model.BoxResult;
import io.camunda.document.Document;
import io.camunda.document.store.DocumentCreationRequest;
import java.util.Optional;
import java.util.stream.Collectors;

public class BoxOperations {

  public static BoxResult execute(BoxRequest request, OutboundConnectorContext context) {
    var api = connectToApi(request.authentication());

    BoxRequest.Operation operation = request.operation();
    if (operation instanceof BoxRequest.Operation.UploadFile) {
      return uploadFile((BoxRequest.Operation.UploadFile) operation, api);
    } else if (operation instanceof BoxRequest.Operation.DownloadFile) {
      return downloadFile((BoxRequest.Operation.DownloadFile) operation, api, context);
    } else if (operation instanceof BoxRequest.Operation.MoveFile) {
      return moveFile((BoxRequest.Operation.MoveFile) operation, api);
    } else if (operation instanceof BoxRequest.Operation.DeleteFile) {
      return deleteFile((BoxRequest.Operation.DeleteFile) operation, api);
    } else if (operation instanceof BoxRequest.Operation.CreateFolder) {
      return createFolder((BoxRequest.Operation.CreateFolder) operation, api);
    } else if (operation instanceof BoxRequest.Operation.DeleteFolder) {
      return deleteFolder((BoxRequest.Operation.DeleteFolder) operation, api);
    } else if (operation instanceof BoxRequest.Operation.Search) {
      return search((BoxRequest.Operation.Search) operation, api);
    } else {
      throw new IllegalArgumentException("Unknown operation: " + operation);
    }
  }

  private static BoxAPIConnection connectToApi(BoxRequest.Authentication authentication) {
    if (authentication instanceof BoxRequest.Authentication.DeveloperToken) {
      return new BoxAPIConnection(
          ((BoxRequest.Authentication.DeveloperToken) authentication).accessToken());
    } else if (authentication instanceof BoxRequest.Authentication.ClientCredentialsUser) {
      BoxRequest.Authentication.ClientCredentialsUser user =
          (BoxRequest.Authentication.ClientCredentialsUser) authentication;
      return BoxCCGAPIConnection.userConnection(
          user.clientId(), user.clientSecret(), user.userId());
    } else if (authentication instanceof BoxRequest.Authentication.ClientCredentialsEnterprise) {
      BoxRequest.Authentication.ClientCredentialsEnterprise enterprise =
          (BoxRequest.Authentication.ClientCredentialsEnterprise) authentication;
      return BoxCCGAPIConnection.applicationServiceAccountConnection(
          enterprise.clientId(), enterprise.clientSecret(), enterprise.enterpriseId());
    } else if (authentication instanceof BoxRequest.Authentication.JWTJsonConfig) {
      BoxConfig boxConfig =
          BoxConfig.readFrom(
              ((BoxRequest.Authentication.JWTJsonConfig) authentication).jsonConfig());
      return BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig);
    } else {
      throw new IllegalArgumentException("Unknown authentication type: " + authentication);
    }
  }

  private static BoxResult.Upload uploadFile(
      BoxRequest.Operation.UploadFile uploadFile, BoxAPIConnection api) {
    var folder = getFolder(uploadFile.folderPath(), api);
    var file = folder.uploadFile(uploadFile.document().asInputStream(), uploadFile.getFileName());
    return new BoxResult.Upload(item(file));
  }

  private static BoxResult.Download downloadFile(
      BoxRequest.Operation.DownloadFile downloadFile,
      BoxAPIConnection api,
      OutboundConnectorContext context) {
    var file = getFile(downloadFile.filePath(), api);
    var document = createDocument(file, context);
    return new BoxResult.Download(item(file), document);
  }

  private static Document createDocument(BoxFile file, OutboundConnectorContext context) {
    var fileContent = download(file);
    var documentCreationRequest =
        DocumentCreationRequest.from(fileContent).fileName(file.getInfo().getName()).build();
    return context.create(documentCreationRequest);
  }

  private static BoxResult deleteFile(
      BoxRequest.Operation.DeleteFile deleteFile, BoxAPIConnection api) {
    BoxFile file = getFile(deleteFile.filePath(), api);
    file.delete();
    return new BoxResult.Generic(item(file));
  }

  private static BoxResult moveFile(BoxRequest.Operation.MoveFile moveFile, BoxAPIConnection api) {
    BoxFile file = getFile(moveFile.filePath(), api);
    BoxFolder folder = getFolder(moveFile.folderPath(), api);
    BoxItem.Info info = file.move(folder);
    return new BoxResult.Generic(item(info));
  }

  private static BoxResult deleteFolder(
      BoxRequest.Operation.DeleteFolder deleteFolder, BoxAPIConnection api) {
    var folder = getFolder(deleteFolder.folderPath(), api);
    folder.delete(deleteFolder.recursive());
    return new BoxResult.Generic(item(folder));
  }

  private static BoxResult createFolder(
      BoxRequest.Operation.CreateFolder createFolder, BoxAPIConnection api) {
    var folder = getFolder(createFolder.folderPath(), api).createFolder(createFolder.name());
    return new BoxResult.Generic(item(folder));
  }

  private static BoxResult.Search search(BoxRequest.Operation.Search search, BoxAPIConnection api) {
    var searchParams = searchParameters(search);
    var offset = Optional.ofNullable(search.offset()).orElse(0L);
    var limit = Optional.ofNullable(search.limit()).orElse(50L);
    BoxSearch boxSearch = new BoxSearch(api);
    var items =
        boxSearch.searchRange(offset, limit, searchParams).stream()
            .map(BoxUtil::item)
            .collect(Collectors.toList());
    return new BoxResult.Search(items);
  }

  private static BoxSearchParameters searchParameters(BoxRequest.Operation.Search search) {
    BoxSearchParameters searchParams = new BoxSearchParameters();
    searchParams.setQuery(search.query());
    Optional.ofNullable(search.sortColumn()).ifPresent(searchParams::setSort);
    Optional.ofNullable(search.sortDirection())
        .map(SortDirection::getValue)
        .ifPresent(searchParams::setDirection);
    return searchParams;
  }
}
