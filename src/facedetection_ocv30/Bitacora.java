/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package facedetection_ocv30;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Eduardo
 */
public class Bitacora {
    
    public static final int INFO = 0;
    public static final int WARNING = 1;
    public static final int SEVERE = 2;
    
    public void registarEnBitacora(String nombreLog, String ruta, String texto, int tipo){
        
        Logger logger = Logger.getLogger(nombreLog);  
        FileHandler fh;  
              
        try {  

            // This block configure the logger with handler and formatter  append
            fh = new FileHandler(ruta, true);  
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();  
            fh.setFormatter(formatter);  
            switch(tipo){
                case INFO:
                    logger.info(texto);
                    break;
                case WARNING:
                    logger.warning(texto);
                    break;
                case SEVERE:
                    logger.severe(texto);
                    break;
                default:
                    break;
            }
            
            fh.close();

        } catch (SecurityException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }
    
}
