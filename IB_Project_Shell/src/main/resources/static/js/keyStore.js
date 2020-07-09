function download() {
	
	var email = $('#emailInput').val();
	
	var downloadUrl = "api/users/downloadKeyStore/" + email;
	
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