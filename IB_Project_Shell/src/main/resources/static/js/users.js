function download(id) {
	var downloadUrl = "api/users/downloadCertificate/" + id;
	
	var xhr = new XMLHttpRequest();
	xhr.open('GET', downloadUrl, true);
	xhr.responseType = 'blob';

	xhr.onload = function(e) {
		if (this.status == 200) {
			var blob = this.response;
			console.log(blob);
			var a = document.createElement('a');
			var url = window.URL.createObjectURL(blob);
			a.href = url;
			a.download = xhr.getResponseHeader('filename');
			a.click();
			window.URL.revokeObjectURL(url);
		}
	};

	xhr.send();
}

function update(email) {
//	console.log(email);
	if(email == '')
		email = '@';
	var url = '/api/users/searchByEmail/' + email;
	$('table tbody').empty();
	
	$.ajax({
		type: 'GET',
		url: url,
		success: function(response) {
//			console.log(response);
			
			for (var i = 0; i < response.length; i++) {
				var newRow = '<tr><td>' + response[i].email + 
					'</td><td><button type="button" onclick="download(' + response[i].id + ')">Download</button></td></tr>';
				$('table tbody').append(newRow);
			}
				
		},
		error: function(response, exception) {
			console.log(exception);
		}
	});
	
};

$(document).ready(function() {
	
	update('');
	
	$('#emailInput').on('input', function() {
		update($('#emailInput').val());
	});
	
});