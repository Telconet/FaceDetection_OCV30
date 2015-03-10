/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package facedetection_ocv30;
import java.io.*;
import java.net.*;
import java.util.HashMap;

/**
 *
 * @author Eduardo
 */
public class KKServerThread  extends Thread{
    
    private Socket socket = null;
    private HashMap<String, HiloProcesamientoCamara> hilos;

    /**
     * Constructor 
     * @param socket Objeto socket que creamos al recibir una conexion
     * @param hilos  el HashMap<IP camara, hiloCamara> de hilos de las camaras
     */
    public KKServerThread(Socket socket, HashMap<String, HiloProcesamientoCamara> hilos) {
        super("KKMultiServerThread");
        this.socket = socket;
        this.hilos = hilos;
        
    }
    
    //Corremos el codigo para comunicarnos con el equipo
    public void run(){
        try{
            
            HiloProcesamientoCamara worker = this.hilos.get(socket.getInetAddress().getHostAddress());
            
            System.out.println("Se conecto la cámara IP: " + socket.getInetAddress().getHostName());
            if(worker.deteccionEnCurso){
                return;         //Alguien esta usando esta camara.
            }
            
            //Ya se puso a correr el hilo por primera vez
            if(!worker.hiloActivo()){
                worker.start();
            }
            
 
            //Notificamos al hilo de la camara que se levante... si esta durmiendo.
            //Para que empiece a procesar el video
            synchronized(worker.monNotificadorInicioDetection){
                worker.deteccionEnCurso = true;
                worker.monNotificadorInicioDetection.notifyAll(); 
            }

            System.out.println("ed");
            
            //Luego, nos vamos a dormir, hasta que el hilo nos avise que termino
            while(!worker.notificarFinDeteccion){
                synchronized(worker.monNotificadorDeteccion){
                    worker.monNotificadorDeteccion.wait();
                }
            }
            
            System.out.println("ed2");

            //Al finalizar el hilo de deteccion, deberiamos
            //ya tener a la persona reconocida... o no.
            String persona = worker.obtenerPersonaDetectada();

            //TODO: VERA3...
            this.socket.close();
        }
        catch(IOException e){
            System.out.println("Error al recibir datos de la cámara IP.");
            e.printStackTrace();
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
                

    }
}
