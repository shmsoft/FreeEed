<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="shmwebfunc" uri="http://freeeed.org/tags/custom"%>

<div class="reg-proj-head">
    Application Settings
</div>
<div class="delimiter">
</div>
<div class="user-box">
  <form action="appsettings.html" method="post">
    <table border="0" cellpadding="10" cellspacing="0">
        <tr>
            <td>Results per page<span class="required">*</span>:</td>
            <td><input type="text" class="form-control" name="results_per_page" value="${appSettings.resultsPerPage}"/></td>
         
            <td>Solr endpoint URL<span class="required">*</span>: </td>
            <td><input type="text" class="form-control" name="solr_endpoint" value="${appSettings.solrEndpoint}"/></td>
          </tr>
          <tr>
            <td colspan="4">
              <span class="explanation">(Fields marked with <span class="required">*</span> are mandatory)</span>
            </td>
          </tr>
          <tr>
            <td colspan="4" class="align-center">
              <input type="Submit" class="action-button"  value="Save"/>
            </td>
          </tr>
          <tr>
            <td colspan="4"  class="align-center"> 
              <c:if test="${fn:length(errors) > 0}">
                <div class="error">
                  <c:forEach var="err" items="${errors}">
                      ${err} <br/>
                  </c:forEach>
                </div>
              </c:if>
              <c:if test="${success}">
                  <div class="success">
                      Your data is stored successfully!
                  </div>
              </c:if>
            </td>
          </tr>
    </table>
    <input type="hidden" name="action" value="save"/>
  </form>
</div>
