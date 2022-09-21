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