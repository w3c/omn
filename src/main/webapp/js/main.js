+function($) {
	'use strict';

	// UPLOAD CLASS DEFINITION
	// ======================

	var pastePanelButton = document.getElementById('paste-panel-button');
	var fileSelectButton = document.getElementById('file-select-button');
	var dropZone = document.getElementById('drop-zone');
	var uploadFileInput = document.getElementById('js-upload-files');
	var fileGlobal;

	pastePanelButton.onclick = function(e) {
		// hide previous alerts
		jQuery("#response-text").hide();
		jQuery("#submitted-text").hide();
		jQuery("#fail-text").hide();

		// show contents of paste panel under the panel
		var submittedText = jQuery("textarea#paste-panel").val();
		jQuery("#submitted-text").show();
		// jQuery("#submitted-text-content").html(submittedText.replace(/\r?\n/g, "<br />" ).replace(/&/g, "&amp;").replace(/>/g, "&gt;").replace(/</g, "&lt;").replace(/"/g, "&quot;"));
		jQuery("#submitted-text-content").text(submittedText);
		
		// make POST request to REST API
		$.post("http://demo.fiteagle.org:8080/omnlib/convert/request/ttl", {
			content : submittedText
		}, function(data, status, jqXHR) {
			jQuery("#response-text").show();
			jQuery("#response-text-content").text(jqXHR.responseText);
		}).fail(function(jqXHR, textStatus, errorThrown) {
			jQuery("#fail-text").show();
			jQuery("#fail-text-content").text(jqXHR.responseText);
		});

		// reset form
		clearAllContent();
		fileGlobal = null;
	}

	fileSelectButton.onclick = function(evt) {

		// if a file was chosen via the file input option
		if (uploadFileInput.files.length > 0) {
			// read file and show contents in message below
			fileGlobal = uploadFileInput.files[0];
		}

		if (fileGlobal) {
			// hide previous alerts
			jQuery("#response-text").hide();
			jQuery("#submitted-text").hide();
			jQuery("#fail-text").hide();

			var r = new FileReader();
			r.onload = function(e) {
				var submittedText = e.target.result;
				jQuery("#submitted-text").show();
				jQuery("#submitted-text-content").text(submittedText);

				// make POST request to REST API
				$
						.post(
								"http://demo.fiteagle.org:8080/omnlib/convert/request/ttl",
								{
									content : submittedText
								},
								function(data, status, jqXHR) {
									jQuery("#response-text").show();
									jQuery("#response-text-content").text(jqXHR.responseText);
								}).fail(
								function(jqXHR, textStatus, errorThrown) {
									jQuery("#fail-text").show();
									jQuery("#fail-text-content").text(
											jqXHR.responseText);
								});

			}
			r.readAsText(fileGlobal);

			// reset form
			clearAllContent();
			fileGlobal = null;
		}
	}

	dropZone.ondrop = function(e) {
		// if a file has not already been chosen via the file input
		if (uploadFileInput.files.length > 0) {
			return false;
		} else {

			// stop any further files being dragged into drag zone
			e.preventDefault();

			// color black
			this.className = 'upload-drop-zone complete';

			// stop further upload by the file input
			uploadFileInput.addEventListener("click", listener);

			// read file and show contents below panel
			var dt = e.dataTransfer;
			var files = dt.files;
			if (dt.files.length > 0) {
				var file = dt.files[0];

				if (file) {
					var fileName = file.name;
					fileGlobal = file;
					jQuery("#drop-zone").html(
							"<i class=\"fa fa-file-o\"></i> <b>".concat(
									fileName, "</b>"));
				} else {
					alert("Failed to load file");
				}
			}
		}
	}

	dropZone.ondragover = function() {
		// if a file has not been selected via the file input
		if (uploadFileInput.files.length == 0) {

			// change the styles to black
			this.className = 'upload-drop-zone drop';
		}
		return false;
	}

	dropZone.ondragleave = function() {
		// return style to grey if drop does not occur
		this.className = 'upload-drop-zone';
		return false;
	}

	uploadFileInput.onchange = function(evt) {
		// stop drag events
		$("drop-zone").off("ondrop");
	}

}(jQuery);

function listener(event) {
	event.preventDefault();
}

/**
 * reset the forms and text alerts
 */
function clearAllContent() {
	document.getElementById('paste-panel').value = "";
	document.getElementById('js-upload-files').value = "";
	document.getElementById('drop-zone').className = 'upload-drop-zone';
	jQuery("#drop-zone").text("Just drag and drop the file here");
	document.getElementById('js-upload-files').removeEventListener("click",
			listener);
}