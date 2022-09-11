package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	// method for adding common data to responce
	@ModelAttribute
	public void addCommonData(Model model, Principal principal)
	{
		String userName = principal.getName();
//		System.out.println("USERNAME: "+userName);
		
		User user = userRepository.getUserByUserName(userName);
//		System.out.println("User: "+user);
		
		model.addAttribute("user", user);		
	}

	// dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal)
	{
		model.addAttribute("title", "Dashboard - Smart Contact Manager");
		return "normal/user_dashboard";
	}
	
	// you profile
	@RequestMapping("/profile")
	public String about(Model model, Principal principal)
	{
		User user = userRepository.getUserByUserName(principal.getName());
		
		model.addAttribute("user", user);
		model.addAttribute("title", "About - Smart Contact Manager");
		return "normal/profile";
	}

	// open add form handler
	@GetMapping("/add_contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title", "Add Contact - Smart Contact Manager");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	// processing add contact form
	@PostMapping("/process_contact")
	public String processContact(
			@ModelAttribute Contact contact,
			@RequestParam("profieImage") MultipartFile file, 
			Principal principal,
			HttpSession session
	)
	{
		try {
			String userName = principal.getName();
			User user = userRepository.getUserByUserName(userName);
			
			// processing and uploading file
			if(file.isEmpty())
			{
				// if the file is empty then try our message
			} else {
				// file upload to the folder and update the name to contact
				contact.setImage(file.getOriginalFilename());
				
				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			contact.setUser(user);
			user.getContacts().add(contact);
			
			userRepository.save(user);
			
			System.out.println("Data: "+contact);
			
			session.setAttribute("message", new Message("Your contact is added !!", "success"));
			
		} catch (Exception e) {
			System.out.println("ERROR: "+e.getMessage());
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong !! Try again..", "danger"));
		}
		return "normal/add_contact_form";
	}
	
	// view contacts handler
	@GetMapping("/view_contacts")
	public String viewContacts(Model model, Principal principal)
	{
		model.addAttribute("title", "View Contact - Smart Contact Manager");
		
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		List<Contact> contacts = contactRepository.findContactsByUser(user.getId());
		
		model.addAttribute("contacts", contacts);
		return "normal/view_contacts";
	}
}
