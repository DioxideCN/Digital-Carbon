package org.sangonomiya.app.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sangonomiya.app.entity.Product;

import java.util.Map;


/**
 * 产品Mapper在数据库中映射repo_product表并以Product对象的形式进行传输
 */
@Mapper
public interface ProductMapper {

    /**
     * 创建对应公司的产品表格，注意传入前需对公司名做限制
     *
     * @param companyId 企业名称
     */
    void createProductTableAction(String companyId);

    void dropProductTableAction(String companyId);

    int insertProduct(String companyId, Product product);

    IPage<Product> selectProductPage(IPage<Product> page, String companyId, @Param(Constants.WRAPPER) Wrapper<Product> queryWrapper);

    int deleteProductById(String companyId, int productId);

    Product selectProductById(String companyId, int productId);

    int updateById(String companyId, int productId, Product product);

    Map<Object, Object> checkTableExists(String companyId);

}
