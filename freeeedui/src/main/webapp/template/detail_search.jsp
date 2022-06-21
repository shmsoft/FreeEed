<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="reg-proj-head">
    Search 
</div>

<div class="search-box">
  <form method="post" action="search.html">
    <input type="hidden" name="action" value="search"/>
    <input type="text" name="query" value=""/><input type="Submit" name="Search" value="Search"/>
  </form>
</div>

<c:choose>
  <c:when test="${searchPerformed}">
    <c:choose>
      <c:when test="${result != null && result.documents != null && fn:length(result.documents) > 0}">
        <div class="result-box-header">
            Query: ${query} 
        </div>
        <div class="result-box-header">
            Results: (${result.totalSize}) 
        </div>
        
        <div class="delimiter2">
        </div>
        
        <div class="operations-box">
            <div class="operation-link">
                <a href="#" onclick="document.getElementById('tag-page').style.display='none';document.getElementById('tag-all').style.display='block';return false;">Tag All Results</a>
            </div>
            <div class="operation-link">
                <a href="#" onclick="document.getElementById('tag-all').style.display='none';document.getElementById('tag-page').style.display='block';return false;">Tag This page</a>
            </div>
            <div class="operation-link">
                <a href="#">Export All as native</a>
            </div>
            <div class="operation-link">
                <a href="#">Export All as image</a>
            </div>
        </div>
        
        <div id="tag-all" class="tag-box">
            <form action="tag.html" method="post">
                <input type="text" name="tag"/>
                <input type="Submit" value="Tag"/>
                <input type="button" value="Cancel" onclick="document.getElementById('tag-all').style.display='none';return false;"/>
                <input type="hidden" name="action" value="tag-all"/>
            </form>
        </div>
        
        <div id="tag-page" class="tag-box">
            <form action="tag.html" method="post">
                <input type="text" name="tag"/>
                <input type="Submit" value="Tag"/>
                <input type="button" value="Cancel" onclick="document.getElementById('tag-page').style.display='none';return false;"/>
                <input type="hidden" name="action" value="tag-page"/>
            </form>
        </div>
        
        <div class="delimiter">
        </div>
        <c:forEach var="doc" items="${result.documents}">
             <div class="result-box">
                <div class="operations-box">
                    <div class="operation-link">
                        <a href="#" onclick="document.getElementById('tag-doc-${doc.documentId}').style.display='block';return false;">Tag</a>
                    </div>
                    <div class="operation-link">
                        <a href="#">Export native</a>
                    </div>
                    <div class="operation-link">
                        <a href="#">Export image</a>
                    </div>
                </div>
                <div id="tag-doc-${doc.documentId}" class="tag-box">
                    <form action="tag.html" method="post">
                        <input type="text" name="tag"/>
                        <input type="Submit" value="Tag"/>
                        <input type="button" value="Cancel" onclick="document.getElementById('tag-doc-${doc.documentId}').style.display='none';return false;"/>
                        <input type="hidden" name="action" value="tag-doc"/>
                        <input type="hidden" name="docid" value="${doc.documentId}"/>
                        <input type="hidden" name="otags" value="${doc.tags}"/>
                    </form>
                </div>
                
                <table border = 0>
                    <c:forEach var="entry" items="${doc.entries}">
                        <tr>
                          <c:choose>
                            <c:when test="${entry.key != 'text'}">
                              <td class="result-box-key">${entry.key}</td>
                              <td>${entry.value}</td>
                            </c:when>
                            <c:otherwise>
                                <td colspan=2 class="result-box-text">
                                  <c:choose>
                                    <c:when test="${fn:length(entry.value) > 300}">
                                      <div id="textid-txt-${doc.documentId}" class="result-box-text-container">
                                        ${entry.value}
                                      </div>
                                      <div id="textid-coll-${doc.documentId}" class="result-box-text-collapse">
                                        The text has been truncated. Click <a href="#" onclick="document.getElementById('textid-coll-${doc.documentId}').style.display='none';document.getElementById('textid-txt-${doc.documentId}').className=' ';return false;">here</a> to see it complete.
                                      </div>
                                    </c:when>
                                    <c:otherwise>
                                      <div>
                                        ${entry.value}
                                      </div>
                                    </c:otherwise>
                                  </c:choose>
                                </td>
                            </c:otherwise>
                          </c:choose>
                        </tr>
                    </c:forEach>
                </table>
            </div>
            <div class="delimiter">
            </div>
        </c:forEach>
        <c:if test="${showPagination}">
          <div class="pagination">
                <div class="prev_page">
                    <c:if test="${showPrev}">
                        <a href="search.html?action=search&page=${currentPage - 1}"> Prev </a>
                    </c:if>
                </div>
                <div class="page">
                    ${currentPage}
                </div>
                <div class="next_page">
                    <c:if test="${showNext}">
                        <a href="search.html?action=search&page=${currentPage + 1}"> Next </a>
                    </c:if>
                </div>
           
         </div>
      </c:if>
      </c:when>
      <c:otherwise>
        <div class="no-result">
            No result
        </div>
        <div class="delimiter3">
        </div>
      </c:otherwise>
    </c:choose>
  </c:when>
  <c:otherwise>
    <div class="delimiter3">
    </div>
  </c:otherwise>
</c:choose>