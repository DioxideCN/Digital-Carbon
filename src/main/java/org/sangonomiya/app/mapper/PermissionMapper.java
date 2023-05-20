package org.sangonomiya.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.sangonomiya.app.entity.Permission;

/**
 * 权限组Mapper在数据库中映射repo_permission_group表并以Permission对象的形式进行传输
 * @author Dioxide.CN
 * @date 2023/3/5 13:14
 * @since 1.0
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}
