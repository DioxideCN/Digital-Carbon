package org.sangonomiya.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.sangonomiya.app.entity.UserVO;

/**
 * 公司Mapper在数据库中映射repo_company表并以Company对象的形式进行传输
 * @author Dioxide.CN
 * @date 2023/2/28 19:01
 * @since 1.0
 */
@Mapper
public interface UserMapper extends BaseMapper<UserVO> {
}
