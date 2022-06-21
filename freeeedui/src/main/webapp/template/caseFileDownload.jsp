<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:choose>
  <c:when test="${error}">
    Data do not exist! Please set the 'Files location' property in Case settings page.
  </c:when>
  <c:otherwise>
    Download should start soon...
  </c:otherwise>
</c:choose>

<div class="delimiter3">
</div>