package ca.concordia.comp354mn.comp354mn_project;

import java.util.Date;

public interface IDataStorage {

    /**
     * Avoids saving multiple records for the same day
     * @return Boolean
     */
    Boolean hasRecordForToday();

    Boolean savePersistent(String string);
}
