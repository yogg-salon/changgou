package com.yan.changgou.item.service;

/**
 * @author yanming
 * @version 1.0 2020/12/25
 */
public interface PageService {
    /**
     * 根据商品ID生成静态页
     * @param spuId
     */
    void createPageHtml(Long spuId);
}
