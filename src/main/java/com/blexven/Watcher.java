package com.blexven;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.Properties;

class Watcher {

    private static String cwd = System.getProperty("user.dir");
    private static Properties filesAndCommands = loadConfigurationFrom(cwd + "/" + "watcher.properties");

    public static void main(String[] args) throws IOException, InterruptedException {

        WatchService watchService = FileSystems.getDefault().newWatchService();

        FileSystems.getDefault().getPath(cwd)
                   .register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

        WatchKey key;

        while ((key = watchService.take()) != null) {

            long previousModification = 0;

            for (WatchEvent<?> event : key.pollEvents()) {

                Path filePath = ((Path) event.context());
                String fileName = filePath.toString();

                long thisModification = filePath.toFile().lastModified();
                boolean notTheSameModificationReportedMultipleTimes = !(thisModification - previousModification < 50);

                if (notTheSameModificationReportedMultipleTimes &&
                    filesAndCommands.containsKey(fileName)) {

                    runRegisteredCommand(fileName);

                }

                previousModification = thisModification;

            }

            key.reset();

        }
    }

    private static Properties loadConfigurationFrom(String propertiesFile) {
        Properties properties = new Properties();
        try {
            FileReader reader = new FileReader(propertiesFile);
            properties.load(reader);

        } catch (FileNotFoundException e) {
            System.err.println("File not found : " + propertiesFile);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error loading properties from: " + propertiesFile);
            e.printStackTrace();
        }

        return properties;
    }


    private static void runRegisteredCommand(String fileName) {
        System.out.println("Should run the command: " + filesAndCommands.getProperty(fileName));

        String[] command = filesAndCommands.getProperty(fileName)
                                           .split(" ");

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO(); // needed because the program "pandoc --help" hangs without it

        try {
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}