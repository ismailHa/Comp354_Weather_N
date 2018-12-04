package ca.concordia.comp354mn.project.persistence;
//
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.FilenameFilter;
//import java.util.ArrayList;
//import java.util.Calendar;
//
//import android.content.Context;
//import ca.concordia.comp354mn.project.interfaces.IDataStorage;
//import ca.concordia.comp354mn.project.utils.App;
//import ca.concordia.comp354mn.project.utils.Helpers;
//
//public class FileStorage implements IDataStorage {
//
//    @Override
//    public ArrayList<String> getFilesList() {
//        return null;
//    }
//
//    @Override
//    public void read(String filename) {
//
//    }
//
//    /**
//     * Returns true if a file has today's date
//     */
//    private class TodayFilter implements FilenameFilter {
//        @Override
//        public boolean accept(File file, String s) {
//
//            if(file.getName().startsWith(s)) {
//                return true;
//             } else {
//                return false;
//            }
//        }
//    }
//
//
//    final Context appContext = App.getAppContext();
//    final File storageDir = appContext.getFilesDir();
//    final Calendar calendar = Calendar.getInstance();
//
//    String todaysDate = Helpers.getFormattedDateToday();
//    TodayFilter filter = new TodayFilter();
//
//    /**
//     * Returns true if there is already a record for today's date.
//     * @param date
//     * @return
//     */
//    @Override
//    public Boolean hasRecordForToday() {
//        File[] records = storageDir.listFiles(filter);
//        return (records.length != 0);
//    }
//
//
//
//
//    /**
//     * Takes a JSON formatted string and saves it to persistent storage
//     * @param string
//     * @return
//     */
//    @Override
//    public void write(String filename, String content) {
//        FileOutputStream os;
//        if(filename.isEmpty()) {
//            filename = todaysDate + ".json";
//        }
//        try {
//            os = appContext.openFileOutput(filename, Context.MODE_PRIVATE);
//            os.write(content.getBytes());
//            os.close();
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//    }
//
