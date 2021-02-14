package com.changgou.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.content.feign.ContentFeign;
import com.changgou.content.pojo.Content;
import com.changgou.item.feign.PageFeign;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@CanalEventListener
public class CanalDataEventListener {



    @Autowired
    private ContentFeign contentFeign;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 增加数据监听
     * @param eventType  当前操作的类型
     * @param rowData   发生变更的一行数据
     */
//    @InsertListenPoint
//    public void onEventInsert(CanalEntry.EventType eventType,
//                              //获取变更后的数据
//                              CanalEntry.RowData rowData) {
//        rowData.getAfterColumnsList().forEach((c) ->
//                System.out.println("By-annotation:" + c.getName() + "::" + c.getValue()));
//
//    }

    /**
     * 修改数据监听
     * @param rowData
     */
//    @UpdateListenPoint
//    public void onEventUpdate(CanalEntry.RowData rowData){
//        System.out.println("updataListenPonit");
//        rowData.getAfterColumnsList().forEach((c)->
//                System.out.println("By-annotation:" + c.getName() + "::" + c.getValue()));
//
//    }

    /**
     * 删除数据监听
     * @param eventType
     */
//    @DeleteListenPoint
//    public void onEventDelete(CanalEntry.EventType eventType){
//        System.out.println("DeletePoint");
//    }


    /**
     * 自定义数据修改监听
     * @param eventType
     * @param rowData
     */
//    @ListenPoint(destination = "example",
//            schema = "changgou_content",
//            table = {"tb_content_category","tb_content"},eventType = CanalEntry.EventType.UPDATE)
//    public void onEventCustomUpdate(CanalEntry.EventType eventType,CanalEntry.RowData rowData){
//        System.err.println("DeleteListenPoint");
//        rowData.getAfterColumnsList().forEach((c)-> System.out.println("By-Annotation:"+c.getName()+"::"+c.getValue()));
//    }



    @ListenPoint(destination = "example",
            schema = "changgou_content",
            table = {"tb_content","tb_content_category"},
            eventType = {
                    CanalEntry.EventType.UPDATE,
                    CanalEntry.EventType.DELETE,
                    CanalEntry.EventType.INSERT
                    }
    )
    public void onEventCustomUpdate(CanalEntry.EventType eventType,CanalEntry.RowData rowData){
        //获取列名，为category_id的值
        String categoryId =getColumnValue(eventType,rowData);
        //调用feign获取该分类下的所有的广告集合
        Result<List<Content>> categoryResult = contentFeign.findByCategory(Long.valueOf(categoryId));
        List<Content> data =categoryResult.getData();
        //3 使用redisTemplate存储到redis中。
        stringRedisTemplate.boundValueOps("cotent_"+categoryId).set(JSON.toJSONString(data));

    }

    private String getColumnValue(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
    String categoryId ="";
    //判断 eventType的操作，如果是删除，则获取beforelist
        if(eventType== CanalEntry.EventType.DELETE){
            for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
                if(column.getName().equalsIgnoreCase("category_id")){
                    categoryId =column.getValue();
                    return categoryId;
                }
            }
        }else{
            //当对数据库的操作是添加或者更新获取修改后的afterlist
            for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
                if(column.getName().equalsIgnoreCase("category_id")){
                    categoryId=column.getValue();
                    return categoryId;
                }
            }
        }
        return categoryId;
    }


    @Autowired
    private PageFeign pageFeign;
    @ListenPoint(destination = "example",
    schema = "changgou_goods",table={"tb_spu"},eventType = {CanalEntry.EventType.UPDATE,CanalEntry.EventType.INSERT,CanalEntry.EventType.DELETE})
    public void onEventCustomSpu(CanalEntry.EventType eventType,CanalEntry.RowData rowData){
        // if判断操作类型
        if(eventType== CanalEntry.EventType.DELETE){
            String spuId = "";
            List<CanalEntry.Column> beforeColumnList = rowData.getBeforeColumnsList();
            for (CanalEntry.Column column : beforeColumnList) {
                if(column.getName().equals("id")){
                    spuId=column.getValue();//spuId
                    break;
                }
            }
            //todo 删除静态页
        }else{
            //新增或者更新
            String spuId = "";
            List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
            for (CanalEntry.Column column : afterColumnsList) {
                if(column.getName().equals("id")){
                    spuId =column.getValue();
                    break;
                }
            }
            //更新 生成静态页
            pageFeign.createHtml(Long.valueOf(spuId));
        }
    }

}
