package cn.hnit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 统一跨域问题解决
 *
 * @author 梁峰源
 * @since: 2021年12月01日 17:20
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 对于所有请求
        registry.addMapping("/**")
                //是否发送凭证
                .allowCredentials(true)
                //允许的请求的来源
                .allowedOriginPatterns("*")
                //允许的方法
                .allowedMethods("GET", "PUT", "POST", "DELETE", "OPTIONS", "HEAD")
                //客户端缓存相应的时长，单位s
                .maxAge(3600)
                .allowCredentials(true)
                //允许所有的头
                .allowedHeaders("*")
                .exposedHeaders("access-control-allow-headers",
                        "access-control-allow-methods",
                        "access-control-allow-origin",
                        "access-control-max-age",
                        "X-Frame-Options")
                .exposedHeaders("*")
                .allowedMethods("*")
                .allowedOrigins("*");
    }
}
