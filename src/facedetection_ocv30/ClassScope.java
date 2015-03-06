/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package facedetection_ocv30;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Eduardo
 */
public class ClassScope {
    private java.lang.reflect.Field LIBRARIES;
    
    public ClassScope(){
        this.LIBRARIES = null;
    }
    
    public void setup(){
        try {
            LIBRARIES = ClassLoader.class.getDeclaredField("loadedLibraryNames");
            LIBRARIES.setAccessible(true);
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(ClassScope.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(ClassScope.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public String[] getLoadedLibraries(final ClassLoader loader) throws IllegalAccessException {
        Vector<String> libraries = (Vector<String>) LIBRARIES.get(loader);
        return libraries.toArray(new String[] {});
    }
}