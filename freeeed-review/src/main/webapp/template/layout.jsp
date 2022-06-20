<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
    <head>
        <title><tiles:getAsString name="title" /></title>
        <link rel="stylesheet" type="text/css" href="css/bootstrap.css" />
        <link rel="stylesheet" type="text/css" href="css/bootstrap-responsive.css" />
        <link rel="stylesheet" type="text/css" href="css/bootstrap-icons.css" />
        <link rel="stylesheet" type="text/css" href="css/styles.css" />
        <link rel="stylesheet" href="http://code.jquery.com/ui/1.9.2/themes/base/jquery-ui.css" />
        <script src="js/jquery-1.8.3.js"></script>
        <script src="js/jquery-ui-1.9.2.custom.js"></script>
        <script>
            $(document).ready(function(){
                $(".menu_button").click(function(){
                    if($(".left").css('left') == "0px")
                    {
                         $( ".left" ).animate({
                            left: "-254px"
                          
                        }, {
                            duration: 200
                        });
                        $( ".content" ).animate({
                            marginLeft: 0,
                            width:  $(document).width() - 12
                           
                        }, {
                            duration: 200
                        });
                    }
                    else
                    {
                         $( ".left" ).animate({
                            left: "0px",
                           
                        }, {
                            duration: 200
                        });
                        $( ".content" ).animate({
                            marginLeft: 254,
                            width: $(document).width() - 266,
                         
                        }, {
                            duration: 200
                        });
                    }
                

                })

            });
        </script>
    </head>
    <body class="body" style="overflow: <c:if test="${loggedVisitor == null}">hidden</c:if>">
    
    <div class="wrapper">
        <div class="header">
            <tiles:insertAttribute name="header" />
        </div>
        <div class="left" style="position: <c:if test="${loggedVisitor == null}">inherit</c:if>">
            <tiles:insertAttribute name="menu" />
        </div>
        <div class="content">
            <tiles:insertAttribute name="body" />
        </div>
    </div>
    <div class="modal-loading"></div>
    
    <script src="js/bootstrap.js"></script>
    </body>
</html>