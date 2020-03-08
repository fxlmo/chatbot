

function sendMessage(query) {

    var query = document.getElementById("textField").value;
    if (query==="") {return;};
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



//when user asks something these two functions below are invoked
jQuery(document).ready(function($) {
    $("#search-form").submit(function(event) {
        //$.get("test", chatBotAnswer(data))
        // Prevent the form from submitting via the browser.
        var msg = document.getElementById("textField").value;
        console.log(msg);
        var query = document.getElementById("textField").value;
        event.preventDefault();
        sendMessage(query);

    });
});
//posts to controller which then provides response using model
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
            /* types of response:
            error - signifies something went wrong: no answer, or question not found
            answer - answer found. will return the answers in a JSON object.
            */
            //Setting datatype to json parses the response
            console.log(response.content[1]);

            if (response.type == "answer"){
                chatBotAnswer("Found these answers:");
                (response.content).forEach(element => {
                    chatBotAnswer(element);
                });
            } else if (response.type == "error" || response.type == "no-answer" ){
                chatBotAnswer("I could not find anything");
            }


            

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

//when add button is pressed this function in invoked
$("#addThreadSubmit").click(function(event) {
    event.preventDefault();
    console.log("got here (button was done clicked")
    //thread ID, subthread ID, body with radio buttons for if an entry is either a question or answer
    var ID = document.getElementById("ID-input").value;
    var subID = document.getElementById("subID-input").value;
    var date = document.getElementById("Date-input").value;
    var body = document.getElementById("Body-input").value;
    var qa;
    var q = $('#q input:radio:checked').val();
    if (q){
        qa = "q";
    } else { qa ="a";}

    

    var threadDetails = {"ID":ID, "SubID":subID, "date":date, "body":body, "qa":qa}

    console.log(threadDetails);
    //combine all variables into Json

    
    var IDcheck = ID === "";
    var subIDCheck = subID === "";
    var dateCheck = true; //TODO change this to check if valid date
    var bodyCheck = body === "";

    if (!IDcheck && !subIDCheck && dateCheck && !bodyCheck) {
        addThreadViaAjax(threadDetails)    
        $('#addThreadModal').modal('hide');
    } else {
        //TODO send message to say they've entered something wrong
        console.log("Something wasn't entered")
        $('#bsalert').on('close.bs.alert', toggleAlert)
    }
});

$('#modalValidation').validate({
    rules: {
        idInput: "required",
        subID: "required",
        bodyInput: "required"
    },
    messages: {
        idInput: "Please provide the thread title",
        subID: "Please provide the subthread title",
        bodyInput: "Please provide the content of the thread"
    }
});

function toggleAlert(){
    $(".alert").toggleClass('in out'); 
    return false; // Keep close.bs.alert event from removing from DOM
}

//Post request to add a new thread
function addThreadViaAjax(thread) {

    

    $.ajax({
        type : "POST",
        contentType : "application/json",
        url : "/admin",
        data : JSON.stringify(thread),
        dataType: "json",
        timeout : 100000,
        success : function(response) {
            console.log(response);
            

        },
        error : function(e) {
            console.log("ERROR: ", e);

        },
        done : function(e) {
            console.log("DONE");
        }
    });
}


