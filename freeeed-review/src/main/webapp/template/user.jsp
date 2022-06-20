<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="shmwebfunc" uri="http://freeeed.org/tags/custom"%>

<div class="reg-proj-head">
    Administrate user 
</div>

<div class="delimiter">
</div>

<div class="user-box">
  <form action="user.html" method="post">
    <table class="case-form-table" cellpadding="10" cellspacing="0">
      <tr>
            <td>Username<span class="required">*</span>:</td>
            <td>
                <c:choose>
                    <c:when test="${action == 'edit'}">
                        ${user.username}
                        <input type="hidden" class="form-control" name="username" value="${user.username}"/>
                    </c:when>
                    <c:otherwise>
                        <input type="text" class="form-control" name="username" value="${user.username}"/>
                        <input type="hidden" name="mode" value="new"/>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>Email<span class="required">*</span>: </td>
            <td><input type="text" class="form-control" name="email" value="${user.email}"/></td>
          </tr>
          <tr>
            <td>First Name<span class="required">*</span>: </td>
            <td><input type="text" class="form-control" name="firstName" value="${user.firstName}"/></td>
            <td>Last Name<span class="required">*</span>: </td>
            <td><input type="text" class="form-control" name="lastName" value="${user.lastName}"/></td>
          </tr>
          <tr>
            <td>Password<span class="required">*</span>: </td>
            <td><input type="password" class="form-control" name="password1" value=""/></td>
            <td>Confirm password<span class="required">*</span>: </td>
            <td><input type="password" class="form-control" name="password2" value=""/></td>
          </tr>
          <tr>
            <td colspan="4">
              <span class="explanation">(Please enter only if you wish to change your passwors)</span>
            </td>
          </tr>
         
          <tr>
            <td colspan="2">
              <input type="checkbox" name="PROCESS" <c:if test="${shmwebfunc:hasRight(user, 'PROCESS')}">checked</c:if>> <span>Process data</span></input>
              <br>
              <br>
              <input type="checkbox"  name="APP_CONFIG" <c:if test="${shmwebfunc:hasRight(user, 'APP_CONFIG')}">checked</c:if>> <span>Application configuration</span></input>
              <br>
              <br>
              <input type="checkbox"  name="USERS_ADMIN" <c:if test="${shmwebfunc:hasRight(user, 'USERS_ADMIN')}">checked</c:if>> <span>Users administration</span></input>
              <br>
              <br>
              <input type="checkbox" name="DOCUMENT_TAG" <c:if test="${shmwebfunc:hasRight(user, 'DOCUMENT_TAG')}">checked</c:if>> <span>Tag documents</span></input>
              <br>
              <br>
              <input type="checkbox" name="CASES" <c:if test="${shmwebfunc:hasRight(user, 'CASES')}">checked</c:if>> sk<span>Edit cases</span></input>
            </td>
          </tr>
          <tr>
            <td colspan="4">
              <span class="explanation">(Fields marked with <span class="required">*</span> are mandatory)</span>
            </td>
          </tr>
          <tr>
            <td colspan="4" class="text-center">
              <input type="Submit" class="action-button" value="Save"/>
            </td>
          </tr>
          <tr>
            <td  colspan="4"  class="align-center">
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