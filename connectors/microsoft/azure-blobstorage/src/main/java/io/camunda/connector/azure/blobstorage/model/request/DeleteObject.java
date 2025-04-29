/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a proprietary license.
 * See the License.txt file for more information. You may not use this file
 * except in compliance with the proprietary license.
 */
package io.camunda.connector.azure.blobstorage.model.request;

import io.camunda.connector.generator.dsl.Property;
import io.camunda.connector.generator.java.annotation.TemplateProperty;
import io.camunda.connector.generator.java.annotation.TemplateSubType;
import jakarta.validation.constraints.NotBlank;

@TemplateSubType(id = "deleteObject", label = "Delete object")
public record DeleteObject(
    @TemplateProperty(
            label = "Azure Blob Storage Container",
            id = "deleteActionContainer",
            group = "deleteObject",
            tooltip = "Container from which an object should be deleted",
            feel = Property.FeelMode.optional,
            binding = @TemplateProperty.PropertyBinding(name = "action.container"))
        @NotBlank
        String container,
    @TemplateProperty(
            label = "Azure Blob Storage blob name",
            id = "deleteActionBlobName",
            group = "deleteObject",
            tooltip = "blob name of the object which should be deleted",
            feel = Property.FeelMode.optional,
            binding = @TemplateProperty.PropertyBinding(name = "action.blobName"))
        @NotBlank
        String blobName)
    implements BlobStorageAction {}
