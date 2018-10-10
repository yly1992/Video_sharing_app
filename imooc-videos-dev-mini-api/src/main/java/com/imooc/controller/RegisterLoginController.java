package com.imooc.controller;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.imooc.pojo.Users;
import com.imooc.pojo.vo.UsersVO;
import com.imooc.service.UserService;
import com.imooc.utils.IMoocJSONResult;
import com.imooc.utils.MD5Utils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(value="用户注册接口", tags= {"注册和登录的Controller"})
public class RegisterLoginController extends BasicController{
	
	@Autowired
	private UserService userService;
	
	@ApiOperation(value="用户注册", notes="用户注册")
	@PostMapping("/register")
	public IMoocJSONResult register(@RequestBody Users user) throws Exception {
		// 1. check null
		if(StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())) {
			return IMoocJSONResult.errorMap("User Name And Password Can Not Be Empty");
		}
		// 2. check username existed
		
		boolean usernameIsExist = userService.queryUsernameIsExist(user.getUsername());
		
		if (!usernameIsExist) {
			user.setNickname(user.getUsername());
			user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
			user.setFansCounts(0);
			user.setReceiveLikeCounts(0);
			user.setFollowCounts(0);
			userService.saveUser(user);
		} else {
			return IMoocJSONResult.errorMsg("用户名已经存在，请换一个再试");
		}
		user.setPassword("");
		
		UsersVO userVO = setUserRedisSessionToken(user);
		
		return IMoocJSONResult.ok(userVO);
	}
	
	@ApiOperation(value="用户登录", notes="用户登录的接口")
	@PostMapping("/login")
	public IMoocJSONResult login(@RequestBody Users user) throws Exception {
		String username = user.getUsername();
		String password = user.getPassword();
		
		if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
			return IMoocJSONResult.ok("用户名或密码不能为空...");
		}
		
		Users userResult = userService.queryUserForLogin(username, 
				MD5Utils.getMD5Str(user.getPassword()));
		if (userResult != null) {
			userResult.setPassword("");
			UsersVO userVO = setUserRedisSessionToken(userResult);
			return IMoocJSONResult.ok(userVO);
		} else {
			return IMoocJSONResult.errorMsg("用户名或密码不正确, 请重试...");
		}
	
	}
	
	@ApiOperation(value="用户注销", notes="用户注销的接口")
	@ApiImplicitParam(name="userId", value="用户id", required=true, 
						dataType="String", paramType="query")
	@PostMapping("/logout")
	public IMoocJSONResult logout(String userId) throws Exception {
		redis.del(USER_REDIS_SESSION + ":" + userId);
		return IMoocJSONResult.ok();
	}
	
	public UsersVO setUserRedisSessionToken(Users userModel) {
		String uniqueToken = UUID.randomUUID().toString();
		redis.set(USER_REDIS_SESSION + ":" + userModel.getId() , uniqueToken, 1000 * 60 * 30);
System.out.println("string " +  userModel.getId());	
		UsersVO userVO = new UsersVO();
		BeanUtils.copyProperties(userModel, userVO);
		userVO.setUserToken(uniqueToken);
		
		return userVO;
	}
	
	
}
