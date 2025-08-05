/**
 * Deep copy utility for Java objects using serialization.
 * 
 * Adapted from:
 * http://javatechniques.com/blog/faster-deep-copies-of-java-objects/
 * 
 * Note: Consider using modern alternatives like:
 * - Record classes with copy constructors
 * - Apache Commons Lang SerializationUtils
 * - Builder pattern with defensive copying
 */
package net.sourceforge.fddtools.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility for making deep copies (vs. clone()'s shallow copies) of
 * objects. Objects are first serialized and then deserialized. Error
 * checking is fairly minimal in this implementation. If an object is
 * encountered that cannot be serialized (or that references an object
 * that cannot be serialized) an error is logged and null is returned.
 * 
 * @deprecated Consider using modern alternatives like record classes,
 *             copy constructors, or Apache Commons Lang SerializationUtils
 */
@Deprecated(since = "1.0", forRemoval = false)
public final class DeepCopy {
    private static final Logger LOGGER = Logger.getLogger(DeepCopy.class.getName());
    
    /**
     * Private constructor to prevent instantiation.
     */
    private DeepCopy() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Returns a copy of the object, or null if the object cannot
     * be serialized.
     * 
     * @param orig the object to copy
     * @return a deep copy of the object, or null if serialization fails
     */
    public static Object copy(Object orig) {
        if (orig == null) {
            return null;
        }
        
        Object obj = null;
        try (var fbos = new FastByteArrayOutputStream();
             var out = new ObjectOutputStream(fbos)) {
            
            // Write the object out to a byte array
            out.writeObject(orig);
            out.flush();
            
            // Retrieve an input stream from the byte array and read
            // a copy of the object back in.
            try (var in = new ObjectInputStream(fbos.getInputStream())) {
                obj = in.readObject();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to serialize object", e);
        } catch (ClassNotFoundException cnfe) {
            LOGGER.log(Level.SEVERE, "Failed to deserialize object", cnfe);
        }
        return obj;
    }
    
    /**
     * Type-safe version of copy method using generics.
     * 
     * @param <T> the type of the object (must be Serializable)
     * @param orig the object to copy
     * @return a deep copy of the object, or null if serialization fails
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T copyTyped(T orig) {
        return (T) copy(orig);
    }
}