package com.rad.server.access.controllers;

import java.util.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import com.rad.server.access.entities.*;
import com.rad.server.access.repositories.*;

/**
 * @author raz_o
 */
@Controller
public class NmsAccessControllers
{
	@Autowired
	private UserRepository		userRepository;
	
	@Autowired
	private RoleRepository		roleRepository;
	
	@Autowired
	private TenantRepository	tenantRepository;

	@GetMapping("/users")
	@ResponseBody
	public List<User> getUsers()
	{
		List<User> users = (List<User>) userRepository.findAll();
		System.out.println("getUsers: " + users);
		return users;
	}

	@PostMapping("/users")
	@ResponseBody
	public User addUser(@RequestBody User user)
	{
		System.out.println("addUser: " + user);
		userRepository.save(user);
		return user;
	}
	
	@GetMapping("/roles")
	@ResponseBody
	public List<Role> getRoles()
	{
		List<Role> roles = (List<Role>) roleRepository.findAll();
		System.out.println("getRoles: " + roles);
		return roles;
	}

	@PostMapping("/roles")
	@ResponseBody
	public Role addRole(@RequestBody Role role)
	{
		System.out.println("addRole: " + role);
		roleRepository.save(role);
		return role;
	}
	
	@GetMapping("/tenants")
	@ResponseBody
	public List<Tenant> getTenants()
	{
		List<Tenant> tenants = (List<Tenant>) tenantRepository.findAll();
		System.out.println("getTenants: " + tenants);
		return tenants;
	}

	@PostMapping("/tenants")
	@ResponseBody
	public Tenant addTenant(@RequestBody Tenant tenant)
	{
		System.out.println("addRole: " + tenant);
		tenantRepository.save(tenant);
		return tenant;
	}
}