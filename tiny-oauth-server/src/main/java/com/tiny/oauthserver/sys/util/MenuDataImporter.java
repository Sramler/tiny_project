package com.tiny.oauthserver.sys.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiny.oauthserver.sys.enums.ResourceType;
import com.tiny.oauthserver.sys.model.Resource;
import com.tiny.oauthserver.sys.repository.ResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 菜单数据导入工具类
 * 用于从 JSON 文件读取菜单数据并插入数据库
 */
@Component
public class MenuDataImporter {
    
    private static final Logger logger = LoggerFactory.getLogger(MenuDataImporter.class);
    
    @Autowired
    private ResourceRepository resourceRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 菜单项数据结构
     */
    public static class MenuItem {
        private String title;
        private String url;
        private String icon;
        private Boolean showIcon;
        private String component;
        private String redirect;
        private List<MenuItem> children;
        
        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        
        public Boolean getShowIcon() { return showIcon; }
        public void setShowIcon(Boolean showIcon) { this.showIcon = showIcon; }
        
        public String getComponent() { return component; }
        public void setComponent(String component) { this.component = component; }
        
        public String getRedirect() { return redirect; }
        public void setRedirect(String redirect) { this.redirect = redirect; }
        
        public List<MenuItem> getChildren() { return children; }
        public void setChildren(List<MenuItem> children) { this.children = children; }
    }
    
    /**
     * 从 JSON 文件导入菜单数据
     * @param jsonFilePath JSON 文件路径
     * @return 导入的资源数量
     */
    @Transactional
    public int importMenuData(String jsonFilePath) {
        try {
            logger.info("开始导入菜单数据，文件路径: {}", jsonFilePath);
            
            // 读取 JSON 文件
            List<MenuItem> menuItems = readMenuJson(jsonFilePath);
            if (menuItems == null || menuItems.isEmpty()) {
                logger.warn("JSON 文件中没有菜单数据");
                return 0;
            }
            
            // 清空现有菜单数据（可选）
            // clearExistingMenuData();
            
            // 导入菜单数据
            int importedCount = importMenuItems(menuItems, null, 1);
            
            logger.info("菜单数据导入完成，共导入 {} 个资源", importedCount);
            return importedCount;
            
        } catch (Exception e) {
            logger.error("导入菜单数据失败", e);
            throw new RuntimeException("导入菜单数据失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 读取菜单 JSON 文件
     * @param jsonFilePath JSON 文件路径
     * @return 菜单项列表
     */
    private List<MenuItem> readMenuJson(String jsonFilePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(jsonFilePath);
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<List<MenuItem>>() {});
        }
    }
    
    /**
     * 清空现有菜单数据
     */
    private void clearExistingMenuData() {
        logger.info("清空现有菜单数据");
        resourceRepository.deleteByTypeIn(List.of(ResourceType.DIRECTORY, ResourceType.MENU));
    }
    
    /**
     * 递归导入菜单项
     * @param menuItems 菜单项列表
     * @param parentId 父级资源ID
     * @param baseSort 基础排序值
     * @return 导入的资源数量
     */
    private int importMenuItems(List<MenuItem> menuItems, Long parentId, int baseSort) {
        int importedCount = 0;
        
        for (int i = 0; i < menuItems.size(); i++) {
            MenuItem menuItem = menuItems.get(i);
            int sort = baseSort + i;
            
            // 创建资源对象
            Resource resource = createResourceFromMenuItem(menuItem, parentId, sort);
            
            // 保存到数据库
            Resource savedResource = resourceRepository.save(resource);
            importedCount++;
            
            logger.debug("导入菜单项: {} (ID: {})", resource.getTitle(), savedResource.getId());
            
            // 递归导入子菜单
            if (menuItem.getChildren() != null && !menuItem.getChildren().isEmpty()) {
                importedCount += importMenuItems(menuItem.getChildren(), savedResource.getId(), 1);
            }
        }
        
        return importedCount;
    }
    
    /**
     * 从菜单项创建资源对象
     * @param menuItem 菜单项
     * @param parentId 父级资源ID
     * @param sort 排序值
     * @return 资源对象
     */
    private Resource createResourceFromMenuItem(MenuItem menuItem, Long parentId, int sort) {
        Resource resource = new Resource();
        
        // 设置基本信息
        resource.setName(generateResourceName(menuItem.getTitle(), parentId));
        resource.setTitle(menuItem.getTitle());
        resource.setUrl(menuItem.getUrl() != null ? menuItem.getUrl() : "");
        resource.setIcon(menuItem.getIcon() != null ? menuItem.getIcon() : "");
        resource.setShowIcon(menuItem.getShowIcon() != null ? menuItem.getShowIcon() : false);
        resource.setComponent(menuItem.getComponent() != null ? menuItem.getComponent() : "");
        resource.setRedirect(menuItem.getRedirect() != null ? menuItem.getRedirect() : "");
        resource.setSort(sort);
        resource.setParentId(parentId);
        
        // 设置默认值
        resource.setUri(""); // 默认空字符串
        resource.setMethod(""); // 默认空字符串
        resource.setHidden(false);
        resource.setKeepAlive(false);
        resource.setPermission(generatePermission(menuItem.getTitle()));
        
        // 判断资源类型
        if (menuItem.getChildren() != null && !menuItem.getChildren().isEmpty()) {
            // 有子菜单，设置为目录类型
            resource.setType(ResourceType.DIRECTORY);
        } else {
            // 没有子菜单，设置为菜单类型
            resource.setType(ResourceType.MENU);
        }
        
        return resource;
    }
    
    /**
     * 生成资源名称
     * @param title 标题
     * @param parentId 父级ID
     * @return 资源名称
     */
    private String generateResourceName(String title, Long parentId) {
        if (title == null || title.trim().isEmpty()) {
            return "menu_" + System.currentTimeMillis();
        }
        
        // 将中文标题转换为拼音或英文名称
        String name = title.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_")
                .replaceAll("_+", "_")
                .trim();
        
        // 如果父级ID不为空，添加前缀
        if (parentId != null) {
            name = "sub_" + name;
        }
        
        return name;
    }
    
    /**
     * 生成权限标识
     * @param title 标题
     * @return 权限标识
     */
    private String generatePermission(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "menu:view";
        }
        
        // 将中文标题转换为权限标识
        String permission = title.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", ":")
                .replaceAll(":+", ":")
                .trim();
        
        return permission + ":view";
    }
    
    /**
     * 从默认路径导入菜单数据
     * @return 导入的资源数量
     */
    public int importDefaultMenuData() {
        return importMenuData("menu.json");
    }
} 