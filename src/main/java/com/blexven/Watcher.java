package com.blexven;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.Properties;

class Watcher {

    private String cwd = System.getProperty("user.dir");
    private WatchService watchService;
    private Properties filesAndCommands = loadConfigurationFrom(cwd + "/" + "watcher.properties");

    public static void main(String[] args) throws IOException, InterruptedException {

        new Watcher().watchJarDirectoryAndRunRegisteredCommands();

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

    public Properties getFilesAndCommands() {
        return filesAndCommands;
    }

    private void runRegisteredCommand(String fileName) {
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

    public void watchJarDirectoryAndRunRegisteredCommands() throws IOException, InterruptedException {
        watchService = FileSystems.getDefault().newWatchService();

        FileSystems.getDefault().getPath(cwd)
                   .register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

        WatchKey key;

        try {
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
        } catch (ClosedWatchServiceException e) {
            System.out.println("Closing done.");
        }
    }

    public void stop () {
        try {
            watchService.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}