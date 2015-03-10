/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package facedetection_ocv30;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;

/**
 * Cada uno de estos objetos maneja una cámara.
 * @author Eduardo
 */
public class HiloProcesamientoCamara extends Thread {
    
    private String ipCamara;
    private Camara camara;
    private String nombreGaleria;
    private String directorio;
    private double probabilidad;
    private ThreadPoolExecutor ejecutor;
    private ServidorImagenes servidorImagenes;
    private DeteccionCaras clienteDeteccionCaras;
    private String ubicacion;
    private boolean camaraActiva;
    private CascadeClassifier clasificador; 
    private int idHiloCamara;
    private String directorioCamara;
    
    protected String persona;
    protected boolean personaDetectada;
    protected final Object mutex;
    protected int hilosActivos;
    protected final Object mutexContadorHilosActivos;
    
    public final Object monNotificadorDeteccion;
    public final Object monNotificadorInicioDetection;
    public boolean deteccionEnCurso;
    public boolean notificarFinDeteccion;
    
    
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
                                    String ubicacion, ThreadPoolExecutor ejecutor, int id) throws Exception{
        this.ipCamara = ipCamara; //rtsp://192.168.137.172/profile2/media.smp
        this.camara = null;
        this.nombreGaleria = nombreGaleria;
        this.directorio = directorio;
        this.probabilidad = probabilidad;
        this.servidorImagenes = servidorImagenes;
        this.clienteDeteccionCaras = clienteDeteccionCaras;
        this.ubicacion = ubicacion;
        this.ejecutor = ejecutor;
        this.mutex = new Object();
        this.idHiloCamara = id;
        this.directorioCamara = directorio.trim() + "\\camara_" + ipCamara;
        this.personaDetectada = false;
        this.hilosActivos = 0;
        this.mutexContadorHilosActivos = new Object();
        this.camaraActiva = false;       
        this.monNotificadorDeteccion = new Object();
        this.monNotificadorInicioDetection = new Object();
        this.deteccionEnCurso = false;
        this.notificarFinDeteccion = false;
        
        
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
        
        //Cargamos el clasificador...
        //Abrimos el clasificador a ser usados por los hilos
        String recurso = null;
        //recurso = directorio + "/recursos/hogcascades/hogcascade_pedestrians.xml";
        recurso = directorio + "/recursos/haarcascades/haarcascade_frontalface_alt_tree.xml"; //--> best so far
        //recurso = directorio + "/recursos/lbpcascades/lbpcascade_profileface.xml";
        //recurso = directorio + "/recursos/haarcascades/haarcascade_frontalface_default.xml";
        //recurso = directorio + "/recursos/haarcascade_frontalface_alt.xml"; //"/recursos/haarcascade_frontalface_alt.xml";

        clasificador = new CascadeClassifier(recurso);
        if(clasificador.empty()){
            System.out.println("Clasificador de caras no cargado... Saliendo del hilo de la cámara " + ipCamara);
            throw new FileNotFoundException("No se puedo cargar el clasificado Haar " + recurso);
        }
        
         //Para camaras Samsung SNV-7080R
        String url = "rtsp://" + this.ipCamara + "/profile2/media.smp";
        this.camara = new Camara(url);
        
        //No se pudo abrir la camara...
        if(camara == null){
           //TODO 
            Bitacora log = new Bitacora();
            log.registarEnBitacora("errores_camara", "errores_camara.txt", "No se pudo abrir la camara con URL " + this.ipCamara + " ubicada en " + this.ubicacion, Bitacora.SEVERE);
            this.camaraActiva = false;
            throw new FileNotFoundException("No se puedo abrir la URL de la camara: " + url);
        }
    }
    
    @Override
    public void run(){
        
        this.notificarFinDeteccion = false;
        
        //Esperamos a que nos notifiquen para inicar la deteccion.
        while(true){
            
            try{
                while(!deteccionEnCurso){
                    synchronized(monNotificadorInicioDetection){
                        monNotificadorInicioDetection.wait();
                    }
                }
            }
            catch(InterruptedException e){
                System.exit(-1);
            }
            
            this.deteccionEnCurso = false;
            
            
            //Camara esta activa.
            synchronized(monNotificadorDeteccion){
                this.camaraActiva = true;
            }
            
            this.persona = null;

            //rtsp://192.168.137.172/profile2/media.smp
            camara.abrirCamara();      //empezara recibir datos.

            if(!this.camara.camaraAbierta()){
                Bitacora log = new Bitacora();
                log.registarEnBitacora("errores", "errores.txt", "No se pudo abrir la cámara " + "rtsp://" + this.ipCamara + "/profile2/media.smp", Bitacora.SEVERE);
            }

            //DEBEMOS LLAMAR imagen.release() para evitar LEAK. Sin embargo, necesitamos la imagen hasta que la procesemos,
            //y ya que el procesamiento toma mas tiempo, empezamos a consumir memoria. 
            //Por lo tanto, debemos mandar un cuadro imagen solo si ha un hilo disponible en el ejecutor.

            Mat imagen = null;
            int i = 0;

            //Grabamos 5 segundos...
            for(i = 0; i < 100; i++){
                try{

                    //Solo si hay CPU disponible leemos la imagen, caso constrario debemos mantenerla en memoria
                    //y si el procesamiento es mas lento que la tasa de lectura de imagenes, eventualmente nos
                    //quedaremos sin memoria.
                    imagen = camara.obtenerCuadro();
                                        ProcesamientoImagenes faceDt = new ProcesamientoImagenes(imagen, i, servidorImagenes, clienteDeteccionCaras, nombreGaleria, directorio, directorioCamara,
                                                                            probabilidad, this, ipCamara, this.clasificador);
                    ejecutor.execute(faceDt);
                }
                catch(RejectedExecutionException e){
                    imagen.release();
                }

                //TODO: si se detecto persona,
                synchronized(this.mutex){
                    if(personaDetectada){
                        Bitacora log_deteccion = new Bitacora(); 
                        log_deteccion.registarEnBitacora("Log_camara_" + ipCamara,  directorio + "Log_camara_" + ipCamara + ".txt", "Se detecto a " + persona + " en el/la " + ubicacion, Bitacora.INFO);
                        this.camara.cerrarCamara();
                        this.camaraActiva = false;
                        return;
                    }
                }
            }
            this.camara.cerrarCamara();
            
            synchronized(monNotificadorDeteccion){
                notificarFinDeteccion = true;
                monNotificadorDeteccion.notifyAll();
            }
        }
    } 
    
    /**
     * 
     * @return 
     */
    public String obtenerPersonaDetectada(){
        return this.persona;
    }
    
    public boolean hiloActivo(){
        return this.camaraActiva;
    }
    
    public int id(){
        return idHiloCamara;
    }
}
