<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="shmwebfunc" uri="http://freeeed.org/tags/custom"%>

<div class="reg-proj-head">
    Cases
</div>
<div class="delimiter">
</div>
<div class="listusers-newuser align-center">
    <a class="action-button" href="usercase.html">Create new case</a>
</div>

<div class="listusers-box scroll">
  <table class="table-bordered" cellpadding="0" cellspacing="0">
    <tr>
        <th class="listusers-header">Edit</th>
        <th class="listusers-header">Name</th>
        <th class="listusers-header">Description</th>
        <th class="listusers-header">Remove</th>
    </tr>
    <c:forEach var="c" items="${cases}">
        <tr>
            <td>
                <a href="usercase.html?action=edit&id=${c.id}"><i class="bi-pencil-fill" title="Edit"></i></a>
            </td>
            <td>${c.name}</td>
            <td>${c.description}</td>
            <td>
              <a href="usercase.html?action=delete&id=${c.id}"><i class="bi-trash-fill" title="Remove"></i></a>
            </td>
        </tr>    
    </c:forEach>
  </table>
</div>