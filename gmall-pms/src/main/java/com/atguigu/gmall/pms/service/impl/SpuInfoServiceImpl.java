package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.ProductAttrValueDao;
import com.atguigu.gmall.pms.dao.SkuInfoDao;
import com.atguigu.gmall.pms.dao.SpuInfoDescDao;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.service.ProductAttrValueService;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SkuSaleAttrValueService;
import com.atguigu.gmall.pms.vo.BaseAttrVO;
import com.atguigu.gmall.pms.vo.SkuInfoVO;
import com.atguigu.gmall.pms.vo.SpuInfoVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.SpuInfoDao;
import com.atguigu.gmall.pms.service.SpuInfoService;
import org.springframework.util.CollectionUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescDao descDao;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoDao skuInfoDao;

    @Autowired
    private SkuImagesService skuImagesService;


    @Autowired
    private SkuSaleAttrValueService saleAttrValueService;


    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo querySpuPage(QueryCondition condition, Long cid) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        //判断分类是否为0
        if (cid!=0){
            wrapper.eq("catalog_id",cid);
        }

        //判断关键字是否为空
        String key = condition.getKey();
        if (StringUtils.isNotBlank(key)){
            wrapper.and(t -> t.eq("id",key).or().like("spu_name",key));
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(condition),
                wrapper
        );

        return new PageVo(page);
    }

    @Override
    public void bigSave(SpuInfoVO spuInfoVO) {
        //1.保存spu相关的3张表
        //1.1 保存pms_spu_info信息
        spuInfoVO.setCreateTime(new Date());
        spuInfoVO.setUodateTime(spuInfoVO.getCreateTime());
        this.save(spuInfoVO);

        Long spuId = spuInfoVO.getId();

        //1.2保存pms_spu_info_desc
        List<String> spuImages = spuInfoVO.getSpuImages();
        if (!CollectionUtils.isEmpty(spuImages)){
            SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
            descEntity.setSpuId(spuId);
            descEntity.setDecript(StringUtils.join(spuImages,","));
            this.descDao.insert(descEntity);
        }
        //1.3 保存pms_product_attr_value
        List<BaseAttrVO> baseAttrs = spuInfoVO.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)){
            List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(baseAttrVO ->{
                ProductAttrValueEntity attrValueEntity = baseAttrVO;
                attrValueEntity.setSpuId(spuId);
                return  attrValueEntity;
            }

            ).collect(Collectors.toList());
            this.productAttrValueService.saveBatch(productAttrValueEntities);
        }

        //2.保存sku相关的3张表
        List<SkuInfoVO> skus = spuInfoVO.getSkus();
        if (CollectionUtils.isEmpty(skus)){
            return;
        }
        skus.forEach(skuInfoVO -> {
            //2.1 保存pms_sku_info
            skuInfoVO.setSpuId(spuId);
            skuInfoVO.setSkuCode(UUID.randomUUID().toString());
            skuInfoVO.setBrandId(spuInfoVO.getBrandId());
            skuInfoVO.setCatalogId(spuInfoVO.getCatalogId());
            List<String> images = skuInfoVO.getImages();
            //设置默认图片
            if (!CollectionUtils.isEmpty(images)){
                skuInfoVO.setSkuDefaultImg(StringUtils.isNotBlank(skuInfoVO.getSkuDefaultImg())?skuInfoVO.getSkuDefaultImg():images.get(0));
            }
            this.skuInfoDao.insert(skuInfoVO);

            Long skuId = skuInfoVO.getSkuId();
            //2.2保存pms_sku_images
            if (!CollectionUtils.isEmpty(images)){
                List<SkuImagesEntity> skuImagesEntitys= images.stream().map(image ->{
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setImgUrl(image);
                    skuImagesEntity.setSkuId(skuId);
                    //设置是否默认图片
                    skuImagesEntity.setDefaultImg(StringUtils.equals(skuInfoVO.getSkuDefaultImg(),image)?1:0);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                this.skuImagesService.saveBatch(skuImagesEntitys);

            }
            //2.3保存psm_sale_attr_value
            List<SkuSaleAttrValueEntity> salAttrs = skuInfoVO.getSaleAttrs();
            if (!CollectionUtils.isEmpty(salAttrs)){
                //设置skuId
                salAttrs.forEach(skuSaleAttrValueEntity -> skuSaleAttrValueEntity.setSkuId(skuId));
                //批量保存销售属性
                this.saleAttrValueService.saveBatch(salAttrs);
            }

            //3.保存营销信息的3张表
            //3.1保存sms_sku_bounds
            //3.2保存sms_sku_ladder
            //3.3保存sms_sku_full_reduction

        });
    }

}