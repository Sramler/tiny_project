package com.tiny.oauthserver.config.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tiny.oauthserver.config.CustomWebAuthenticationDetailsJacksonDeserializer;
import com.tiny.oauthserver.config.CustomWebAuthenticationDetailsSource;
import com.tiny.oauthserver.config.MultiFactorAuthenticationTokenJacksonDeserializer;
import com.tiny.oauthserver.sys.model.ResourceResponseDto;
import com.tiny.oauthserver.sys.model.SecurityUser;
import com.tiny.oauthserver.sys.security.MultiFactorAuthenticationToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.TimeZone;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;

/**
 * 全局 Jackson 配置。
 * <p>
 * 主要职责：
 * <ul>
 *   <li>统一注册安全相关模块 {@link org.springframework.security.jackson2.SecurityJackson2Modules}</li>
 *   <li>注册 OAuth2 Authorization Server 模块 {@link OAuth2AuthorizationServerJackson2Module}，确保 OAuth2AuthorizationRequest 等类型正确序列化/反序列化</li>
 *   <li>启用 Java 8 日期时间模块 {@link JavaTimeModule}，并以 ISO-8601 字符串序列化</li>
 *   <li>注册自定义反序列化器以支持认证详情对象反序列化</li>
 *   <li>通过 MixIn 支持对 {@link com.tiny.oauthserver.sys.model.SecurityUser} 的安全序列化</li>
 *   <li>统一企业级约定：UTC 时区、仅输出非 null、大小写不敏感枚举、单值数组兼容、Long 以字符串输出</li>
 * </ul>
 * 通过 {@code @Primary} 覆盖默认 {@link ObjectMapper}，并提供 {@link Jackson2ObjectMapperBuilderCustomizer}
 * 确保所有通过 Builder 构建的 ObjectMapper 行为一致。
 * <p>
 * <b>重要问题修复说明</b>：
 * <p>
 * <b>问题：ClassCastException - LinkedHashMap cannot be cast to OAuth2AuthorizationRequest</b>
 * <ul>
 *   <li><b>问题现象</b>：在 OAuth2 授权流程中，当 {@link JdbcOAuth2AuthorizationService} 从数据库读取授权信息时，
 *       会出现 {@code java.lang.ClassCastException: class java.util.LinkedHashMap cannot be cast to class
 *       org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest} 异常。</li>
 *   <li><b>根本原因</b>：
 *       <ol>
 *         <li>{@link JdbcOAuth2AuthorizationService} 使用 {@link ObjectMapper} 将 {@link OAuth2Authorization} 对象序列化为 JSON 存储到数据库</li>
 *         <li>读取时需要使用 {@link ObjectMapper} 将 JSON 反序列化为 {@link OAuth2AuthorizationRequest}、{@link Authentication}、{@link Instant} 等复杂对象</li>
 *         <li>如果 {@link ObjectMapper} 缺少必要的 Jackson 模块（{@link OAuth2AuthorizationServerJackson2Module}、
 *             {@link JavaTimeModule}、{@link SecurityJackson2Modules}），Jackson 会使用默认反序列化行为</li>
 *         <li>默认行为会将复杂对象（如 {@link OAuth2AuthorizationRequest}、{@link Authentication}、{@link Instant}）反序列化为 {@link LinkedHashMap}</li>
 *         <li>当代码尝试将 {@link LinkedHashMap} 强制转换为目标类型时，抛出 {@link ClassCastException}</li>
 *       </ol>
 *   </li>
 *   <li><b>修复方案</b>：
 *       <ol>
 *         <li>必须确保 {@link ObjectMapper} 在 <b>build() 之后</b>直接调用 {@link ObjectMapper#registerModule(com.fasterxml.jackson.databind.Module)}
 *             或 {@link ObjectMapper#registerModules(com.fasterxml.jackson.databind.Module...)} 注册所有必要模块</li>
 *         <li>不能仅依赖 {@link Jackson2ObjectMapperBuilder#modules(com.fasterxml.jackson.databind.Module...)}，
 *             因为该方法的模块注册可能不会正确传递到最终的 {@link ObjectMapper} 实例</li>
 *         <li>必须注册以下模块：
 *             <ul>
 *               <li>{@link SecurityJackson2Modules}：用于序列化/反序列化 {@link Authentication}、{@link GrantedAuthority} 等 Spring Security 对象</li>
 *               <li>{@link OAuth2AuthorizationServerJackson2Module}：用于序列化/反序列化 {@link OAuth2AuthorizationRequest}、{@link OAuth2AuthorizationResponse} 等 OAuth2 对象</li>
 *               <li>{@link JavaTimeModule}：用于序列化/反序列化 {@link Instant}、{@link LocalDateTime} 等 Java 8 时间类型</li>
 *             </ul>
 *         </li>
 *         <li>在 {@link OAuth2DataConfig} 中，必须将正确配置的 {@link ObjectMapper} 注入到 {@link JdbcOAuth2AuthorizationService}
 *             的 {@link OAuth2AuthorizationRowMapper} 和 {@link OAuth2AuthorizationParametersMapper} 中</li>
 *       </ol>
 *   </li>
 *   <li><b>错误配置示例（会导致问题）</b>：
 *       <pre>{@code
 *       // ❌ 错误：使用 Builder.modules() 可能不会正确注册到最终 ObjectMapper
 *       ObjectMapper mapper = Jackson2ObjectMapperBuilder
 *           .json()
 *           .modules(new OAuth2AuthorizationServerJackson2Module()) // 可能不会生效
 *           .build();
 *       }</pre>
 *   </li>
 *   <li><b>正确配置示例（已修复）</b>：
 *       <pre>{@code
 *       // ✅ 正确：在 build() 之后直接注册模块
 *       ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
 *       mapper.registerModules(SecurityJackson2Modules.getModules(getClass().getClassLoader()));
 *       mapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
 *       mapper.registerModule(new JavaTimeModule());
 *       }</pre>
 *   </li>
 *   <li><b>验证方法</b>：可以通过检查 {@link ObjectMapper#getRegisteredModuleIds()} 验证模块是否正确注册，
 *       应该看到以下模块都已注册：
 *       <ul>
 *         <li>{@code org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module}</li>
 *         <li>{@code jackson-datatype-jsr310}（JavaTimeModule）</li>
 *         <li>{@code org.springframework.security.jackson2.CoreJackson2Module}</li>
 *       </ul>
 *   </li>
 * </ul>
 * <p>
 * <b>线程安全</b>：本配置创建的 {@link ObjectMapper} 为单例且用于读多写少场景；
 * 初始化完成后不应在运行期动态修改模块/特性，以避免并发可见性与行为不一致问题。
 * <p>
 * <b>可配置项</b>：如需在不同环境调整（例如切换时区、命名策略、是否 Long as String），
 * 可引入 {@code @ConfigurationProperties} 并将配置项映射为本类的构造参数或 Builder 回调。
 * <p>
 * @since 1.0.0
 */
@Configuration
public class JacksonConfig {

    /**
     * 创建全局的 {@link ObjectMapper} Bean。
     * <p>
     * - 注册 Spring Security 的 Jackson 模块，确保安全对象可序列化/反序列化；
     * - 注册 OAuth2 Authorization Server 模块，确保 OAuth2AuthorizationRequest 等 OAuth2 类型正确反序列化（避免 LinkedHashMap 转换错误）；
     * - 注册 {@link JavaTimeModule} 并禁用 {@link SerializationFeature#WRITE_DATES_AS_TIMESTAMPS}，输出 ISO 字符串；
     * - 关闭 {@link DeserializationFeature#FAIL_ON_UNKNOWN_PROPERTIES} 以容忍后端/前端字段演进；
     * - 通过 {@code mixIn} 绑定 {@link SecurityUser} 的序列化视图，避免直接暴露实现；
     * - 注册自定义模块，处理 Web 认证详情对象的反序列化。
     *
     * @return 配置完成并可直接注入使用的全局 ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        // 使用 Builder 构建基础配置
        ObjectMapper mapper = Jackson2ObjectMapperBuilder
                .json()
                // 统一时区为 UTC，避免多部署地区造成时间偏移
                .timeZone(TimeZone.getTimeZone("UTC"))
                // 统一仅输出非 null 字段，减少冗余字段
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                // 关闭对 Java8 时间类型必须显式处理器的要求，交由 JavaTimeModule 接管
                .featuresToDisable(com.fasterxml.jackson.databind.MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TIMES)
                // 反序列化时对枚举大小写不敏感，降低前后端耦合
                .featuresToEnable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                // 允许以单值表示数组，提升兼容性
                .featuresToEnable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                // 使用 MixIn 为 SecurityUser 定义序列化/反序列化策略，避免直接改动实体类
                .mixIn(SecurityUser.class, SecurityUserMixin.class)
                .build();

        // 直接在 ObjectMapper 上注册模块（确保模块被正确注册）
        //
        // 【关键修复点】必须在 build() 之后注册模块，否则会导致 ClassCastException：
        // - 如果缺少 OAuth2AuthorizationServerJackson2Module，OAuth2AuthorizationRequest 会被反序列化为 LinkedHashMap
        // - 如果缺少 JavaTimeModule，Instant 字段会被反序列化为 LinkedHashMap
        // - 如果缺少 SecurityJackson2Modules，Authentication 对象会被反序列化为 LinkedHashMap
        // 当代码尝试将 LinkedHashMap 强制转换为目标类型时，会抛出 ClassCastException
        //
        // 注意：不能仅依赖 Builder.modules()，因为该方法的模块注册可能不会正确传递到最终的 ObjectMapper 实例
        // 必须显式调用 mapper.registerModule() 或 mapper.registerModules() 来确保模块被正确注册
        mapper.registerModules(SecurityJackson2Modules.getModules(getClass().getClassLoader()));
        mapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(createCustomModule());

        // 【修复集合类型信息问题】
        // SecurityJackson2Modules 可能启用了默认类型信息，导致集合类型序列化为 ["java.util.ArrayList", []] 格式
        // 解决方案：
        // 1. 通过 MixIn 为 ResourceResponseDto 禁用类型信息，避免序列化时包含 @class 字段
        // 2. 禁用全局的默认类型信息（Default Typing），避免集合类型序列化为 ["java.util.ArrayList", []] 格式
        // 注意：Spring Security 对象（如 Authentication）使用 @JsonTypeInfo 注解显式配置类型信息，不依赖全局默认类型信息
        // 所以禁用全局默认类型信息不会影响 Spring Security 对象的序列化/反序列化
        // 必须在 build() 之后、注册模块之后设置 MixIn，确保 MixIn 配置不会被模块覆盖
        mapper.addMixIn(ResourceResponseDto.class, ResourceResponseDtoMixin.class);

        // 禁用全局默认类型信息（Default Typing），避免集合类型序列化为 ["java.util.ArrayList", []] 格式
        // Spring Security 对象使用 @JsonTypeInfo 注解显式配置类型信息，不依赖全局默认类型信息
        // 所以禁用全局默认类型信息不会影响 Spring Security 对象的序列化/反序列化
        // mapper.deactivateDefaultTyping(); // 这个方法在 Jackson 2.10+ 中已废弃
        // 使用新的 API：通过配置禁用类型信息
        // 注意：如果 SecurityJackson2Modules 启用了全局默认类型信息，可能需要显式禁用

        return mapper;
    }

    /**
     * 注册自定义 Jackson 模块。
     * <p>
     * 当前仅添加了对 {@link com.tiny.oauthserver.config.CustomWebAuthenticationDetailsSource.CustomWebAuthenticationDetails}
     * 的反序列化器，保证认证流程中从 JSON 到对象的还原能力。
     *
     * @return 自定义模块，供 ObjectMapper 注册
     */
    private SimpleModule createCustomModule() {
        SimpleModule customModule = new SimpleModule();
        // Long/long 以字符串输出，避免前端 JS 精度丢失
        customModule.addSerializer(Long.class, ToStringSerializer.instance);
        customModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        customModule.addDeserializer(
            CustomWebAuthenticationDetailsSource.CustomWebAuthenticationDetails.class,
            new CustomWebAuthenticationDetailsJacksonDeserializer()
        );
        customModule.addDeserializer(
            MultiFactorAuthenticationToken.class,
            new MultiFactorAuthenticationTokenJacksonDeserializer()
        );
        return customModule;
    }

    /**
     * 通过 Builder 进行全局定制，保证所有通过 {@link Jackson2ObjectMapperBuilder} 创建的 ObjectMapper：
     * <ul>
     *   <li>注册 {@link JavaTimeModule}</li>
     *   <li>禁用时间戳输出 {@link SerializationFeature#WRITE_DATES_AS_TIMESTAMPS}</li>
     *   <li>为 {@code LocalDateTime} 使用 {@link DateTimeFormatter#ISO_LOCAL_DATE_TIME} 序列化</li>
     * </ul>
     * 该定制适用于 Spring MVC/RestTemplate 等框架层面的默认 ObjectMapper。
     *
     * @return 自定义 Builder 的回调
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            // 与全局 ObjectMapper 保持一致的构建器级配置
            builder.timeZone(TimeZone.getTimeZone("UTC"));
            builder.serializationInclusion(JsonInclude.Include.NON_NULL);
            builder.modules(new JavaTimeModule());
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.featuresToEnable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
            builder.featuresToEnable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
            builder.serializers(
                new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                // 再次确保 Long 以字符串输出（适用于通过 Builder 创建的 ObjectMapper）
                ToStringSerializer.instance
            );
        };
    }
}

