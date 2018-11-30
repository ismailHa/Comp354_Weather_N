package ca.concordia.comp354mn.comp354mn_project;


import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.Calendar;

import android.content.Context;

public class FileStorage implements IDataStorage{


    /**
     * Returns true if a file has today's date
     */
    private class TodayFilter implements FilenameFilter {
        @Override
        public boolean accept(File file, String s) {

            if(file.getName().startsWith(s)) {
                return true;
             } else {
                return false;
            }
        }
    }


    final Context appContext = App.getAppContext();
    final File storageDir = appContext.getFilesDir();
    final Calendar calendar = Calendar.getInstance();


    final Integer year = calendar.get(Calendar.YEAR);
    final Integer month = calendar.get(Calendar.MONTH);
    final Integer day = calendar.get(Calendar.DATE);

    String todaysDate = String.format("%d-%d-%d", year, month, day);
    TodayFilter filter = new TodayFilter();

    /**
     * Returns true if there is already a record for today's date.
     * @param date
     * @return
     */
    @Override
    public Boolean hasRecordForToday() {
        File[] records = storageDir.listFiles(filter);
        return (records.length != 0);
    }

    /**
     * Takes a JSON formatted string and saves it to persistent storage
     * @param string
     * @return
     */
    @Override
    public Boolean savePersistent(String string) {
        FileOutputStream os;
        final String filename = todaysDate + ".json";
        try {
            os = appContext.openFileOutput(filename, Context.MODE_PRIVATE);
            os.write(string.getBytes());
            os.close();
        } catch(Exception e) {
                e.printStackTrace();
                return false;
            }
        return true;
        }

    }

