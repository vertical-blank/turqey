
function loadFile(file, onload) {
  if (!/^image\/(png|jpeg|gif)$/.test(file.type)) return;

  var fr = new FileReader();
  fr.onload = function(evt) {
      var imageObj = new Image();
      imageObj.onload = onload;
      imageObj.src = fr.result;
    }
  fr.readAsDataURL(file);
}

