/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a proprietary license.
 * See the License.txt file for more information. You may not use this file
 * except in compliance with the proprietary license.
 */
package io.camunda.connector.box;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import io.camunda.connector.box.model.BoxPath;
import io.camunda.connector.box.model.BoxResult;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BoxUtil {

  public static BoxFile getFile(String path, BoxAPIConnection api) {
    return new BoxFile(api, getItem(path, api).getID());
  }

  public static BoxFolder getFolder(String path, BoxAPIConnection api) {
    return new BoxFolder(api, getItem(path, api).getID());
  }

  public static BoxItem.Info getItem(String path, BoxAPIConnection api) {
    return findItem(path, api)
        .orElseThrow(() -> new RuntimeException("Could not find item: " + path));
  }

  public static Optional<BoxItem.Info> findItem(String path, BoxAPIConnection api) {
    return findItem(BoxPath.from(path), api);
  }

  public static Optional<BoxItem.Info> findItem(BoxPath path, BoxAPIConnection api) {
    if (path instanceof BoxPath.Root) {
      return Optional.of(BoxFolder.getRootFolder(api).getInfo());
    }
    if (path instanceof BoxPath.Id id) {
      return Optional.of(new BoxFile(api, id.id()).getInfo());
    }
    if (path instanceof BoxPath.Segments segments) {
      return findItemInTree(BoxFolder.getRootFolder(api), segments);
    }
    throw new RuntimeException("Unknown path type: " + path);
  }

  public static Optional<BoxItem.Info> findItemInTree(BoxFolder folder, BoxPath.Segments segments) {
    String segment = segments.segments().get(0);
    return findItemByName(items(folder), segment)
        .flatMap(
            item ->
                segments.isPathEnd()
                    ? Optional.of(item)
                    : findItemInTree(
                        new BoxFolder(folder.getAPI(), item.getID()),
                        segments.withoutFirstSegment()));
  }

  private static Stream<BoxItem.Info> items(BoxFolder folder) {
    return StreamSupport.stream(folder.spliterator(), false);
  }

  private static Optional<BoxItem.Info> findItemByName(Stream<BoxItem.Info> items, String name) {
    return items.filter(item -> item.getName().equals(name)).findFirst();
  }

  public static byte[] download(BoxFile file) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      file.download(out);
      return out.toByteArray();
    } catch (Throwable e) {
      throw new RuntimeException("Error downloading file: " + file.getID(), e);
    }
  }

  public static BoxResult.Item item(BoxFile file) {
    return new BoxResult.Item(file.getID(), "file");
  }

  public static BoxResult.Item item(BoxFolder folder) {
    return new BoxResult.Item(folder.getID(), "folder");
  }

  public static BoxResult.Item item(BoxItem.Info info) {
    return new BoxResult.Item(info.getID(), info.getType());
  }
}
