const toggleSidebar = () => {
	if($(".sidebar").is(":visible")) {
		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "2%");
	} else {
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "20%");
	}
}

const search = () => {
	let query = $("#search-input").val();
	
	if(query == '') {
		$(".search_result").hide();
	} else {
		let url = `http://localhost:8282/search/${query}`;
		fetch(url).then((response) => {
			return response.json();
		}).then((data) => {
			let text = `<div class='list-group'>`
			data.forEach((contact) => {
				text += `<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'>${contact.name}</a>`;
			});
			text += `</div>`;
			
			$(".search_result").html(text);
			$(".search_result").show();
		});
	}
}

const payment = async() => {
	
	console.log("Payment Started...");
	
	let payAmount = document.getElementById("amount").value;
	if(!payAmount) 
	{
		console.log("Amount is required !!");
		swal("Failed !!", "Amount is required !!", "error");
		return;
	}
	
	const data = await fetch('/create_order', {
	  method: 'POST',
	  headers: {
	    'Content-Type': 'application/json'
	  },
	  body: JSON.stringify({amount: payAmount, info: "order_request"})
	});
	
	const response = await data.json();
	
	console.log(JSON.stringify(response));
	
	if(response.status == "created") {
		let options = {
			key: "rzp_test_********", // Enter the Key ID generated from the Dashboard
			amount: response.amount, // Amount is in currency subunits. Default currency is INR. Hence, 50000 refers to 50000 paise
			currency: response.currency,
			name: "Contact Manager",
			description: "Donation",
			image: "https://upload.wikimedia.org/wikipedia/commons/thumb/a/ab/Android_O_Preview_Logo.png/1024px-Android_O_Preview_Logo.png",
			order_id: response.id, //This is a sample Order ID. Pass the `id` obtained in the response of Step 1
			"handler": function(response) {
				console.log(response.razorpay_payment_id);
				console.log(response.razorpay_order_id);
				console.log(response.razorpay_signature);
				console.log("payment successful !!");
				
				updatePaymentOnServer(
					response.razorpay_payment_id,
					response.razorpay_order_id
				);
				
				swal("Good job!", "congrates !! payment sucessful !!", "success");	
			},
			prefill: {
		        name: "",
		        email: "",
		        contact: "",
		    },
		    notes: {
		        address: "Razorpay for Learn",
		    },
		    theme: {
		        color: "#3399cc",
		    }
		};
		
		let rzp = new Razorpay(options);
		
		rzp.on('payment.failed', function (response){
		    console.log(response.error.code);
		    console.log(response.error.description);
		    console.log(response.error.source);
		    console.log(response.error.step);
		    console.log(response.error.reason);
		    console.log(response.error.metadata.order_id);
		    console.log(response.error.metadata.payment_id);
		    swal("Failed !!", "oops !! payment failed !!", "error");
		});
		
		rzp.open();
	}
}

const updatePaymentOnServer = async(payment_id, order_id) => {
	
	const data = await fetch('/update_order', {
	  method: 'POST',
	  headers: {
	    'Content-Type': 'application/json'
	  },
	  body: JSON.stringify({payment_id: payment_id, order_id: order_id, status: "paid" })
	});
	
	const response = await data.json();
	
	console.log(JSON.stringify(response));
}