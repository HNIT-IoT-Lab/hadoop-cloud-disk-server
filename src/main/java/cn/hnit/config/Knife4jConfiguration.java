package cn.hnit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * API文档配置类,通过 http://localhost:端口/doc.html 打开API文档
 *
 *
 * @since: 2021年12月13日 0:06
 * @author: 梁峰源
 */
@Configuration
@EnableSwagger2WebMvc
public class Knife4jConfiguration {
    @Bean(value = "defaultApi2")
    public Docket defaultApi2() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("Hadoop云盘系统")
                        .description("# Hadoop云盘系统API文档")
                        .termsOfServiceUrl("47.101.206.1")
                        .contact(new Contact("梁峰源","47.101.206.1","fengyuan-liang@foxmail.com"))
                        .version("1.0")
                        .build())
                //分组名称
                .groupName("2.0版本")
                .select()
                //这里指定扫描包路径，需要扫描Controller
                .apis(RequestHandlerSelectors.basePackage("cn.hnit.controller"))
                .paths(PathSelectors.any())
                .build();
    }
}
