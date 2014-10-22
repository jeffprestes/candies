/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.braintreepayments.developer.demo.candies.servlets;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import com.braintreegateway.ValidationError;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 *
 * @author jprestes
 */
public class Checkout extends HttpServlet {

    private String mqttServer = "";
    private String mqttQueue = "";
    private String mqttPort = "";
    private String mqttClientId = "";
    
    @Override
    public void init (ServletConfig cfg) throws ServletException    {
        
        try {
            //InputStream inputStream = Checkout.class.getResourceAsStream("/var/candies/candies.properties");
            File arq = new File("/var/candies/candies.properties");
            FileReader inputStream = new FileReader(arq);
            Properties prop = new Properties();
            prop.load(inputStream);
            
            this.mqttQueue = prop.getProperty("mqttqueue");
            this.mqttServer = prop.getProperty("mqttserver");
            this.mqttClientId = prop.getProperty("mqttclientid");
            this.mqttPort = prop.getProperty("mqttport");
            
            
        } catch (Exception ex) {
            Logger.getLogger(Checkout.class.getName()).log(Level.SEVERE, "Checkout Servlet could not load MQTT Properties. Loading default...", ex);
            this.mqttQueue = "jeffprestes/candies/world";
            this.mqttServer = "iot.eclipse.org";
            this.mqttClientId = "candies-jeff-localserver";
            this.mqttPort = "1883";
        }
        
    }
    
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
                out.println("<h3>Releasing candies...</h3>");
                
                try {
                    String topic        = this.mqttQueue;   
                    int qos             = 2;
                    String broker       = "tcp://" + this.mqttServer + ":" + this.mqttPort;
                    String clientId     = this.mqttClientId;
                    String msg          = "release";
                    MemoryPersistence persistence = new MemoryPersistence();
                    MqttClient client;

                    client = new MqttClient(broker, clientId, persistence);
                    MqttConnectOptions connOpts = new MqttConnectOptions();
                    connOpts.setCleanSession(true);

                    client.connect(connOpts);

                    MqttMessage message = new MqttMessage(msg.getBytes());
                    message.setQos(qos);

                    client.publish(topic, message);
                    
                    client.disconnect();

                    client.close();

                    client = null;
                    
                    out.println("Message sent to machine to release the candy");
                    
                } catch (MqttException me)     {
                    System.out.println("reason "+me.getReasonCode());
                    System.out.println("msg "+me.getMessage());
                    System.out.println("loc "+me.getLocalizedMessage());
                    System.out.println("cause "+me.getCause());
                    System.out.println("excep "+me);
                    me.printStackTrace();
                    out.println("Error sending message to Machine");
                }
                
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
