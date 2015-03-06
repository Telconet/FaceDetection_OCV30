package facedetection_ocv30;


import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;
import com.dropbox.core.DbxWriteMode;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Eduardo
 */
public class ServidorImagenes {
    
    private String dbxKey;
    private String dbxSecret;
    private String tokenAcceso;
    private DbxClient cliente;
    private DbxRequestConfig config;
    private DbxAppInfo appInfo;
    
    /**
     * Constructor
     * @param dbxKey la llave de la aplicacion propocionada por Dropbox
     * @param dbxSecret el secrete de la aplicacion porporcionado por Dropbox
     */
    public ServidorImagenes(String dbxKey, String dbxSecret){
        this.dbxKey = dbxKey;
        this.dbxSecret = dbxSecret;
        this.cliente = null;
        this.tokenAcceso = null;
        this.config = null;
        this.appInfo = null;
    }
    
    
    /**
     * Este metodo nos permite autorizar nuestra aplicacion para ser usada en Dropbox
     * El resultado de esta funcion es obtener el token de acceso por primera vez,
     * o leerlo si ya existe localemente.
     * @param rutaArchivoToken
     * @return 
     */
    private boolean autenticarAplicacion(String rutaArchivoToken, String appStr){
        
        File archivoToken = null;
        if(rutaArchivoToken != null){
            System.out.println(rutaArchivoToken);
            archivoToken = new File(rutaArchivoToken);
        }
        
        this.appInfo = new DbxAppInfo(this.dbxKey, this.dbxSecret);
        this.config = new DbxRequestConfig(appStr, Locale.getDefault().toString());           //"tnFace/1.0"
        
        if(rutaArchivoToken == null || !archivoToken.exists()){
            try{
                //Inicializamos la cuenta dropbo           
                DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);

                String authorizeUrl = webAuth.start();
                System.out.println("1. Go to: " + authorizeUrl);
                System.out.println("2. Click \"Allow\" (you might have to log in first)");
                System.out.println("3. Copy the authorization code.");
                String code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();

                DbxAuthFinish authFinish = webAuth.finish(code);
                tokenAcceso = authFinish.accessToken;

                //Guardamos el token...
                BufferedWriter escritor = new BufferedWriter(new FileWriter(archivoToken));
                escritor.write(tokenAcceso);
                escritor.close();
                
                return true;
            }
            catch(IOException e){
                Bitacora log = new Bitacora();
                log.registarEnBitacora("errores.txt", "errores.txt", e.getMessage() + ": No se puedo leer el token de autorización Dropbox", Bitacora.SEVERE);
                System.exit(-1);
                return false;
            }
            catch(DbxException d){
                Bitacora log = new Bitacora();
                log.registarEnBitacora("errores.txt", "errores.txt", d.getMessage() + ": Problema con la conexion con Dropbox", Bitacora.SEVERE);
                System.exit(-1);
                return false;
            }
        }
        else{
            try{
                //Leemos el string de autorizacion...
                FileReader lectorArchivo = new FileReader(archivoToken);
                try (BufferedReader lector = new BufferedReader(lectorArchivo)) {
                    tokenAcceso = lector.readLine().trim();
                }
                catch(IOException e){
                    Bitacora log = new Bitacora();
                    log.registarEnBitacora("errores.txt", "errores.txt", e.getMessage() + ": No se puedo leer el token de autorización Dropbox", Bitacora.SEVERE);
                    System.exit(-1);
                }
                 return true;
            }
            catch(IOException e){
                Bitacora log = new Bitacora();
                log.registarEnBitacora("errores.txt", "errores.txt", e.getMessage() + ": No se puedo leer el token de autorización Dropbox", Bitacora.SEVERE);
                System.exit(-1);
                return false;
            }
        }
    }
    
    /**
     * Conecta a la cuenta Dropbox dada, para permitir subida y descarga de archivos.
     * @param rutaArchivoToken
     * @param appStr
     * @return 
     */
    public boolean conectarADropbox(String rutaArchivoToken, String appStr ){
        
        if(this.autenticarAplicacion(rutaArchivoToken, appStr)){
            this.cliente = new DbxClient(this.config, this.tokenAcceso);
            
            if(this.cliente != null){
                return true;
            }
            else{
                return false;
            }
        }
        return false;
    }
    
    /**
     * Sube el archivo rutaArchivoLocal a la ruta rutaDbx en Dropbox
     * @param rutaArchivoLocal
     * @param rutaDbx
     * @return 
     */
    public boolean subirArchivo(String rutaArchivoLocal, String rutaDbx){
        try{
            if(this.cliente != null){
                //Ahora intentamos subir un archivos...
                File archivoASubir = new File(rutaArchivoLocal);

                if(!archivoASubir.exists()){
                        Bitacora log = new Bitacora();
                        log.registarEnBitacora("errores.txt", "errores.txt", "El archivo a subir no existe.", Bitacora.WARNING);
                        return false;
                }

                FileInputStream inputStream = new FileInputStream(archivoASubir);
                String url = "";

                try {
                    DbxEntry.File uploadedFile = cliente.uploadFile(rutaDbx, DbxWriteMode.force(), archivoASubir.length(), inputStream);
                    //System.out.println("Archivo subido: " + uploadedFile.toString());
                } finally {
                    inputStream.close();
                }
                
                return true;
            }
        }
        catch(IOException e){
            Bitacora log = new Bitacora();
            log.registarEnBitacora("errores.txt", "errores.txt", e.getMessage() + ": No se pudo abrir el archivo para subir a Dropbox", Bitacora.SEVERE);
            return false;
        }
        catch(DbxException f){
            Bitacora log = new Bitacora();
            log.registarEnBitacora("errores.txt", "errores.txt", f.getMessage() + ": No se pudo subir el archivo a Dropbox", Bitacora.SEVERE);
            return false;
        }
        return false;
    }
    
    
    /**
     * Retor
     * @param rutaDbx
     * @return la URL para descarga del archivo
     */
    public String obtenerURLDescarga(String rutaDbx){
        try{
            if(this.cliente != null){
               String url = this.cliente.createShareableUrl(rutaDbx); 

                if(url != null){
                    url = url.substring(0, url.length() - 1) + "1";              //cambiamos de ?dl=0 a ?dl=1    //nullpointerexception...
                    return url;
                }
                else return null;
            }
            else return null;
        }
        catch(DbxException e){
            Bitacora log = new Bitacora();
            log.registarEnBitacora("errores.txt", "errores.txt", e.getMessage() + ": No se pudo crear la URL  del archvio Dropbox " + rutaDbx, Bitacora.SEVERE);
            return null;
        }
    }
}
