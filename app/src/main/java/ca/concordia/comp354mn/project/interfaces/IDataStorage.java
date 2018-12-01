package ca.concordia.comp354mn.project.interfaces;

public interface IDataStorage {

    /**
     * Avoids saving multiple records for the same day
     * @return true if a record exists for today, false otherwise
     */
    Boolean hasRecordForToday();

    /**
     * Save given data to a persistent storage medium (internal mem,network,etc)
     * @param string plaintext stringified representation of data
     * @return true if save was successful, false otherwise
     */
    Boolean savePersistent(String string);
}
