package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.pojo.SkuInfo;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.service.SearchResultMapperImpl;
import com.changgou.search.service.SkuService;
import entity.Result;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 描述
 *
 *
 * @version 1.0
 * @package com.changgou.search.service.impl *
 * @since 1.0
 */
@Service
public class SkuServiceImpl implements SkuService {


    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SkuEsMapper skuEsMapper;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;


    @Override
    public void importEs() {
        //1.调用 goods微服务的fegin 查询 符合条件的sku的数据
        Result<List<Sku>> skuResult = skuFeign.findByStatus("1");
        List<Sku> data = skuResult.getData();//sku的列表
        //将sku的列表 转换成es中的skuinfo的列表
        List<SkuInfo> skuInfos = JSON.parseArray(JSON.toJSONString(data), SkuInfo.class);
        for (SkuInfo skuInfo : skuInfos) {
            //获取规格的数据  {"电视音响效果":"立体声","电视屏幕尺寸":"20英寸","尺码":"165"}

            //转成MAP  key: 规格的名称  value:规格的选项的值
            Map<String, Object> map = JSON.parseObject(skuInfo.getSpec(), Map.class);
            skuInfo.setSpecMap(map);
        }


        // 2.调用spring data elasticsearch的API 导入到ES中
        skuEsMapper.saveAll(skuInfos);
    }


    @Override
    public Map search(Map<String, String> searchMap) {
        String keywords =null;
        //1.获取到关键字
        if(searchMap!=null){
            keywords=searchMap.get("keywords");
        }
        //2.判断是否为空 如果 为空 给一个默认 值:华为
        if (StringUtils.isEmpty(keywords)) {
            keywords = "华为";
        }
        //3.创建 查询构建对象
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //4.设置 查询的条件

        // 4.1 商品分类的列表展示: 按照商品分类的名称来分组
        //terms  指定分组的一个别名
        //field 指定要分组的字段名
        // size 指定查询结果的数量 默认是10个
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategoryGroup").field("categoryName").size(50));
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrandGroup").field("brandName").size(100));
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpecGroup").field("spec.keyword").size(10000));

        //匹配查询  先分词 再查询  主条件查询
        //参数1 指定要搜索的字段
        //参数2 要搜索的值(先分词 再搜索)
        //增加过滤条件
        BoolQueryBuilder boolQueryBuilder =QueryBuilders.boolQuery();
        Integer pageNum = 1;
        //默认情况下是第一页
        if(searchMap!=null){
            if(!StringUtils.isEmpty(searchMap.get("brand"))){
                //过滤查询的条件设置 商品品牌的条件
                boolQueryBuilder.filter(QueryBuilders.termQuery("brandName",searchMap.get("brand")));
            }
            if(!StringUtils.isEmpty(searchMap.get("category"))){
              //过滤查询的条件设置商品分类的条件
                boolQueryBuilder.filter(QueryBuilders.termQuery("categoryName",searchMap.get("category")));
            }
            //过滤的条件设置   规格条件
            for (String key : searchMap.keySet()) {
                if(key.startsWith("spec_")){
                    //截取规格名称
                    boolQueryBuilder.filter(
                            QueryBuilders.termQuery("specMap."+key.substring(5)+".keyword",searchMap.get(key)));
                }
            }
            /**
             * 过滤查询的条件设置  介个区间的过滤查询
             * 0-5000 3000-*左边不写默认为0，右边不写默认为*
             */
            if (!StringUtils.isEmpty(searchMap.get("price"))) {
                 String[] split= searchMap.get("price").split("-");
                if(!split[1].equals("*")){
                    //当右边不是*的时候
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").from(split[0],true).to(split[1],true));

                }else{
                    //当右边是*的时候
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(split[0]));
                }
            }
            if(!StringUtils.isEmpty(searchMap.get("pageNum"))){
                try {
                    pageNum =Integer.parseInt(searchMap.get("pageNum"));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    pageNum = 1;
                }
            }
        }
        //构建过滤查询
        nativeSearchQueryBuilder.withFilter(boolQueryBuilder);
        //构建分页查询


        Integer pageSize  = 3;//每页三条记录
        nativeSearchQueryBuilder.withPageable(PageRequest.of(pageNum-1,pageSize));
        //5.构建查询对象(封装了查询的语法)
        //构建排序查询   String sortRule="";
        //        String sortFiled="";

        if(searchMap!=null){
            String sortRule =searchMap.get("sortRule");
            String  sortFiled =searchMap.get("sortFiled");
            if(!StringUtils.isEmpty(sortRule)&&!StringUtils.isEmpty(sortFiled)){
                nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(sortFiled).order(sortRule.equals("DESC")? SortOrder.ASC:SortOrder.DESC));
            }
        }
        //设置高亮条件，被搜索的关键字高亮
        nativeSearchQueryBuilder.withHighlightFields(new HighlightBuilder.Field("name"));
        nativeSearchQueryBuilder.withHighlightBuilder(new HighlightBuilder().preTags("<em style=\"color:red\">").postTags("</em>"));
        //设置住关键字查询
        nativeSearchQueryBuilder.withQuery(QueryBuilders.multiMatchQuery(keywords,"name","categoryName","brandName"));

        NativeSearchQuery nativeSearchQuery = nativeSearchQueryBuilder.build();
        //6.执行查询
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(nativeSearchQuery, SkuInfo.class, new SearchResultMapperImpl() );


        // 获取聚合结果  获取商品分类的列表数据
        StringTerms stringTermsCategory = (StringTerms) skuInfos.getAggregation("skuCategoryGroup");
        //获取聚合结果 获取商品品牌的列表数据
        StringTerms stringTermBrand =(StringTerms)skuInfos.getAggregation("skuBrandGroup");
        //获取聚合结果，获取商品规格的列表数据
        StringTerms skuSpecgroup = (StringTerms) skuInfos.getAggregation("skuSpecGroup");
        List<String> categoryList =getStringsCategoryList(stringTermsCategory);
        List<String> brandList = getStringBrandList(stringTermBrand);
        Map<String, Set<String>> specMap = getStringSetMap(skuSpecgroup);
        //7.获取结果  返回map

        List<SkuInfo> content = skuInfos.getContent();//当前的页的集合
        int totalPages = skuInfos.getTotalPages();//总页数
        long totalElements = skuInfos.getTotalElements();//总记录数

        Map<String,Object> resultMap =new HashMap<>();
        resultMap.put("categoryList",categoryList);//商品分类的列表数据
        resultMap.put("brandList",brandList);//商品分类的列表数据
        resultMap.put("specMap",specMap);
        resultMap.put("rows",content);
        resultMap.put("total",totalElements);
        resultMap.put("totalPages",totalPages);
        resultMap.put("pageNum",pageNum);
        resultMap.put("pageSize",30);
        return resultMap;
    }

    /**
     * 获取规格列表数据
     * @param stringTermsSpec
     * @return
     */
    private Map<String, Set<String>> getStringSetMap(StringTerms stringTermsSpec) {
        Map<String,Set<String>> specMap=new HashMap<>();
        Set<String> specList =new HashSet<>();
        if(stringTermsSpec !=null){
            for (StringTerms.Bucket bucket : stringTermsSpec.getBuckets()){
                specList.add(bucket.getKeyAsString());
            }
        }
        for(String specjson :specList){
            Map<String,String> map =JSON.parseObject(specjson,Map.class);
            for(Map.Entry<String,String> entry :map.entrySet()){
                String key =entry.getKey();//规格名字
                String value =entry.getValue(); //规格选项值
                Set<String> specValues =specMap.get(key);
                if(specValues == null){
                    specValues=new HashSet<String>();
                }
                //将当前规格加入到集合中
                specValues.add(value);
                //将数据存入到specMap中
                specMap.put(key,specValues);
            }
        }
        return specMap;

    }

    /**
     * 获取品牌列表
     * @param stringTermsBrand
     * @return
     */
    private List<String> getStringBrandList(StringTerms stringTermsBrand){
        List<String> brandList =new ArrayList<>();
        if(stringTermsBrand !=null){
            for (StringTerms.Bucket bucket :stringTermsBrand.getBuckets()){
                brandList.add(bucket.getKeyAsString());
            }
        }
        return brandList;
    }




    /**
     * 获取分类列表
     * @param stringTerms
     * @return
     */


    private List<String> getStringsCategoryList(StringTerms stringTerms){
        List<String> categoryList = new ArrayList<>();
        if (stringTerms != null) {
            for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
                String keyAsString = bucket.getKeyAsString();
                System.out.println(keyAsString);//就是商品分类的数据
                categoryList.add(keyAsString);
            }
        }
        return categoryList;
    }

}
