package org.sangonomiya.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.sangonomiya.app.entity.Notification;

/**
 * @author Dioxide.CN
 * @date 2023/3/21 14:00
 * @since 1.0
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
