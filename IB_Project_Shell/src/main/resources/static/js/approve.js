function activate(id) {
//	console.log(id);
	$.ajax({
		type: 'PUT',
		url: '/api/users/activate/' + id,
		success: function(response) {
			$('td').filter(function() {
				return $(this).text() == id;
			}).closest('tr').remove();
			
			if ($('table tbody tr').length < 1) {
				$('table').hide();
				$('#info').text('There is no users to activate');
				$('#info').show();
			}
		},
		error: function(response, exception) {
				console.log(exception);
		}
	});
};

$(document).ready(function() {
	
	$.ajax({
		type: 'GET',
		url: '/api/users/inactive',
		success: function(response) {
//			console.log(response);
			
			if (response.length < 1) {
				$('table').hide();
				$('#info').text('There is no users to activate');
				$('#info').show();
			}
			
			for (var i = 0; i < response.length; i++) {
				var newRow = '<tr><td>' + response[i].id + '</td><td>' + response[i].email +
					'</td><td><button type="button" onclick="activate(' + response[i].id + ')">Activate</button></td></tr>';
				$('table tbody').append(newRow);
			}
				
		},
		error: function(response, exception) {
			console.log(exception);
		}
	});
	
	
});