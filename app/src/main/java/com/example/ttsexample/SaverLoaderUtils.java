package com.example.ttsexample;

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
import java.util.HashMap;
import java.util.Map;

public class SaverLoaderUtils {

    public static HashMap<String, String> loadNovelMapFromLocal(String fileName, Context context){
        try (FileInputStream fis = context.openFileInput(fileName);
             ObjectInputStream ois =  new ObjectInputStream(fis)) {
            return  (HashMap) ois.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    public static StringBuilder loadFromLocal(String filename, Context context){
        try (FileInputStream fis = context.openFileInput(filename);
             InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(inputStreamReader)
        ) {
            String line = reader.readLine();
            StringBuilder stringBuilder = new StringBuilder();
            if(line != null) {
                stringBuilder.append(line);
            }
            JeanniusLogger.log(stringBuilder.toString());
            return stringBuilder;

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return new StringBuilder("");
    }

    public static void saveLocally(Map<String, String> map, String filename, Context context) {
        try(FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos)){
            oos.writeObject(map);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveLocally(String value, String filename, Context context){
        try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(value.getBytes(StandardCharsets.UTF_8));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
