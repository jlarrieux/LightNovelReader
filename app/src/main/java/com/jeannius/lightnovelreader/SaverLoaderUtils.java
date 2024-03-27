package com.jeannius.lightnovelreader;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SaverLoaderUtils {
    private static final Object lock = new Object();

    public static HashMap<String, String> loadNovelMapFromLocal(String fileName, Context context){
        synchronized (lock) {
            try (FileInputStream fis = context.openFileInput(fileName);
                 ObjectInputStream ois =  new ObjectInputStream(fis)) {
                return  (HashMap<String, String>) ois.readObject();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return new HashMap<>();
        }
    }

    public static Set<String> loadSetFromLocal(String fileName, Context context){
        synchronized (lock) {

            try (FileInputStream fis = context.openFileInput(fileName);
                 ObjectInputStream ois =  new ObjectInputStream(fis)) {
                return  (Set<String>) ois.readObject();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return new HashSet<>();

        }
    }

    public static String loadFromLocal(String filename, Context context){
        synchronized (lock){
            try (FileInputStream fis = context.openFileInput(filename);
                 InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(inputStreamReader)
            ) {
                String line = reader.readLine();
                StringBuffer stringBuffer = new StringBuffer();
                if(line != null) {
                    stringBuffer.append(line);
                }
                JeanniusLogger.log(stringBuffer.toString());
                return stringBuffer.toString();

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            return "";

        }
    }

    public static void saveLocally(Map<String, String> map, String filename, Context context) {
        synchronized (lock) {
            try(FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos)){
                oos.writeObject(map);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static void saveLocally(String value, String filename, Context context){
        synchronized (lock) {
            try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
                fos.write(value.getBytes(StandardCharsets.UTF_8));
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    public static void saveLocally(Set<String> list, String filename, Context context){
        synchronized (lock) {
            try(FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos)){
                oos.writeObject(list);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
