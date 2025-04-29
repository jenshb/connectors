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
import io.camunda.document.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@TemplateSubType(id = "uploadObject", label = "Upload object")
public record UploadObject(
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
        String blobName,
    @TemplateProperty(
            label = "Document",
            group = "uploadObject",
            id = "uploadActionDocument",
            tooltip = "Document to upload on Azure Blob Storage",
            type = TemplateProperty.PropertyType.String,
            feel = Property.FeelMode.required,
            binding = @TemplateProperty.PropertyBinding(name = "action.document"))
        @NotNull
        Document document)
    implements BlobStorageAction {}
