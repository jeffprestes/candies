/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.braintreepayments.developer.demo.candies.servlets;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Environment;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import com.braintreegateway.ValidationError;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jprestes
 */
public class Checkout extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet checkout</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet checkout at " + request.getContextPath() + "</h1>");
        
            BraintreeGateway gateway = (BraintreeGateway) request.getSession().getAttribute("gateway");

            String pmn = request.getParameter("payment_method_nonce");

            TransactionRequest paymentReq = new TransactionRequest().amount(new BigDecimal("1.00")).paymentMethodNonce(pmn);

            Result<Transaction> result = gateway.transaction().sale(paymentReq);

            if (result.isSuccess()) {
                // transaction successfully submitted for settlement
                Transaction transaction = result.getTarget();
                out.println("<h1> Transaction ID: " + transaction.getId() + "</h1>");
                out.println("<h2>" + transaction.getProcessorResponseText() + "</h2>");
                
            } else if (result.getTransaction() != null) {
                out.println("Message: " + result.getMessage());
                Transaction transaction = result.getTransaction();
                out.println("Error processing transaction:");
                out.println("  Status: " + transaction.getStatus());
                out.println("  Code: " + transaction.getProcessorResponseCode());
                out.println("  Text: " + transaction.getProcessorResponseText());
                
            } else {
                out.println("Message: " + result.getMessage());
                for (ValidationError error : result.getErrors().getAllDeepValidationErrors()) {
                    out.println("Attribute: " + error.getAttribute());
                    out.println("  Code: " + error.getCode());
                    out.println("  Message: " + error.getMessage());
                }
            }
        
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
