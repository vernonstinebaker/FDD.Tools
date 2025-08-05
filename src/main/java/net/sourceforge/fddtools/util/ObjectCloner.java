/**
 * Modern object cloning utility using serialization.
 * 
 * This class provides deep copying functionality for Serializable objects
 * using Java's serialization mechanism. It serves as a replacement for
 * the deprecated DeepCopy class.
 * 
 * For better performance and type safety, consider implementing:
 * - Copy constructors in your classes
 * - The Cloneable interface with proper clone() methods
 * - Builder pattern with defensive copying
 */
package net.sourceforge.fddtools.util;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for creating deep copies of Serializable objects.
 * 
 * This implementation uses Java serialization to create deep copies,
 * ensuring that all nested objects are also copied (provided they
 * implement Serializable).
 * 
 * @since 1.0
 */
public final class ObjectCloner {
    private static final Logger LOGGER = Logger.getLogger(ObjectCloner.class.getName());
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private ObjectCloner() {
        throw new AssertionError("Utility class - do not instantiate");
    }
    
    /**
     * Creates a deep copy of the provided object using serialization.
     * 
     * The object and all its nested objects must implement Serializable
     * for this method to work correctly.
     * 
     * @param original the object to clone
     * @return a deep copy of the object, or null if cloning fails
     */
    public static Object deepClone(Object original) {
        if (original == null) {
            return null;
        }
        
        if (!(original instanceof Serializable)) {
            LOGGER.log(Level.WARNING, 
                "Object of type {0} does not implement Serializable", 
                original.getClass().getName());
            return null;
        }
        
        try {
            // Serialize the object to a byte array
            FastByteArrayOutputStream fbos = new FastByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(fbos)) {
                oos.writeObject(original);
                oos.flush();
            }
            
            // Deserialize from the byte array to create a deep copy
            try (ObjectInputStream ois = new ObjectInputStream(fbos.getInputStream())) {
                return ois.readObject();
            }
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, 
                "Failed to clone object due to I/O error", e);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, 
                "Failed to clone object due to class not found", e);
        }
        
        return null;
    }
    
    /**
     * Creates a type-safe deep copy of the provided object.
     * 
     * This method provides compile-time type safety by using generics.
     * The object and all its nested objects must implement Serializable.
     * 
     * @param <T> the type of the object (must extend Serializable)
     * @param original the object to clone
     * @return a deep copy of the object, or null if cloning fails
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deepCloneTyped(T original) {
        return (T) deepClone(original);
    }
    
    /**
     * Checks if an object can be cloned using this utility.
     * 
     * @param obj the object to check
     * @return true if the object implements Serializable, false otherwise
     */
    public static boolean isCloneable(Object obj) {
        return obj instanceof Serializable;
    }
}