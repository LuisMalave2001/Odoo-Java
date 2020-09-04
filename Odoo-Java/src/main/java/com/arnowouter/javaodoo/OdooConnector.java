/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arnowouter.javaodoo;

import com.arnowouter.javaodoo.client.Client;
import com.arnowouter.javaodoo.defaults.ConnectorDefaults;
import com.arnowouter.javaodoo.exceptions.ConnectorException;
import com.arnowouter.javaodoo.exceptions.ExceptionMessages;
import com.arnowouter.javaodoo.util.DatabaseParams;
import com.arnowouter.javaodoo.util.VersionInfo;
import de.timroes.axmlrpc.XMLRPCException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import java.util.*;

import com.arnowouter.javaodoo.util.Query;
import org.jetbrains.annotations.NotNull;

/**
 *
 * @author  Arno Soontjens
 * @author  Luis Mora
 */


public class OdooConnector {
    
    private Client odooClient;
    
    private String protocol;
    private String hostName;
    private int connectionPort;
    private DatabaseParams dbParams;
    
    private int odooUserId;

    public OdooConnector() {
    }
    
    public OdooConnector(String hostName) throws MalformedURLException{
        this(hostName, false);
    }
    
    public OdooConnector(String hostName, boolean ignoreInvalidSSL) throws MalformedURLException {
        URL newURL = new URL(hostName);
        odooClient = new Client(newURL,ignoreInvalidSSL);
    }
    
    public OdooConnector(String protocol, String hostName, int connectionPort) throws ConnectorException {
        this(protocol,hostName,connectionPort,false);
    }
    
    public OdooConnector(String protocol, String hostName, int connectionPort, boolean ignoreInvalidSSL)
            throws ConnectorException 
    {
        this.protocol = protocol;
        this.hostName = hostName;
        this.connectionPort = connectionPort;
        try {
            URL newURL = new URL(protocol,hostName,String.valueOf(connectionPort));
            odooClient = new Client(newURL,ignoreInvalidSSL);
        } catch (MalformedURLException ex) {
            throw new ConnectorException(ex.getMessage(), ex);
        }
    }
    
    public OdooConnector(String hostName, DatabaseParams dbParams) throws MalformedURLException {
        this(hostName,dbParams,false);
    }
    
    public OdooConnector(String hostName, DatabaseParams dbParams, boolean ignoreInvalidSSL) throws MalformedURLException {
        URL newURL = new URL(hostName);
        this.dbParams = dbParams;
        odooClient = new Client(newURL, ignoreInvalidSSL);
    }
    
    public OdooConnector(String protocol, String hostName, int connectionPort, DatabaseParams dbParams) throws ConnectorException {
        this(protocol,hostName,connectionPort,false);
    }
    
    public OdooConnector(String protocol, String hostName, int connectionPort, DatabaseParams dbParams, boolean ignoreInvalidSSL)
            throws ConnectorException 
    {
        this.protocol = protocol;
        this.hostName = hostName;
        this.connectionPort = connectionPort;
        this.dbParams = dbParams;
        this.odooUserId = -1;
        createClient(ignoreInvalidSSL);
    }
  
    public OdooConnector(String protocol, String hostName, int connectionPort, String databaseName, String databaseLogin, String databasePassword, boolean ignoreInvalidSSL)
            throws ConnectorException 
    {
        this.protocol = protocol;
        this.hostName = hostName;
        this.connectionPort = connectionPort;
        this.dbParams = new DatabaseParams(databaseName, databaseLogin, databasePassword);
        this.odooUserId = -1;
        createClient(ignoreInvalidSSL);
    }
    
public Map<String,String> setupTestDataBase(URL url) {
        Map<String, String> info = null;
        try {
            Client client = new Client(url);
            info = client.callToStartTestDatabase(url);
        } catch (MalformedURLException | XMLRPCException ex) {
            System.out.println(ex.getMessage());
        }
        return info;
    }
            
public int authenticate() throws ConnectorException {
        try {
            odooUserId = odooClient.authenticate(dbParams);
            return odooUserId;
        } catch (XMLRPCException ex) {
            throw new ConnectorException(ex.getMessage(), ex);
        }
    }
    
public VersionInfo getVersion() throws ConnectorException {
        try {
            return odooClient.getVersion();
        } catch (XMLRPCException ex) {
            throw new ConnectorException(ex.getMessage(), ex);
        }
    }
    
public Object[] getAllFieldsForModel(String model) throws ConnectorException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
public Object geoLocalize(int id) throws ConnectorException {
        
        try {
            Object[] params = {
                dbParams.getDatabaseName(),
                odooUserId,
                dbParams.getDatabasePassword(),
                "res.partner",
                ConnectorDefaults.ACTION_UPDATE_LOCATION,
                asList(id)
            };

            return (Object) odooClient.updateGeoLocation(params);
        } catch (XMLRPCException ex) {
            System.out.println(ex.getMessage());
            throw new ConnectorException(ex.getMessage(), ex);
        }
    }


public Integer[] createRecords(String model, List<HashMap<String, Object>> listOfNewRecordValues) throws ConnectorException {
        Object[] recordIds = (Object[]) this.executeModelMethod("account.move", "create", listOfNewRecordValues.toArray());
        Integer[] recordIdsList = Arrays.asList(recordIds).toArray(new Integer[0]);
        return recordIdsList;
    }

public Integer[] createRecord(String model, HashMap<String, Object> newRecordValues) throws ConnectorException {
        return this.createRecords(model, new ArrayList<>(singletonList(newRecordValues)));
    }
    
public Object[] read(String model, int[] requestedIds) throws ConnectorException {
        return read(model, requestedIds, new Object[0]);
    }
    
public Object[] read(String model, int[] requestedIds, Object[] requestedFields) throws ConnectorException {
        if(!isAuthenticated()) throw new ConnectorException(ExceptionMessages.EX_MSG_NOT_AUTHENTENTICATED);
        Object[] idsToRead = new Object[requestedIds.length];
        for(int i=0;i<requestedIds.length;i++) {
            idsToRead[i] = requestedIds[i];
        }
        
        try {
            Object[] params = {
                dbParams.getDatabaseName(),
                odooUserId,
                dbParams.getDatabasePassword(),
                model,
                ConnectorDefaults.ACTION_READ,
                asList(asList(idsToRead)),
                new HashMap() {{
                    put(ConnectorDefaults.ODOO_FIELDS, asList(requestedFields));
                }}
            };

            return (Object[]) odooClient.read(params);
        } catch (XMLRPCException ex) {
            throw new ConnectorException(ex.getMessage(), ex);
        }
    }
    
public int count(String model, Query query) throws ConnectorException {
        return count(model, query.getQueryObject());
    }
    
public int count(String model, Object[] query) throws ConnectorException {
        //TODO: implement this
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
public int[] search(String model, Query query) throws ConnectorException {
        return search(model, query.getQueryObject());
    }

public int[] search(String model, Object[] query) throws ConnectorException {
        try {
            Object[] params = {
                dbParams.getDatabaseName(),
                odooUserId,
                dbParams.getDatabasePassword(),
                model,
                ConnectorDefaults.ACTION_SEARCH,
                asList(asList(query))
                    //TODO: implement offset and limit (pagination)
            };
            return odooClient.search(params);
        } catch (XMLRPCException ex) {
            throw new ConnectorException(ex.getMessage(), ex);
        }
    }

public Object[] searchAndRead(String model, Object[] requestedFields) throws ConnectorException {
        return searchAndRead(model, new Object[0],requestedFields);
    }
    
public Object[] searchAndRead(String model, Query query, Object[] requestedFields) throws ConnectorException {
        return searchAndRead(model, query.getQueryObject(), requestedFields);
    }
    
public Object[] searchAndRead(String model, Object[] query, Object[] requestedFields) throws ConnectorException {

        Map<String, Object> requestedFieldMap = new HashMap<>();
        requestedFieldMap.put(ConnectorDefaults.ODOO_FIELDS, asList(requestedFields));
        return (Object[]) this.executeModelMethod(model, ConnectorDefaults.ACTION_SEARCH_READ, query, requestedFieldMap);

//        if(!isAuthenticated()) throw new ConnectorException(ExceptionMessages.EX_MSG_NOT_AUTHENTENTICATED);
//        try {
//            Object[] params = new Object[]{
//                dbParams.getDatabaseName(),
//                odooUserId,
//                dbParams.getDatabasePassword(),
//                model,
//                ,
//                asList(asList(query)),
//
//            };
//            return (Object[]) odooClient.searchAndRead(params);
//        } catch (XMLRPCException ex) {
//            throw new ConnectorException(ex.getMessage(), ex);
//        }
    }

public boolean updateRecords(String model, Integer[] recordIds, HashMap<String, Object> dataToUpdate) throws ConnectorException {
        List<HashMap<String, Object>> dataToUpdateAsList = new ArrayList<>(singletonList(dataToUpdate));
        return (boolean) this.executeMethod(model, ConnectorDefaults.ACTION_UPDATE_RECORD, recordIds, dataToUpdateAsList.toArray());
    }

public boolean deleteRecords(String model, Integer[] idsToBeDeleted) throws ConnectorException {
        return (boolean) this.executeMethod(model, ConnectorDefaults.ACTION_DELETE_RECORD, idsToBeDeleted);
    }
    
    private boolean isAuthenticated() {
        return odooUserId != -1;
    }
    
    private void createClient(boolean ignoreInvalidSSL) throws ConnectorException {
        try {
            odooClient = new Client(protocol, hostName, connectionPort, ignoreInvalidSSL);
        } catch (MalformedURLException ex) {
            throw new ConnectorException(ex.getMessage(), ex);
        }
    }

    public void setProtocol(String protocol) {this.protocol = protocol;}
    public void setHostName(String hostName) {this.hostName = hostName;}
    public void setConnectionPort(int connectionPort) {this.connectionPort = connectionPort;}
public void setDbParams(DatabaseParams dbParams) {this.dbParams = dbParams;}

public Object executeModelMethod(String model, String method, Object[] arguments, Map<String, Object> keywordArguments) throws ConnectorException {

        if (model == null) {throw new NullPointerException("Model cannot be null");}
        if (method == null) {throw new NullPointerException("Method cannot be null");}
        if (arguments == null) {throw new NullPointerException("Arguments cannot be null");}

        if(!isAuthenticated()) throw new ConnectorException(ExceptionMessages.EX_MSG_NOT_AUTHENTENTICATED);
        try {
            ArrayList<Object> paramList = this.prepareArgsList(model, method);

            // Arguments
            paramList.add(singletonList(asList(arguments)));

            // keywordArguments
            if (keywordArguments != null && !keywordArguments.isEmpty()) {
                paramList.add(keywordArguments);
            }

            Object[] paramsArray = paramList.toArray();

            return odooClient.executeModelMethod(paramsArray);
        } catch (XMLRPCException ex) {
            throw new ConnectorException(ex.getMessage(), ex);
        }
    }

public Object executeModelMethod(String model, String method, Object[] arguments) throws ConnectorException {
        return this.executeModelMethod(model, method, arguments, null);
    }

public Object executeModelMethod(String model, String method) throws ConnectorException {
        return this.executeModelMethod(model, method, new Object[0]);
    }

public Object executeMethod(String model, String method, Integer[] recordIds, Object[] arguments, Map<String, Object> keywordArguments) throws ConnectorException {

        if (model == null) {throw new NullPointerException("Model cannot be null");}
        if (method == null) {throw new NullPointerException("Method cannot be null");}
        if (recordIds == null) {throw new NullPointerException("RecordIds cannot be null");}
        if (arguments == null) {throw new NullPointerException("Arguments cannot be null");}

        if(!isAuthenticated()) throw new ConnectorException(ExceptionMessages.EX_MSG_NOT_AUTHENTENTICATED);
        try {
            ArrayList<Object> paramList = this.prepareArgsList(model, method);

            ArrayList<Object> argumentArrayList = new ArrayList<>();

            argumentArrayList.add(asList(recordIds));

            // Arguments
            if (arguments.length > 0) {
                argumentArrayList.addAll(asList(arguments));
            }

            // keywordArguments
            if (keywordArguments != null && !keywordArguments.isEmpty()) {
                paramList.add(keywordArguments);
            }

            paramList.add(argumentArrayList);
            Object[] paramsArray = paramList.toArray();

            return odooClient.executeModelMethod(paramsArray);
        } catch (XMLRPCException ex) {

            if (model.equals("account.move") && ex.getMessage().contains("cannot marshal None unless allow_none is enabled")){
                return null;
            }

            throw new ConnectorException(ex.getMessage(), ex);
        }
    }

public Object executeMethod(String model, String method, Integer[] recordIds, Object[] arguments) throws ConnectorException {
        return this.executeMethod(model, method, recordIds, arguments, null);
    }

public Object executeMethod(String model, String method, Integer[] recordIds) throws ConnectorException {
        return this.executeMethod(model, method, recordIds, new Object[0], null);
    }

    private ArrayList<Object> prepareArgsList(String model, String method){
        ArrayList<Object> paramList = new ArrayList<>();

        paramList.add(dbParams.getDatabaseName());
        paramList.add(odooUserId);
        paramList.add(dbParams.getDatabasePassword());
        paramList.add(model);
        paramList.add(method);

        return paramList;
    }

    public void setOdooUserId(int odooUserId) {this.odooUserId = odooUserId;}

    public String getProtocol() {return protocol;}
    public String getHostName() {return hostName;}
    public int getConnectionPort() {return connectionPort;}
    public DatabaseParams getDbParams() {return dbParams;}
    public int getOdooUserId() {return odooUserId;}
}
