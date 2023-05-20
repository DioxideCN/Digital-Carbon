package org.sangonomiya.app.service;

import org.sangonomiya.app.core.ResponseBounce;
import org.sangonomiya.app.entity.UserVO;
import org.sangonomiya.app.extension.annotation.AzygoService;
import org.sangonomiya.app.extension.annotation.RequestConsistency;
import org.springframework.stereotype.Service;

/**
 * @author Dioxide.CN
 * @date 2023/3/21 14:03
 * @since 1.0
 */
@Service
public interface INotificationService {
    @RequestConsistency
    ResponseBounce<Object> getUserRecentNotification(String username, int page);

    @RequestConsistency
    @AzygoService(keyPos = {0})
    ResponseBounce<Object> getUserSimpleNotification(String username);

    @RequestConsistency
    ResponseBounce<Object> getNotificationDetail(String username, int messageId);

    @RequestConsistency
    ResponseBounce<Object> markMessageAsRead(String username, int messageId);

    @RequestConsistency
    ResponseBounce<Object> batchMarkMessageAsRead(String username, int[] awaitMessage);

    @RequestConsistency
    ResponseBounce<Object> deleteMessage(String username, int messageId);

    @RequestConsistency
    ResponseBounce<Object> batchDeleteMessage(String username, int[] awaitMessage);

    void sendMessageToClient(String message, UserVO sender, UserVO receiver);
}
