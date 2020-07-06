function register() {
	
	$('#errorInfo').hide();
	
	var email = $('#emailInput').val();
	var password = $('#passwordInput').val();
	
	console.log(email);
	console.log(password);
	
	$.ajax({
		type: 'POST',
		url: '/api/users',
		contentType: 'application/json',
		dataType: 'json',
		data: JSON.stringify(
			{
			'email': email,
			'password': password
			}
		),
		success: function(response) {
			window.location.replace('http://localhost:8443')
				
		},
		error: function(response, exception) {
			if (response.status == 409) {
				$('#errorInfo').text('Email already taken!');
				$('#errorInfo').show();
			}
		}
	})
	
	
	
};