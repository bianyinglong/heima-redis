package com.hmdp.service.impl;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_LIST_KEY;
import static com.hmdp.utils.RedisConstants.CACHE_SHOP_LIST_TTL;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result getTypeList() {
        // 从缓存中查询
        List<String> list = stringRedisTemplate.opsForList().range(CACHE_SHOP_LIST_KEY, 0, -1);

        if (list != null && !list.isEmpty()) {
            List<ShopType> typeList = new ArrayList<>();
            for (String s : list) {
                ShopType shopType = JSONUtil.toBean(s, ShopType.class);
                typeList.add(shopType);
            }
            return Result.ok(typeList);
        }
        // 未命中，从数据库中查询
        List<ShopType> typeList = query().orderByAsc("sort").list();
        ArrayList<String> shopTypeList = new ArrayList<>();
        for (ShopType item : typeList) {
            shopTypeList.add(JSONUtil.toJsonStr(item));
        }
        stringRedisTemplate.opsForList().rightPushAll(CACHE_SHOP_LIST_KEY, shopTypeList);
        stringRedisTemplate.expire(CACHE_SHOP_LIST_KEY, CACHE_SHOP_LIST_TTL, TimeUnit.MINUTES);
        return Result.ok(typeList);
    }
}
