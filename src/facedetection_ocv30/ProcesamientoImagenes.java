/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package facedetection_ocv30;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

/**
 *
 * @author Eduardo
 */
public class ProcesamientoImagenes implements Runnable{
    
    private Mat imagen;
    private int indice;
    private ServidorImagenes imgServer;
    private DeteccionCaras faceServer;
    private String galeria;
    private double probabilidad;
    private String urlImagen;
    private String directorio;
    private String directorioImagenes;
    private HiloProcesamientoCamara nuestroHilo;
    private String id;
    private CascadeClassifier faceDetector;
    private MatOfRect faceDetections;
    
    public ProcesamientoImagenes(Mat imagen, int indice, ServidorImagenes imgServer, DeteccionCaras faceServer, String galeria, String directorioTrabajo, 
                                    String directorioImagenes, double probabilidad, HiloProcesamientoCamara nuestroHilo, String id, CascadeClassifier clasificador){
        this.imagen = imagen;
        this.indice = indice;
        this.imgServer = imgServer;
        this.faceServer = faceServer;
        this.urlImagen = null;
        this.galeria = galeria;
        this.probabilidad = probabilidad;
        this.nuestroHilo = nuestroHilo;
        this.id = id;
        
        //Obtenemos el directorio de trabajo
        /*URL location = ProcesamientoImagenes.class.getProtectionDomain().getCodeSource().getLocation();
        this.directorio = location.getFile();
        this.directorio = this.directorio.substring(1);
        this.directorio = this.directorio.replace("dist/FaceDetection.jar", "");*/
        this.directorio = directorioTrabajo;
        this.directorioImagenes = directorioImagenes;
        this.faceDetector = clasificador;

    }
    
    /**
     * Cuando el objeto sea GC, necesitamos liberar los recursos OpenCV, ya que estamos usando una libreria nativa
     * @throws Exception
     * @throws Throwable 
     */
    /*@Override
    public void finalize() throws Exception, Throwable{
        try {
           this.imagen.release();
           this.faceDetections.release();
        }
        finally {
           super.finalize();
        }
    }*/

    /*public ProcesamientoImagenes(String urlImagen, int indice, ServidorImagenes imgServer, DeteccionCaras faceServer, String galeria, String directorioTrabajo, double probabilidad){
        this.imagen = null;
        this.indice = indice;
        this.imgServer = imgServer;
        this.faceServer = faceServer;
        this.urlImagen = urlImagen;
        this.persona = null;
        this.galeria = galeria;
        this.directorio = directorioTrabajo;  
        this.probabilidad = probabilidad;
    }*/

    @Override
    public void run() {
        
        try{
            //Usamos try para poder usar finally, y llamar imagen.release() para evitar memory leak
            //Si el hilo principal nos ha dicho que terminemos, salimos.
            if(Thread.interrupted()){
                //imagen = null;
                return;
            }

            synchronized(nuestroHilo.mutex){
                if(nuestroHilo.personaDetectada){
                        //Si ya se encontro una cara, no procesamos.                        
                        return;
                }
            }

            //Liberar el MatOfRect tambien
           faceDetections = new MatOfRect();
           
           //Grayscale??
           //Mat imagenGrayscale = new Mat();
           //imagenGrayscale = imagen;
           //Imgproc.cvtColor(imagen, imagenGrayscale, Imgproc.COLOR_RGB2GRAY);
            
           
           //Detectamos la cara 
           //Tamaño minimo de cara (50x50 pixels), usamos heuristica PUNNY, 1.1 incremento de ventana, y al manos 3 cuadros para considerar cara detectada.
           //Es muy importante que la cara sea puesta de frente a la camara y ocupe al menos 1/16 del area de la imagen
           faceDetector.detectMultiScale(imagen, faceDetections, 1.1, 2, /*org.opencv.objdetect.Objdetect.CASCADE_DO_CANNY_PRUNING*/0, new Size(100, 100), new Size(1080, 1080));

           //faceDetector.detectMultiScale(imagen, faceDetections);
           System.out.println(String.format("OpenCV detecto %s caras en la imagen camera%d.jpg, para la camara %s", faceDetections.toArray().length, this.indice, this.id)); 
           //Imgcodecs.imwrite(this.directorioImagenes + "/camara" + indice + ".jpg", imagen);
           
           //Detectar...
           if(faceDetections.toArray().length > 0){
                //Escribir imagen para Dbx
                Imgcodecs.imwrite(this.directorioImagenes + "/camara" + indice + ".jpg", imagen);

                //En esta seccion escribimos la imagen a disco con un cuadro señalando la cara..
                for (Rect rect : faceDetections.toArray()) {
                
                    Imgproc.rectangle(imagen, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                            new Scalar(0, 255, 0));
                }


               String filename = this.directorioImagenes + "/detected_faces/output" + this.indice + ".jpg";

               boolean what = Imgcodecs.imwrite(filename, imagen);
               //System.out.println(String.format("Escribiendo %s", filename));
               //Fin del dibujo del cuadro de deteccion.

               //Si hay caras, mandamos a Kairos...
               this.imgServer.subirArchivo(this.directorioImagenes + "/camara" + indice + ".jpg", "/" + id +"/camara" + indice + ".jpg");
               String url = this.imgServer.obtenerURLDescarga("/" + id + "/camara" + indice + ".jpg");

               synchronized(nuestroHilo.mutex){        
                    if(nuestroHilo.personaDetectada){
                         imagen = null;
                        System.out.println("Persona ya fue reconocida, saliendo del hilo...");
                        return;
                    }
               }

                //Mandamos fotos a Kairos
                System.out.println("KAIROS: Imagen " + this.indice + " enviada a Kairos...");
                String nombre = this.faceServer.reconocer(url, this.galeria, this.probabilidad);
                //String nombre = "nadie";

                //synchronized(FaceDetection.mutex){
                synchronized(nuestroHilo.mutex){

                    //if(FaceDetection.personaEncontrada){
                    if(nuestroHilo.personaDetectada){
                            //Si ya se encontro una cara, no procesamos.
                            System.out.println("Persona ya fue reconocida, saliendo del hilo...");
                            return;
                    }

                   if(!nombre.equals("nadie")){
                           nuestroHilo.personaDetectada = true;
                           nuestroHilo.persona = nombre;
                           imagen = null;
                           System.out.println("Hola " + nombre + ". (Imagen " + this.indice+ ")");
                           return;                
                    }
                    else{
                        System.out.println("No se reconocio a nadie en la imagen " + indice);
                    }
               }
           }
           else{
               System.out.println("No se detecto cara en la imagen " + indice);
           }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
           this.imagen.release();
           if(faceDetections != null){
                this.faceDetections.release();
            }
           
            synchronized(this.nuestroHilo.mutexContadorHilosActivos){
                this.nuestroHilo.hilosActivos--;
            }
        }
    }
    
}
