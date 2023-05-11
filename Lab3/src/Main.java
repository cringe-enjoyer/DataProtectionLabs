import java.io.*;
import java.util.ArrayList;
public class Main {
    private static String key = "";
    private static final int HEADER = 110;
    public static void main(String[] args) throws IOException {
        String inputFilePath = "F:\\OpenSSLLabs\\Labs\\DefenseLabs\\Lab3\\res\\tux.bmp";
        String bytesImageFile = "F:\\OpenSSLLabs\\Labs\\DefenseLabs\\Lab3\\res\\tux.txt";
        String bytesEncryptImageFile = "F:\\OpenSSLLabs\\Labs\\DefenseLabs\\Lab3\\res\\tuxEnc.txt";
        createKey();
        System.out.println(key);

        String outputFilePath = "F:\\OpenSSLLabs\\Labs\\DefenseLabs\\Lab3\\res\\tux-" + Mods.ECB + ".bmp";
        String decryptedFilePath = "F:\\OpenSSLLabs\\Labs\\DefenseLabs\\Lab3\\res\\tux-" + Mods.ECB + "-decrypt.bmp";
        cryptAES(inputFilePath, bytesImageFile, bytesEncryptImageFile, outputFilePath, Mods.ECB, false);
        cryptAES(outputFilePath, bytesImageFile, bytesEncryptImageFile, decryptedFilePath, Mods.ECB, true);

        outputFilePath = "F:\\OpenSSLLabs\\Labs\\DefenseLabs\\Lab3\\res\\tux-" + Mods.CBC + ".bmp";
        decryptedFilePath = "F:\\OpenSSLLabs\\Labs\\DefenseLabs\\Lab3\\res\\tux-" + Mods.CBC + "-decrypt.bmp";
        cryptAES(inputFilePath, bytesImageFile, bytesEncryptImageFile, outputFilePath, Mods.CBC, false);
        cryptAES(outputFilePath, bytesImageFile, bytesEncryptImageFile, decryptedFilePath, Mods.CBC, true);

        outputFilePath = "F:\\OpenSSLLabs\\Labs\\DefenseLabs\\Lab3\\res\\tux-" + Mods.CFB + ".bmp";
        decryptedFilePath = "F:\\OpenSSLLabs\\Labs\\DefenseLabs\\Lab3\\res\\tux-" + Mods.CFB + "-decrypt.bmp";
        cryptAES(inputFilePath, bytesImageFile, bytesEncryptImageFile, outputFilePath, Mods.CFB, false);
        cryptAES(outputFilePath, bytesImageFile, bytesEncryptImageFile, decryptedFilePath, Mods.CFB, true);

        outputFilePath = "F:\\OpenSSLLabs\\Labs\\DefenseLabs\\Lab3\\res\\tux-" + Mods.OFB + ".bmp";
        decryptedFilePath = "F:\\OpenSSLLabs\\Labs\\DefenseLabs\\Lab3\\res\\tux-" + Mods.OFB + "-decrypt.bmp";
        cryptAES(inputFilePath, bytesImageFile, bytesEncryptImageFile, outputFilePath, Mods.OFB, false);
        cryptAES(outputFilePath, bytesImageFile, bytesEncryptImageFile, decryptedFilePath, Mods.OFB, true);
    }

    private static void cryptAES(String inputFilePath, String tmpFilePath, String encTmpFilePath, String outputFilePath,
                                 Mods mode, boolean decrypt) {
        byte[] header = new byte[0];
        byte[] imageBytes = new byte[0];
        try (FileInputStream reader = new FileInputStream(new File(inputFilePath))) {
            header = new byte[HEADER]; // Заголовок картинки
            reader.read(header);
            imageBytes = new byte[reader.available()]; // Байты картинки
            reader.read(imageBytes);
        } catch (Exception e) {
            System.out.println(e);
        }
        try (FileOutputStream tmpImageFile = new FileOutputStream(tmpFilePath)) {
            tmpImageFile.write(imageBytes); // Сохраняем байты картинки в отдельный файл
        } catch (Exception ex) {
            System.out.println(ex);
        }

        opensslEnc(tmpFilePath, encTmpFilePath, mode, decrypt);

        byte[] cryptedImageBytes = new byte[0];
        try (FileInputStream tmpBytesFis = new FileInputStream(encTmpFilePath)) {
            cryptedImageBytes = new byte[tmpBytesFis.available()];
            tmpBytesFis.read(cryptedImageBytes); // Извлекаем обработанные байты
        } catch (Exception ex) {
            System.out.println(ex);
        }

        try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
            fos.write(header); // Незашифрованный заголовок
            fos.write(cryptedImageBytes); // Обработанные байты
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    /**
     * Метод шифровки/дешифровки
     *
     * @param inputFilePath путь файла картинки
     * @param outputFilePath путь файла сохранения зашифрованных данных картинки
     * @param mode метод шифрования
     * @param decrypt дешифровать или нет
     */
    private static void opensslEnc(String inputFilePath, String outputFilePath, Mods mode, boolean decrypt) {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            ArrayList<String> com = new ArrayList<>();
            com.add("E:\\Git\\usr\\bin\\openssl.exe");
            com.add("enc");
            if (decrypt)
                com.add("-d");
            com.add("-aes-256-" + mode.toString());
            com.add("-in");
            com.add(inputFilePath);
            com.add("-out");
            com.add(outputFilePath);
            com.add("-pass");
            com.add("pass:" + key);
            pb.command(com);
            Process process = pb.start();
            String line, result = "";
            try (BufferedReader reader = process.inputReader()) {
                while ((line = reader.readLine()) != null) {
                    result = line;
                    System.out.println(result);
                }
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Метод создания ключа
     *
     * @throws IOException
     */
    private static void createKey() throws IOException {
        ProcessBuilder pb = new ProcessBuilder();
        ArrayList<String> com = new ArrayList<>();
        com.add("E:\\Git\\usr\\bin\\openssl.exe");
        com.add("rand");
        com.add("-hex");
        com.add("16");
        pb.command(com);
        Process process = pb.start();
        BufferedReader reader = process.inputReader();
        key = reader.readLine();
        reader.close();
    }

    /**
     * Методы шифрования
     */
    private enum Mods {
        ECB,
        CBC,
        CFB,
        OFB;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
