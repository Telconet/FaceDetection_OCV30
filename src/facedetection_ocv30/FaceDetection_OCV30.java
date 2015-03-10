/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package facedetection_ocv30;

/**
 *
 * @author Eduardo
 */
import com.dropbox.core.DbxException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencv.core.*;



public class FaceDetection_OCV30{
    
    //public static boolean personaEncontrada = false;          //solo para pruebas
    public static final Object mutex = new Object();
     

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, DbxException, InterruptedException, IllegalAccessException {
        
        int cores = Runtime.getRuntime().availableProcessors();
                
        //Primero verficamos que exista el archivo de configuracion
        if(args.length == 0){
            System.out.println("No encontramos la ruta del archivo de configuración. ¿Olvido agregarla como argumento?");
            System.exit(-1);
        }
        else{
            File f = new File(args[0]);
            if(!f.exists() || f.isDirectory()){
                System.out.println("¡Ups! El archivo que especificaste no existe.");
                System.exit(-1);
            }
        }
        
        //Obtenemos el archivo de configuracion...
        Configuracion config = new Configuracion(args[0]);
        
        //Cargamos la librerias nativas
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
                
        //NO usar underscore en nombre de galeria
        String nombreGaleria = config.obtenerParametro(Configuracion.NOMBRE_GALERIA); //"TNtest";       
        
        String appId = config.obtenerParametro(Configuracion.KAIROS_APP_ID); //"752d01e5";
        String appKey = config.obtenerParametro(Configuracion.KAIROS_APP_KEY); //"f05dd012cb70fc6256357a9aeb17af2d"; 
        
        DeteccionCaras clienteDeteccionCaras = new DeteccionCaras(appId, appKey);
                
        //Directorio de trabajo
        String directorioTrabajo = null;
        directorioTrabajo = config.obtenerParametro(Configuracion.DIRECTORIO_TRABAJO);
        
        File f = new File(directorioTrabajo);
        if(!f.exists() || !f.isDirectory()){
            System.out.println("¡Ups! El diretorio de trabajo (" + directorioTrabajo +") no existe o no es una carpeta.");
            System.exit(-1);
        }
        
        /*URL ubicacion = ProcesamientoImagenes.class.getProtectionDomain().getCodeSource().getLocation();
        directorio = ubicacion.getFile();
        directorio = directorio.substring(1);
        
        if(directorio.contains("build/classes")){
            directorio = directorio.replace("/build/classes", "");
        }
        else{
            directorio = directorio.replace("/dist/FaceDetection.jar", "");
        }*/
        
        System.out.println(directorioTrabajo);
        
        //Servidor Dropbox
        System.out.println(config.obtenerParametro(Configuracion.ARCHIVO_TOKEN_DBX).trim());
        String dbxKey = config.obtenerParametro(Configuracion.KEY_DBX); //"cuiclds3bj72a53";
        String dbxSecret = config.obtenerParametro(Configuracion.SECRETO_DBX); //"m44th47fwrn9d0t";
        ServidorImagenes servidorImagenes = new ServidorImagenes(dbxKey, dbxSecret);
        String rutaArchivoToken = config.obtenerParametro(Configuracion.ARCHIVO_TOKEN_DBX).trim(); 
        
        f = new File(rutaArchivoToken);
        if(!f.exists() || f.isDirectory()){
            System.out.println("¡Ups! El archivo del token de Dropbox (" + rutaArchivoToken +") no existe.");
            System.exit(-1);
        }
        
        boolean conectadoADbx = servidorImagenes.conectarADropbox(rutaArchivoToken, config.obtenerParametro(Configuracion.STRING_APP_DBX)); //"/token.txt", "tnFace/1.0"
        
        if(!conectadoADbx){
            System.out.println("¡Ups! No nos pudimos conectar a Dropbox.");
            System.exit(-1);
        }
        
        //Hasta aqui OK...
                      
        //Multicamara...
        //Creamos las camaras...
        String[] ipCamaras = config.obtenerParametro(Configuracion.IP_CAMARAS).split(";");
        String[] ubicacionCamaras = config.obtenerParametro(Configuracion.UBICACION_CAMARAS).split(";");
                
        if(ipCamaras.length != ubicacionCamaras.length){
               System.out.println("El numero de IPs y ubicaciones de camaras no son igual. Por favor verifique el archivo de configuracion " + config.obtenerParametro(args[0]));
        }
        
        //Obtenemos la probabilidad de reconocimiento deseada
        double probabilidad = -1.0;
        
        try{
            probabilidad = Double.parseDouble(config.obtenerParametro(Configuracion.PROBABILIDAD_RECON));
        }
        catch(NumberFormatException e){
            System.out.println("Probabilidad incorrecta/no encontrada.");
            System.exit(-1);
        }
        
        //Pool de hilos...
        BlockingQueue<Runnable> colaDeEspera = new LinkedBlockingQueue<>(100*ipCamaras.length);         //Queue debe ser limitada  (5 segundos por cámara...)
        ThreadPoolExecutor ejecutor = new ThreadPoolExecutor(cores, cores, 10, TimeUnit.SECONDS, colaDeEspera);

        //new ThreadPoolExecutor
        //Hilos de reconocimiento.
        //ArrayList<Thread> hilosCamaras = new ArrayList<>();
        HashMap<String, HiloProcesamientoCamara> hilosCamaras = new HashMap<>();
        
        long millis_before = System.currentTimeMillis();
        
        //Creamos un hilo por camara...
        for(int i = 0; i < ipCamaras.length; i++){
            try {
                HiloProcesamientoCamara hiloCamara = new HiloProcesamientoCamara(ipCamaras[i], directorioTrabajo.trim(),
                        nombreGaleria, probabilidad, servidorImagenes, clienteDeteccionCaras, ubicacionCamaras[i], ejecutor, i);
                
     
                hilosCamaras.put(ipCamaras[i], hiloCamara);

            } catch (Exception ex) {
                Logger.getLogger(FaceDetection_OCV30.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }  
        
        //Puerto a escuchar
        int puerto = 21;
        try{
            puerto = Integer.parseInt(config.obtenerParametro(Configuracion.PUERTO_FTP));
        }
        catch(NumberFormatException e){
            System.out.println("Puerto FTP invalido. La aplicacion se cerrará");
            System.exit(-1);
        }
        
        //Empezamos a escuchar las solicitudes FTP, que para nosostros significan que 
        //se ha detectado movimiento.
        //Socket...
        //Escuchar to
        InetAddress addr = InetAddress.getByName("0.0.0.0");
        try{
            ServerSocket socketServidor = new ServerSocket(puerto, 50, addr); 
            boolean escuchar = true;

            //Aceptamos conexiones de clientes...
            while(escuchar){
               new KKServerThread(socketServidor.accept(), hilosCamaras).start();
            }
        }
        catch(IOException ex){
            ex.printStackTrace();
            System.exit(-1);
        }
        
        
        //Espramos a finalizar??
       /* for(int i = 0; i < hilosCamaras.size(); i++){
            Thread tmp = hilosCamaras.get(i);
            tmp.join();
        }*/
        
        //tiempo...
        long millis_after = System.currentTimeMillis();
        long tiempo = (millis_after - millis_before ) / 1000;
        System.out.println(String.format("Tiempo de ejecucion %.2f segundos", (float)tiempo));
        
        //ejecutor.awaitTermination(cores, TimeUnit.);
        
        //Usar hilo.join() o ejecutor.shutdown(); NO USAR shutdown ya que cualquier tarea enviada despues del shutdown sera rechazada
        
    }
}
