

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

/*//when add button is pressed this function in invoked
    $("#addThreadSubmit").click(function(event) {
        console.log("got here")
        //thread ID, subthread ID, body with radio buttons for if an entry is either a question or answer
        var ID = document.getElementById("ID-input").value;
        var subID = document.getElementById("subID-input").value;
        var date = document.getElementById("Date-input").value;
        var body = document.getElementById("Body-input").value;
        console.log("here")
        var qa;
        var q = $('#q input:radio:checked').val();
        console.log("got")
        if (q){
            qa = "q";
        } else { qa ="a";}

        event.preventDefault();

        var threadDetails = {"ID":ID, "SubID":subID, "date":date, "body":body, "qa":qa}

        console.log(threadDetails);
        //combine all variables into Json

        addThreadViaAjax(threadDetails)

        $('#addThreadModal').modal('hide');



    });

*/

 // Example starter JavaScript for disabling form submissions if there are invalid fields
 (function() {
    'use strict';
    window.addEventListener('load', function() {
      // Fetch all the forms we want to apply custom Bootstrap validation styles to
      var forms = document.getElementsByClassName('needs-validation');
      // Loop over them and prevent submission
      var validation = Array.prototype.filter.call(forms, function(form) {
        form.addEventListener('submit', function(event) {
          if (form.checkValidity() === false) {
            event.preventDefault();
            event.stopPropagation();
          }
            form.classList.add('was-validated');
          if (form.checkValidity() === true) {
            form.classList.remove('was-validated')
            console.log("got here")
            //thread ID, subthread ID, body with radio buttons for if an entry is either a question or answer
            var ID = document.getElementById("ID-input").value;
            var subID = document.getElementById("subID-input").value;
            var date = document.getElementById("Date-input").value;
            var body = document.getElementById("Body-input").value;
            console.log("here")
            var qa;
            var q = $('#q input:radio:checked').val();
            console.log("got")
            if (q){
                qa = "q";
            } else { qa ="a";}

            event.preventDefault();

            var threadDetails = {"ID":ID, "SubID":subID, "date":date, "body":body, "qa":qa}

            console.log(threadDetails);
            //combine all variables into Json

            addThreadViaAjax(threadDetails)

            console.log("got here");
            $('#addThreadModal').modal('hide');

            $( "div.success" ).fadeIn( 300 ).delay( 1500 ).fadeOut( 400 );
          }

        }, false);
      });
    }, false);
  })();

$('#addThreadModal').on('hidden.bs.modal', function () {
    console.log("got to reset")
    $(this).find('form').trigger('reset');
})

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

//DELETE STUFF =====================================

$('#btnDelThread').on('click', function(){
    console.log("delete button clicked");
    $('#dbxDelete').empty();
    $('#btnGoDelete').prop('disabled', true);
    $('#dbxDelete').prop('disabled',true);
    $('#dbxDelete').append('<option value="" disabled selected> Fetching threads... </option>');
    getThreadsViaAjax();
})

//Populate dropdown list
function getThreadsViaAjax() {
    $.ajax({
        type : "POST",
        contentType : "application/json",
        url : "/admin/delete",
        timeout : 100000,
        success : function(response) {
            console.log("RESPONSE FROM GET THREAD (edited) ", response.content);
            $('#dbxDelete').empty();
            $('#dbxDelete').prop('disabled',false);
            $('#dbxDelete').append('<option value="" disabled selected> Choose a thread </option>');
            i = 0;
            (response.content).forEach(element => {
                $('#dbxDelete').append('<option value="' + element + '">' + element + '</option>');
                i++;
            });
        },
        error : function(e) {
            console.log("ERROR: ", e);

        },
        done : function(e) {
            console.log("DONE");
        }
    });
}

$('#dbxDelete').on('change', function(e){
    $('#btnGoDelete').prop('disabled', false);
    console.log("Your selection is in fact -- " + $('#dbxDelete').find("option:selected").val());
});

function deleteThreadViaAjax(thread) {

}




