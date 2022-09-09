package com.smart.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
//	method for adding common data to responce
	@ModelAttribute
	public void addCommonData(Model model, Principal principal)
	{
		String userName = principal.getName();
		System.out.println("USERNAME: "+userName);
		
		User user = userRepository.getUserByUserName(userName);
		System.out.println("User: "+user);
		
		model.addAttribute("user", user);		
	}

//	dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal)
	{
		model.addAttribute("title", "Dashboard - Smart Contact Manager");
		return "normal/user_dashboard";
	}
	
//	you profile
	@RequestMapping("/profile")
	public String about(Model model, Principal principal)
	{
		User user = userRepository.getUserByUserName(principal.getName());
		
		model.addAttribute("user", user);
		model.addAttribute("title", "About - Smart Contact Manager");
		return "normal/profile";
	}
//	18:12 53
//	open add form handler
	@GetMapping("/add_contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title", "Add Contact - Smart Contact Manager");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
}
