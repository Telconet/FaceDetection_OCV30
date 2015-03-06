/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package facedetection_ocv30;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;

/**
 * Cada uno de estos objetos maneja una cámara.
 * @author Eduardo
 */
public class HiloProcesamientoCamara implements Runnable {
    
    private String ipCamara;
    private Camara camara;
    private String nombreGaleria;
    private String directorio;
    private double probabilidad;
    private ThreadPoolExecutor ejecutor;
    private ServidorImagenes servidorImagenes;
    private DeteccionCaras clienteDeteccionCaras;
    private String ubicacion;
    private boolean cerrarCamara;
    protected String persona;
    protected boolean personaDetectada;
    protected final Object mutex;
    private int idHiloCamara;
    private String directorioCamara;
    protected int hilosActivos;
    protected final Object mutexContadorHilosActivos;
    
    
    /**
     * Constructor
     * @param ipCamara
     * @param directorio
     * @param nombreGaleria
     * @param probabilidad
     * @param servidorImagenes
     * @param clienteDeteccionCaras
     * @param ubicacion
     * @param ejecutor
     * @param id 
     */
    public HiloProcesamientoCamara(String ipCamara, String directorio, String nombreGaleria, double probabilidad, ServidorImagenes servidorImagenes, DeteccionCaras clienteDeteccionCaras, 
                                    String ubicacion, ThreadPoolExecutor ejecutor, int id){
        this.ipCamara = ipCamara; //rtsp://192.168.137.172/profile2/media.smp
        this.camara = null;
        this.nombreGaleria = nombreGaleria;
        this.directorio = directorio;
        this.probabilidad = probabilidad;
        this.servidorImagenes = servidorImagenes;
        this.clienteDeteccionCaras = clienteDeteccionCaras;
        this.ubicacion = ubicacion;
        this.ejecutor = ejecutor;
        this.cerrarCamara = false;
        this.mutex = new Object();
        this.idHiloCamara = id;
        this.directorioCamara = directorio.trim() + "\\camara_" + ipCamara;
        this.personaDetectada = false;
        this.hilosActivos = 0;
        this.mutexContadorHilosActivos = new Object();
        
         //Verificamos que exista el directorio
        File f = new File(directorioCamara);
        if(!f.exists() || !f.isDirectory()){
            //Intentamos crear el directorio
            try{
                f.mkdir();
                f = new File(directorioCamara + "/detected_faces");
                
                if(!f.exists() || !f.isDirectory()){
                    f.mkdir();
                }                
            }
            catch(Exception e){
                System.out.println("¡Ups! No se pudo crear el directorio para la camara " + ipCamara);
                System.exit(-1);
            }
        }
    }
    
    @Override
    public void run(){
        
       
        //Para camaras Samsung SNV-7080R
        this.camara = new Camara("rtsp://" + this.ipCamara + "/profile2/media.smp");
               
        //No se pudo abrir la camara...
        if(camara == null){
           //TODO 
            Bitacora log = new Bitacora();
            log.registarEnBitacora("errores_camara", "errores_camara.txt", "No se pudo abrir la camara con URL " + this.ipCamara + " ubicada en " + this.ubicacion, Bitacora.SEVERE);
        }
        
        //rtsp://192.168.137.172/profile2/media.smp
        camara.abrirCamara();      //empezara recibir datos.
        /*while(true){
            Mat imagen = camara.obtenerCuadro();
            imagen.release();
        }*/
        int cuadrosCapturados = 0;
        
        //DEBEMOS LLAMAR imagen.release() para evitar LEAK. Sin embargo, necesitamos la imagen hasta que la procesemos,
        //y ya que el procesamiento toma mas tiempo, empezamos a consumir memoria. 
        //Por lo tanto, debemos mandar un cuadro imagen solo si ha un hilo disponible en el ejecutor.
        int cores = Runtime.getRuntime().availableProcessors();
        
        //Abrimos el clasificador a ser usados por los hilos
        CascadeClassifier clasificador;        

        String recurso = null;
        //recurso = directorio + "/recursos/hogcascades/hogcascade_pedestrians.xml";
        recurso = directorio + "/recursos/haarcascades/haarcascade_frontalface_alt_tree.xml"; //--> best so far
        //recurso = directorio + "/recursos/lbpcascades/lbpcascade_profileface.xml";
        //recurso = directorio + "/recursos/haarcascades/haarcascade_frontalface_default.xml";
        //recurso = directorio + "/recursos/haarcascade_frontalface_alt.xml"; //"/recursos/haarcascade_frontalface_alt.xml";

        clasificador = new CascadeClassifier(recurso);
        if(clasificador.empty()){
            System.out.println("Clasificador de caras no cargado... Saliendo del hilo de la cámara " + ipCamara);
            return;
        }
        
        
        Mat imagen = null;
        int i = 0;
        
        for(i = 0; i < 100; ){
        //while(true){
            try{
                int activos = ejecutor.getActiveCount();
                /*if(cuadrosCapturados == 0){
                   camara.abrirCamara(); 
                }*/

                //Solo si hay CPU disponible leemos la imagen, caso constrario debemos mantenerla en memoria
                //y si el procesamiento es mas lento que la tasa de lectura de imagenes, eventualmente nos
                //quedaremos sin memoria.
                //synchronized(mutexContadorHilosActivos){
                    //if( hilosActivos < cores){
                        hilosActivos++;
                        //System.out.println(String.format("Activos: %s, i: %s, tamaño cola: %d", activos, i, ejecutor.getQueue().size()));

                        imagen = camara.obtenerCuadro();
                                            ProcesamientoImagenes faceDt = new ProcesamientoImagenes(imagen, i, servidorImagenes, clienteDeteccionCaras, nombreGaleria, directorio, directorioCamara,
                                                                                probabilidad, this, ipCamara, clasificador);
                        ejecutor.execute(faceDt);
                        i++;
                        cuadrosCapturados++;
                    //}
                //}
            }
            catch(RejectedExecutionException e){
                //System.out.println("Buffer lleno. Descartando el cuadro " + i);
                imagen.release();
            }
            
            
            //TODO: si se detecto persona,
            synchronized(this.mutex){
                if(personaDetectada){
                    //TODO: realizar accion... Cerrar cámara??
                    Bitacora log_deteccion = new Bitacora(); 
                    log_deteccion.registarEnBitacora("Log_camara_" + ipCamara,  directorio + "Log_camara_" + ipCamara + ".txt", "Se detecto a " + persona + " en el/la " + ubicacion, Bitacora.INFO);
                    this.personaDetectada = false;
                    this.persona = "";
                    System.exit(-1);
                }
            }
            
            /*if(cuadrosCapturados == 20){
                this.camara.cerrarCamara();
                try{
                    Thread.sleep(1000);
                }
                catch(InterruptedException e){
                    System.out.println("Hilo de camara " + this.ipCamara + " se levanto antes de tiempo");
                }
            }*/
        }
        
        //
        while(hilosActivos != 0){
            
            try{
                Thread.sleep(500);
            }
            catch(InterruptedException e){
                System.out.println("duh!");
            }
        }
    } 
}
