<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="shmwebfunc" uri="http://freeeed.org/tags/custom"%>

<script src="js/jquery-fu.js"></script>
<script src="js/case.js"></script>
<script>
  $(document).ready(function(){
    let input = document.getElementById("uploadfile");
    let fileName = document.getElementById("fileName")

    input.addEventListener("change", ()=>{
        let inputFile = document.querySelector("input[type=file]").files[0];
        fileName.innerText = inputFile.name;
    })
  });
  
</script>
<div class="reg-proj-head">
    Edit Case 
</div>

<div class="delimiter">
</div>



<div class="user-box">
  <form action="usercase.html" method="post">
  <c:if test="${usercase != null}">
      <input type="hidden" name="id" value="${usercase.id}"/>
  </c:if>
    <table class="case-form-table" cellpadding="10" cellspacing="0">
        <tr>
            <td class="table-label">Name<span class="required">*</span>:</td>
            <td><input type="text" class="form-control" name="name" value="${usercase.name}"/></td>
          </tr>
          <tr>
            <td>Description<span class="required">*</span>: </td>
            <td><textarea class="form-control" name="description">${usercase.description}</textarea></td>
          </tr>
          <tr>
            <td>Solr source<span class="required">*</span>: </td>
            <td>
                <select class="form-control" name="solrsource">
                    <c:forEach var="core" items="${cores}">
                        <option value="${core}" ${core == usercase.solrSourceCore ? 'selected' : ''}>${core}</option>
                    </c:forEach>
                </select>
            </td>
          </tr>
          <tr>
            <td colspan="2">
              <span class="explanation">(For experts! Use only when you know your SHMcloud project code)</span>
            </td>
          </tr>
          <tr>
            <td>Files location: </td>
            <td><input type="text" class="form-control" name="filesLocation" value="${usercase.filesLocation}"/></td>
          </tr>
          <tr>
            <td>Upload file: </td>
            <td>
              <div class="upload-file">
                <label for="uploadfile">
                  <i class="bi-file-earmark-zip-fill"></i>  Select File... 
                  <input id="uploadfile" type="file" name="file"/>
                </label>
              </div>
              <span id="fileName"></span>
              <input id="uploadfilebutton" type="button" class="action-button" value="Upload">
                <input id="uploadedFileId" type="hidden" name="filesLocationUp" value=""/>
            </td>
          </tr>
          <tr>
            <td colspan="2">
              <span class="explanation">Please upload the native.zip file, produced by your FreeEed player application. All other types of files will be rejected.
              Please visit <a href="https://github.com/markkerzner/FreeEedUI/wiki/Quick-Start" target="_blank">our Wiki</a> for more information.
              </span>
            </td>
          </tr>
          <tr>
            <td>&nbsp;</td>
            <td>
                <div class="uploadedFileBox" id="uploadedFileBoxId" style="display:${usercase.uploadedFile != null ? 'block' : 'none'}">Will use: <span id="uploadedFileNameId">${usercase.uploadedFile}</span></div>
            </td>
          </tr>
          <tr>
            <td colspan="2">
              <span class="explanation">(Fields marked with <span class="required">*</span> are mandatory)</span>
            </td>
          </tr>
          <tr>
            <td colspan="2" class="text-center">
              <input type="Submit" class="action-button" value="Save"/>
            </td>
          </tr>
          <tr>
            <td  colspan="2"  class="align-center">
                <c:if test="${fn:length(errors) > 0}">
                <div class="error">
                  <c:forEach var="err" items="${errors}">
                      ${err} <br/>
                  </c:forEach>
                </div>
              </c:if>
            </td>
          </tr>
    </table>
    <input type="hidden" name="action" value="save"/>
  </form>
</div>