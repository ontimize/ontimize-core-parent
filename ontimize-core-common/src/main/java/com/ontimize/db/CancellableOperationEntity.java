package com.ontimize.db;

import java.rmi.Remote;

public interface CancellableOperationEntity extends Remote {

    public String getOperationUniqueIdentifier() throws Exception;

    public void cancelOperation(String operationId) throws Exception;

}
