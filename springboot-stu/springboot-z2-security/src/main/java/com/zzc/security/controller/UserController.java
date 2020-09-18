package com.zzc.security.controller;

import com.zzc.security.vo.HttpResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 * @author Louis 
 * @date Oct 31, 2018
 */
@RestController
@RequestMapping("user")
public class UserController {

	
	@PreAuthorize("hasAuthority('sys:user:view')")
	@GetMapping(value="/findAll")
	public HttpResult findAll() {
		return HttpResult.ok("the findAll service is called success.");
	}
	
	@PreAuthorize("hasAuthority('sys:user:edit')")
	@GetMapping(value="/edit")
	public HttpResult edit() {
		return HttpResult.ok("the edit service is called success.");
	}
	
	@PreAuthorize("hasAuthority('sys:user:delete')")
	@GetMapping(value="/delete")
	public HttpResult delete() {
		return HttpResult.ok("the delete service is called success.");
	}

}
