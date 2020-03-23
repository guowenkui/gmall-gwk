package com.atguigu.gmall.search;

import com.atguigu.gmall.search.pojo.Goods;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

@SpringBootTest
class GmallSearchApplicationTests {

	@Autowired
	private ElasticsearchRestTemplate restTemplate;

	@Test
	void contextLoads() {

		this.restTemplate.createIndex(Goods.class);
		this.restTemplate.putMapping(Goods.class);
	}

}
