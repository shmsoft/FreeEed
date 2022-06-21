<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

 
    
    <div class="your-search">
        <div class="your-search-label">
            Your search  <c:if test="${fn:length(searched) > 0}"> <a href="#" onclick="removeAllSearch()">Remove All</a></c:if>
        </div>
        <div class="your-search-box">
            <c:forEach var="search" items="${searched}">
                <div class="your-search-box-search">
                    <div class="your-search-box-name">
                        <div class="your-search-box-name-inner ${search.highlight}" title="${search.name}">
                            ${search.name}
                        </div>
                        <div class="your-search-box-delete" title="Remove from search">
                            <i class="bi-trash-fill" title="Remove" onclick="removeSearch(${search.id - 1})"></i>
                        </div>
                    </div>
                </div>
            </c:forEach>
            <div class="spacer" style="clear: both;"></div>
        </div>
        
       
    </div>

<c:choose>
  <c:when test="${result != null && result.documents != null && fn:length(result.documents) > 0}">

  
    <div class="your-search">
        <div class="your-search-label">
            Results: (${result.totalSize}) 
        </div>
        
        <div class="your-search-box">
            <div class="operations-box">
                <div class="operation-link">
                    <a href="javascript:;" class="operation-link-text action-button" onclick="tagAllBox()">Tag All Results</a>
                </div>
                <div class="operation-link">
                    <a href="javascript:;" class="operation-link-text action-button" onclick="tagPageBox()">Tag This page</a>
                </div>
                <div class="operation-link">
                    <a class="operation-link-text action-button" href="filedownload.html?action=exportImageAll">Export as images</a>
                </div>
                <div class="operation-link">
                    <a class="operation-link-text action-button" href="filedownload.html?action=exportNativeAll">Export as natives</a>
                </div>
                <div class="tags-remove-box">
                </div>
            </div>
            
            <div id="tag-all" class="tag-box">
                Tag All Results:
                <input id="tag-all-text" class="tag-doc-field-cl form-control" type="text" name="tag" onkeypress="newAllTagEnter(tagAll, event)"/>
                <input type="button" class="action-button" value="Tag" onclick="tagAll()"/>
                <input type="button" class="action-button" value="Cancel" onclick="document.getElementById('tag-all').style.display='none';return false;"/>
            </div>
            
            <div id="tag-page" class="tag-box">
                Tag This Page:
                <input id="tag-page-text" class="tag-doc-field-cl form-control" type="text" name="tag" onkeypress="newAllTagEnter(tagPage, event)"/>
                <input type="button" class="action-button" value="Tag" onclick="tagPage()"/>
                <input type="button" class="action-button" value="Cancel" onclick="document.getElementById('tag-page').style.display='none';return false;"/>
                </form>
            </div>
        </div>
    </div>
    
    <div class="delimiter2">
    </div>
    
    <table border="0" cellpadding="0" cellspacing="0" class="result-search-table">
    <tr><td valign="top" class="result-search-table-list">
    
    <div class="result-list scroll">
        <table border="0" cellpadding="5" cellspacing="0" class="table-bordered">
            <tr>
                <th>Id</th>
                <th>From/Creator</th>
                <th>Subject/Filename</th>
                <th>Date</th>
            </tr>
            
            <c:forEach var="doc" items="${result.documents}">
            <tr id="row-${doc.documentId}" class="result-list-row" onclick="selectDocument('${doc.documentId}')">
                <td><div class="result-list-id">${doc.documentId}</div></td>
                <td><div class="result-list-from">${doc.from}</div></td>
                <td><div class="result-list-subject">${doc.subject}</div></td>
                <td class="table-last-row"><div class="result-list-date">${doc.date}</div></td>
            </tr>
            </c:forEach>
        </table>
    </div>
    <c:if test="${showPagination}">
        <div class="pagination">
         
                <div class="prev_page" style="pointer-events: <c:if test="${!showPrev}">none</c:if>" onclick="changePage(${currentPage - 1})">
                    
                        <i title="Prev" class="bi-chevron-left"></i>
                    
                </div>
                <div class="page">
                    ${currentPage}
                </div>
                <div class="next_page" style="pointer-events: <c:if test="${!showNext}">none</c:if>" onclick="changePage(${currentPage + 1})">
                    
                        <i title="Next" class="bi-chevron-right"></i>
                    
                </div>
            
        </div>
        </c:if>
    </td>
    <td valign="top" class="result-search-table-details">
    
    <div class="result-details">
    
    <c:forEach var="doc" items="${result.documents}">
         <input type="hidden" id="solrid" class="solrid" value="${doc.documentId}"/>
         <div class="result-box" id="doc-${doc.documentId}" style="display:none">

             <div class="document-tags">
               <div class="document-tags-label">
                 Tags (<span id="tags-total-${doc.documentId}">${fn:length(doc.tags)}</span>)
               </div>
               <div class="document-tags-table" id="tags-box-${doc.documentId}">
                   <table id="tags-table-${doc.documentId}" border="0" cellpadding="0" cellspacing="0">
                       <c:forEach var="tag" items="${doc.tags}">
                           <input type="hidden" class="doc-tag-${doc.documentId}" value="${tag.value}"/>
                           <tr class="document-tags-row">
                              <td><div class="document-tags-tag">${tag.value}</div></td>
                              <td><a href="#" onclick="deleteTag('${doc.documentId}', this, '${tag.name}')"><img src="images/delete.gif"/></a></td>
                           </tr>
                       </c:forEach>
                   </table>
               </div>
            </div> 

            <div class="operations-box">
                <div class="operation-link">
                    <a class="operation-link-text html-preview action-button" data="${doc.documentPath}" uid="${doc.uniqueId}">Preview</a>
                </div>
                <div class="operation-link">
                    <a href="javascript:;" class="operation-link-text action-button" onclick="$('#tag-doc-${doc.documentId}').slideToggle(200);">Tag</a>
                </div>
                <div class="operation-link">
                    <a class="operation-link-text action-button" href="filedownload.html?action=exportNative&docPath=${doc.documentPath}&uniqueId=${doc.uniqueId}">Export native</a>
                </div>
                <div class="operation-link">
                    <a class="operation-link-text action-button" href="filedownload.html?action=exportImage&docPath=${doc.documentPath}&uniqueId=${doc.uniqueId}">Export image</a>
                </div>
            </div>
            <div id="tag-doc-${doc.documentId}" class="tag-box details">
                <input id="tag-doc-field-${doc.documentId}" class="tag-doc-field-cl form-control" type="text" name="tag" onkeypress="newTagEnter('${doc.documentId}', event)"/>
                <input type="button" class="action-button" value="Tag" onclick="newTag('${doc.documentId}')"/>
                <input type="button" class="action-button" value="Cancel" onclick="document.getElementById('tag-doc-${doc.documentId}').style.display='none';return false;"/>
            </div>
            <div class="result-div scroll">
            <table border = 0>
                <c:forEach var="entry" items="${doc.entries}">
                    <tr>
                      <c:choose>
                        <c:when test="${entry.key != 'text'}">
                          <td class="result-box-key">${entry.key}</td>
                          <td><div class="result-box-value">${entry.value}</div></td>
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
        </div>
    </c:forEach>
    </div>
    
    </td></tr>
    </table>

  </c:when>
  <c:otherwise>
    <div class="delimiter2">
    </div>
    
    <div class="no-result">
        No result
    </div>
    <div class="delimiter3">
    </div>
  </c:otherwise>
</c:choose>