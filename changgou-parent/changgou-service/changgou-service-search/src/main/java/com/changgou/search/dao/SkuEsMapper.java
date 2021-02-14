package com.changgou.search.dao;

import com.changgou.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkuEsMapper extends ElasticsearchCrudRepository<SkuInfo,Long> {
}
