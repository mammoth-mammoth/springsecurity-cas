package com.hou.cassecurity.mapper;

import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import com.hou.cassecurity.pojo.User;


@Mapper
public interface UserMapper {
	
	@Select("SELECT id,user_name,pwd,avalibale,note FROM t_user WHERE user_name=#{username}")

	@Results({
	@Result(id=true, column="id", property="id"),
	@Result(column="user_name", property="username"),
	@Result(column="avalibale", property="avalibale"),
	@Result(column="pwd", property="pwd"),
	@Result(column="note", property="note"),
	@Result(column="user_name", property="roles",
	many=@Many(select="com.hou.cassecurity.mapper.RoleMapper.findByUserName"))
	})
	User findByUsername(String username);
	
}
