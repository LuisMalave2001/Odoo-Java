/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arnowouter.javaodoo;

import com.arnowouter.javaodoo.exceptions.ConnectorException;
import com.arnowouter.javaodoo.util.DatabaseParams;
import com.arnowouter.javaodoo.util.Query;
import com.arnowouter.javaodoo.util.VersionInfo;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Arno
 */
public interface IOdooConnector {
    public Map<String,String> setupTestDataBase(URL url);
    public int authenticate() throws ConnectorException;
    public Object geoLocalize(int id) throws ConnectorException;
    public VersionInfo getVersion() throws ConnectorException;
    public Object[] getAllFieldsForModel(String model) throws ConnectorException;
    public Object[] read(String model, int[] requestedIds) throws ConnectorException;
    public Object[] read(String model, int[] requestedIds, Object[] requestedFields) throws ConnectorException;
    public int[] search(String model, Query query) throws ConnectorException;
    public int[] search(String model, Object[] query) throws ConnectorException;
    public int count(String model, Query query) throws ConnectorException;
    public int count(String model, Object[] query) throws ConnectorException;
    public Object[] searchAndRead(String model, Object[] requestedFields) throws ConnectorException;
    public Object[] searchAndRead(String model, Query query, Object[] requestedFields) throws ConnectorException;
    public Object[] searchAndRead(String model, Object[] query, Object[] requestedFields) throws ConnectorException;
    public Integer[] createRecords(String model, List<HashMap<String, Object>> listOfNewRecordValues) throws ConnectorException;
    public Integer[] createRecord(String model, HashMap<String, Object> newRecordValues) throws ConnectorException;
    public boolean updateRecords(String model, Integer[] recordIds, HashMap<String, Object> dataToUpdate) throws ConnectorException;
    public boolean deleteRecords(String model, Integer[] idsToBeDeleted) throws ConnectorException;
    public void setDbParams(DatabaseParams dbParams);

    public Object executeModelMethod(String model, String method, Object[] arguments, Map<String, Object> keywordArguments) throws ConnectorException;
    public Object executeModelMethod(String model, String method, Object[] arguments) throws ConnectorException;
    public Object executeModelMethod(String model, String method) throws ConnectorException;

    public Object executeMethod(String model, String method, Integer[] recordIds, Object[] arguments, Map<String, Object> keywordArguments) throws ConnectorException;
    public Object executeMethod(String model, String method, Integer[] recordIds, Object[] arguments) throws ConnectorException;
    public Object executeMethod(String model, String method, Integer[] recordIds) throws ConnectorException;

}
