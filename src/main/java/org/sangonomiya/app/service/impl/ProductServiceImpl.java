package org.sangonomiya.app.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.sangonomiya.app.core.Response;
import org.sangonomiya.app.core.ResponseBounce;
import org.sangonomiya.app.entity.LifeCycle;
import org.sangonomiya.app.entity.Product;
import org.sangonomiya.app.entity.UserVO;
import org.sangonomiya.app.extension.RedisAction;
import org.sangonomiya.app.extension.annotation.AzygoService;
import org.sangonomiya.app.extension.annotation.RequestConsistency;
import org.sangonomiya.app.extension.annotation.ShitMountain;
import org.sangonomiya.app.extension.component.GeoUnit;
import org.sangonomiya.app.extension.component.WordUnit;
import org.sangonomiya.app.mapper.LifeCycleMapper;
import org.sangonomiya.app.mapper.ProductMapper;
import org.sangonomiya.app.service.ICompanyService;
import org.sangonomiya.app.service.IProductService;
import org.sangonomiya.app.service.IUserService;
import org.sangonomiya.groovy.DataHandler;
import org.sangonomiya.kotlin.param.ProductCreateParam;
import org.sangonomiya.kotlin.param.ProductUpdateParam;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ProductServiceImpl implements IProductService {

    @Resource
    private ProductMapper productMapper;

    @Resource
    private IUserService userService;

    @Resource
    private ICompanyService companyService;

    @Resource
    private LifeCycleMapper lifeCycleMapper;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private GeoUnit geoUnit;

    @Resource
    private WordUnit wordUnit;

    /**
     * 创建公司产品表
     *
     * @param companyId 公司的名称(id)
     * @return 返回提交结果封装体
     */
    @Override
    public ResponseBounce<Object> createProductTable(String companyId) {
        if (checkTableExists(companyId)) return Response.fail("该公司的产品表已经创建！");
        productMapper.createProductTableAction(companyId);
        lifeCycleMapper.createLifeCycleTableAction(companyId);
        return Response.success();
    }

    /**
     * 删除公司产品表
     *
     * @param companyId 公司的名称(id)
     * @return 返回提交结果封装体
     */
    @Override
    public ResponseBounce<Object> dropProductTable(String companyId) {
        if (!checkTableExists(companyId)) return Response.fail("该公司的产品表不存在！");
        productMapper.dropProductTableAction(companyId);
        lifeCycleMapper.dropLifeCycleTableAction(companyId);
        return Response.success();
    }

    /**
     * 新增产品
     *
     * @param username 提交请求的用户的用户名
     * @param param    产品参数对象
     * @return 返回提交结果封装体
     */
    @Override
    @RequestConsistency
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseBounce<Object> createProduct(String username, String CompanyId, ProductCreateParam param) {

        UserVO user = userService.getUserByUsername(username);

        if (!companyService.hasInCompany(user)) return Response.fail("您不属于该公司，无法创建产品");  // 判断公司归属
        if (!checkTableExists(CompanyId)) return Response.fail("产品表格不存在，请先创建");  // 判断表格

        // 插入产品数据
        Product product = new Product();
        product.setName(param.getProduct_name());
        product.setAmount(param.getProduct_amount());
        product.setModel(param.getProduct_model());
        product.setPic(param.getProduct_pic());
        product.setType(param.getProduct_type());
        product.setUnit(param.getProduct_unit());
        product.setWeight(param.getProduct_weight());
        product.setStatisticsPeriodAfter(param.getStatistics_period_after());
        product.setStatisticsPeriodBefore(param.getStatistics_period_before());
        product.setLifeCycleType(param.getProduct_lifecycle());
        product.setTag("");
        LifeCycle lifeCycle = new LifeCycle();
        lifeCycle.setProductionStage(JSONObject.toJSONString(param.getProduction_stage()));
        lifeCycle.setPackingStage(JSONObject.toJSONString(param.getPacking_stage()));
        lifeCycle.setDisuseStage(JSONObject.toJSONString(param.getDisuse_stage()));
        lifeCycle.setUseStage(JSONObject.toJSONString(param.getUse_stage()));
        lifeCycle.setSaleStage(JSONObject.toJSONString(param.getSale_stage()));
        lifeCycle.setRawMaterialAcquisitionStage(JSONObject.toJSONString(
                param.getRaw_material_acquisition_stage()));

        productMapper.insertProduct(String.valueOf(CompanyId), product);
        lifeCycle.setId(product.getId());  // 获取当前产品的Id用于作为lifeCycle的Id
        lifeCycleMapper.insertLifeCycle(String.valueOf(CompanyId), lifeCycle);
        Map<String, Object> result = new HashMap<>();
        result.put("product_id", product.getId());
        return Response.success(result);
    }

    /**
     * 删除产品
     *
     * @param username  提交请求的用户的用户名
     * @param companyId 公司的名称(id)
     * @param productId 产品的id
     * @return 返回提交结果封装体
     */
    @Override
    @RequestConsistency
    public ResponseBounce<Object> deleteProduct(String username, String companyId, Integer productId) {
        UserVO user = userService.getUserByUsername(username);
        if (!companyService.hasInCompany(user)) return Response.fail("您不属于该公司，无法删除产品");  // 判断公司归属
        if (!checkTableExists(companyId)) return Response.fail("产品表格不存在，请先创建");  // 判断表格
        if (productMapper.selectProductById(companyId, productId) == null)      // 判断产品是否存在
            return Response.fail("产品不存在");
        if (lifeCycleMapper.selectLifeCycleById(companyId, productId) == null)
            return Response.fail("产品生命周期不存在");

        productMapper.deleteProductById(companyId, productId);
        lifeCycleMapper.deleteLifeCycleById(companyId, productId);

        return Response.success();
    }


    /**
     * 更新产品
     *
     * @param username 提交请求的用户的用户名
     * @param param    产品参数对象
     * @return 返回提交结果封装体
     */
    @ShitMountain
    @Override
    @RequestConsistency
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @AzygoService(keyPos = {0, 1})
    public ResponseBounce<Object> updateProduct(String username, String companyId, ProductUpdateParam param) {
        UserVO user = userService.getUserByUsername(username);

        if (!companyService.hasInCompany(user)) return Response.fail("您不属于该公司，无法更新产品信息");  // 判断公司归属
        if (!checkTableExists(companyId)) return Response.fail("产品表格不存在，请先创建");  // 判断表格
        // 判断存在性
        Product product = productMapper.selectProductById(companyId, param.getId());
        if (product == null)   // 判断产品是否存在
            return Response.fail("产品不存在");
        LifeCycle lifeCycle = lifeCycleMapper.selectLifeCycleById(companyId, param.getId());
        if (lifeCycle == null)
            return Response.fail("产品生命周期不存在");

        // 更新产品数据
        if (DataHandler.isAvailable(param.getProduct_name())) product.setName(param.getProduct_name());
        if (param.getProduct_amount() != null) product.setAmount(param.getProduct_amount());
        if (DataHandler.isAvailable(param.getProduct_model())) product.setModel(param.getProduct_model());
        if (DataHandler.isAvailable(param.getProduct_pic())) product.setPic(param.getProduct_pic());
        if (DataHandler.isAvailable(param.getProduct_type())) product.setType(param.getProduct_type());
        if (DataHandler.isAvailable(param.getProduct_unit())) product.setUnit(param.getProduct_unit());
        if (param.getProduct_weight() != null) product.setWeight(param.getProduct_weight());
        if (param.getStatistics_period_after() != null)
            product.setStatisticsPeriodAfter(param.getStatistics_period_after());
        if (param.getStatistics_period_before() != null)
            product.setStatisticsPeriodBefore(param.getStatistics_period_before());
        if (param.getProduct_lifecycle() != null) product.setLifeCycleType(param.getProduct_lifecycle());

        // LifeCycle部分
        if (param.getProduction_stage() != null)
            lifeCycle.setProductionStage(JSONObject.toJSONString(param.getProduction_stage()));
        if (param.getPacking_stage() != null)
            lifeCycle.setPackingStage(JSONObject.toJSONString(param.getPacking_stage()));
        if (param.getDisuse_stage() != null)
            lifeCycle.setDisuseStage(JSONObject.toJSONString(param.getDisuse_stage()));
        if (param.getUse_stage() != null)
            lifeCycle.setUseStage(JSONObject.toJSONString(param.getUse_stage()));
        if (param.getSale_stage() != null)
            lifeCycle.setSaleStage(JSONObject.toJSONString(param.getSale_stage()));
        if (param.getRaw_material_acquisition_stage() != null)
            lifeCycle.setRawMaterialAcquisitionStage(JSONObject.toJSONString(
                    param.getRaw_material_acquisition_stage()));

        productMapper.updateById(companyId, param.getId(), product);
        lifeCycleMapper.updateById(companyId, param.getId(), lifeCycle);
        return Response.success();
    }

    /**
     * 获取产品所有信息
     *
     * @param username  提交请求的用户的用户名
     * @param companyId 公司的名称(id)
     * @param productId 产品的id
     * @return 返回提交结果封装体
     */
    @Override
    @RequestConsistency
    public ResponseBounce<Object> getAllById(String username, String companyId, Integer productId) {
        UserVO user = userService.getUserByUsername(username);
        if (!companyService.hasInCompany(user)) return Response.fail("您不属于该公司，无法获取产品信息");  // 判断公司归属
        if (!checkTableExists(companyId)) return Response.fail("产品表格不存在，请先创建");  // 判断表格
        Product product = productMapper.selectProductById(companyId, productId);
        if (product == null) return Response.fail("产品不存在");
        LifeCycle lifeCycle = lifeCycleMapper.selectLifeCycleById(companyId, productId);
        if (lifeCycle == null) return Response.fail("产品对应的LifeCycle不存在");
        JSONObject json = mergeProduct(product, lifeCycle);
        if (json == null) return Response.fail("产品建模数据有问题，请更新正确的或删除");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        json.put("statisticsPeriodBefore", sdf.format(product.getStatisticsPeriodBefore())); // 日期格式化
        json.put("statisticsPeriodAfter", sdf.format(product.getStatisticsPeriodAfter()));
        return Response.success(json);
    }

    /**
     * 获取产品信息
     *
     * @param username  提交请求的用户的用户名
     * @param companyId 公司的名称(id)
     * @param productId 产品的id
     * @return 返回提交结果封装体
     */
    @Override
    @RequestConsistency
    public ResponseBounce<Object> getProductById(String username, String companyId, Integer productId) {
        UserVO user = userService.getUserByUsername(username);

        if (!companyService.hasInCompany(user)) return Response.fail("您不属于该公司，无法获取产品信息");  // 判断公司归属
        if (!checkTableExists(companyId)) return Response.fail("产品表格不存在，请先创建");  // 判断表格

        Product product = productMapper.selectProductById(companyId, productId);
        if (product == null) return Response.fail("产品不存在");

        LifeCycle lifeCycle = lifeCycleMapper.selectLifeCycleById(companyId, productId);
        if (lifeCycle == null) return Response.fail("产品对应的LifeCycle不存在");

        JSONObject json = (JSONObject) JSON.toJSON(product);
        if (json == null) return Response.fail("产品建模数据有问题，请更新正确的或删除");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        json.put("statisticsPeriodBefore", sdf.format(product.getStatisticsPeriodBefore())); // 日期格式化
        json.put("statisticsPeriodAfter", sdf.format(product.getStatisticsPeriodAfter()));
        return Response.success(json);
    }

    /**
     * 通过产品ID获取产品对应的生命周期
     *
     * @param username  请求用户的用户名
     * @param companyId 该产品所属于的企业ID
     * @param productId 所请求的产品ID
     * @return 返回提交结果封装体
     */
    @Override
    @RequestConsistency
    public ResponseBounce<Object> getLifeCycleById(String username, String companyId, Integer productId) {
        UserVO user = userService.getUserByUsername(username);
        if (!companyService.hasInCompany(user)) return Response.fail("您不属于该公司，无法获取产品信息");  // 判断公司归属
        if (!checkTableExists(companyId)) return Response.fail("产品表格不存在，请先创建");  // 判断表格
        LifeCycle lifeCycle = lifeCycleMapper.selectLifeCycleById(companyId, productId);
        if (lifeCycle == null) return Response.fail("产品对应的LifeCycle不存在");
        JSONObject json = new JSONObject();
        putLifeCycle(lifeCycle, json);
        json.put("lifeCycleType", productMapper.selectProductById(companyId, productId).getLifeCycleType());
        return Response.success(json);
    }

    /**
     * 获取产品信息列表
     *
     * @param username  请求用户的用户名
     * @param companyId 请求用户所属的公司
     * @param pageNum   请求的列表页数
     * @param pageSize  请求的页面大小
     * @param key       筛选所用的关键字，null则为不筛选
     * @return 返回提交结果封装体
     */

    @Override
    @RequestConsistency
    public ResponseBounce<Object> getProductList(String username, String companyId, int pageNum, int pageSize, String key) {
        UserVO user = userService.getUserByUsername(username);

        if (!companyService.hasInCompany(user)) return Response.fail("您不属于该公司，无法创建产品");  // 判断公司归属
        if (!checkTableExists(companyId)) return Response.fail("产品表格不存在，请先创建");  // 判断表格

        if (pageSize > 100) pageSize = 100;
        if (pageSize < 0) return Response.fail("pageSize不能小于0");

        //利用分页传输数据
        Page<Product> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Product> qw = new QueryWrapper<>();
        if (key != null) qw.like("name", key);
        IPage<Product> mapIPage = productMapper.selectProductPage(page, companyId, qw);
        Map<String, Object> map = new HashMap<>();
        map.put("currentPage", pageNum);
        map.put("pageSize", pageSize);
        map.put("total", mapIPage.getTotal());
        map.put("totalPage", mapIPage.getPages());
        map.put("records", mapIPage.getRecords());
        return Response.success(map);
    }

    /**
     * 获取运输路径数据
     *
     * @param username  请求用户的用户名
     * @param companyId 请求用户所属的公司
     * @param productId 所请求的产品Id
     * @return 返回提交结果的封装体
     */
    @Override
    @RequestConsistency
    public ResponseBounce<Object> getGeoPath(String username, String companyId, Integer productId) {
        UserVO user = userService.getUserByUsername(username);

        if (!companyService.hasInCompany(user)) return Response.fail("您不属于该公司，无法创建产品");  // 判断公司归属
        if (!checkTableExists(companyId)) return Response.fail("产品表格不存在，请先创建");  // 判断表格

        LifeCycle lifeCycle = lifeCycleMapper.selectLifeCycleById(companyId, productId);
        if (lifeCycle == null) return Response.fail("产品对应的LifeCycle不存在");

        String[] jsons = {lifeCycle.getRawMaterialAcquisitionStage(),
                lifeCycle.getProductionStage(),
                lifeCycle.getPackingStage(),
                lifeCycle.getSaleStage(),
                lifeCycle.getUseStage(),
                lifeCycle.getDisuseStage()};
        ArrayList<Map<String,String>> pathArray = new ArrayList<>();
        for (String str:jsons) {
            JSONObject json = JSONObject.parse(str);
            JSONArray processes = json.getJSONArray("processes");
            for (Object process :processes) {
                JSONObject jsonProcess = (JSONObject) process;
                JSONArray inputs = jsonProcess.getJSONArray("input");
                JSONArray outputs = jsonProcess.getJSONArray("output");
                if (getGeoByNodeJson(pathArray, inputs)) return Response.fail("高德接口错误或Redis错误");
                if (getGeoByNodeJson(pathArray, outputs)) return Response.fail("高德接口错误或Redis错误");
            }
        }
        Map<String,Object> resMap = new HashMap<>();
        resMap.put("paths",pathArray);
        resMap.put("count",pathArray.size());
        return Response.success(resMap);
    }

    /**
     * 获取所有运输路径数据
     *
     * @param username  请求用户的用户名
     * @param companyId 请求用户所属的公司
     * @return 返回提交结果的封装体
     */
    @Override
    @RequestConsistency
    public ResponseBounce<Object> getAllGeoPath(String username, String companyId) {
        UserVO user = userService.getUserByUsername(username);

        if (!companyService.hasInCompany(user)) return Response.fail("您不属于该公司，无法创建产品");  // 判断公司归属
        if (!checkTableExists(companyId)) return Response.fail("产品表格不存在，请先创建");  // 判断表格

        String res = redisTemplate.opsForValue().get(RedisAction.geoRes(companyId));
        if (res != null) {
            return Response.success(JSONObject.parseObject(res));
        }
        Page<Product> page = new Page<>(1,-1);
        QueryWrapper<Product> qw = new QueryWrapper<>();
        IPage<Product> mapIPage = productMapper.selectProductPage(page, companyId, qw);
        List<Product> list = mapIPage.getRecords();
        ArrayList<Map<String,String>> pathArray = new ArrayList<>();
        for (Product product : list) {
            LifeCycle lifeCycle = lifeCycleMapper.selectLifeCycleById(companyId, product.getId());
            if (lifeCycle == null) return Response.fail("产品对应的LifeCycle不存在");

            String[] jsons = {lifeCycle.getRawMaterialAcquisitionStage(),
                    lifeCycle.getProductionStage(),
                    lifeCycle.getPackingStage(),
                    lifeCycle.getSaleStage(),
                    lifeCycle.getUseStage(),
                    lifeCycle.getDisuseStage()};
            for (String str:jsons) {
                JSONObject json = JSONObject.parse(str);
                JSONArray processes = json.getJSONArray("processes");
                for (Object process :processes) {
                    JSONObject jsonProcess = (JSONObject) process;
                    JSONArray inputs = jsonProcess.getJSONArray("input");
                    JSONArray outputs = jsonProcess.getJSONArray("output");
                    if (getGeoByNodeJson(pathArray, inputs)) return Response.fail("高德接口错误或Redis错误");
                    if (getGeoByNodeJson(pathArray, outputs)) return Response.fail("高德接口错误或Redis错误");
                }
            }
        }
        Map<String,Object> resMap = new HashMap<>();
        resMap.put("paths",pathArray);
        resMap.put("count",pathArray.size());
        Boolean var = redisTemplate.opsForValue().setIfAbsent(
                RedisAction.geoRes(companyId),JSONObject.toJSONString(resMap),15,TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(var)) return Response.fail("Redis错误");
        return Response.success(resMap);
    }

    /**
     * 下载报告
     * @param username 请求用户名
     * @param companyId 公司id
     * @param productId 产品id
     * @param response HttpServletResponse对象
     * @return 返回提交结果的封装体
     */
    @Override
    @RequestConsistency
    public ResponseBounce<Object> downloadReport(String username, String companyId, Integer productId, HttpServletResponse response) {
        UserVO user = userService.getUserByUsername(username);

        if (!companyService.hasInCompany(user)) return Response.fail("您不属于该公司，无法创建产品");  // 判断公司归属
        if (!checkTableExists(companyId)) return Response.fail("产品表格不存在，请先创建");  // 判断表格

        Product product = productMapper.selectProductById(companyId, productId);
        if (product == null) return Response.fail("产品不存在");

        response.reset();
        response.setContentType("application/octet-stream;charset=gb2312");
        response.setCharacterEncoding("gb2312");
        try{
            response.addHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(product.getName()+"报告.docx", "gb2312"));
            wordUnit.setData(username,companyId,productId,response.getOutputStream());
            return Response.success("生成成功，即将开始下载");
        }catch (IOException e){
            return Response.success("未知错误:"+e.getMessage());
        }
    }



    /**
     * 检查表格是否存在
     *
     * @param companyName 公司的名称(id)
     * @return 返回表格是否存在
     */
    private boolean checkTableExists(String companyName) {
        Map<Object, Object> res = productMapper.checkTableExists(companyName);
        Map<Object, Object> res2 = lifeCycleMapper.checkTableExists(companyName);
        return res != null && res.size() == 1 && res2 != null && res2.size() == 1;
    }

    /**
     * 合并product和lifeCycle对象，并返回一个合并后的JSONObject
     *
     * @param product   需要合并的产品对象
     * @param lifeCycle 需要合并的lifeCycle对象
     * @return 返回一个合并后的JSONObject对象，当json数据出现异常时返回null
     */
    private static JSONObject mergeProduct(Product product, LifeCycle lifeCycle) {
        JSONObject json = (JSONObject) JSON.toJSON(product);
        try {
            putLifeCycle(lifeCycle, json);
        } catch (JSONException err) {
            return null;
        }
        return json;
    }

    /**
     * 将生命周期转换为json
     *
     * @param lifeCycle 要转换的LifeCycle对象
     * @param json      目标json对象
     */
    private static void putLifeCycle(LifeCycle lifeCycle, JSONObject json) {
        json.put("raw_material_acquisition_stage", JSONObject.parseObject(lifeCycle.getRawMaterialAcquisitionStage()));
        json.put("production_stage", JSONObject.parseObject(lifeCycle.getProductionStage()));
        json.put("packing_stage", JSONObject.parseObject(lifeCycle.getPackingStage()));
        json.put("sale_stage", JSONObject.parseObject(lifeCycle.getSaleStage()));
        json.put("use_stage", JSONObject.parseObject(lifeCycle.getUseStage()));
        json.put("disuse_stage", JSONObject.parseObject(lifeCycle.getDisuseStage()));
    }

    /**
     * 获取地理数据
     * @param pathArray Array
     * @param inputs JSONArray
     * @return 是否成功
     */
    private boolean getGeoByNodeJson(ArrayList<Map<String, String>> pathArray, JSONArray inputs) {
        for (Object input: inputs) {
            JSONObject jsonInput = (JSONObject) input;
            JSONArray paths = jsonInput.getJSONArray("path");
            for (Object path:paths) {
                JSONObject jsonPath = (JSONObject) path;
                String origin = jsonPath.getString("origin");
                String destination = jsonPath.getString("destination");
                String geoOrigin;
                String geoDestination;
                if (redisTemplate.opsForValue().get(RedisAction.geo(origin)) != null) {
                    if("NULL".equals((redisTemplate.opsForValue().get(RedisAction.geo(origin))))) continue;
                    geoOrigin = redisTemplate.opsForValue().get(RedisAction.geo(origin));
                }else{
                    JSONObject geoData;
                    try{
                        geoData = JSONObject.parseObject(geoUnit.getGeoCode(origin));
                    }catch(Exception e){
                        e.printStackTrace();
                        return false;
                    }
                    if(geoData.getIntValue("count") == 0){//匹配失败，则放弃匹配
                        Boolean var2 = redisTemplate.opsForValue()
                                .setIfAbsent(RedisAction.geo(origin), "NULL", 6, TimeUnit.HOURS);
                        if (Boolean.FALSE.equals(var2)) {
                            return true;
                        }
                        continue;
                    }
                    JSONObject geocode = (JSONObject)(geoData.getJSONArray("geocodes").get(0));
                    geoOrigin = geocode.getString("location");
                    Boolean var1 = redisTemplate.opsForValue()
                            .setIfAbsent(RedisAction.geo(origin), geoOrigin, 6, TimeUnit.HOURS);

                    if (Boolean.FALSE.equals(var1)) {
                        return true;
                    }
                }
                if (redisTemplate.opsForValue().get(RedisAction.geo(destination)) != null) {
                    if("NULL".equals((redisTemplate.opsForValue().get(RedisAction.geo(destination))))) continue;
                    geoDestination = redisTemplate.opsForValue().get(RedisAction.geo(destination));
                }else{
                    JSONObject geoData;
                    try{
                        geoData = JSONObject.parseObject(geoUnit.getGeoCode(destination));
                    }catch(Exception e){
                        e.printStackTrace();
                        return false;
                    }
                    if(geoData.getIntValue("count") == 0){//匹配失败，则放弃匹配
                        continue;
                    }
                    if(geoData.getIntValue("count") == 0){//匹配失败，则放弃匹配
                        Boolean var2 = redisTemplate.opsForValue()
                                .setIfAbsent(RedisAction.geo(destination), "NULL", 6, TimeUnit.HOURS);
                        if (Boolean.FALSE.equals(var2)) {
                            return true;
                        }
                        continue;
                    }
                    JSONObject geocode = (JSONObject)(geoData.getJSONArray("geocodes").get(0));
                    geoDestination = geocode.getString("location");
                    Boolean var1 = redisTemplate.opsForValue()
                            .setIfAbsent(RedisAction.geo(destination), geoDestination, 6, TimeUnit.HOURS);

                    if (Boolean.FALSE.equals(var1)) {
                        return true;
                    }
                }
                Map<String,String> line = new HashMap<>();
                line.put("origin",origin);
                line.put("destination",destination);
                line.put("geoOrigin",geoOrigin);
                line.put("geoDestination",geoDestination);
                pathArray.add(line);
            }
        }
        return false;
    }

}
