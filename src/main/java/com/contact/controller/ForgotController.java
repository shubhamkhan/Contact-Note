package com.contact.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.contact.dao.UserRepository;
import com.contact.entities.User;
import com.contact.service.EmailService;

@Controller
public class ForgotController {
	
	Random random = new Random(100000);
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bcrypt;
	
	// email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm()
	{
		return "forgot_email_form";
	}
	
	@PostMapping("/send_otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession session)
	{
		System.out.println("Email: "+email);
		
		// generating otp of 4 digit
		int otp = random.nextInt(999999);
		
		System.out.println("OTP: "+otp);
		
		String subject = "OTP From Contact Note";
		String message = "<div style='text-align: -webkit-center'><h1 style='border: 1px solid #2F944A; width: 180px; padding: 5px'> OTP: "+otp+"</h1></div>";
		String to = email;
		
		boolean flag = emailService.sendEmail(subject, message, to);
		
		if(flag)
		{
			session.setAttribute("otp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
		} else {
			session.setAttribute("message", "Check your email id!!");
			return "forgot_email_form";
		}
	}
	
	// verify otp
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") Integer otp, HttpSession session)
	{
		Integer sessionOtp = (int)session.getAttribute("otp");
		String email = (String)session.getAttribute("email");
		System.out.println("OTP>>"+sessionOtp+">>"+otp);
		if(sessionOtp.equals(otp))
		{
			User user = userRepository.getUserByUserName(email);
			
			if(user == null)
			{
				session.setAttribute( "message", "User does not exits with this email !!");
				return "forgot_email_form";				
			} else {
				return "password_change_form";
			}
		} else {
			session.setAttribute("message", "You have entered wrong OTP!!");
			return "verify_otp";
		}
	}
	
	// change password
	@PostMapping("/change_password")
	public String changePassword(@RequestParam("newPassword") String newPassword, HttpSession session)
	{
		String email = (String)session.getAttribute("email");
		User user = userRepository.getUserByUserName(email);
		user.setPassword(bcrypt.encode(newPassword));
		userRepository.save(user);
		session.setAttribute("message", "You have entered wrong OTP!!");
		return "redirect:/signin?change=Password change successfully..";
	}

}
