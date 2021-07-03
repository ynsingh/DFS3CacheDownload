package com.ehelpy.brihaspati4.dfs3Ufs1Core.dfs3Mgr;


import com.ehelpy.brihaspati4.simulateGC.communication.Receiver;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is used to create object of DFSBufferMgr.
 * There are inputRoutingBuffer and outputRoutingBuffer.
 */
public class DFS3BufferMgr {

    private static final List<File> inputDFSBuffer = new LinkedList<>();
    private static final List<File> outputDFSBuffer = new LinkedList<>();
    private static final ReentrantLock inputBufferLock = new ReentrantLock();
    private static final ReentrantLock outputBufferLock = new ReentrantLock();
    private static DFS3BufferMgr dfsBufferMgr;
    private static final Logger log = Logger.getLogger(Receiver.class.getName());

    /**
     * This is the default constructor of the class.
     * However this is made private so that it cannot be accessed from outside the class.
     */
    private DFS3BufferMgr() {
    }

    /**
     * @return - Object of DFSBufferMgr.
     * This is made singleton object as only one instance can be accessed.
     */
    public static DFS3BufferMgr getInstance() {
        if (dfsBufferMgr == null) {
            dfsBufferMgr = new DFS3BufferMgr();
        }
        return dfsBufferMgr;
    }

    /**
     * @return - object of inputRoutingBuffer
     */
    private List<File> getInputDFSBuffer() {
        return inputDFSBuffer;
    }

    /**
     * @param file - The File object is given as input argument.
     * @return - boolean value true if the file is added successfully.
     */
    public boolean addToInputBuffer(File file) {
        inputBufferLock.lock();
        inputDFSBuffer.add(file);
        file.deleteOnExit();
        log.debug("File added to Input buffer");
        inputBufferLock.unlock();
        return true;
    }

    /**
     * This method is used to fetch file from the inputBuffer one by one.
     * @return - File
     */
    public File fetchFromInputBuffer() {
        inputBufferLock.lock();
        File file = null;
        try{
            file = inputDFSBuffer.get(0);
            inputDFSBuffer.remove(0);
        } catch(Exception e) {
            log.debug("There is no File in Input Buffer");
        }
        inputBufferLock.unlock();
        return file;
    }

    /**
     * @param file - File object is given as input argument.
     * @return - true if the file is added successfully.
     */
    public boolean addToOutputBuffer(File file) {
        outputBufferLock.lock();
        outputDFSBuffer.add(file);
        log.debug("File added to Output buffer");
        file.deleteOnExit();
        outputBufferLock.unlock();
        return true;
    }

    /**
     * This method is used to fetch file from the bufferMgr.
     * @return - File.
     */
    public File fetchFromOutputBuffer() {
        outputBufferLock.lock();
        File file = null;
        try{
            file = outputDFSBuffer.get(0);
            outputDFSBuffer.remove(0);
        } catch(Exception e) {
            log.debug("There is no File in Output Buffer");
        }
        outputBufferLock.unlock();
        return file;
    }
}
