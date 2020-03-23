package com.atguigu.gmall.pms.api;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface GmallPmsApi {

    @GetMapping("/pms/spuinfo/list")
    public Resp<PageVo> list(QueryCondition queryCondition);

    @GetMapping("/pms/skuinfo/{spuId}")
    public Resp<List<SkuInfoEntity>>  querySkuBySpuId(@PathVariable("spuId")Long spuId);


    @GetMapping("/pms/brand/info/{brandId}")
    public Resp<BrandEntity> queryBrandById(@PathVariable("brandId") Long brandId);


    @GetMapping("/pms/category/info/{catId}")
    public Resp<CategoryEntity> queryCategoryById(@PathVariable("catId") Long catId);

    @GetMapping("/pms/productattrvalu/{spuId}")
    public Resp<List<ProductAttrValueEntity>> queryAttrValue(@PathVariable("spuId")Long spuId);
}
