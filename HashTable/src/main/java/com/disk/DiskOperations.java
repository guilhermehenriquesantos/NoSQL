package com.disk;

import com.google.protobuf.ByteString;
import com.utils.BigIntegerHandler;
import com.utils.ByteStringHandler;
import com.utils.LongHandler;
import com.utils.ValueHandler;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiskOperations implements Disk{
    private BufferedWriter bufferedWriter = null;
    private final static Logger LOGGER = Logger.getLogger(DiskOperations.class.getName());

    public ConcurrentHashMap<BigInteger, ValueHandler> retrieveRecords(){
        BufferedReader bufferedReader = null;
        ConcurrentHashMap<BigInteger, ValueHandler> storage = new ConcurrentHashMap<>();
        if(checkFileCreation()){
            try{
                FileReader fileReader = new FileReader(PATH_FILE);
                bufferedReader = new BufferedReader(fileReader);
                String currentLine;
                while ((currentLine = bufferedReader.readLine()) != null){
                    String []data = currentLine.split("[\\s:]+", 4);
                    BigInteger key = BigIntegerHandler.fromStringToBigInteger(data[0]);
                    Long version = LongHandler.convertFromStringToLong(data[1]);
                    Long timestamp = LongHandler.convertFromStringToLong(data[2]);
                    ByteString dataBytes = ByteStringHandler.convertFromStringToByteString(data[3]);

                    ValueHandler valueHandler = new ValueHandler();
                    valueHandler.setVersion(version);
                    valueHandler.setTimestamp(timestamp);
                    valueHandler.setData(dataBytes);

                    storage.put(key, valueHandler);
                }
            }catch (IOException ioException){
                LOGGER.log(Level.INFO, "" + ioException.getCause());
            }finally {
                try {
                    bufferedReader.close();
                }catch (IOException ioException){
                    LOGGER.log(Level.INFO, "" + ioException.getCause());
                }
            }

        }
        return storage;
    }
    @Override
    public ValueHandler read(BigInteger key) {
        BufferedReader bufferedReader = null;
        ValueHandler valueHandler = new ValueHandler();
        try{
            FileReader fileReader = new FileReader(PATH_FILE);
            bufferedReader = new BufferedReader(fileReader);
            String currentLine;
            while((currentLine = bufferedReader.readLine()) != null){
                String [] data = currentLine.split("[\\s:]+", 4);
                BigInteger bigIntegerKey = BigIntegerHandler.fromStringToBigInteger(data[0]);
                if((bigIntegerKey.compareTo(key) == 0)){
                    String version = data[1];
                    String timestamp = data[2];
                    String dataBytes = data[3];

                    valueHandler.setVersion(LongHandler.convertFromStringToLong(version));
                    valueHandler.setTimestamp(LongHandler.convertFromStringToLong(timestamp));
                    valueHandler.setData(ByteStringHandler.convertFromStringToByteString(dataBytes));

                    return valueHandler;
                }
            }
        }catch(IOException ioException){
            ioException.printStackTrace();
        }finally {
            try{
                bufferedReader.close();
            }catch(IOException ioException){
                ioException.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public synchronized boolean write(ConcurrentHashMap<BigInteger, ValueHandler> hashMap){
        try{
            FileWriter fileWriter = new FileWriter(PATH_FILE);
            bufferedWriter = new BufferedWriter(fileWriter);
            for(Map.Entry<BigInteger, ValueHandler> entry: hashMap.entrySet()){
                bufferedWriter.write(entry.getKey() + " : "
                        + entry.getValue().getVersion() + "  "
                        + entry.getValue().getTimestamp() + " "
                        + entry.getValue().getData());
                bufferedWriter.newLine();
            }
        }catch (IOException ioException){
            ioException.printStackTrace();
            return false;
        }finally {
            try{
                bufferedWriter.close();
            }catch (IOException ioException){
                ioException.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public synchronized boolean delete(BigInteger key) {
        return true;
    }

    @Override
    public synchronized boolean delete(BigInteger key, long version) {
        return true;
    }

    @Override
    public boolean update(BigInteger key, ValueHandler valueHandler, long version) {
        return false;
    }

    private boolean checkFileCreation(){
        if(Files.exists(Paths.get(PATH_FILE)))
            return true;

        return false;
    }
}

