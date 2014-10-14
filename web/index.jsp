<%-- 
    Document   : index
    Created on : Oct 14, 2014, 1:51:25 PM
    Author     : jprestes
--%>

<%@page import="com.braintreegateway.BraintreeGateway"%>
<%@page import="com.braintreegateway.Environment"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%

    BraintreeGateway gateway = new BraintreeGateway(
            Environment.SANDBOX,
            "4963ym4c6dxrgzng",
            "w5f9q49md3nwjd8q",
            "6ef75cc2d78f5e599ede4fba61021474"
    );  
    
    String clientToken = gateway.clientToken().generate();
%>
<html>
    <head>
        <title>Buy your candies and get happy</title>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <script src="https://js.braintreegateway.com/v2/braintree.js"></script>
    </head>
    <body>
        <div>Chocolate makes you happy!</div>
        <form id="checkout" method="post" action="/checkout">
            <div id="dropin"></div>
            <input type="submit" value="Pay $1">
        </form>
        <script>
            braintree.setup("<%=clientToken%>", 
            'dropin', {
                container: 'dropin'
            });
        </script>
    </body>
</html>

