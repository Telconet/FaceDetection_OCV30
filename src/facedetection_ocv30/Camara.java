/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package facedetection_ocv30;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.*;

/**
 *
 * @author Eduardo
 */
public class Camara {
    
    private String urlCamara;
    private int dispositivo;
    private VideoCapture camara;
    private boolean camaraAbierta;
    
    public Camara(String url){
        this.urlCamara = url;
        this.camara = null;
        this.dispositivo = -1;
    }
    
    public Camara(int camara){
        this.urlCamara = null;
        this.camara = null;
        this.dispositivo = camara;
        this.camaraAbierta = false;
    }
    
    public void abrirCamara(){
        
        System.out.println(System.getProperty("java.library.path"));
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        this.camara = new VideoCapture();
        
        if(urlCamara == null){
          
            camara.open(this.dispositivo); //? camar
            //camara.ope
        }
        else{
            //CV_CAP_PROP_FPS = 5
            this.camara.open(this.urlCamara);
            //this.camara.open(0);
            System.out.println("FPS: " + this.camara.get(5));
           
           
            System.out.println(this.camara.get(5));
        }
        
        if(!camara.isOpened()){
            System.out.println("Error al abrir la camara.");
            this.camaraAbierta = false;
            
        }
        else{
            System.out.println("Camara OK.");
            this.camaraAbierta = true;
        }
        
    }
    
    public void cerrarCamara(){
        if(this.camara != null){
            this.camara.release();
        }
    }
    
    /**
     * Obtiene un cuadro del stream...
     */
    public boolean camaraAbierta(){
        return this.camaraAbierta;
    }
    
    public Mat obtenerCuadro(){
        //LEAK!!!
        if(this.camaraAbierta){
            Mat cuadro = new Mat();
            //cuadro.
            boolean status = this.camara.read(cuadro);

            //Imgcodecs.imre
            return cuadro;
            


        }
        return null;
    }
    
}
