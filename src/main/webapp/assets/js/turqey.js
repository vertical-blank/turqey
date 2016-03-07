
function loadFile(file) {
  if (!/^image\/(png|jpeg|gif)$/.test(file.type)) return;

  var fr = new FileReader();
  fr.onload = function(evt) {
      var imageObj = new Image();
      imageObj.onload = function() {
        var canvas = document.getElementById('photo');
        var context = canvas.getContext('2d');
        canvas.width = imageObj.width;
        canvas.height = imageObj.height;
        context.drawImage(imageObj, 0, 0);
      }
      imageObj.src = fr.result;
    }
  fr.readAsDataURL(file);
}

