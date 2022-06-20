<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="shmwebfunc" uri="http://freeeed.org/tags/custom"%>

<div class="reg-proj-head">
  Users
</div>

<div class="delimiter">
</div>

<div class="listusers-newuser align-center">
    <a class="action-button" href="user.html">Create new user</a>
</div>

<div class="listusers-box .scroll">
    <table class="table-bordered" cellpadding="0" cellspacing="0">
        <tr>
        <th class="listusers-header">Edit</th>
        <th class="listusers-header">Username</th>
        <th class="listusers-header">First Name</th>
        <th class="listusers-header">Last Name</th>
        <th class="listusers-header">Email</th>
        <th class="listusers-header">Remove</th>
    </tr>
    <c:forEach var="user" items="${users}">
        <tr>
            <td>
                <a href="user.html?action=edit&username=${user.username}"><i class="bi-pencil-fill" title="Edit"></i></a>
            </td>
            <td>${user.username}</td>
            <td>${user.firstName}</td>
            <td>${user.lastName}</td>
            <td>${user.email}</td>
            <td>
                <c:if test="${user.username != 'admin'}">
                  <a href="user.html?action=delete&username=${user.username}"><i class="bi-trash-fill" title="Remove"></i></a>
                </c:if>
                &nbsp;
            </td>
        </tr>    
    </c:forEach>
  </table>
</div>