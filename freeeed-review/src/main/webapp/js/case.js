 $(document).ready(function() {
    $("body").bind({
        ajaxStart: function() { 
            $(this).addClass("loading"); 
        },
        ajaxStop: function() { 
            $(this).removeClass("loading"); 
        }    
    });
    
    $('#uploadfile').ajaxfileupload({
          'action': 'fileupload.html',
          'onComplete': function(json) {
            var res = JSON.parse(json);
            if (res.status == 'error') {
                alert("Error uploading file!");
            } else {
                $("#uploadedFileId").val(res.file);
                $("#uploadedFileNameId").text(res.fileShort);
                $("#uploadedFileBoxId").show();
            }
            
            $(".body").removeClass("loading");
          },
          'onStart': function() {
            $(".body").addClass("loading"); 
          },
          'onCancel': function() {
            $(".body").removeClass("loading");
          }
    })
});