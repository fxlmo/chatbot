function isEnter(event) {
    if (event.keyCode == 13) {
        sendMessage();
    }
}
//document.getElementById("send").onclick = function() {
    //sendMessage();
//}

function sendMessage() {
    var query = document.getElementById("textField").value;
    if (query=="") {return;};
    
    printMessage(query, "user");
    clearInputField();
    chatBotAnswer(query);
}

//shows message and updates html
function printMessage(msg, user) {
  var newMsg = document.createElement('DIV');
     
    if (user == "user") {
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
    
    var chatBotAnswer = "test";
    printMessage(chatBotAnswer);
    gotoBottom();
}


