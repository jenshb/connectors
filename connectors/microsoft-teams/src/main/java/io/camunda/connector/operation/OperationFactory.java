/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a proprietary license.
 * See the License.txt file for more information. You may not use this file
 * except in compliance with the proprietary license.
 */
package io.camunda.connector.operation;

import io.camunda.connector.model.request.data.CreateChannel;
import io.camunda.connector.model.request.data.CreateChat;
import io.camunda.connector.model.request.data.GetChannel;
import io.camunda.connector.model.request.data.GetChannelMessage;
import io.camunda.connector.model.request.data.GetChat;
import io.camunda.connector.model.request.data.GetMessageInChat;
import io.camunda.connector.model.request.data.ListChannelMembers;
import io.camunda.connector.model.request.data.ListChannelMessages;
import io.camunda.connector.model.request.data.ListChannels;
import io.camunda.connector.model.request.data.ListChatMembers;
import io.camunda.connector.model.request.data.ListChats;
import io.camunda.connector.model.request.data.ListMessageRepliesInChannel;
import io.camunda.connector.model.request.data.ListMessagesInChat;
import io.camunda.connector.model.request.data.MSTeamsRequestData;
import io.camunda.connector.model.request.data.SendMessageInChat;
import io.camunda.connector.model.request.data.SendMessageToChannel;
import io.camunda.connector.operation.channel.CreateChannelOperation;
import io.camunda.connector.operation.channel.GetChannelMessageOperation;
import io.camunda.connector.operation.channel.GetChannelOperation;
import io.camunda.connector.operation.channel.ListChannelMembersOperation;
import io.camunda.connector.operation.channel.ListChannelMessagesOperation;
import io.camunda.connector.operation.channel.ListChannelsOperation;
import io.camunda.connector.operation.channel.ListMessageRepliesInChannelOperation;
import io.camunda.connector.operation.channel.SendMessageToChannelOperation;
import io.camunda.connector.operation.chat.CreateChatOperation;
import io.camunda.connector.operation.chat.GetChatOperation;
import io.camunda.connector.operation.chat.GetMessageInChatOperation;
import io.camunda.connector.operation.chat.ListChatMembersChatOperation;
import io.camunda.connector.operation.chat.ListChatsOperation;
import io.camunda.connector.operation.chat.ListMessagesInChatOperation;
import io.camunda.connector.operation.chat.SendMessageInChatChatOperation;

public class OperationFactory {
  public Operation getService(final MSTeamsRequestData data) {
    if (data instanceof CreateChat) {
      return new CreateChatOperation((CreateChat) data);
    } else if (data instanceof GetChat) {
      return new GetChatOperation((GetChat) data);
    } else if (data instanceof GetMessageInChat) {
      return new GetMessageInChatOperation((GetMessageInChat) data);
    } else if (data instanceof ListChatMembers) {
      return new ListChatMembersChatOperation((ListChatMembers) data);
    } else if (data instanceof ListChats) {
      return new ListChatsOperation((ListChats) data);
    } else if (data instanceof ListMessagesInChat) {
      return new ListMessagesInChatOperation((ListMessagesInChat) data);
    } else if (data instanceof SendMessageInChat) {
      return new SendMessageInChatChatOperation((SendMessageInChat) data);
    } else if (data instanceof CreateChannel) {
      return new CreateChannelOperation((CreateChannel) data);
    } else if (data instanceof GetChannel) {
      return new GetChannelOperation((GetChannel) data);
    } else if (data instanceof GetChannelMessage) {
      return new GetChannelMessageOperation((GetChannelMessage) data);
    } else if (data instanceof ListChannelMembers) {
      return new ListChannelMembersOperation((ListChannelMembers) data);
    } else if (data instanceof ListChannelMessages) {
      return new ListChannelMessagesOperation((ListChannelMessages) data);
    } else if (data instanceof ListChannels) {
      return new ListChannelsOperation((ListChannels) data);
    } else if (data instanceof ListMessageRepliesInChannel) {
      return new ListMessageRepliesInChannelOperation((ListMessageRepliesInChannel) data);
    } else if (data instanceof SendMessageToChannel) {
      return new SendMessageToChannelOperation((SendMessageToChannel) data);
    } else {
      throw new IllegalArgumentException(
          "Unknown MSTeamsRequestData type: " + data.getClass().getName());
    }
  }
}
