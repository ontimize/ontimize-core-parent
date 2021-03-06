package com.ontimize.db;

import java.rmi.Remote;
import java.util.Hashtable;

public interface MetadataEntity extends Remote {

    public static final String INSERT_KEYS = "InsertKeys";

    /**
     * Autonumerical property.
     */
    public static final String AUTONUMERICAL = "Autonumerical";

    /**
     * GeneratedKey property
     */
    public static final String GENERATED_KEY = "GeneratedKey";


    public Hashtable getMetadata(int sessionId) throws Exception;

}
