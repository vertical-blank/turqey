#!/usr/local/bin/node

var sass = require('node-sass');
sass.render({
  file: "materialize.scss",
  outputStyle: 'compressed',
}, function(err, result) {
  if(err){
    console.log("ERROR:");
    console.log(err);
  }
  else {
    console.log("RESULT:");
    console.log(result);
    var fs = require('fs');
    fs.writeFile('/home/yohei/turqey/src/main/webapp/assets/vender/css/materialize.css', result.css, function (err) {
      console.log(err);
    });
  }
});

