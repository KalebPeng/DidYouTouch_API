package com.example.springboot002.demos.web.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    // 1. 配置密码编码器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. 配置用户详情服务（内存用户）
    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        // 创建管理员用户
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin"))
                .roles("ADMIN", "USER")  // 同时拥有管理员和用户角色
                .build();

        // 创建普通用户
        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("user"))
                .roles("USER")
                .build();

        // 创建访客用户（只读权限）
        UserDetails guest = User.builder()
                .username("guest")
                .password(passwordEncoder().encode("guest"))
                .roles("GUEST")
                .build();

        return new InMemoryUserDetailsManager(admin, user, guest);
    }

    // 3. 配置 HTTP 安全规则
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 授权配置
                .authorizeRequests()
                // 公开访问的路径
                .antMatchers(
                        "/",                    // 首页
                        "/api/health",          // 健康检查
                        "/api/info",            // 应用信息
                        "/api/auth/register",   // 用户注册
                        "/api/auth/login",      // 用户登录
                        "/login",               // 登录页面
                        "/logout",              // 登出
                        "/error"                // 错误页面
                ).permitAll()

                // Swagger UI 和 API 文档（公开访问）
                .antMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api-docs/**",
                        "/v3/api-docs/**",
                        "/webjars/**"
                ).permitAll()

                // 用户 API - 需要 USER 角色
                .antMatchers(
                        "/api/users/**",
                        "/api/test/**"
                ).hasAnyRole("USER", "ADMIN")

                // 管理员 API - 需要 ADMIN 角色
                .antMatchers("/api/admin/**").hasRole("ADMIN")

                // 其他所有请求都需要认证
                .anyRequest().authenticated()
                .and()

                // 表单登录配置
                .formLogin()
                .loginPage("/login")           // 自定义登录页面
                .loginProcessingUrl("/login")  // 登录处理URL
                .defaultSuccessUrl("/api/info", true)  // 登录成功后跳转
                .failureUrl("/login?error=true")       // 登录失败后跳转
                .permitAll()
                .and()

                // 登出配置
                .logout()
                .logoutUrl("/logout")          // 登出URL
                .logoutSuccessUrl("/login?logout=true")  // 登出成功后跳转
                .invalidateHttpSession(true)   // 使session失效
                .deleteCookies("JSESSIONID")   // 删除cookie
                .permitAll()
                .and()

                // 记住我功能（可选）
                .rememberMe()
                .tokenValiditySeconds(86400)   // 记住我1天
                .key("remember-me-key")
                .and()

                // 禁用 CSRF（开发环境方便测试）
                .csrf().disable()

                // 启用 HTTP 基本认证（可选）
                .httpBasic()
                .realmName("Spring Boot 002");
    }

    // 4. 配置 Web 安全（忽略静态资源等）
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(
                "/css/**",      // CSS 文件
                "/js/**",       // JavaScript 文件
                "/images/**",   // 图片文件
                "/favicon.ico", // 网站图标
                "/static/**",   // 静态资源
                "/resources/**" // 资源文件
        );
    }
}