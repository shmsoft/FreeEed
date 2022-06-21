<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="shmwebfunc" uri="http://freeeed.org/tags/custom"%>

<c:choose>
    <c:when test="${loggedVisitor == null}">
    
  <div class="login_back">
            <div class="col-12 text-center">

                <img src="./images/FreeEED-01.png" width="250"/>
                <br/>
                <div class="row justify-content-center">
                    <div class="login_content">
                        <hr>
                        <h4>Login</h4>
                        <div class="card-body">
                            <c:if test="${loginError}">
                                <div class="error">
                                    Invalid username or password!
                                </div>
                            </c:if>
                            <form action="login.html" method="post">
                                <div class="form-group">
                                    <input type="text" class="form-control login" value="admin" placeholder="Username" name="username"/>
                                </div>
                                <div class="form-group">
                                    <input type="password" class="form-control login" value="admin" placeholder="Password" name="password"/>
                                </div>
                                <br>
                                <div class="row">
                                    <div class="col-12">
                                        <input type="Submit" class="login_btn" value="Login"/>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
            <div class="login-footer">
                <b>
                FreeEed&trade; Review V9.4.0
                </b>
                <br>
                Document review part of the FreeEed&trade; eDiscovery
                <br/>
                Click <a href="https://github.com/shmsoft/FreeEed/wiki/Review" target="_blank">here</a> for
                documentation
            </div>
        </div>
    </c:when>
    <c:otherwise>
    
        <ul>
            <li>
                <i class="bi-arrow-right-circle-fill"></i> 
                <a class="menulink" href="login.html"> Home </a></li>
            <li> 
                <i class="bi-arrow-right-circle-fill"></i>
                <a class="menulink" href="search.html"> Search </a></li>

            <c:if test="${shmwebfunc:hasRight(loggedVisitor.user, 'CASES')}">
                <li> 
                    <i class="bi-arrow-right-circle-fill"></i>
                    <a class="menulink" href="listcases.html"> Cases </a></li>
            </c:if>

            <c:if test="${shmwebfunc:hasRight(loggedVisitor.user, 'APP_CONFIG')}">
                <li> 
                    <i class="bi-arrow-right-circle-fill"></i>
                    <a class="menulink" href="appsettings.html"> Application settings </a></li>
            </c:if>

            <c:if test="${shmwebfunc:hasRight(loggedVisitor.user, 'USERS_ADMIN')}">
                <li> 
                    <i class="bi-arrow-right-circle-fill"></i>
                    <a class="menulink" href="listusers.html"> User Administration </a></li>
            </c:if>

            <li> 
                <i class="bi-arrow-right-circle-fill"></i>
                <a class="menulink" href="logout.html"> Logout </a></li>
        </ul>
     
    </c:otherwise>
</c:choose>