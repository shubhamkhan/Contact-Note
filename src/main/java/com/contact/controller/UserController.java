package com.contact.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.contact.dao.ContactRepository;
import com.contact.dao.UserRepository;
import com.contact.entities.Contact;
import com.contact.entities.User;
import com.contact.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
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
		model.addAttribute("title", "Dashboard - Contact Note");
		return "normal/user_dashboard";
	}
	
	// you profile
	@GetMapping("/profile")
	public String about(Model model, Principal principal)
	{
		User user = userRepository.getUserByUserName(principal.getName());
		
		model.addAttribute("user", user);
		model.addAttribute("title", "About - Contact Note");
		return "normal/profile";
	}
	
	// open setting handler
	@GetMapping("/settings")
	public String openSettings(Model model)
	{
		model.addAttribute("title", "Settings - Contact Note");
		return "normal/settings";
	}
	
	// change password
	@PostMapping("/change_password")
	public String changePassword(
			@RequestParam("oldPassword") String oldPassword, 
			@RequestParam("newPassword") String newPassword,
			Principal principal,
			HttpSession session
	)
	{
		String userName = principal.getName();
		User currentUser = userRepository.getUserByUserName(userName);
		
		if(bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			currentUser.setPassword(bCryptPasswordEncoder.encode(newPassword));
			userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your password is successfully changed !!", "success"));
		} else {
			session.setAttribute("message", new Message("Please enter correct old password !! Try again..", "danger"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/index";
	}

	// open add form handler
	@GetMapping("/add_contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title", "Add Contact - Contact Note");
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
				System.out.println("File is empty");
				contact.setImage("contacts.png");
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
	@GetMapping("/view_contacts/{page}")
	public String viewContacts(@PathVariable("page") Integer page, Model model, Principal principal)
	{
		model.addAttribute("title", "View Contact - Contact Note");
		
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		
		PageRequest pageable = PageRequest.of(page, 2);
		
		Page<Contact> contacts = contactRepository.findContactsByUser(user.getId(),pageable);
		
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		
		return "normal/view_contacts";
	}

	// view single contact details
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(
			@PathVariable("cId") Integer cId,
			Model model,
			Principal principal
	)
	{
		Optional<Contact> contactOptional = contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		
		if(user.getId() == contact.getUser().getId())
		{
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		
		return "normal/contact_detail";
	}
	
	// delete contact handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(
			@PathVariable("cId") Integer cId, 
			Principal principal,
			Model model, 
			HttpSession session
	)
	{
		Contact contact = contactRepository.findById(cId).get();
		
		//contact.setUser(null);
		//contactRepository.delete(contact);
		
		User user = userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		userRepository.save(user);
		
		session.setAttribute("message", new Message("Contact delete succesfully...", "success"));
		return "redirect:/user/view_contacts/0";
	}
	
	// open update form handler
	@PostMapping("/update_contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId, Model model)
	{
		model.addAttribute("title", "Update Contact - Contact Note");
		
		Contact contact = contactRepository.findById(cId).get();
		model.addAttribute("contact",contact);
		
		return "normal/update_form";
	}
	
	// update contact handler
	@RequestMapping(value = "/process_update", method = RequestMethod.POST)
	public String updateHandler(
			@ModelAttribute Contact contact, 
			@RequestParam("profieImage") MultipartFile file, 
			Model model,
			HttpSession session,
			Principal principal
	)
	{
		try {
			
			Contact oldContact = contactRepository.findById(contact.getcId()).get();
			
			if(!file.isEmpty())
			{
				//
				File deleteFile = new ClassPathResource("static/img").getFile();
				File deleteImage = new File(deleteFile, oldContact.getImage());
				deleteImage.delete();
				
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
			} else {
				contact.setImage(oldContact.getImage());
			}
			
			User user = userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your contact is updated!!", "success"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
}
