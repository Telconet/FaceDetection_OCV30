/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package facedetection_ocv30;

import java.util.*;
import java.io.*;

/**
 *
 * @author Eduardo
 */
public class Configuracion {
    
    //Strings que buscaremos en el archivo de configuracion
    public static final String SECRETO_DBX = "secreto-dropbox";
    public static final String KEY_DBX = "llave-dropbox";
    public static final String KAIROS_APP_ID = "id-app-kairos";
    public static final String KAIROS_APP_KEY = "llave-app-kairos";
    public static final String NOMBRE_GALERIA = "nombre-galeria";
    public static final String STRING_APP_DBX = "string-app-dbx";
    public static final String DIRECTORIO_TRABAJO = "directorio-de-trabajo";
    public static final String ARCHIVO_TOKEN_DBX = "archivo-token-dbx";
    public static final String IP_CAMARAS = "ip-camaras";
    public static final String UBICACION_CAMARAS = "ubicacion-camaras";
    public static final String PROBABILIDAD_RECON = "probabilidad-reconocimiento-cara";
    //public static final String 
    
    
    //Propiedades donde almacenaremos los datos leidos del
    //archivo de texto
    private Properties propiedades;
    
    public Configuracion(String ruta){
        
        try{
            this.propiedades = new Properties();
            this.propiedades.load(new FileInputStream(ruta));
            
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("Error al crear configuracion");
        }
    }
    
    /**
     * obtiene un parametro del archivo de base de datos
     * @param nombreParametro
     * @return
     */
    public String obtenerParametro(String nombreParametro){
        try{
            return this.propiedades.getProperty(nombreParametro); 
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("Parametro no existe");
            return null;
        }
    }

    
}
