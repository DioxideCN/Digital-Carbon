package org.sangonomiya.app.core.message;

/**
 * @author Dioxide.CN
 * @date 2023/3/20 23:36
 * @since 1.0
 */
public enum MessageType {
    RICH_MESSAGE, // 富文本 0
    SIMPLE_MESSAGE, // 简单文本 1
    ACTIVE_MESSAGE, // 可操作文本 2
    INVITATION_MESSAGE; // 企业邀请类型 3
}
