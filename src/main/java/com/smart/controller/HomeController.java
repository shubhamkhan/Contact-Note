package com.smart.controller;

import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.smart.dao.PaymentOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.PaymentOrder;
import com.smart.entities.User;
import com.smart.helper.Message;


@Controller
public class HomeController {
	
	Random random = new Random(100000);
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PaymentOrderRepository paymentOrderRepository;

	@RequestMapping("/")
	public String home(Model model)
	{
		model.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}
	
	@RequestMapping("/signup")
	public String signup(Model model)
	{
		model.addAttribute("title", "Register - Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}
	
    //handler for register user
	@RequestMapping(value = "/do_register", method = RequestMethod.POST)
	public String registerUser(
			@Valid 
			@ModelAttribute("user") User user,
			BindingResult resultone,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, 
			Model model,
			HttpSession session
			)
	{
		try {
			if(!agreement) {
				System.out.println("You have not agreed the terms and conditions");
				throw new Exception("You have not agreed the terms and conditions");
			}
			
			if(resultone.hasErrors()) {
				System.out.println("ERROR "+resultone.toString());
				model.addAttribute("user", user);
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			User result = this.userRepository.save(user);
			
			model.addAttribute("user", new User());
			session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
			
			System.out.println("Agreement"+agreement);
			System.out.println("User"+result);
			
			return "signup";
			
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Somethimg went wrong !! "+e.getMessage(), "alert-danger"));
			return "signup";
		}
	}
	
    // handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model model)
	{
		model.addAttribute("title", "Login - Smart Contact Manager");
		return "login";
	}
	
	// handler for donation from
	@GetMapping("/donate")
	public String donateUs(Model model)
	{
		model.addAttribute("title", "Donate - Smart Contact Manager");
		return "donate";
	}
	
	// creating order for payment
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data) throws RazorpayException
	{
		System.out.println(data);
		
		int amount = Integer.parseInt(data.get("amount").toString());
		int txn = random.nextInt(999999);
		Order order = null;
		try {
			RazorpayClient razorpayClient = new RazorpayClient("rzp_test_********", "*********");
			
			JSONObject options = new JSONObject();
			options.put("amount", amount*100); // amount in the smallest currency unit
			options.put("currency", "INR");
			options.put("receipt", "txn_"+txn);
			
			// creating new order
			order = razorpayClient.orders.create(options);
			System.out.println(order);
			
			// save the order in database
			PaymentOrder paymentOrder = new PaymentOrder();
			
			paymentOrder.setOrderId(order.get("id"));
			paymentOrder.setAmount(order.get("amount").toString());
			paymentOrder.setReceipt(order.get("receipt"));
			paymentOrder.setStatus(order.get("status"));
			paymentOrder.setCreatedAt(order.get("created_at").toString());
			
			paymentOrderRepository.save(paymentOrder);
					
		} catch (RazorpayException e) {
		  // Handle Exception
		  System.out.println(e.getMessage());
		}
		
		return order.toString();
	}
	
	@PostMapping("/update_order")
	@ResponseBody
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data) throws RazorpayException
	{		
		try {
			System.out.println(data);
			RazorpayClient razorpayClient = new RazorpayClient("rzp_test_********", "*********");
			Payment payment = razorpayClient.payments.fetch(data.get("payment_id").toString());
			
			PaymentOrder paymentOrder = paymentOrderRepository.findByOrderId(data.get("order_id").toString());
			
			paymentOrder.setPaymentId(data.get("payment_id").toString());
			paymentOrder.setStatus(data.get("status").toString());
			
			paymentOrder.setEmailId(payment.get("email"));
			paymentOrder.setContactNo(payment.get("contact"));
			paymentOrder.setPayMethod(payment.get("method"));
			
			paymentOrderRepository.save(paymentOrder);
		} catch (RazorpayException e) {
		  // Handle Exception
		  System.out.println(e.getMessage());
		}
		return ResponseEntity.ok(Map.of("message", "Updated"));
	}
}
