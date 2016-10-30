function reSizeImage(screenWidth){

var images = document.getElementsByTagName('img');

if (images == null || images.length == 0){
       return null;
}

for(var i=0; i<images.length; i++)
images[i].style.width = screenWidth;


}