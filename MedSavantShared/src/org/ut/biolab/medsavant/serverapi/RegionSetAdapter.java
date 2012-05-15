/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.ut.biolab.medsavant.serverapi;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;

import com.healthmarketscience.rmiio.RemoteInputStream;

import org.ut.biolab.medsavant.importing.FileFormat;
import org.ut.biolab.medsavant.model.GenomicRegion;
import org.ut.biolab.medsavant.model.RegionSet;


/**
 *
 * @author mfiume
 */
public interface RegionSetAdapter extends Remote {

    public void addRegionSet(String sessID, String regionSetName, int genomeID, RemoteInputStream fileStream, char delim, FileFormat fileFormat, int numHeaderLines) throws IOException, SQLException, RemoteException;
    public void removeRegionSet(String sessID, int regionSetID) throws SQLException, RemoteException;
    public List<RegionSet> getRegionSets(String sessID) throws SQLException, RemoteException;
    public int getNumberRegions(String sessID, int regionSetID) throws SQLException, RemoteException;
    public List<GenomicRegion> getRegionsInSet(String sessID, int regionSetID, int limit) throws SQLException, RemoteException;
    public boolean isNameInUse(String sessID, String name) throws SQLException, RemoteException;
}