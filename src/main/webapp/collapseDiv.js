var coll = document.getElementsByClassName("collapseButton");
var contents = document.getElementsByClassName("collapseableContent");
var i;

for (i = 0; i < coll.length; i++) {
  console.log(i + "th round");
  var content = contents[i]
  coll[i].addEventListener("click", function() {
    this.classList.toggle("active");

    //get number (assigned in the same order)
    myNumber = this.id.replace('collapseButton_', '');
    var collapseableContent = document.getElementById("collapseableContent_" + myNumber);

    //hide/show the div and switch the button's description
    if (collapseableContent.style.maxHeight){
      collapseableContent.style.maxHeight = null;
      this.innerHTML="&plus;"
    } else {
      collapseableContent.style.maxHeight = collapseableContent.scrollHeight + "px";
      this.innerHTML="&minus;"
    }
  });


}

//initially click first element one time to open it
  coll[0].click();