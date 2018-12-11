package com.hou.cassecurity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 ** cas服务的配置信息
 * @author houzhonglan
 * @date 2018年12月10日 上午11:11:46
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix="spring.cas")
public class CasServerProperties {

	/** cas服务地址  */
	private String casServerUrlPrefix;
	/** cas登陆地址  */
    private String casServerLoginUrl;
    /** cas登出地址  */
    private String casServerLogoutUrl;
    /** 本应用的地址，该地址需要能被cas服务器访问到  */
    private String serverName;
    /** 开始session */
    private boolean useSession = true;
    /** 重定向后验证  */
    private boolean redirectAfterValidation = true;
    /** 忽略的地址 */
    private String ignorePattern = "\\.(js|img|css)(\\?.*)?$";
    /** 忽略地址表达式的类型 */
    private String ignoreUrlPatternType = "REGEX";
    /** 编码方式 */
    private String encoding = "UTF-8";

}
