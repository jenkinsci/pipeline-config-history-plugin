/*
    Doc:
        Your elements must be named collapseButton_<YOUR_ID> and collapsableContent_<YOUR_ID>
        For the use of single-line view and side-by-side view please start <YOUR_ID>s with  "SBS" or "SL"
        and use integer numbers starting at 0.
        If done so, the startup will uncollapse SBS0 and SL0 at page load.

        Also, the buttons need to be of class "collapseButton" and the contents of class "collapseableContent".

        Call initCollapseDiv() once to open the first contents.
*/

var coll = document.getElementsByClassName("collapseButton");
var contents = document.getElementsByClassName("collapseableContent");

function toggleCollapseContent(collapseButton, collapseableContent) {
    //hide/show the div and switch the button's description
        if (collapseableContent.style.maxHeight){
          collapseableContent.style.maxHeight = null;
          collapseButton.innerHTML="&#x25b6;"
        } else {
          collapseableContent.style.maxHeight = collapseableContent.scrollHeight + "px";
          collapseButton.innerHTML="&#x25bc;"
        }
}

function getTheOtherNumber(myNumber) {
    if (myNumber.startsWith("SBS")) {
        return "SL" + myNumber.substring(3);
    } else if (myNumber.startsWith("SL")) {
        return "SBS" + myNumber.substring(2);
    }
}

function getCollapseableContentByNumber(number) {
    return document.getElementById("collapseableContent_" + number);
}

function getCollapseButtonByNumber(number) {
    return document.getElementById("collapseButton_" + number);
}

function getNumberFromCollapseButton(collapseButton) {
    return collapseButton.id.replace('collapseButton_', '');
}


/*
##################################################################
###                          Startup                          ####
##################################################################
*/

function initCollapseDiv() {
    // Add the click listeners.
    for (i = 0; i < coll.length; i++) {
        coll[i].addEventListener("click", function() {
            this.classList.toggle("active");

            myNumber = getNumberFromCollapseButton(this);
            toggleCollapseContent(getCollapseButtonByNumber(myNumber), getCollapseableContentByNumber(myNumber));

            //TODO imlement: do the same for the other element
            if (myNumber.startsWith("SBS") || myNumber.startsWith("SL")) {
                //toggle the other element, too

                myOtherNumber = getTheOtherNumber(myNumber);
                console.log("this number: " + myNumber + ", other number: " + myOtherNumber);
                toggleCollapseContent(getCollapseButtonByNumber(myOtherNumber), getCollapseableContentByNumber(myOtherNumber))
            }
        });
    }

    // Initially click both first elements one time to open the contents.
    for (i = 0; i < coll.length; i++) {
        myNumber = coll[i].id.replace('collapseButton_', '');
        console.log("mynumber: " + myNumber);
        if (myNumber === "SBS0") {
        //if (myNumber === "SBS0" || myNumber === "SL0") {
            console.log("found case:: " + myNumber);
            coll[i].click();
        }
    }
}

