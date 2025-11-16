# Spring Security formLogin 配置选项详解

## 📋 配置项总览

### 1. **基本配置**

| 配置项                         | 类型   | 默认值         | 说明                          |
| ------------------------------ | ------ | -------------- | ----------------------------- |
| `loginPage("/login")`          | String | `/login`       | 自定义登录页面 URL            |
| `loginProcessingUrl("/login")` | String | `/login`       | 登录表单提交 URL（POST 请求） |
| `defaultSuccessUrl("/")`       | String | `/`            | 登录成功后的默认重定向 URL    |
| `failureUrl("/login?error")`   | String | `/login?error` | 登录失败后的重定向 URL        |

### 2. **字段名配置**

| 配置项                          | 类型   | 默认值     | 说明                    |
| ------------------------------- | ------ | ---------- | ----------------------- |
| `usernameParameter("username")` | String | `username` | 表单中用户名字段的 name |
| `passwordParameter("password")` | String | `password` | 表单中密码字段的 name   |

### 3. **高级处理器配置**

| 配置项                    | 类型                         | 说明                   |
| ------------------------- | ---------------------------- | ---------------------- |
| `successHandler(handler)` | AuthenticationSuccessHandler | 自定义登录成功处理逻辑 |
| `failureHandler(handler)` | AuthenticationFailureHandler | 自定义登录失败处理逻辑 |

### 4. **权限配置**

| 配置项        | 说明                                                          |
| ------------- | ------------------------------------------------------------- |
| `permitAll()` | 允许所有人访问登录相关页面（loginPage 和 loginProcessingUrl） |

---

## 🔧 实际配置示例

### 当前项目配置

```java
.formLogin(formLogin -> formLogin
    .loginPage("/login")                    // 自定义登录页面
    .loginProcessingUrl("/login")           // 登录提交 URL
    .defaultSuccessUrl("/", false)          // 成功重定向（优先返回原页面）
    .failureUrl("/login?error=true")        // 失败重定向
    .permitAll()                            // 允许访问登录页面
)
```

### 字段名自定义示例

如果表单使用不同的字段名：

```java
.formLogin(formLogin -> formLogin
    .usernameParameter("user")              // 表单中用户名字段为 "user"
    .passwordParameter("pwd")               // 表单中密码字段为 "pwd"
)
```

对应的 HTML 表单：

```html
<form method="post" action="/login">
  <input type="text" name="user" />
  <!-- 注意：name="user" -->
  <input type="password" name="pwd" />
  <!-- 注意：name="pwd" -->
  <button type="submit">登录</button>
</form>
```

### 自定义成功处理器示例

```java
@Service
public class CustomSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException {
        String username = authentication.getName();

        // 根据角色重定向到不同页面
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            response.sendRedirect("/admin/dashboard");
        } else {
            response.sendRedirect("/user/home");
        }

        // 记录日志
        log.info("用户 {} 登录成功", username);
    }
}

// 在配置中使用
.formLogin(formLogin -> formLogin
    .successHandler(customSuccessHandler)
)
```

### 自定义失败处理器示例

```java
@Service
public class CustomFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                       HttpServletResponse response,
                                       AuthenticationException exception) throws IOException {
        String errorMessage;
        if (exception instanceof BadCredentialsException) {
            errorMessage = "用户名或密码错误";
        } else if (exception instanceof DisabledException) {
            errorMessage = "账户已被禁用";
        } else if (exception instanceof LockedException) {
            errorMessage = "账户已被锁定";
        } else {
            errorMessage = "登录失败，请重试";
        }

        response.sendRedirect("/login?error=" +
            URLEncoder.encode(errorMessage, "UTF-8"));
    }
}
```

---

## 💡 关键点说明

### defaultSuccessUrl 的第二个参数

```java
.defaultSuccessUrl("/", false)  // false：优先返回原访问的页面
.defaultSuccessUrl("/", true)   // true：总是返回根路径
```

**false 的行为（推荐）**：

- 用户访问 `/protected/page` → 被拦截跳转登录页
- 登录成功后 → 自动返回 `/protected/page`

**true 的行为**：

- 无论什么情况登录成功 → 都返回 `/`

### 内置处理器

Spring Security 提供了几个内置处理器：

1. **SavedRequestAwareAuthenticationSuccessHandler**
   - 保存原始请求，登录后返回原页面

```java
SavedRequestAwareAuthenticationSuccessHandler handler =
    new SavedRequestAwareAuthenticationSuccessHandler();
handler.setDefaultTargetUrl("/");
handler.setAlwaysUseDefaultTargetUrl(false);
```

2. **SimpleUrlAuthenticationFailureHandler**
   - 简单重定向到失败 URL

```java
SimpleUrlAuthenticationFailureHandler handler =
    new SimpleUrlAuthenticationFailureHandler();
handler.setDefaultFailureUrl("/login?error");
```

---

## 🎯 推荐配置

### 开发环境（前后端分离）

```java
.formLogin(formLogin -> formLogin
    .loginPage("/login")
    .loginProcessingUrl("/login")
    .defaultSuccessUrl("/", false)
    .failureUrl("/login?error=true")
    .permitAll()
)
```

### 生产环境（需要详细错误处理）

```java
.formLogin(formLogin -> formLogin
    .loginPage("/login")
    .loginProcessingUrl("/login")
    .successHandler(customSuccessHandler)  // 自定义成功处理
    .failureHandler(customFailureHandler)  // 自定义失败处理
    .permitAll()
)
```

---

## 📚 相关文档

- [Spring Security 官方文档 - Form Login](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/form.html)
- 项目示例文件：`FormLoginConfigExample.java`
- 当前配置：`DefaultSecurityConfig.java`
