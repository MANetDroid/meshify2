package com.manetdroid.meshify2.framework.controllers.forwardManager;

import android.os.Handler;
import android.os.Looper;

import com.manetdroid.meshify2.api.Config;
import com.manetdroid.meshify2.api.Meshify;
import com.manetdroid.meshify2.api.Message;
import com.manetdroid.meshify2.framework.exceptions.MessageException;

/**
 * The MessageNotifier class is responsible for notifying the MessageListener on the main thread
 * when messages are received or when message delivery fails.
 */
class MessageNotifier {

    /**
     * Constructs a new MessageNotifier with the provided configuration.
     *
     * @param config The configuration to use for message notification.
     */
    MessageNotifier(Config config) {
        // Initialization logic, if needed.
    }

    /**
     * Notifies the MessageListener on the main thread when a direct message is received.
     *
     * @param message The received direct message.
     */
    void onMessageReceived(Message message) {
        new Handler(Looper.getMainLooper()).post(() -> Meshify.getInstance().getMeshifyCore().getMessageListener().onMessageReceived(message));
    }

    /**
     * Notifies the MessageListener on the main thread when a broadcast message is received.
     *
     * @param message The received broadcast message.
     */
    void onBroadcastMessageReceived(Message message) {
        new Handler(Looper.getMainLooper()).post(() -> Meshify.getInstance().getMeshifyCore().getMessageListener().onBroadcastMessageReceived(message));
    }

    /**
     * Notifies the MessageListener on the main thread when a message delivery fails.
     *
     * @param message          The original message that failed to be delivered.
     * @param messageException The exception indicating the cause of the delivery failure.
     */
    void onMessageFailed(Message message, MessageException messageException) {
        new Handler(Looper.getMainLooper()).post(() -> Meshify.getInstance().getMeshifyCore().getMessageListener().onMessageFailed(message, messageException));
    }
}
