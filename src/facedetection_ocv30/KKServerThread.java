/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package facedetection_ocv30;
import java.io.*;
import java.net.*;

/**
 *
 * @author Eduardo
 */
public class KKServerThread  extends Thread{
    
    private Socket socket = null;

    public KKServerThread(Socket socket) {
        super("KKMultiServerThread");
        this.socket = socket;
       
        System.out.println("Se conecto la cámara IP: " + socket.getInetAddress().getHostName());
    }
    
    //Corremos el codigo para comunicarnos con el equipo
    public void run(){
        try{

            KnockKnockProtocol kkp = new KnockKnockProtocol();
            
            kkp.procesarDatos();
            this.socket.close();
        }
        catch(IOException e){
            System.out.println("Error al recibir datos de la cámara IP.");
            e.printStackTrace();
        }

    }
}
