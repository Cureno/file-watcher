package com.blexven;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.HashSet;

class Watcher {

    private static HashSet<String> fileNamesToWatch = new HashSet<>();

    public static void main(String[] args) throws IOException, InterruptedException {


        if (args.length > 0) {
            System.out.println("Got some arguments ");

            fileNamesToWatch.addAll(Arrays.asList(args));
        }
        else {
            String fallbackFile = "hello.txt";
            fileNamesToWatch.add(fallbackFile);

            System.out.println("No files specified ");
        }

        fileNamesToWatch.forEach(filepath ->
                                         System.out.print("\twatching: " + filepath + "\n")
        );

        FileSystem fileSystem = FileSystems.getDefault();
        WatchService watchService = fileSystem.newWatchService();

        Path currentWorkingDirectory = fileSystem.getPath(System.getProperty("user.dir"));
        currentWorkingDirectory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
        System.out.println("Watching directory: " + currentWorkingDirectory.getFileName().toAbsolutePath());
        System.err.println();


        WatchKey key;

        while ((key = watchService.take()) != null) {

            long previousModification = 0;

            for (WatchEvent<?> event : key.pollEvents()) {

                Path filePath = ((Path) event.context());

                long thisModification = filePath.toFile().lastModified();
                boolean notTheSameModifiactionReportedASecondTime = !(thisModification - previousModification < 50);

                if (notTheSameModifiactionReportedASecondTime) {

                    // we do the work
                    if (fileNamesToWatch.contains(filePath.toString())) {
                        System.out.println("We care about this: " + filePath);

                        Path parent = filePath.toAbsolutePath().getParent();
                        Path resolved = parent.resolve("dist/index.html");
                        System.out.println("Resolved is: " + resolved);

                        new Pandoc(filePath, resolved).run();

                    }
                }

                previousModification = thisModification;

            }

            key.reset();

        }

    }

    static class Pandoc {

        private final Path outputFile;
        private final Path sourceFile;

        Pandoc(Path sourceFile, Path outputFile) {
            this.sourceFile = sourceFile;
            this.outputFile = outputFile;
            if (Files.notExists(outputFile.getParent())) {
                try {
                    Files.createDirectories(outputFile.getParent());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        void run() {

            ProcessBuilder pb = new ProcessBuilder("pandoc", "-s", sourceFile.toString(), "-o", outputFile.toString());
            pb.inheritIO(); // needed because the program "pandoc --help" hangs without it

            try {
                pb.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}