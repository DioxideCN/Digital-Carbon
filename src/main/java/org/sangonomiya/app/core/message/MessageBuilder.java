package org.sangonomiya.app.core.message;

import com.alibaba.fastjson2.JSON;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sangonomiya.app.core.lazy.LazyOptional;
import org.sangonomiya.app.extension.exception.IllegalCompositeTypeException;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * {@link MessageBuilder} 采用了WebFlux的流式设计模式
 * <p>
 * 并依托 {@link LazyOptional} 构建了一个完整的MessageBuilder流
 * <p>
 * 经过MessageBuilder链式构建的消息可以以String或Json的形式返回，这些消息
 * 通常用在WebSocket的消息传输过程中，同样也是前端解析该类消息的一种规范
 *
 * @author Dioxide.CN
 * @date 2023/3/20 23:22
 * @since 1.0
 */
@Getter
@SuppressWarnings("all")
public class MessageBuilder implements Serializable {

    // 消息结构列表
    private String title; // 消息头
    private MessageType type; // 消息类型
    private String body; // 消息体
    private MessageAction[] actions; // 操作类型
    private String sender; // 发送人名称
    private int duration = 0; // 有效时间
    private TimeUnit timeUnit; // 有效时间单位
    private String secret; // 附带的消息
    private String key; // 关键识别位

    public MessageBuilder key(@NotNull String key) {
        this.key = key;
        return this;
    }

    public MessageBuilder title(@NotNull String title) {
        this.title = title;
        return this;
    }

    public MessageBuilder type(@NotNull MessageType type) {
        this.type = type;
        return this;
    }

    public MessageBuilder body(@NotNull String body) {
        this.body = body;
        return this;
    }

    public MessageBuilder action(@NotNull MessageAction ...actions) {
        this.actions = actions;
        return this;
    }

    public MessageBuilder duration(int duration, @NotNull TimeUnit timeUnit) {
        this.duration = duration;
        this.timeUnit = timeUnit;
        return this;
    }

    public MessageBuilder secret(String secret) {
        this.secret = secret;
        return this;
    }

    /**
     * 以String的形式返回MessageBuilder的构造结果
     * @return 返回构造结果
     */
    @Nullable
    public String build() {
        try {
            checkActions();
            checkDuration();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return JSON.toJSONString(this);
    }

    private void checkActions() throws IllegalCompositeTypeException {
        if (this.actions.length == 0)
            this.actions = new MessageAction[]{MessageAction.NONE_ACTION};

        // 不允许多个Actions中出现NONE操作，互斥
        if (this.actions.length > 1) {
            for (MessageAction action : this.actions) {
                if (action.equals(MessageAction.NONE_ACTION))
                    throw new IllegalCompositeTypeException();
            }
        }
    }

    private void checkDuration() {
        if (this.actions.length == 0) return;
        if (this.actions.length == 1 && this.actions[0].equals(MessageAction.NONE_ACTION)) return;

        if (this.duration == 0)
            throw new IllegalArgumentException();
    }

}
