package com.hou.cassecurity.compent;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.hou.cassecurity.mapper.UserMapper;
import com.hou.cassecurity.pojo.Role;

@Service
public class CustomCasUserDetailsService implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken>{

	@Autowired
	private UserMapper userMapper;
	
	@Override
	public UserDetails loadUserDetails(CasAssertionAuthenticationToken token) throws UsernameNotFoundException {
		
		System.out.println("当前认证成功的用户名:"+token.getName());
		
		//加载用户权限信息，注意这里已经由cas完成认证，不再需要加载密码了
		
		com.hou.cassecurity.pojo.User user = userMapper.findByUsername(token.getName());
		List<Role> roles = user.getRoles();
		//准备权限集合
		List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
		
		for (Role role : roles) {
			grantedAuthorities.add(new SimpleGrantedAuthority(role.getRolename()));
		}
		//注意这里user中的密码可以不写，因为cas已经验证过了，现在只是需要把该用户的权限注入security中，但是也不能是null，null就会报错	    
	    return new User(token.getName(), "", grantedAuthorities);
	    
	}
	
	

}
