package com.hou.cassecurity;

import javax.servlet.annotation.WebListener;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
@EnableGlobalMethodSecurity(jsr250Enabled=true,prePostEnabled=true,securedEnabled=true)  //开启spring security方法级别权限注解
@MapperScan("com.hou.*")
@ServletComponentScan //扫描servlet注解，比如@webfilter @WebListener 建filter和listener注入servlet容器中
public class CasSecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(CasSecurityApplication.class, args);
	}
}
