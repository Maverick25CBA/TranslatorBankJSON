/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.translator.controller;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import dk.translator.dto.LoanRequestDTO;
import dk.translator.messaging.Receive;
import dk.translator.messaging.Send;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author marekrigan
 */
public class TranslateToBankJSON 
{
    private static Gson gson;
    private static final String REPLY_QUEUE_NAME = "queue_normalizerBankJSON";
    
    public static void receiveMessages() throws IOException,InterruptedException
    {
        gson = new Gson();
        
        HashMap<String,Object> objects = Receive.setUpReceiver();
        
        QueueingConsumer consumer = (QueueingConsumer) objects.get("consumer");
        Channel channel = (Channel) objects.get("channel");
        
        LoanRequestDTO loanRequest;
//        List<String> selectedBanks;
        
        while (true) 
        {
          QueueingConsumer.Delivery delivery = consumer.nextDelivery();
          String message = new String(delivery.getBody());
          
          AMQP.BasicProperties props = delivery.getProperties();
          AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder().correlationId(props.getCorrelationId()).replyTo(REPLY_QUEUE_NAME).build();
          
          String routingKey = delivery.getEnvelope().getRoutingKey();

          System.out.println(" [x] Received '" + routingKey + "':'" + message + "'");
          
          loanRequest = gson.fromJson(message, LoanRequestDTO.class);
          
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

        }
        
    }
    
    public static void sendMessage(LoanRequestDTO loanRequest, AMQP.BasicProperties props) throws IOException
    {
        String message = gson.toJson(loanRequest);
        
        Send.sendMessage(message, props);
    }   
}
