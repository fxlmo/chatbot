<<<<<<< HEAD
function isEnter(event) {
    if (event.keyCode === 13) {
        sendMessage();
    }
}
//document.getElementById("send").onclick = function() {
    //sendMessage();
//}

function sendMessage() {
    var query = document.getElementById("textField").value;
    if (query==="") {return;};
=======


function sendMessage(query) {

    if (query=="") {return;};
>>>>>>> a923ad209e6a95b44eb5fa2961d94b3d8ff6ff8a
    
    printMessage(query, "user");
    clearInputField();
    searchViaAjax(query);
}

//shows message and updates html
function printMessage(msg, user) {
  var newMsg = document.createElement('DIV');
     
    if (user === "user") {
        newMsg.className = 'userMessage';

    } else {
      newMsg.className = 'chatBotMessage';
    }
    
  newMsg.appendChild(document.createTextNode(msg));
  document.getElementById("chatText").appendChild(newMsg);
}

//clears text box
function clearInputField() {
    document.getElementById("textField").value = ""; document.getElementById("textField").focus();    
    gotoBottom();
}

//auto scrolls down when border is reached
function gotoBottom(){
    
    var scrollThisMuch =  -(document.getElementById("chatBox").clientHeight - document.getElementById("chatText").scrollHeight);
    
    document.getElementById("chatBox").scrollTop = scrollThisMuch;
}

//generate answer here using ajax request to controller
function chatBotAnswer(msg) {
    var chatBotAnswer = msg;
    printMessage(chatBotAnswer);
    gotoBottom();
}




jQuery(document).ready(function($) {
    $("#search-form").submit(function(event) {
        //$.get("test", chatBotAnswer(data))
        // Prevent the form from submitting via the browser.
<<<<<<< HEAD
        var msg = document.getElementById("textField").value;
        console.log(msg);
=======
        var query = document.getElementById("textField").value;
>>>>>>> a923ad209e6a95b44eb5fa2961d94b3d8ff6ff8a
        event.preventDefault();
        sendMessage(query);

    });
});

function searchViaAjax(msg) {
    console.log("search ajax: " + msg);
    var data = msg;

    $.ajax({
        type : "POST",
        contentType : "application/json",
        url : "/index",
        data : JSON.stringify(data),
        dataType: "json",
        timeout : 100000,
        success : function(response) {

            // TODO: string process JSON response
            //Setting datatype to json parses the response
            chatBotAnswer(response.field)

            //console.log("SUCCESS: ", data);
        },
        error : function(e) {
            console.log("ERROR: ", e);
            
        },
        done : function(e) {
            console.log("DONE");
        }
    });
}

