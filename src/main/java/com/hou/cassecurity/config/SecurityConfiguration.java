package com.hou.cassecurity.config;

import java.util.ArrayList;
import java.util.List;

import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

/**
 ** cas与sercurity整合，需要配置的对象
 *
 * 在cas与security整合中， 首先需要做的是将应用的登录认证入口改为使用CasAuthenticationEntryPoint。
 * 所以首先我们需要配置一个CasAuthenticationEntryPoint对应的bean，
 * 然后指定需要进行登录认证时使用该AuthenticationEntryPoint。
 * 配置CasAuthenticationEntryPoint时需要指定一个ServiceProperties，
 * 该对象主要用来描述service（Cas概念）相关的属性，主要是指定在Cas Server认证成功后将要跳转的地址。
 *
 *
 * CasAuthenticationFilter认真过滤器，负责认证跳转和票据验证 
 *
 * @author houzhonglan
 * @date 2018年12月10日 上午11:12:01
 */
@Configuration
@EnableConfigurationProperties(value= {CasServerProperties.class})
public class SecurityConfiguration {
	
	@Autowired
	private CasServerProperties casServerProperties;
	
	@Autowired
	private AuthenticationUserDetailsService<CasAssertionAuthenticationToken> userDetailsService;
	
	
	@Bean
	public AuthenticationManager authenticationManager(CasAuthenticationProvider provider) {
		
		List<AuthenticationProvider> providers = new ArrayList<>();
		providers.add(provider);
		
		ProviderManager providerManager = new ProviderManager(providers);
		
		return providerManager;
		
	}
	
	
	
	/**
	 ** 我们自己应用的配置信息，该对象主要用于构建CasAuthenticationEntryPoint。
	 * @return
	 * @date 2018年12月10日 上午11:21:16
	 */
	@Bean
	public ServiceProperties serviceProperties() {
		ServiceProperties serviceProperties = new ServiceProperties();
		//设置默认的cas登陆后回跳地址
		serviceProperties.setService(casServerProperties.getServerName()+"/index");
		//设置我们应用是否敏感
		serviceProperties.setSendRenew(false);
		//设置是否对未拥有ticket的访问均需要验证
		serviceProperties.setAuthenticateAllArtifacts(true);
		return serviceProperties;
	}
	
	/** 
	 ** CAS认证过滤器，主要实现票据认证和认证成功后的跳转。
	 * @return
	 * @date 2018年12月10日 上午11:25:56
	 */
	@Bean
	public CasAuthenticationFilter casAuthenticationFilter(AuthenticationManager auth,ServiceProperties serviceProperties) {
		CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
		//给过滤器设置我们应用的基本配置
		casAuthenticationFilter.setServiceProperties(serviceProperties);
		//给过滤器设置认证管理器
		casAuthenticationFilter.setAuthenticationManager(auth);
		//设置过滤器到cas server认证的地址
		casAuthenticationFilter.setFilterProcessesUrl(casServerProperties.getCasServerLoginUrl());
		//设置是否继续执行其他过滤器，在完成认证前
		casAuthenticationFilter.setContinueChainBeforeSuccessfulAuthentication(false);
        //设置认证成功后的处理handler
		casAuthenticationFilter.setAuthenticationSuccessHandler(new SimpleUrlAuthenticationSuccessHandler("/demo/admin"));
		return casAuthenticationFilter;
	}
	
	/**
	 ** 认证的入口，即跳转至服务端的cas地址
	 * security框架整合cas认证的入口，也就是security不再走自己的认证入口，而是cas的，该对象就是cas的认证入口
	 * @return
	 * @date 2018年12月10日 上午11:41:54
	 */
	@Bean
	public CasAuthenticationEntryPoint casAuthenticationEntryPoint(ServiceProperties serviceProperties) {
		
		CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
		//security框架整合cas认证的入口，也就是security不再走自己的认证入口，而是cas的，该对象就是cas的认证入口
		casAuthenticationEntryPoint.setServiceProperties(serviceProperties);
		casAuthenticationEntryPoint.setLoginUrl(casServerProperties.getCasServerLoginUrl());
		return casAuthenticationEntryPoint;
	}
	
	
	/**
	 ** 配置TicketValidator在登录认证成功后验证ticket
	 * 该对象就是一个ticket校验器
	 * @return
	 * @date 2018年12月10日 上午11:47:36
	 */
	@Bean
	public Cas20ServiceTicketValidator cas20ServiceTicketValidator() {
		//需要设置cas server的前缀，也就是根路径
		Cas20ServiceTicketValidator cas20ServiceTicketValidator = new Cas20ServiceTicketValidator(casServerProperties.getCasServerUrlPrefix());
		return cas20ServiceTicketValidator;
	}
	
	/**
	 ** 该对象为cas校验对象，TicketValidator、AuthenticationUserDetailService属性必须设置;
     * serviceProperties属性主要应用于ticketValidator用于去cas服务端检验ticket
	 * @param userDetailsService 
	 * @param serviceProperties
	 * @param ticketValidator
	 * @return
	 * @date 2018年12月10日 下午3:45:13
	 */
	@Bean("casProvider")
	public CasAuthenticationProvider casAuthenticationProvider(AuthenticationUserDetailsService<CasAssertionAuthenticationToken> userDetailsService,
	    ServiceProperties serviceProperties, Cas20ServiceTicketValidator ticketValidator) {
	   
		CasAuthenticationProvider provider = new CasAuthenticationProvider();
	    provider.setKey("casProvider");
	    provider.setServiceProperties(serviceProperties);
	    provider.setTicketValidator(ticketValidator);
	    provider.setAuthenticationUserDetailsService(userDetailsService);
		return provider;
	}
	
	@Bean
	public LogoutFilter logoutFilter() {
		String logoutRedirectPath = casServerProperties.getCasServerLogoutUrl()+ "?service=" +casServerProperties.getServerName()+"/index";
        LogoutFilter logoutFilter = new LogoutFilter(logoutRedirectPath, new SecurityContextLogoutHandler());
        logoutFilter.setFilterProcessesUrl(casServerProperties.getCasServerLogoutUrl());
        return logoutFilter;
        
	}

}
