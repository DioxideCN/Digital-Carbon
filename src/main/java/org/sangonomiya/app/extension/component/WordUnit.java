package org.sangonomiya.app.extension.component;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.config.ConfigureBuilder;
import com.deepoove.poi.data.Charts;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.sangonomiya.app.entity.Company;
import org.sangonomiya.app.entity.LifeCycle;
import org.sangonomiya.app.entity.Product;
import org.sangonomiya.app.entity.WordData;
import org.sangonomiya.app.mapper.CompanyMapper;
import org.sangonomiya.app.mapper.LifeCycleMapper;
import org.sangonomiya.app.mapper.ProductMapper;
import org.sangonomiya.app.mapper.RelateMapper;
import org.sangonomiya.kotlin.service.IPythonCallerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class WordUnit {

    @Resource
    private ProductMapper productMapper;

    @Resource
    private CompanyMapper companyMapper;

    @Resource
    private RelateMapper relateMapper;

    @Resource
    private LifeCycleMapper lifeCycleMapper;

    private final OkHttpClient okHttpClient = new OkHttpClient();

    @Value("${dataAnalysisServer.serverUrl}")
    private String serverUrl;

    @Value("${dataAnalysisServer.secretKey}")
    private String secretKey;

    public void createWord(WordData data , OutputStream stream) throws IOException {
        InputStream resourceAsStream = this.getClass().getResourceAsStream("/word/report.docx");
        if(resourceAsStream == null) return;
        ConfigureBuilder builder = Configure.builder();
        LoopRowTableRenderPolicy policy = new LoopRowTableRenderPolicy();
        builder.bind("processData",policy);
        builder.bind("inputs",policy);
        builder.bind("outputs",policy);
        builder.useSpringEL();
        XWPFTemplate template = XWPFTemplate.compile(resourceAsStream,builder.build()).render(data);
        template.writeAndClose(stream);
    }

    /**
     * 获取报告相关数据
     * @param username 用户名
     * @param companyId 公司id
     * @param productId 产品id
     * @throws IOException 可能抛出IO异常
     */
    public void setData(String username, String companyId, Integer productId, OutputStream stream) throws IOException {
        Product p = productMapper.selectProductById(companyId,productId);
        Company com = companyMapper.selectById(companyId);
        String requestUrl = serverUrl + "product-emission/";
        Request request= new Request.Builder()
                .url(requestUrl)
                .post(new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("companyId", companyId)
                        .addFormDataPart("productId", productId.toString())
                        .build())
                .header("secret-key", secretKey)
                .build();
        String response = Objects.requireNonNull(okHttpClient.newCall(request).execute().body()).string();
        WordData.EmissionData productEmission = JSONObject.parseObject(response).to(WordData.EmissionData.class);
        LifeCycle lifeCycle = lifeCycleMapper.selectLifeCycleById(companyId,productId);
        ArrayList<WordData.ProcessData> processData = new ArrayList<>();
        String[] stages = {lifeCycle.getRawMaterialAcquisitionStage(),
                            lifeCycle.getProductionStage(),
                            lifeCycle.getPackingStage(),
                            lifeCycle.getSaleStage(),
                            lifeCycle.getUseStage(),
                            lifeCycle.getDisuseStage()};
        String[] stages_name ={"原料获取","生产","包装","分销","使用","废弃"};
        for (int i = 0; i < 6; i++) {
            JSONObject jsonObject = JSONObject.parseObject(stages[i]);
            JSONArray array = jsonObject.getJSONArray("processes");
            for (Object object:array) {
                JSONObject object1 = (JSONObject) object;
                WordData.ProcessData processData1 = new WordData.ProcessData();
                processData1.setName(object1.getString("name"));
                processData1.setProduct(object1.getJSONObject("outputProduct").getString("name"));
                processData1.setStage(stages_name[i]);
                processData.add(processData1);
                JSONArray array1 = object1.getJSONArray("input");
                List<WordData.NodeData> input = getNodeInfo(array1);
                processData1.setInputs(input);
                array1 = object1.getJSONArray("output");
                List<WordData.NodeData> output = getNodeInfo(array1);
                processData1.setOutputs(output);
            }
        }

        WordData.ChartsData chartsData = new WordData.ChartsData();

        List<Map<String,String>> sort = productEmission.getSortByCategory();
        List<String> title = new ArrayList<>();
        List<Double> series = new ArrayList<>();
        for (Map<String,String> data:sort) {
            title.add(data.get("name"));
            series.add(Double.valueOf(data.get("value")));
        }
        String[] titleArray = title.toArray(new String[0]);
        Double[] seriesArray = series.toArray(new Double[0]);
        chartsData.setSortByCategory(Charts.ofSingleSeries("按类型的碳足迹构成分析",titleArray)
                .series("排放量",seriesArray).create());

        sort = productEmission.getSortByName();
        title = new ArrayList<>();
        series = new ArrayList<>();
        for (Map<String,String> data:sort) {
            title.add(data.get("name"));
            series.add(Double.valueOf(data.get("value")));
        }
        titleArray = title.toArray(new String[0]);
        seriesArray = series.toArray(new Double[0]);
        chartsData.setSortByName(Charts.ofSingleSeries("按名称的碳足迹构成分析",titleArray)
                .series("排放量",seriesArray).create());

        sort = productEmission.getSortByProcess();
        title = new ArrayList<>();
        series = new ArrayList<>();
        for (Map<String,String> data:sort) {
            title.add(data.get("name"));
            series.add(Double.valueOf(data.get("value")));
        }
        titleArray = title.toArray(new String[0]);
        seriesArray = series.toArray(new Double[0]);
        chartsData.setSortByProcess(Charts.ofSingleSeries("按工序的碳足迹构成分析",titleArray)
                .series("排放量",seriesArray).create());

        sort = productEmission.getSortByStage();
        title = new ArrayList<>();
        series = new ArrayList<>();
        for (Map<String,String> data:sort) {
            title.add(data.get("name"));
            series.add(Double.valueOf(data.get("value")));
        }
        titleArray = title.toArray(new String[0]);
        seriesArray = series.toArray(new Double[0]);
        chartsData.setSortByStage(Charts.ofSingleSeries("按周期的碳足迹构成分析",titleArray)
                .series("排放量",seriesArray).create());

        WordData data = new WordData();
        data.setProduct(p);
        data.setCompany(com);
        data.setEmissionData(productEmission);
        data.setOwner(relateMapper.selectAllOperatorFromCompany(Integer.parseInt(companyId)).get(0));
        data.setProcessData(processData);
        data.setChartsData(chartsData);
        createWord(data,stream);
    }

    /**
     * 获取输入输出信息
     * @param array1 输入输出数组
     * @return 格式化后的List
     */
    private List<WordData.NodeData> getNodeInfo(JSONArray array1) {
        List<WordData.NodeData> output = new ArrayList<>();
        array1.forEach((Object object2)->{
            JSONObject object3 = (JSONObject) object2;
            WordData.NodeData nodeData = new WordData.NodeData();
            nodeData.setName(object3.getString("name"));
            nodeData.setUnit(object3.getJSONArray("unit").getString(1));
            nodeData.setAmount(object3.getIntValue("amount"));
            nodeData.setWeight(object3.getDoubleValue("weight"));
            nodeData.setCategory(object3.getString("category"));
            nodeData.setImpactFactor(object3.getString("impact_factor"));
            output.add(nodeData);
        });
        return output;
    }


}
