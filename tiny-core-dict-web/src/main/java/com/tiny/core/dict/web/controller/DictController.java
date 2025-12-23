package com.tiny.core.dict.web.controller;

import com.tiny.core.dict.runtime.DictRuntime;
import com.tiny.core.dict.service.DictItemService;
import com.tiny.core.dict.service.DictTypeService;
import com.tiny.core.dict.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 字典 REST API 控制器
 *
 * @author Tiny Platform
 * @version 1.0.0
 */
@RestController
@RequestMapping("/dict")
public class DictController {

    private final DictRuntime dictRuntime;
    private final DictTypeService dictTypeService;
    private final DictItemService dictItemService;

    public DictController(DictRuntime dictRuntime,
                         DictTypeService dictTypeService,
                         DictItemService dictItemService) {
        this.dictRuntime = dictRuntime;
        this.dictTypeService = dictTypeService;
        this.dictItemService = dictItemService;
    }

    /**
     * 获取字典标签（最常用接口）
     */
    @GetMapping("/label")
    public ResponseEntity<String> getLabel(
            @RequestParam String dictCode,
            @RequestParam String value,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        String label = dictRuntime.getLabel(dictCode, value, tenantId);
        return ResponseEntity.ok(label);
    }

    /**
     * 获取字典所有项
     */
    @GetMapping("/{dictCode}")
    public ResponseEntity<Map<String, String>> getDict(
            @PathVariable String dictCode,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        Map<String, String> dict = dictRuntime.getDict(dictCode, tenantId);
        return ResponseEntity.ok(dict);
    }

    /**
     * 批量获取字典标签
     */
    @PostMapping("/labels/batch")
    public ResponseEntity<Map<String, String>> getLabelsBatch(
            @Valid @RequestBody BatchLabelRequest request,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        Map<String, String> labels = dictRuntime.getLabels(
            request.getDictCode(), request.getValues(), tenantId);
        return ResponseEntity.ok(labels);
    }

    /**
     * 分页查询字典类型
     */
    @GetMapping("/types")
    public ResponseEntity<PageResponse<DictTypeDTO>> listTypes(
            @Valid DictTypeQueryDTO query,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        Page<DictTypeDTO> page = dictTypeService.listTypes(query, tenantId, pageable);
        PageResponse<DictTypeDTO> response = new PageResponse<>(
            page.getContent(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 创建字典类型
     */
    @PostMapping("/types")
    public ResponseEntity<DictTypeDTO> createType(
            @Valid @RequestBody DictTypeCreateDTO dto,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        DictTypeDTO result = dictTypeService.createType(dto, tenantId);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新字典类型
     */
    @PutMapping("/types/{id}")
    public ResponseEntity<DictTypeDTO> updateType(
            @PathVariable Long id,
            @Valid @RequestBody DictTypeUpdateDTO dto,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        DictTypeDTO result = dictTypeService.updateType(id, dto, tenantId);
        return ResponseEntity.ok(result);
    }

    /**
     * 删除字典类型
     */
    @DeleteMapping("/types/{id}")
    public ResponseEntity<Void> deleteType(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        dictTypeService.deleteType(id, tenantId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 分页查询字典项
     */
    @GetMapping("/items")
    public ResponseEntity<PageResponse<DictItemDTO>> listItems(
            @Valid DictItemQueryDTO query,
            @PageableDefault(size = 20, sort = "sortOrder", direction = Sort.Direction.ASC)
            Pageable pageable,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        Page<DictItemDTO> page = dictItemService.listItems(query, tenantId, pageable);
        PageResponse<DictItemDTO> response = new PageResponse<>(
            page.getContent(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 创建字典项
     */
    @PostMapping("/items")
    public ResponseEntity<DictItemDTO> createItem(
            @Valid @RequestBody DictItemCreateDTO dto,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        DictItemDTO result = dictItemService.createItem(dto, tenantId);
        return ResponseEntity.ok(result);
    }

    /**
     * 批量创建字典项
     */
    @PostMapping("/items/batch")
    public ResponseEntity<List<DictItemDTO>> createItemsBatch(
            @Valid @RequestBody List<DictItemCreateDTO> dtos,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        List<DictItemDTO> results = dictItemService.createItemsBatch(dtos, tenantId);
        return ResponseEntity.ok(results);
    }

    /**
     * 更新字典项
     */
    @PutMapping("/items/{id}")
    public ResponseEntity<DictItemDTO> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody DictItemUpdateDTO dto,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        DictItemDTO result = dictItemService.updateItem(id, dto, tenantId);
        return ResponseEntity.ok(result);
    }

    /**
     * 删除字典项
     */
    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        dictItemService.deleteItem(id, tenantId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 刷新字典缓存
     */
    @PostMapping("/cache/refresh")
    public ResponseEntity<Void> refreshCache(
            @RequestParam(required = false) String dictCode,
            @RequestHeader("X-Tenant-ID") Long tenantId) {
        if (dictCode != null) {
            dictRuntime.refreshCache(dictCode, tenantId);
        }
        // 如果 dictCode 为空，可以刷新所有缓存（需要扩展 DictRuntime 接口）
        return ResponseEntity.ok().build();
    }
}

