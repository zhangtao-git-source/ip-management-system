package com.cmcc.ip.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cmcc.ip.system.common.Result;
import com.cmcc.ip.system.entity.AddressPool;
import com.cmcc.ip.system.service.AddressPoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/pool")
public class AddressPoolController {

    private final AddressPoolService addressPoolService;

    public AddressPoolController(AddressPoolService addressPoolService) {
        this.addressPoolService = addressPoolService;
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) String poolCode,
            @RequestParam(required = false) Integer ipType,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        AddressPool query = new AddressPool();
        query.setPoolCode(poolCode);
        query.setIpType(ipType);
        query.setRegionCode(regionCode);
        query.setStatus(status);

        IPage<AddressPool> page = addressPoolService.queryList(query, pageNum, pageSize);

        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getRecords());
        data.put("total", page.getTotal());
        data.put("pageNum", page.getCurrent());
        data.put("pageSize", page.getSize());
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<AddressPool> getById(@PathVariable Long id) {
        AddressPool pool = addressPoolService.getById(id);
        if (pool == null) {
            return Result.error(404, "地址池不存在");
        }
        return Result.success(pool);
    }

    @PostMapping("/add")
    public Result<AddressPool> add(@RequestBody AddressPool pool) {
        log.info("新增地址池: {}", pool);
        if (pool.getPoolCode() == null || pool.getPoolCode().isEmpty()) {
            return Result.error(400, "地址池编码不能为空");
        }
        if (pool.getStartAddress() == null || pool.getEndAddress() == null) {
            return Result.error(400, "起始地址和结束地址不能为空");
        }

        AddressPool existing = addressPoolService.getByPoolCode(pool.getPoolCode());
        if (existing != null) {
            return Result.error(400, "地址池编码已存在");
        }

        pool.setStatus(AddressPool.PoolStatus.ENABLED.getValue());
        boolean success = addressPoolService.save(pool);
        if (success) {
            addressPoolService.generateIpRecords(pool);
            return Result.success(pool);
        }
        return Result.error(500, "新增失败");
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody AddressPool pool) {
        log.info("更新地址池: {}", pool);
        if (pool.getId() == null) {
            return Result.error(400, "ID不能为空");
        }
        boolean success = addressPoolService.update(pool);
        return success ? Result.success("更新成功") : Result.error(500, "更新失败");
    }

    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        log.info("更新地址池状态: id={}, status={}", id, status);
        AddressPool pool = addressPoolService.getById(id);
        if (pool == null) {
            return Result.error(404, "地址池不存在");
        }
        pool.setStatus(status);
        boolean success = addressPoolService.update(pool);
        return success ? Result.success("状态更新成功") : Result.error(500, "状态更新失败");
    }
}
