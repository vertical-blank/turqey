
function loadImageFile(file, onload) {
  if (!/^image\/(png|jpeg|gif)$/.test(file.type)) return;

  var fr = new FileReader();
  fr.onload = function(evt) {
      var imageObj = new Image();
      imageObj.onload = onload;
      imageObj.src = this.result;
    }
  fr.readAsDataURL(file);
}

function loadFile(file, onload) {
  var fr = new FileReader();
  fr.onload = function(evt) {
    onload(this.result);
  }
  fr.readAsDataURL(file);
}




