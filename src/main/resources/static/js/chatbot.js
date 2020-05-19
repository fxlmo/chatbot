var context = "none";
var contextThreads = [];

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

function sendMessage(query) {

    var query = document.getElementById("textField").value;
    if (query==="") {return;};
    printMessage(query, "user");
    clearInputField();
    if (context == "multi") {
        // check if query is an integer
        var check = parseInt(query, 10);
        console.log(check);
        if (check <= contextThreads.length) {
            if (check === contextThreads.length) {
                chatBotAnswer("Ok. Can I help with anything else?")
                resetViaAjax();
            } else {
                printMessage("thinking", "typing");
                searchViaAjax(contextThreads[check]);
            }
            context = "none";
            contextThreads = [];
        } else {
            console.log("Nah something dodge has gone on here like");
            chatBotAnswer("Sorry, I didn't quite get that. Try again.")
        }
    } else {
        printMessage("thinking", "typing");
        searchViaAjax(query);
    }
}

//shows message and updates html
function printMessage(msg, user) {
  var newMsg = document.createElement('DIV');
    var bubClass; 
    if (user === "user") {
        newMsg.className = 'userMessage';
        bubClass = 'speech-bubble-user';
    } else if(user === "typing") {
        newMsg.className = 'typeMessage';
        bubClass = 'speech-bubble-chatbot';
        msg = "thinking...";
    } else if (user === "ans") {
        newMsg.className = "chatBotMessage";
        bubClass = 'speech-bubble-answer';
    } else {

        bubClass = 'speech-bubble-chatbot';
        var container = document.getElementById("chatBox");
        var elements = container.getElementsByClassName("typeMessage");

        while (elements[0]) {
            elements[0].parentNode.removeChild(elements[0]);
        }
        newMsg.className = 'chatBotMessage'; 
    }
    
    var child = document.createElement('p');
    child.className = bubClass;
    child.appendChild(document.createTextNode(msg));
    newMsg.appendChild(child);
    document.getElementById("chatText").appendChild(newMsg);
    gotoBottom();
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

function chatBotReturnedAnswer(msg) {
    var chatBotAnswer = msg;
    printMessage(chatBotAnswer, "ans");
    gotoBottom();
}
//Greet user
$(window).on('load',function () {
    context = "none";
    contextThreads = [];
    resetViaAjax();
    chatBotAnswer("Hi! What would you like to know?");
})

//when user asks something these two functions below are invoked
jQuery(document).ready(function($) {
    $("#search-form").submit(function(event) {
        //$.get("test", chatBotAnswer(data))
        // Prevent the form from submitting via the browser.
        var msg = document.getElementById("textField").value;
        var query = document.getElementById("textField").value;
        event.preventDefault();
        sendMessage(query);
    });
});
//posts to controller which then provides response using model
function searchViaAjax(msg) {
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

            if (response.type == "answer"){
                chatBotAnswer("Found these answers:");
                (response.content).forEach(element => {
                    chatBotReturnedAnswer(element);
                });
                chatBotAnswer("Do you need to know anything else?");
            } else if (response.type == "answers") {
                context = "multi";
                chatBotAnswer("I found information in these threads! Type the number that matches your desired thread:");
                var ind = 0;
                (response.content).forEach( element => {
                    chatBotAnswer(ind +") " +  element);
                    contextThreads.push(element);
                    ind++;
                })
                console.log("Threads = ", contextThreads);
                chatBotAnswer(ind + ") None of the above");
            } else if (response.type == "error" || response.type == "no-answer" ){
                chatBotAnswer("I could not find anything");
                chatBotAnswer("Can I help with anything else?");
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

function resetViaAjax() {

    $.ajax({
        type : "POST",
        contentType : "application/json",
        url : "/index/reset",
        dataType: "json",
        timeout : 100000,
        success : function(response) {
            console.log("Reset all contexts");
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
            //thread ID, subthread ID, body with radio buttons for if an entry is either a question or answer
            var ID = document.getElementById("ID-input").value;
            var subID = document.getElementById("subID-input").value;
            var date = document.getElementById("Date-input").value;
            var body = document.getElementById("Body-input").value;
            var qa;
            var q = $('#qa input:radio:checked').val();
            console.log(q, q == 'on');
            if (q == 'on'){
                qa = "q";
            } else { qa ="a";}

            console.log(qa);
            event.preventDefault();

            var threadDetails = {"ID":ID, "SubID":subID, "date":date, "body":body, "qa":qa}

            //combine all variables into Json

            addThreadViaAjax(threadDetails)

            $('#addThreadModal').modal('hide');

          }

        }, false);
      });
    }, false);
  })();

$('#addThreadModal').on('hidden.bs.modal', function () {
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
            $( "div.success" ).fadeIn( 300 );
        },
        error : function(e) {
            console.log("ERROR: ", e);
            $('#dbxDelete').empty();
            $('#btnGoDelete').prop('disabled', true);
            $('#dbxDelete').prop('disabled',true);
            $( "div.danger" ).fadeIn( 300 );
        },
        done : function(e) {
            console.log("DONE");
        }
    });
}

$('#btnDismissSuccess').on('click', function() {
    $( "div.success" ).fadeOut( 300 );
})
$('#btnDismissFail').on('click', function() {
    $( "div.danger" ).fadeOut( 300 );
})

//DELETE STUFF =====================================

$('#threadContainer').on('mouseenter', 'div.delThreadContainer', function() {
    this.classList.add('entered');
    this.classList.remove('left');
})

$('#threadContainer').on('click', 'div.delThreadContainer', function() {
    $(this).find(":checkbox").click();
})

$('#threadContainer').on('mouseleave', 'div.delThreadContainer', function() {
    this.classList.add('left');
    this.classList.remove('entered');
})

$('#btnDelThread').on('click', function(){
    if ($('#dbxDelete').prop('disabled')) {
        $('#dbxDelete').empty();
        $('#btnGoDelete').prop('disabled', true);
        $('#dbxDelete').prop('disabled',true);
        $('#dbxDelete').append('<option value="" disabled selected> Fetching threads... </option>');
        getThreadsViaAjax();
    }
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
            $('#threadContainer').empty();
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
            $('#dbxDelete').empty();
            $('#btnGoDelete').prop('disabled', true);
            $('#dbxDelete').prop('disabled',true);
            $( "div.danger" ).fadeIn( 300 );

        },
        done : function(e) {
            console.log("DONE");
        }
    });
}

var threads = [];
//Populate dropdown list
function getSubThreadsViaAjax(thread) {
    $.ajax({
        type : "POST",
        contentType : "application/json",
        url : "/admin/threads",
        data : JSON.stringify(thread),
        dataType: "json",
        timeout : 100000,
        success : function(response) {
            console.log("List of threads: (edited) ", response.content);
            $('#threadContainer').empty();
            threads = [];
            i = 0;
            (response.content).forEach(element => {
                threadid = JSON.stringify(element._id);
                date = element.date;
                body = element.body;
                subthreadid = element.subthread
                qa = element.qa
                if (qa == 'q') {
                    qa = "Question";
                } else {
                    qa = "Answer";
                }
                $('#threadContainer').append('<div class="divContainer"> <div class="container delThreadContainer"> <div class="child"> <input type="checkbox" id="' + i + '"> </div> <div class="child"> <p class="thread text-dark">' + date + " -- " +  subthreadid + " -- " + qa + '</p> <p class="thread text-dark">' + body + '</p> </div> </div> </div>')
                threads.push(element._id)
                i++;
            });
        },
        error : function(e) {
            console.log("ERROR: ", e);
            $('#dbxDelete').empty();
            $('#btnGoDelete').prop('disabled', true);
            $('#dbxDelete').prop('disabled',true);
            $( "div.danger" ).fadeIn( 300 );
        },
        done : function(e) {
            console.log("DONE");
        }
    });
}

$('#dbxDelete').on('change', function(e){
    $('#dbxDelete').prop('disabled', true);
    thread = $('#dbxDelete').find("option:selected").val();
    getSubThreadsViaAjax(thread)
    $('#dbxDelete').prop('disabled', false);
});

$('#btnDelete').on('click', function() {
    delThreads = [];
    $('#threadContainer input:checked').each(function() {
        id =$(this).attr('id');
        delThreads.push(threads[id]);
    })
    if (delThreads.length > 0) {
        deleteThreadViaAjax(delThreads);
    } else {
        // Handle no checkboxes clicked (feedback)
        console.log("No checkboxes clicked boss")
    }
})

function deleteThreadViaAjax(threads) {
    $.ajax({
        type : "DELETE",
        contentType : "application/json",
        url : "/admin/delete",
        data : JSON.stringify(threads),
        dataType: "json",
        timeout : 100000,
        success : function(response) {
            console.log(response);
            $('#deleteThreadModal').modal('hide');
            getThreadsViaAjax();
            $( "div.success" ).fadeIn( 300 );
        },
        error : function(e) {
            console.log("ERROR: ", e);
            $('#dbxDelete').empty();
            $('#btnGoDelete').prop('disabled', true);
            $('#dbxDelete').prop('disabled',true);
            $( "div.danger" ).fadeIn( 300 );
        },
        done : function(e) {
            console.log("DONE");
        }
    });
}


$('#btnAddThread').on('click', function() {
    console.log("worked")
})
//$('#addThreadModal').bind('load', function() { alert($('#addThreadModal1').hasClass('modal fade show')); });


$('#addThreadModal').on('shown.bs.modal', function () {
    var class1 = $(this).attr("class");
    if(class1 == "modal fade xshow"){
        console.log("done")
    }
    
})

