/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package facedetection_ocv30;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Eduardo
 */
public class DeteccionCaras {
    
    public static final String URL_KAIROS = "http://api.kairos.com/";
    
    private String appId, appKey;
    
    public DeteccionCaras(String appId, String appKey){
        this.appId = appId;
        this.appKey = appKey;
    }
            
            
    /**
     * Enrola una persona con el id dado.
     * @param rutaImagen
     * @param idSujeto
     * @param galeria
     * @return 
     */
    public int enrolar(String rutaImagen, String idSujeto, String galeria){
        
        try{
                
            String request = "{\"url\":\"" + rutaImagen + "\",\"gallery_name\":\"" + galeria + "\",\"subject_id\":\"" +idSujeto + "\",\"selector\":\"SETPOSE\", \"symmetricFill\": \"true\"}";

            //Enviar solicitud... .header("X-Mashape-Key", "<required>") https://kairos-face-recognition.p.mashape.com/recognize  
            HttpResponse<JsonNode> respuesta = Unirest.post("http://api.kairos.com/enroll")           
            .header("Content-Type", "application/json")
            .header("app_id", this.appId)
            .header("app_key", this.appKey)
            .header("Accept", "application/json")
            .body(request)
            .asJson();
            
            if(respuesta.getBody().toString().contains("success")){
                return 0;
            }
            else{
                Bitacora log = new Bitacora();
                
                //Obtenemos el arreglo del objeto JSON (Error)          //CHECK si hay JSONObject??
                JSONArray arreglo = respuesta.getBody().getArray();
                JSONObject objetoJSON = arreglo.getJSONObject(0);
                
                //Luego obtenemos el objeto JSON con los errores y mensajes...
                arreglo = objetoJSON.getJSONArray("Errors");
                objetoJSON = arreglo.getJSONObject(0);
                
                //Extraemos los campos.
                StringBuilder str = new StringBuilder();
                Iterator<?> keys = objetoJSON.keys();
                
                str.append(respuesta.getStatus());
                str.append(" ");
                str.append(respuesta.getStatusText());
                str.append(" - ");
                
                int i = 0;
                //Extraemos los mensajes de error.
                while( keys.hasNext() ){
                    String key = (String)keys.next();
                   
                    if(i != 0){
                        str.append(", ");
                    }
                    str.append(key);
                    str.append(": ");
                    str.append(objetoJSON.get(key).toString());
                    i++;
                }
                                
                log.registarEnBitacora("log_errores","errores.txt", "Solicitud ENROLL: " + request, Bitacora.INFO);
                log.registarEnBitacora("log_errores","errores.txt", str.toString(), Bitacora.SEVERE);
                
                //Escribir que paso
                return -1;
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Detecta caras en la foto, y nos devuelve informacion de cada una.
     * @param rutaImagen
     * @return 
     */
    public JSONArray detectar(String rutaImagen){
        try{
            
            String request = "{\"url\":\"" + rutaImagen + "\",\"selector\":\"SETPOSE\",\"minHeadScale\":\".0625\"}";
                       
            HttpResponse<JsonNode> respuesta = Unirest.post("http://api.kairos.com/detect")           
            .header("Content-Type", "application/json")
            .header("app_id", appId)
            .header("app_key", appKey)
            .header("Accept", "application/json")
            .body(request)
            .asJson();
            
            if(respuesta.getBody().toString().contains("images")){
              
                JSONArray ret = respuesta.getBody().getArray().getJSONObject(0).getJSONArray("images").getJSONObject(0).getJSONArray("faces");
              
                //TODO: Procesar informacion caras.
                //Usamos getJSONObject(index) para obtener el objeto JSON con los atributos de la cara (index es el numbero de cara).
                return ret;
            }
            else{
                Bitacora log = new Bitacora();
                
                //Obtenemos el arreglo del objeto JSON (Error)
                JSONArray arreglo = respuesta.getBody().getArray();
                JSONObject objetoJSON = arreglo.getJSONObject(0);
                
                //Luego obtenemos el objeto JSON con los errores y mensajes...
                arreglo = objetoJSON.getJSONArray("Errors");
                objetoJSON = arreglo.getJSONObject(0);
                
                //Extraemos los campos.
                StringBuilder str = new StringBuilder();
                Iterator<?> keys = objetoJSON.keys();
                
                str.append(respuesta.getStatus());
                str.append(" ");
                str.append(respuesta.getStatusText());
                str.append(" - ");
                
                int i = 0;
                //Extraemos los mensajes de error.
                while( keys.hasNext() ){
                    String key = (String)keys.next();
                   
                    if(i != 0){
                        str.append(", ");
                    }
                    str.append(key);
                    str.append(": ");
                    str.append(objetoJSON.get(key).toString());
                    i++;
                }
                                
                log.registarEnBitacora("log_errores","errores.txt", "Solicitud DETECT: " + request, Bitacora.INFO);
                log.registarEnBitacora("log_errores","errores.txt", str.toString(), Bitacora.SEVERE);
                
                //Escribir que paso
                return null;
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    
    /**
     *Detecta caras en la foto, y nos devuelve informacion de cada una.
     *Devolvemos el id (nombre) de la cara más probable.
     * @param rutaImagen
     * @param galeria
     * @param probabilidadMinima
     * @return 
     */
    public String reconocer(String rutaImagen, String galeria, double probabilidadMinima){
        try{
            
            if(probabilidadMinima > 1.0){
                System.out.println("Probabilidad inválida (" + probabilidadMinima + ")");
                return "nadie";
            }
            
            String probabilidad;
            probabilidad = Double.toString(probabilidadMinima);
            
            String request = "{\"url\":\"" + rutaImagen + "\",\"gallery_name\":\"" + galeria + "\",\"minHeadScale\":\".0625\",\"threshold\":\""+ probabilidad + "\",\"selector\":\"SETPOSE\"}";
            
            HttpResponse<JsonNode> respuesta = Unirest.post("http://api.kairos.com/recognize")           
            .header("Content-Type", "application/json")
            .header("app_id", appId)
            .header("app_key", appKey)
            .header("Accept", "application/json")
            .body(request)
            .asJson();
            
            if(respuesta.getBody().toString().contains("success")){
              
                JSONArray ret = respuesta.getBody().getArray().getJSONObject(0).getJSONArray("images").getJSONObject(0).getJSONArray("candidates"); //.getJSONObject(0).getJSONArray("faces");
              
                //TODO: Procesar informacion caras.
                String persona = this.procesarResultadosCarasReconocidas(ret);
                return persona;
            }
            else if(respuesta.getBody().toString().contains("No match found")){
                System.out.println("KAIROS: No hubo match con nadie en la imagen");
                return "nadie";
            }
            else if(respuesta.getBody().toString().contains("no faces found")){
                 System.out.println("KAIROS: No se encontraron caras en la imagen");
                return "nadie";
            }
            
            else{
                Bitacora log = new Bitacora();
                
                System.out.println(respuesta.getBody());
                
                //Obtenemos el arreglo del objeto JSON (Error)
                JSONArray arreglo = respuesta.getBody().getArray();
                JSONObject objetoJSON = arreglo.getJSONObject(0);
                
                //Luego obtenemos el objeto JSON con los errores y mensajes...
                arreglo = objetoJSON.getJSONArray("Errors");
                objetoJSON = arreglo.getJSONObject(0);
                
                //Extraemos los campos.
                StringBuilder str = new StringBuilder();
                Iterator<?> keys = objetoJSON.keys();
                
                str.append(respuesta.getStatus());
                str.append(" ");
                str.append(respuesta.getStatusText());
                str.append(" - ");
                
                int i = 0;
                //Extraemos los mensajes de error.
                while( keys.hasNext() ){
                    String key = (String)keys.next();
                   
                    if(i != 0){
                        str.append(", ");
                    }
                    str.append(key);
                    str.append(": ");
                    str.append(objetoJSON.get(key).toString());
                    i++;
                }
                                
                log.registarEnBitacora("log_errores","errores.txt", "Solicitud DETECT: " + request, Bitacora.INFO);
                log.registarEnBitacora("log_errores","errores.txt", str.toString(), Bitacora.SEVERE);
                
                //Escribir que paso
                return "nadie";
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return "nadie";
        }
    }
        
    /**
     * Extraemos los resultados devueltos por Kairos dentro del objeto JSON.
     * @param resultados
     * @return 
     */
    private String procesarResultadosCarasReconocidas(JSONArray resultados){
        try{
            Map<String,ArrayList<Double>> multiMap = new HashMap<String,ArrayList<Double>>();

            for(int i = 0; i < resultados.length(); i++){
                JSONObject objetoJSON = resultados.getJSONObject(i);

                //Extraemos los mensajes de error.
                Iterator<?> keys = objetoJSON.keys();

                //No tomar en cuenta enrollment_timestamp
                //Necesitamos un multimap para guardar los porcentajes de match
                //para cada nombre

                while( keys.hasNext() ){
                    String key = (String)keys.next();

                    if(!key.equalsIgnoreCase("enrollment_timestamp")){
                        double probabilidad = Double.parseDouble(objetoJSON.get(key).toString());

                        if(multiMap.get(key) == null){
                            //Si no tenemos lista para este nombre, la agregamos
                            ArrayList<Double> arreglo = new ArrayList<>();
                            arreglo.add(probabilidad);
                            multiMap.put(key, arreglo);
                        }
                        else{
                            //Obtenemos el arreglo, y agregamos el valor
                            ArrayList<Double> arreglo = multiMap.get(key);
                            arreglo.add(probabilidad);
                        }

                        //System.out.println(key.toString() + " - " +objetoJSON.get(key).toString());
                    }
                }
                
                String persona = null;
                if(!multiMap.isEmpty()){
                   persona = this.tabularResultdaos(multiMap);
                   return persona;
                }
            }
            return null;
        }
        catch(Exception e){
            Bitacora log = new Bitacora();
            log.registarEnBitacora("excepciones","excepciones.txt", e.getMessage(), Bitacora.SEVERE);
            return null;
        }
    } 
    
    /**
     * Con los resultados de Kairos, los combinamos para obtener el nombre de la persona mas
     * probable.
     * @param resultados
     * @return 
     */
    private String tabularResultdaos(Map<String,ArrayList<Double>> resultados){
       try{
            System.out.println("TABULANDO RESULTADOS...!");
            //No tomar en cuenta enrollment_timestamp
            //Necesitamos un multimap para guardar los porcentajes de match
            //para cada nombre
           double promedioMasAlto = 0.0;
           int j = -1;
           ArrayList<String> nombres = new ArrayList<>();
           ArrayList<Double> promedios = new ArrayList<>();

            for(Entry<String, ArrayList<Double>> item: resultados.entrySet()){
                nombres.add(item.getKey());                 //nombre de la persona
                ArrayList listaProbabilidades = resultados.get(item.getKey());
                
                Iterator<?> iterador = listaProbabilidades.iterator();
                
                double promedio = 0.0;
                int i = 0;
                
                //Calcula promedio...
                while(iterador.hasNext()){
                    Double valor = (Double)iterador.next();
                    promedio += valor.doubleValue();
                    i++;
                }
                promedio = promedio / i;
                //System.out.println("Persona: " + item.getKey() + ", promedio: " + promedio);
                promedios.add(promedio);  
                
                
                if(promedio > promedioMasAlto){
                    j++;
                    promedioMasAlto = promedio;
                }
            }
            
            //TODO devolver nombre de persona más probable
            try{
                String personaProbable = nombres.get(j);
                String[] nombreApellido = personaProbable.split("-");
                                 
                
                return nombreApellido[0] + " " + nombreApellido[1];
            }
            catch(IndexOutOfBoundsException e){
                //No encontramos a nadie
                return "nadie";
            }
       }
       catch(Exception e){
           Bitacora log = new Bitacora();
           log.registarEnBitacora("excepciones","excepciones.txt", e.getMessage(), Bitacora.SEVERE);
           return null;
       }
    }
}
