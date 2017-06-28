package com.sino.scaffold.controller;

import java.util.List;

import org.nutz.dao.Cnd;
import org.nutz.lang.random.R;
import org.nutz.plugin.spring.boot.service.entity.PageredData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.sino.scaffold.bean.User;
import com.sino.scaffold.bean.User.Status;
import com.sino.scaffold.service.UserService;

/**
 * @author kerbores
 *
 */
@RestController
public class IndexController {

	@Autowired
	UserService userService;

	@GetMapping("/")
	public String index() {
		return "Hello sino soft!";
	}

	@GetMapping("list")
	public List<User> list() {
		return userService.queryAll();
	}

	@GetMapping("add")
	public User add() {
		User user = new User();
		user.setStatus(R.random(0, 2) == 1 ? Status.ENABLED : Status.DISABLED);
		user.setUserName(String.format("sino%d", R.random(0, 100000)));
		user.setEmail(String.format("test%d@sinosoft.com.cn", R.random(0, 100000)));
		userService.save(user);
		return user;
	}

	@GetMapping("delete/{id}")
	public int delete(@PathVariable("id") long id) {
		return userService.delete(id);
	}

	@GetMapping("page/{page}")
	public PageredData<User> pagerAll(@PathVariable("page") int page) {
		return userService.searchByPage(page, 10, null);
	}

	@GetMapping("search/{key}/{page}")
	public PageredData<User> pagerSearch(@PathVariable("key") String key, @PathVariable("page") int page) {
		return userService.searchByPage(page, 10, Cnd.where("userName", "like", String.format("%%%s%%", key)));
	}
}
