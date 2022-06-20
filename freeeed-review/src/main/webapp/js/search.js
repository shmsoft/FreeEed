var lastDocId = null;
var documentsMap = new Object();
var allTags = new Object();

function selectDocument(docId) {
    if (docId == lastDocId) {
        return;
    }
    
    $("#row-" + docId).addClass("result-list-row-selected");
    $("#doc-" + docId).show();
    
    if (lastDocId != null) {
        $("#row-" + lastDocId).removeClass("result-list-row-selected");
        $("#doc-" + lastDocId).hide();
    }
    
    lastDocId = docId;
}

function initPage(docId) {
    selectDocument(docId);
    initTags();
}

function newTagEnter(docId, e) {
    var charCode;
    
    if (e && e.which) {
        charCode = e.which;
    } else if(window.event) {
        e = window.event;
        charCode = e.keyCode;
    }

    if(charCode != 13) {
        return;
    }
    
    newTag(docId);
}

function newTag(docId) {
    var tag = $("#tag-doc-field-" + docId).val();
    if (tag == null || tag.length == 0) {
        return;
    }
    
    $.ajax({
      type: 'POST',
      url: 'tag.html',
      data: { action: 'newtag', docid: docId, tag: tag},
      success:function(data){
        if (data != 'SUCCESS') {
            return;
        }
        
        displayTag(docId, tag);
        
        $("#tag-doc-" + docId).hide();
        $("#tag-doc-field-" + docId).val('');
      },
      error:function(){
        alert("Technical error, try that again in a few moments!");
      }
    });
}

function displayTag(docId, tag) {
    addCaseTag(tag);
    
    if (documentsMap[docId][tag] != null) {
        return;
    } else {
        documentsMap[docId][tag] = 1;
    }
    
    var docIdParam = '"' + docId + '"';
    var tagParam = '"' + tag + '"';
    $("#tags-table-" + docId).append("<tr class='document-tags-row'>" +
                              "<td><div class='document-tags-tag'>" + tag + "</div></td>" +
                              "<td><a href='#' onclick='deleteTag(" + docIdParam + ", this, " + tagParam + ")'><img src='images/delete.gif'/></a></td>" +
                           "</tr>");
    var total = parseInt($("#tags-total-" + docId).html()) + 1;
    $("#tags-total-" + docId).html(total);
}

function deleteTag(docId, el, tag) {
    $.ajax({
      type: 'POST',
      url: 'tag.html',
      data: { action: 'deletetag', docid: docId, tag: tag},
      success:function(data){
        $(el).parent().parent().remove();
        var total = parseInt($("#tags-total-" + docId).html()) - 1;
        $("#tags-total-" + docId).html(total);
        if (total == 0) {
            $("#tags-box-" + docId).hide();
        }
      },
      error:function(){
        alert("Technical error, try that again in a few moments!");
      }
    });
}

function search() {
    var queryStr = $("#search-query").val();

    $.ajax({
      type: 'POST',
      url: 'dosearch.html',
      data: { action: 'search', query: queryStr},
      success:function(data){
        lastDocId = null;
        
        $("#result-ajax").html(data);
        
        var solrId = $("#solrid").val();
        if (solrId != null) {
            initPage(solrId);
        }
        
        $("#search-query").val('');
      },
      error:function(){
        alert("Technical error, try that again in a few moments!");
      }
    });
}

function addTagToSearch(tag) {
    $.ajax({
      type: 'POST',
      url: 'dosearch.html',
      data: { action: 'tagsearch', tag: tag},
      success:function(data){
        lastDocId = null;
        
        $("#result-ajax").html(data);
        
        var solrId = $("#solrid").val();
        if (solrId != null) {
            initPage(solrId);
        }
      },
      error:function(){
        alert("Technical error, try that again in a few moments!");
      }
    });
}

function changePage(page) {
    $.ajax({
      type: 'POST',
      url: 'dosearch.html',
      data: { action: 'changepage', page: page},
      success:function(data){
        lastDocId = null;
      
        $("#result-ajax").html(data);
        
        var solrId = $("#solrid").val();
        if (solrId != null) {
            initPage(solrId);
        }
      },
      error:function(){
        alert("Technical error, try that again in a few moments!");
      }
    });
}

function removeSearch(id) {
    $.ajax({
      type: 'POST',
      url: 'dosearch.html',
      data: { action: 'remove', id: id},
      success:function(data){
        lastDocId = null;
      
        $("#result-ajax").html(data);
        
        var solrId = $("#solrid").val();
        if (solrId != null) {
            initPage(solrId);
        }
      },
      error:function(){
        alert("Technical error, try that again in a few moments!");
      }
    });
}

function removeAllSearch() {
    $.ajax({
      type: 'POST',
      url: 'dosearch.html',
      data: { action: 'removeall'},
      success:function(data){
        lastDocId = null;
      
        $("#result-ajax").html(data);
        
        var solrId = $("#solrid").val();
        if (solrId != null) {
            initPage(solrId);
        }
      },
      error:function(){
        alert("Technical error, try that again in a few moments!");
      }
    });
}

function initTags() {
   $(".document-tags-table").hide();
   $(".document-tags-label").click(function() {
       $(this).next(".document-tags-table").slideToggle(200);
   });
   
   $(".solrid").each(function(index) {
       var docId = $(this).val();
       documentsMap[docId] = new Object();
       $(".doc-tag-" + docId).each(function(index) {
           var tag = $(this).val();
           documentsMap[docId][tag] = 1;
       });
   });
   
   $(".tag-doc-field-cl").autocomplete({source : "tagauto.html"});
   $("#tag-all-text").autocomplete({source : "tagauto.html"});
   $("#tag-page-text").autocomplete({source : "tagauto.html"});
}

function tagAllBox() {
    $("#tag-all").slideToggle(200);
    $("#tag-page").hide();
}

function tagPageBox() {
    $("#tag-page").slideToggle(200);
    $("#tag-all").hide();
}

function newAllTagEnter(callFunc, e) {
    var charCode;
    
    if (e && e.which) {
        charCode = e.which;
    } else if(window.event) {
        e = window.event;
        charCode = e.keyCode;
    }

    if(charCode != 13) {
        return;
    }
    
    callFunc();
}

function tagAll() {
    tagDocuments("tag-all-text", "tag-all", "tagall");
}

function tagPage() {
    tagDocuments("tag-page-text", "tag-page", "tagpage");
}

function tagDocuments(textId, boxId, action) {
    var tag = $("#" + textId).val();
    if (tag == null || tag.length == 0) {
        return;
    }
    
    $.ajax({
      type: 'POST',
      url: 'tag.html',
      data: { action: action, tag: tag},
      success:function(data) {
        if (data != 'SUCCESS') {
            return;
        }
        
        for (var docId in documentsMap) {
            displayTag(docId, tag);
        }
        
        $("#" + boxId).hide();
        $("#" + textId).val('');
      },
      error:function(){
        alert("Technical error, try that again in a few moments!");
      }
    });
}

function addCaseTag(tag) {
    if (allTags[tag] == null) {
        allTags[tag] = 1;
        appendCaseTag(tag);
    }
}

function appendCaseTag(tag) {
    $(".case-tags-box-body").append("<div id='" + tag + "' class='case-tags-box-row' onclick='addTagToSearch(\"" + tag + "\")'>" + tag + "</div>");
}

$(document).ready(function() {
    $("body").bind({
        ajaxStart: function() { 
            $(this).addClass("loading"); 
        },
        ajaxStop: function() { 
            $(this).removeClass("loading"); 
        }    
    });
    
    $('#search-query').keypress(function(e) {
        if (e.keyCode == 13) {
            search();
        }
    });
    
    for (var t in allTags) {
        appendCaseTag(t);
    }
    
    $("body").on("click", ".html-preview", function () {
    	var docId = $(this).attr("data");
    	var uId = $(this).attr("uid");
    	
    	$.ajax({
	      type: 'GET',
	      url: 'filedownload.html',
	      data: { action: 'exportHtml', docPath : docId, uniqueId : uId},
	      success:function(data) {
	        $("#html_preview_modal_content").html(data);
	        $('#html_preview_modal').modal('show');
	      },
	      error:function(){
	        alert("Technical error, try that again in a few moments!");
	      }
    	});
    });
});