package com.tiny.idempotent.aspect;

import com.tiny.idempotent.annotation.Idempotent;
import com.tiny.idempotent.exception.IdempotentException;
import com.tiny.idempotent.service.IdempotentService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 幂等性切面
 * 
 * <p>拦截标记了 {@link Idempotent} 注解的方法，实现幂等性控制。</p>
 * 
 * @author Auto Generated
 * @since 1.0.0
 */
@Aspect
public class IdempotentAspect {
    
    private static final Logger log = LoggerFactory.getLogger(IdempotentAspect.class);
    
    private static final String IDEMPOTENCY_KEY_HEADER = "X-Idempotency-Key";
    
    private final IdempotentService idempotentService;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    
    public IdempotentAspect(IdempotentService idempotentService) {
        this.idempotentService = idempotentService;
    }
    
    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 生成幂等性 key
        String key = generateKey(joinPoint, method, idempotent);
        
        // 检查并设置幂等性 token
        boolean isFirstRequest = idempotentService.checkAndSet(key, idempotent.expireTime());
        
        if (!isFirstRequest) {
            log.warn("检测到重复请求: method={}, key={}", method.getName(), key);
            throw new IdempotentException(idempotent.message());
        }
        
        try {
            // 执行方法
            Object result = joinPoint.proceed();
            log.debug("幂等性检查通过，执行方法: method={}, key={}", method.getName(), key);
            return result;
        } catch (Throwable e) {
            // 如果执行失败，删除 token，允许重试
            log.warn("方法执行失败，删除幂等性token以允许重试: method={}, key={}, error={}", 
                    method.getName(), key, e.getMessage());
            idempotentService.delete(key);
            throw e;
        }
    }
    
    /**
     * 生成幂等性 key
     */
    private String generateKey(ProceedingJoinPoint joinPoint, Method method, Idempotent idempotent) {
        String keyExpression = idempotent.key();
        
        // 如果指定了 key 表达式，使用 SpEL 解析
        if (!keyExpression.isEmpty()) {
            return parseKeyExpression(joinPoint, method, keyExpression);
        }
        
        // 否则使用默认策略
        return generateDefaultKey(joinPoint, method);
    }
    
    /**
     * 解析 SpEL 表达式生成 key
     */
    private String parseKeyExpression(ProceedingJoinPoint joinPoint, Method method, String expression) {
        try {
            EvaluationContext context = createEvaluationContext(joinPoint, method);
            Expression expr = parser.parseExpression(expression);
            Object value = expr.getValue(context);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            log.warn("解析幂等性key表达式失败: expression={}, error={}", expression, e.getMessage());
            return generateDefaultKey(joinPoint, method);
        }
    }
    
    /**
     * 创建 SpEL 表达式上下文
     */
    private EvaluationContext createEvaluationContext(ProceedingJoinPoint joinPoint, Method method) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        
        // 获取方法参数名
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        Object[] args = joinPoint.getArgs();
        
        // 将参数添加到上下文
        if (parameterNames != null && args != null) {
            for (int i = 0; i < parameterNames.length && i < args.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        
        // 添加 HttpServletRequest 到上下文
        HttpServletRequest request = getRequest();
        if (request != null) {
            context.setVariable("request", request);
        }
        
        return context;
    }
    
    /**
     * 生成默认的幂等性 key
     */
    private String generateDefaultKey(ProceedingJoinPoint joinPoint, Method method) {
        HttpServletRequest request = getRequest();
        
        // 优先使用请求头中的 X-Idempotency-Key
        if (request != null) {
            String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);
            if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
                return method.getName() + ":" + idempotencyKey;
            }
        }
        
        // 否则使用方法名 + 参数值的 MD5
        String methodName = method.getDeclaringClass().getName() + "." + method.getName();
        String argsString = Arrays.stream(joinPoint.getArgs())
                .map(arg -> arg != null ? arg.toString() : "null")
                .collect(Collectors.joining(","));
        String key = methodName + ":" + argsString;
        return methodName + ":" + DigestUtils.md5DigestAsHex(key.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 获取当前请求
     */
    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}

