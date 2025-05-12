package com.manetdroid.meshify2.api;

import com.manetdroid.meshify2.framework.exceptions.MessageException;

/**
 * This interface allows defining callback functions for the message listening process.
 */
public interface MessageListener {

    /**
     * Callback when a message is received successfully.
     *
     * @param message The Message object representing the received message.
     */
    void onMessageReceived(Message message);

    /**
     * Callback when a message fails to be received successfully.
     *
     * @param message   The Message object representing the failed message.
     * @param exception The MessageException object captured at runtime.
     */
    void onMessageFailed(Message message, MessageException exception);

    /**
     * Callback when a broadcast message is received successfully.
     *
     * @param message The Message object representing the received message.
     */
    void onBroadcastMessageReceived(Message message);

    /**
     * Callback when a message is successfully sent.
     *
     * @param messageId The identifier of the sent message.
     */
    void onMessageSent(String messageId);
}
