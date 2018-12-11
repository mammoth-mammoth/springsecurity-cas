# springsecurity-cas
springsecurity整合cas

springsecurity的springsecurityfilterchian中有一个cas的拦截器位置，因此可以通过它把cas整合进springsecurity中
cas负责认证，security通过userDetails负责加载权限，通过认证管理器赋予权限

所以在springsecurity的configeration没有太多变化，变化的地方如下：
第一：AuthenticationManager认证管理器，需要更换成casAuthenticationProvider对象。cas提供的认证管理器，把认证管理工作交给cas
第二：认证的entypiont认证入口点需要更换成cas提供的casAuthenticationFilter，该对象会拦截请求，进而调用cas提供的falter进行认证处理

如下配置security的configuration
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

casAuthenticationProvider实现了AuthenticationProvider接口，所以可以作为认证管理器提供者加载到security中


那么接下来就需要配置cas中的认证功能，也就是casAuthenticationFilter对象，由于casAuthenticationFilter对象涉及较多的依赖因此具体配置如下
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

那么接下来分析几个关键的对象
security中认证由AuthenticationManager管理器调用Authentication对象。AuthenticationManager有两种构建方式：1、由AuthenticationManagerBuilder对象构建，builder可以传入内存中用户信息，也可以传入两个sql语句，告诉builder去那里自己查找用户信息，或者实现userDetailsService接口中的loadByUsername方法，自定义提供一个userDetails对象，userDetails是个接口，一般提供其实现类user对象即可。2、AuthenticationManager也是一个接口，可以通过其实现类ProviderManager的构造方法，构建一个AuthenticationManager对象，该构造方法需要传入用户的认证信息也就是AuthenticationProvider对象集合。我们可以自定义获取用户信息的方式并且生成一个List<AuthenticationProvider>对象注入到ProviderManager中。
  
 cas中负责认证，认证的主要是认证跳转和票据验证
 casAuthenticationFilter就是其中负责跳转和票据验证的类，要完成认证，该类需要依赖认证管理器，因此需要把AuthenticationManager注入。要完成跳转还需要设置cas server的url信息等和本地服务器的信息，因此还需要一个ServiceProperties对象，该对象就是保存这些信息的。认证和票据验证的功能也是由其他类提供实现的，这个filter只是调度这些类
 
 认证功能，根据刚刚的分析我们知道security要实现认证，就必须用到AuthenticationManager认证管理器，而AuthenticationManager是个接口一般提供其实现类ProviderManager即可，构造ProviderManager对象需要传入AuthenticationProvider用户信息提供者对象的集合，一般情况下，我们都是通过数据库查询生成AuthenticationProvider这个对象。但是与cas整合过程中，用户信息由cas提供，因此cas提供了AuthenticationProvider接口的实现类CasAuthenticationProvider，因此我们在cas中需要配置CasAuthenticationProvider  cas认证信息提供者，并把他注入到AuthenticationManager认证管理器中。
 
票据验证功能：票据验证功能由cas20ServiceTicketValidator对象提供，因此我们还需要配置一个票据验证的对象，该对象实现了AbstractUrlBasedTicketValidator对象，因此具有票据验证的功能，还有一些对象也继承了它，也能实现票据验证，具体怎么选择，视情况而定

功能都全部准备好了，但是还需要一个入口，也就是与security的结合点，因此还需要配置一个AuthenticationEntryPoint认证接入点对象。在cas中提供一个它的实现类CasAuthenticationEntryPoint，因此还需要在configuration中注入这个对象




