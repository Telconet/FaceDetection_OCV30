/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package facedetection_ocv30;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import org.xml.sax.InputSource;
import org.w3c.dom.*;

/**
 *
 * @author Eduardo
 */
public class Vera {
    
    private String ip;
    private int idVera;
    
    public Vera(String ip, int idVera){
        this.ip = ip;
        this.idVera = idVera;
    }
    
    /**
     * Usa solicitud GET para ejecutar escena el el Vera3.
     * Respuesta devuelta como XML
     * @param escena
     * @return 
     */
    public boolean ejecutarEscena(int escena){
        try {
            String url = "http://" + ip +":3480/";
            String solicitud = "data_request?id=action&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=RunScene&SceneNum=" + escena;
            
             HttpResponse<String> respuesta = Unirest.get(url + solicitud)           
                .header("Content-Type", "text/xml")
                .header("Accept", "text/xml").asString();
             
             if(respuesta != null){
                 System.out.println(respuesta.getBody());
             }
            return true;
        } catch (UnirestException ex) {
            //Logger.getLogger(Vera.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * Respuesta devuelta cono XML
     * @param escena
     * @return 
     */
    public boolean grabarEscena(){
        try {
            String url = "http://" + ip +":3480/";
            String solicitud = "data_request?id=scene&action=record";
            
             HttpResponse<String> respuesta = Unirest.get(url + solicitud)           
                .header("Content-Type", "text/xml")
                .header("Accept", "text/xml").asString();
             
             if(respuesta != null){
                 System.out.println(respuesta.getBody());
             }
            return true;
        } catch (UnirestException ex) {
            //Logger.getLogger(Vera.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * Respuesta devuelta como XML
     * @return 
     */
    public boolean detenerGrabacionEscena(){
        try {
            String url = "http://" + ip +":3480/";
            String solicitud = "data_request?id=scene&action=stoprecord";
            
             HttpResponse<String> respuesta = Unirest.get(url + solicitud)           
                .header("Content-Type", "text/xml")
                .header("Accept", "text/xml").asString();
             
             if(respuesta != null){
                 System.out.println(respuesta.getBody());
             }
            return true;
        } catch (UnirestException ex) {
            //Logger.getLogger(Vera.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * respuesta es objeto JSON... Devuelve objeto JSON con los equipos que cambiaron de estado.
     * [{"id":1,"action":{"arguments":[{"name":"DeviceNum","value":"507"},{"name":"serviceId","value":"urn:upnp-org:serviceId:Dimming1"},{"name":"action","value":"SetLoadLevelTarget"},{"name":"newLoadlevelTarget","value":"100"},{"name":"rand","value":"0.8404575632885098"}],"action":"SetLoadLevelTarget","service":"urn:upnp-org:serviceId:Dimming1","device":507}},{"id":2,"action":{"arguments":[{"name":"DeviceNum","value":"507"},{"name":"serviceId","value":"urn:upnp-org:serviceId:Dimming1"},{"name":"action","value":"SetLoadLevelTarget"},{"name":"newLoadlevelTarget","value":"100"},{"name":"rand","value":"0.5078676079865545"}],"action":"SetLoadLevelTarget","service":"urn:upnp-org:serviceId:Dimming1","device":507}},{"id":3,"action":{"arguments":[{"name":"DeviceNum","value":"513"},{"name":"serviceId","value":"urn:upnp-org:serviceId:Dimming1"},{"name":"action","value":"SetLoadLevelTarget"},{"name":"newLoadlevelTarget","value":"100"},{"name":"rand","value":"0.8627623654901981"}],"action":"SetLoadLevelTarget","service":"urn:upnp-org:serviceId:Dimming1","device":513}}]
     * @return 
     * 
     */
    public boolean listarGrabacionEscena(){
        try {
            String url = "http://" + ip +":3480/";
            String solicitud = "data_request?id=scene&action=listrecord";
            
             HttpResponse<JsonNode> respuesta = Unirest.get(url + solicitud)           
                .header("Content-Type", "text/xml")
                .header("Accept", "application/json").asJson();
             
             if(respuesta != null){
                 System.out.println(respuesta.getBody());
             }
            return true;
        } catch (UnirestException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * Mostrar escena
     * @param escena
     * {"id":36,"name":"#7 ON ALL FOCOS","Timestamp":1422196337,"onDashboard":0,"groups":[{"delay":0,"actions":[{"arguments":[{"name":"newLoadlevelTarget","value":"100"}],"action":"SetLoadLevelTarget","service":"urn:upnp-org:serviceId:Dimming1","device":"507"},{"arguments":[{"name":"newLoadlevelTarget","value":"100"}],"action":"SetLoadLevelTarget","service":"urn:upnp-org:serviceId:Dimming1","device":"509"},{"arguments":[{"name":"Preset","value":"1"}],"action":"SetPreset","service":"urn:intvelt-com:serviceId:HueColors1","device":"510"},{"arguments":[{"name":"Preset","value":"1"}],"action":"SetPreset","service":"urn:intvelt-com:serviceId:HueColors1","device":"508"},{"arguments":[{"name":"Preset","value":"1"}],"action":"SetPreset","service":"urn:intvelt-com:serviceId:HueColors1","device":"512"},{"arguments":[{"name":"newLoadlevelTarget","value":"100"}],"action":"SetLoadLevelTarget","service":"urn:upnp-org:serviceId:Dimming1","device":"511"},{"arguments":[{"name":"Preset","value":"1"}],"action":"SetPreset","service":"urn:intvelt-com:serviceId:HueColors1","device":"514"},{"arguments":[{"name":"newLoadlevelTarget","value":"100"}],"action":"SetLoadLevelTarget","service":"urn:upnp-org:serviceId:Dimming1","device":"513"},{"arguments":[{"name":"Preset","value":"1"}],"action":"SetPreset","service":"urn:intvelt-com:serviceId:HueColors1","device":"516"},{"arguments":[{"name":"newLoadlevelTarget","value":"100"}],"action":"SetLoadLevelTarget","service":"urn:upnp-org:serviceId:Dimming1","device":"515"},{"arguments":[{"name":"Preset","value":"1"}],"action":"SetPreset","service":"urn:intvelt-com:serviceId:HueColors1","device":"518"},{"arguments":[{"name":"newLoadlevelTarget","value":"100"}],"action":"SetLoadLevelTarget","service":"urn:upnp-org:serviceId:Dimming1","device":"517"},{"arguments":[{"name":"Preset","value":"1"}],"action":"SetPreset","service":"urn:intvelt-com:serviceId:HueColors1","device":"520"},{"arguments":[{"name":"newLoadlevelTarget","value":"100"}],"action":"SetLoadLevelTarget","service":"urn:upnp-org:serviceId:Dimming1","device":"519"},{"arguments":[{"name":"Preset","value":"1"}],"action":"SetPreset","service":"urn:intvelt-com:serviceId:HueColors1","device":"522"},{"arguments":[{"name":"newLoadlevelTarget","value":"100"}],"action":"SetLoadLevelTarget","service":"urn:upnp-org:serviceId:Dimming1","device":"521"},{"arguments":[{"name":"Preset","value":"1"}],"action":"SetPreset","service":"urn:intvelt-com:serviceId:HueColors1","device":"524"},{"arguments":[{"name":"newLoadlevelTarget","value":"100"}],"action":"SetLoadLevelTarget","service":"urn:upnp-org:serviceId:Dimming1","device":"523"},{"arguments":[{"name":"Preset","value":"1"}],"action":"SetPreset","service":"urn:intvelt-com:serviceId:HueColors1","device":"526"},{"arguments":[{"name":"newLoadlevelTarget","value":"100"}],"action":"SetLoadLevelTarget","service":"urn:upnp-org:serviceId:Dimming1","device":"525"},{"arguments":[{"name":"Preset","value":"1"}],"action":"SetPreset","service":"urn:intvelt-com:serviceId:HueColors1","device":"528"},{"arguments":[{"name":"newLoadlevelTarget","value":"100"}],"action":"SetLoadLevelTarget","service":"urn:upnp-org:serviceId:Dimming1","device":"527"},{"arguments":[{"name":"Preset","value":"1"}],"action":"SetPreset","service":"urn:intvelt-com:serviceId:HueColors1","device":"530"},{"arguments":[{"name":"newLoadlevelTarget","value":"100"}],"action":"SetLoadLevelTarget","service":"urn:upnp-org:serviceId:Dimming1","device":"529"},{"arguments":[{"name":"Preset","value":"1"}],"action":"SetPreset","service":"urn:intvelt-com:serviceId:HueColors1","device":"532"},{"arguments":[{"name":"newLoadlevelTarget","value":"100"}],"action":"SetLoadLevelTarget","service":"urn:upnp-org:serviceId:Dimming1","device":"531"},{"arguments":[{"name":"Preset","value":"1"}],"action":"SetPreset","service":"urn:intvelt-com:serviceId:HueColors1","device":"534"},{"arguments":[{"name":"newLoadlevelTarget","value":"100"}],"action":"SetLoadLevelTarget","service":"urn:upnp-org:serviceId:Dimming1","device":"533"}]}],"room":1}
     */
    public boolean ListarEscena(int escena){
        try {
            String url = "http://" + ip +":3480/";
            String solicitud = "data_request?id=scene&action=list&scene=" + escena;
            
             HttpResponse<JsonNode> respuesta = Unirest.get(url + solicitud)           
                .header("Content-Type", "text/xml")
                .header("Accept", "application/json").asJson();
             
             if(respuesta != null){
                 System.out.println(respuesta.getBody());
             }
            return true;
        } catch (UnirestException ex) {
            //Logger.getLogger(Vera.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            return false;
        }
    }
    
    public boolean ejecutarHealing(){
         try {
            String url = "http://" + ip + "/";
             
            String solicitud = "port_49451/upnp/control/dev_1";
            //Unirest.po
            String healingXml = "<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body> <u:HealNetwork xmlns:u=\"urn:schemas-micasaverde-org:service:ZWaveNetwork:1\"><BatteryMinutes>0</BatteryMinutes><Node></Node><StressCycles>2</StressCycles><Configure>0</Configure><ManualRoute>0</ManualRoute></u:HealNetwork></s:Body></s:Envelope>";
         
          byte[] xmlBytes = healingXml.getBytes();
          
             HttpResponse<String> respuesta = Unirest.post(url + solicitud)           
                .header("Content-Type", "text/xml")
                .header("Accept", "text/xml, text/html, application/xml, text/javascript, */*").header("MIME-Version", "1.0").
                     header("Authorization","Digest username=\"ttopic\", realm=\"HomeControl\", nonce=\"fbd3a229f4d41d12d8ba073cd8ae2f91\", uri=\"/port_49451/upnp/control/dev_1\", response=\"9764724d2906b3d73387e2c9deeada6e\", qop=auth, nc=0000019f, cnonce=\"6e5d39dad65c2c38\"")
                     .header("X-Requested-with", "XMLHttpRequest").header("X-Prototype-Version", "1.7").header("SOAPACTION", "\"urn:schemas-micasaverde-org:service:ZWaveNetwork:1#HealNetwork\"").
                     header("Referer", "http://" + ip + "/cmh/").header("Pragma", "no-cache").header("Cache-Control", "no-cache").body(xmlBytes).asString();
             
             if(respuesta != null){
                 Bitacora log = new Bitacora();
                 log.registarEnBitacora("healing_status.txt", "healing_status.txt", respuesta.getBody(), Bitacora.INFO);
                 System.out.println(respuesta.getBody());
             }
            return true;
        } catch (Exception ex) {
            //Logger.getLogger(Vera.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            return false;
        }
    }
           
    
    
}
