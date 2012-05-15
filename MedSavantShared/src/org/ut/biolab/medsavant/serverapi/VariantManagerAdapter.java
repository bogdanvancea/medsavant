package org.ut.biolab.medsavant.serverapi;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.sqlbuilder.Condition;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import org.ut.biolab.medsavant.format.CustomField;
import org.ut.biolab.medsavant.model.SimpleVariantFile;

/**
 *
 * @author mfiume
 */
public interface VariantManagerAdapter extends Remote
{

  public int uploadVariants(String sid, RemoteInputStream[] fileStreams, String[] fileNames, int projectId, int referenceId, String[][] variantTags, boolean includeHomoRef) throws RemoteException, IOException, Exception;
  public void publishVariants(String sid, int projectID, int referenceID, int updateID) throws Exception;
  public void publishVariants(String sid, int projectID) throws Exception;
  public void cancelPublish(String sid, int projectID, int referenceID, int updateID) throws Exception;
  public int updateTable(String sid, int projectId, int referenceId, int[] annotationIds, List<CustomField> variantFields) throws Exception;
  public int removeVariants(String sid, int projectId, int referenceId, List<SimpleVariantFile> files) throws Exception;
  public RemoteInputStream exportVariants(String sessionId, int projectId, int referenceId, Condition[][] conditions) throws SQLException, RemoteException, IOException, InterruptedException;
  
}
