package com.example.productivitybuddy_janamircic_kids.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ConfigLoader {

    private final Properties properties = new Properties();

    public ConfigLoader(String filename) throws FileNotFoundException {
        try(InputStream inputStream = new FileInputStream(filename)){
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long getMonitorInterval() {
        return Long.parseLong(properties.getProperty("monitor.interval"));
    }

    public String getMappingFile(){
        return properties.getProperty("mapping.file");
    }

    public long getSnapshotInterval() {
        return Long.parseLong(properties.getProperty("snapshot.interval"));
    }

    public List<String> getFixedSnapshotTimes(){
        List<String> fixedSnapshotTimes = new ArrayList<>();
        int i = 1;
        while(true){
            String time = properties.getProperty("snapshot.fixed_time_" + i);
            if(time == null) break;
            fixedSnapshotTimes.add(time);
            i++;
        }
        return fixedSnapshotTimes;
    }
}
