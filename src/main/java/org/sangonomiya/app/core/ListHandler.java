package org.sangonomiya.app.core;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Dioxide.CN
 * @date 2023/3/28 21:49
 * @since 1.0
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(List.class)
public class ListHandler implements TypeHandler<List<String>> {
    @Override
    public void setParameter(PreparedStatement preparedStatement, int i, List<String> strings, JdbcType jdbcType) throws SQLException {
        // 遍历List类型的入参，拼装为String类型，使用Statement对象插入数据库
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < strings.size(); j++) {
            if (j == strings.size() - 1) {
                sb.append(strings.get(j));
            } else {
                sb.append(strings.get(j)).append(",");
            }
        }
        preparedStatement.setString(i, sb.toString());
    }

    @Override
    public List<String> getResult(ResultSet resultSet, String s) throws SQLException {
        // 获取String类型的结果，使用","分割为List后返回
        String resultString = resultSet.getString(s);
        if (StringUtils.isNotEmpty(resultString)) {
            List<String> strings = Arrays.asList(resultString.split(","));
            return new ArrayList<>(strings);
        }
        return null;
    }

    @Override
    public List<String> getResult(ResultSet resultSet, int i) throws SQLException {
        // 获取String类型的结果，使用","分割为List后返回
        String resultString = resultSet.getString(i);
        if (StringUtils.isNotEmpty(resultString)) {
            List<String> strings = Arrays.asList(resultString.split(","));
            return new ArrayList<>(strings);
        }
        return null;
    }

    @Override
    public List<String> getResult(CallableStatement callableStatement, int i) throws SQLException {
        // 获取String类型的结果，使用","分割为List后返回
        String resultString = callableStatement.getString(i);
        if (StringUtils.isNotEmpty(resultString)) {
            List<String> strings = Arrays.asList(resultString.split(","));
            return new ArrayList<>(strings);
        }
        return null;
    }
}

