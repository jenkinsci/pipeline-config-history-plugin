
var sideBySide = document.getElementById("content_sideBySideView");
var singleLine = document.getElementById("content_singleLineView");

//These do not work as needed:
//##style.display = "none"##, ##style.maxHeight = "0px"##, ##style.visibility = "hidden"##

//hide the side-by-side view at startup and show the other view.
//this must be done here since the collapseDiv script must be run before the sbs view is hidden.
hideElement(sideBySide);

function hideElement(element) {
    element.style.position = "absolute";
    //the element shouldn't get any higher than this...
    element.style.top = "-999999px";
    element.style.left = "-999999px";
}

function showElement(element) {
    element.style.position = "static";
}

function toggleSideBySide_SingleLine() {
    sideBySide = document.getElementById("content_sideBySideView");
    singleLine = document.getElementById("content_singleLineView");
    button = document.getElementById("toggleView");

    if (sideBySide.style.position === "absolute") {
        //means sideBySide is hidden
        hideElement(singleLine);
        showElement(sideBySide);
        button.innerHTML = "Show single-line view";
    } else {
        //means singleLine is hidden
        hideElement(sideBySide);
        showElement(singleLine);

        button.innerHTML = "Show side-by-side view";
    }
}