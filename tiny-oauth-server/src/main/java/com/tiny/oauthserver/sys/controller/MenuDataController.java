package com.tiny.oauthserver.sys.controller;

import com.tiny.oauthserver.sys.util.MenuDataImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 菜单数据导入控制器
 * 提供菜单数据导入的 API 接口
 */
@RestController
@RequestMapping("/sys/menu-data")
public class MenuDataController {
    
    private static final Logger logger = LoggerFactory.getLogger(MenuDataController.class);
    
    @Autowired
    private MenuDataImporter menuDataImporter;
    
    /**
     * 导入默认菜单数据
     * @return 导入结果
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importDefaultMenuData() {
        try {
            logger.info("开始导入默认菜单数据");
            
            int importedCount = menuDataImporter.importDefaultMenuData();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "菜单数据导入成功");
            result.put("importedCount", importedCount);
            
            logger.info("菜单数据导入成功，共导入 {} 个资源", importedCount);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("导入菜单数据失败", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "菜单数据导入失败: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 导入指定 JSON 文件的菜单数据
     * @param jsonFilePath JSON 文件路径
     * @return 导入结果
     */
    @PostMapping("/import/{jsonFilePath}")
    public ResponseEntity<Map<String, Object>> importMenuData(@PathVariable("jsonFilePath") String jsonFilePath) {
        try {
            logger.info("开始导入菜单数据，文件路径: {}", jsonFilePath);
            
            int importedCount = menuDataImporter.importMenuData(jsonFilePath);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "菜单数据导入成功");
            result.put("importedCount", importedCount);
            result.put("jsonFilePath", jsonFilePath);
            
            logger.info("菜单数据导入成功，共导入 {} 个资源", importedCount);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("导入菜单数据失败", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "菜单数据导入失败: " + e.getMessage());
            result.put("jsonFilePath", jsonFilePath);
            
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    /**
     * 获取导入状态
     * @return 导入状态信息
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getImportStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "菜单数据导入服务正常运行");
        result.put("available", true);
        
        return ResponseEntity.ok(result);
    }
} 