package com.hou.cassecurity.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebListener;

import org.springframework.context.annotation.Configuration;

/**
 ** @WebFilter(urlPatterns="" , filterName="casRedirectFilter") 参数说明：urlPatterns：该filter拦截的路径，filterName：拦截器的名称
 * @author houzhonglan
 * @date 2018年12月11日 上午11:15:30
 */
//@Configuration
//@WebFilter(urlPatterns="" , filterName="casRedirectFilter")
//public class HttpParamsFilter implements Filter {
//
//	@Override
//	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
//			throws IOException, ServletException {
//		// TODO 自动生成的方法存根
//		
//	}
//
//}
