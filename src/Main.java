import java.io.*;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Main {

    private static void openZip(File pathZip, File pathFiles) throws IOException {
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(pathZip), Charset.forName("cp866"));
             BufferedInputStream fileIn = new BufferedInputStream(zipIn)) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    File dir = new File(pathFiles, entry.getName());
                    if (!dir.exists() && !dir.mkdirs())
                        throw new IOException("Ошибка при создании директории " + dir.getPath());
                } else {
                    int data;
                    byte[] b = new byte[128];
                    try (BufferedOutputStream fileOut =
                                 new BufferedOutputStream(
                                         new FileOutputStream(
                                                 new File(pathFiles, entry.getName()), false))) {
                        while ((data = fileIn.read(b)) != -1)
                            fileOut.write(b, 0, data);
                        fileOut.flush();
                    }
                }
                zipIn.closeEntry();
            }
        }
    }

    private static GameProgress openProgress(File file) throws Exception {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (GameProgress) in.readObject();
        }
    }

    public static void main(String[] args) {
        try (final Scanner scanner = new Scanner((System.in))) {
            System.out.print("Укажите путь к архиву [*.zip]: ");
            final File pathZip = new File(scanner.nextLine());
            if (pathZip.exists() && pathZip.isFile() && pathZip.getName().endsWith(".zip")) {
                System.out.print("Укажите путь для разархивирования " + pathZip.getName() + ": ");
                final File pathFiles = new File(scanner.nextLine());
                if ((pathFiles.exists() || pathFiles.mkdirs()) && pathFiles.isDirectory()) {
                    try {
                        openZip(pathZip, pathFiles);
                        File[] files = pathFiles.listFiles(((dir, name) -> name.endsWith(".dat")));
                        if (files != null && files.length != 0) {
                            System.out.println();
                            for (File f : files) {
                                try {
                                    System.out.println(openProgress(f));
                                } catch (Exception e) {
                                    System.err.println("Ошибка чтения файлов " + f.getName());
                                }
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Ошибка чтения файлов в формате zip");
                        System.err.println(e.getMessage());
                    }
                }
            }
        }
    }
}