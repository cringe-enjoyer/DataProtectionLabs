import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class Main {
    private static final int HEADER = 122;
    private static ArrayList<Integer> key;
    private static String originalHash = "";

    public static void main(String[] args) throws IOException {
        ProcessBuilder process = new ProcessBuilder();
        ArrayList<String> com = new ArrayList<>();
        com.add("E:\\Git\\usr\\bin\\openssl.exe");
        com.add("dgst");
        com.add("-sha1");
        com.add("F:\\OpenSSLLabs\\Lab2\\res\\leasing.txt");
        process.command(com);
        addInfo();
        process.redirectOutput(new File("F:\\OpenSSLLabs\\Lab2\\res\\out.txt"));
        process.start();
        String hash = getInfo();
        if (originalHash.equals(hash))
            System.out.println("Хеши совпадают");
    }

    private static String getInfo() {
        String hash = "";
        try (FileInputStream reader = new FileInputStream("F:\\OpenSSLLabs\\Lab2\\res\\28e.bmp")) {
            byte[] header = new byte[HEADER];
            reader.read(header);
            byte[] image = new byte[reader.available()];
            reader.read(image);
            char[] charBitsArray = new char[8];
            for (int pos = 0; pos <= key.size(); pos++) {
                if (pos % 8 == 0 && pos != 0) {
                    String bitsImg = new String(charBitsArray);
                    byte byteImg = (byte) Integer.parseInt(bitsImg, 2);
                    hash += toHexString(byteImg);
                    charBitsArray = new char[8];
                    if (pos == 160)
                        break;
                }
                String imageByteBits = String.format("%8s", Integer.toBinaryString(image[key.get(pos)] & 0xff))
                        .replace(' ', '0');
                char[] imageByteBinaryArray = imageByteBits.toCharArray();
                charBitsArray[pos % 8] = imageByteBinaryArray[7];
            }
            System.out.println(hash);
        }catch (Exception e) {
            System.out.println(e);
        }
        return hash;
    }

    private static void addInfo() {
        byte[] hashAr = toByteArray(getHash());
        String hashBits = "";
        for (byte b : hashAr) {
            hashBits += String.format("%8s", Integer.toBinaryString(b & 0xff)).replace(' ', '0');
        }
        System.out.println(hashBits);
        char[] hashBitsCharArray = hashBits.toCharArray();
        byte[] image = new byte[0];
        byte[] header = new byte[0];
        try (FileInputStream reader = new FileInputStream("F:\\OpenSSLLabs\\Lab2\\res\\28.bmp")) {
            header = new byte[HEADER];
            reader.read(header);
            image = new byte[reader.available()];
            reader.read(image);
        } catch (Exception e) {
            System.out.println(e);
        }
        key = createKey(image.length);

        for (int i = 0; i < key.size(); i++) {
            String imageBits = Integer.toBinaryString(image[key.get(i)]);
            image[key.get(i)] = (byte) Integer.parseUnsignedInt(imageBits
                    .replace(imageBits.charAt(imageBits.length() - 1), hashBitsCharArray[i]), 2);
        }
        try (FileOutputStream writer = new FileOutputStream("F:\\OpenSSLLabs\\Lab2\\res\\28e.bmp")) {
            writer.write(header);
            writer.write(image);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    private static String getHash() {
        String hash = "";
        try (var reader = new BufferedReader(new FileReader("F:\\OpenSSLLabs\\Lab2\\res\\out.txt"))) {
            String str = reader.readLine();
            hash = str.split("=")[1];
        } catch (Exception ex) {
            System.out.println(ex);
        }
        originalHash = hash.trim();
        return hash.trim();
    }

    private static ArrayList<Integer> createKey(int len) {
        ArrayList<Integer> res = new ArrayList<>();
        Random rnd = new Random();
        while (res.size() != 160) {
            int pos = rnd.nextInt(len);
            if (!res.contains(pos)) {
                res.add(pos);
            }
        }
        return res;
    }

    private static byte[] toByteArray(String hex) {
        byte[] hexArray = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            hexArray[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return hexArray;
    }

    private static String toHexString(byte b) {
        String result = "";
        result += Integer.toString((b & 0xff) + 0x100, 16).substring(1);
        return result;
    }
}
