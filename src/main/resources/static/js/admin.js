$(document).ready(function() {
    $("#modalValidation").validator({
        fields: {
            idInput: {
                validators: {
                    notEmpty: {
                        message: "The thread ID is required"
                    }
                }
            },
            subID: {
                validators: {
                    notEmpty: {
                        message: "The subthread ID is required"
                    }
                }
            },
            body: {
                validators: {
                    notEmpty: {
                        message: "The body is required"
                    }
                }
            }
        },
    });


    // function validate() {
    //     console.log("Ayy we actually doing some validation today")
    //     var body=document.getElementById('body')=="";
    //     if (!body) {
    //         alert("Enter a body you dummy");
    //         return false;
    //     }
    // }

    //This needs to be on form submission
    function submission() {
        event.preventDefault();
        console.log("we validated and bout to submit g")
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
    }
});


$('#modalValidation').submit(function(){
    e.preventDefault();
    var ID = document.getElementById("ID-input").value;
    var subID = document.getElementById("subID-input").value;
    var date = document.getElementById("Date-input").value;
    var body = document.getElementById("Body-input").value;
    console.log("Body = " + body + " HAHA CUM");
    var qa;
    var q = $('#q input:radio:checked').val();
    if (q){
        qa = "q";
    } else { qa ="a";}

    var threadDetails = {"ID":ID, "SubID":subID, "date":date, "body":body, "qa":qa}

    console.log(threadDetails);
    //combine all variables into Json
    
    addThreadViaAjax(threadDetails)    
})

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


//when add button is pressed this function in invoked
// function toggleAlert(){
//     $(".alert").toggleClass('in out'); 
//     return false; // Keep close.bs.alert event from removing from DOM
// }
// //Post request to add a new thread


