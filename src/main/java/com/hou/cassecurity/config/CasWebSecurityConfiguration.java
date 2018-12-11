package com.hou.cassecurity.config;

import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.session.SingleSignOutHttpSessionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsUtils;
@Configuration
@Order(SecurityProperties.BASIC_AUTH_ORDER)
public class CasWebSecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Autowired
    private CasAuthenticationEntryPoint casAuthenticationEntryPoint;

    @Autowired
    private CasAuthenticationProvider casAuthenticationProvider;

    @Autowired
    private CasAuthenticationFilter casAuthenticationFilter;

    @Autowired
    private LogoutFilter logoutFilter;

    @Autowired
    private CasServerProperties casServerProperties;
    
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers().frameOptions().disable();

        http.csrf().disable();

        http.authorizeRequests()
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                .antMatchers("/static/**").permitAll() // 不拦截静态资源
                .antMatchers("/api/**").permitAll()  // 不拦截对外API
                .antMatchers("/index").permitAll() // "/index"路径可以匿名访问
                    .anyRequest().authenticated();  // 所有资源都需要登陆后才可以访问。

        http.logout().permitAll();  // 不拦截注销

        http.exceptionHandling().authenticationEntryPoint(casAuthenticationEntryPoint);
        // 单点注销的过滤器，必须配置在SpringSecurity的过滤器链中，如果直接配置在Web容器中，貌似是不起作用的。我自己的是不起作用的。
        SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
        singleSignOutFilter.setCasServerUrlPrefix(casServerProperties.getCasServerUrlPrefix());

        http.addFilter(casAuthenticationFilter)
                .addFilterBefore(logoutFilter, LogoutFilter.class)
                .addFilterBefore(singleSignOutFilter, CasAuthenticationFilter.class);

        http.antMatcher("/**");
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(casAuthenticationProvider);
    }

    @Bean
    public ServletListenerRegistrationBean<SingleSignOutHttpSessionListener> singleSignOutHttpSessionListener(){
        ServletListenerRegistrationBean<SingleSignOutHttpSessionListener> servletListenerRegistrationBean =
                new ServletListenerRegistrationBean<>();
        servletListenerRegistrationBean.setListener(new SingleSignOutHttpSessionListener());
        return servletListenerRegistrationBean;
    }



}
