import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

public class Voter {
    private static String privateKeyPath;
    private static String publicKeyPath;
    public static BigInteger e;
    public static BigInteger privateEx;
    public static BigInteger module;
    public static void main(String[] args) {
        String publicKeyFilePath = "F:\\OpenSSLLabs\\Labs\\DefenseLabs\\Lab4\\res\\publicKey.pem";
        generatePrivateKey();
        generatePublicKey();
        e = getExp();
        privateEx = getPrivateEx();
        module = getModule();
        /*System.out.println(e);
        System.out.println(module);*/

        String[] options = new String[] {
                "1",
                "2",
                "3",
                "4"
        };
        String messagePath = writeMessage(options[1]);

        //String encMesPath = encryptFile(messagePath, publicKeyFilePath);
        //String signaturePath = sendToRegistrar(blindMess, multiplier);
/*        String signaturePath = sendToRegistrar(encMesPath);

        if (sendToCounter(signaturePath, encMesPath))
            System.out.println("Голос засчитан");
        else
            System.out.println("Голос не засчитан. Провал верификации");*/

        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            messagePath = options[random.nextInt(0,4)];
            BigInteger message = new BigInteger(1, messagePath.getBytes());
            BigInteger multiplier = new BigInteger(1, generateRandomMultiplier(module).toByteArray());

            BigInteger blindMess = coverMessage(message, multiplier);
            BigInteger signature = sendToRegistrar(blindMess);
            BigInteger uncoveredSignedMes = uncoverMessage(signature, multiplier);
            if (sendToCounter(messagePath, uncoveredSignedMes))
                System.out.println("Голос засчитан");
            else
                System.out.println("Голос не засчитан. Провал верификации");
        }
        Counter.showResults();
    }

    /**
     * Генерируем случайный множитель взаимно простой с модулем ключа
     *
     * @param module модуль ключа
     *
     * @return случайное число
     */
    private static BigInteger generateRandomMultiplier(BigInteger module) {
        Random rnd = new Random();
        BigInteger res = new BigInteger(module.bitLength(), rnd);
        while (res.compareTo(module) >= 0 || !res.gcd(module).equals(BigInteger.ONE)) {
            res = new BigInteger(module.bitLength(), rnd);
        }
        return res;
    }

    /**
     * Скрываем сообщение по формуле (message * (multiplier^e) % module) % module
     *
     * @param message сообщение
     * @param multiplier множитель
     * @return скрытое сообщение
     */
    private static BigInteger coverMessage(BigInteger message, BigInteger multiplier) {
/*        byte[] message = new byte[0];
        try (FileInputStream reader = new FileInputStream(messagePath)) {
            message = reader.readAllBytes();
        } catch (Exception ex) {
            System.out.println("coverMessage: " + ex);
        }*/
        return new BigInteger(1, message.multiply(multiplier.modPow(e, module)).mod(module).toByteArray()); // (message * (multiplier^e) % module) % module
    }

    /**
     * Раскрываем сообщение
     *
     * @param message скрытое сообщение
     * @param multiplier множитель
     * @return раскрытое сообщение
     */
    private static BigInteger uncoverMessage(BigInteger message, BigInteger multiplier) {
        return message.multiply(multiplier.modInverse(module)).mod(module);
    }

    /**
     * Отправляет регистратору
     * @param message сообщение
     * @return ответ от регистратора
     */
    private static BigInteger sendToRegistrar(BigInteger message) {
        String answer = Registrar.signCurs(Utils.bytesToHexString(message.toByteArray()), module, privateEx);
        BigInteger result = new BigInteger(1, Utils.hexStringToBytes(answer));
        return result;
    }

    /**
     * Отправить зашифрованное сообщение регистратору для генерации цифровой подписи
     *
     * @param message путь к зашифрованному файлу
     * @return путь к файлу с цифровой подписью
     */
    private static String sendToRegistrar(String message) {
        return Registrar.sign(message, privateKeyPath);
    }

    /**
     * Отправляет счётчику для верификации подписи и подсчёта голоса
     *
     * @param signaturePath путь к цифровой подписи
     * @param messagePath путь к зашифрованному сообщению
     * @return результат проверки true если пройдена и false если не пройдена
     */
    private static boolean sendToCounter(String signaturePath, String messagePath) {
        return Counter.verify(publicKeyPath, signaturePath, messagePath, privateKeyPath);
    }

    /**
     * Отправляет счётчику для верификации подписи и подсчёта голоса
     *
     * @param message оригинальное сообщение
     * @param signedMessage подписанное сообщение
     * @return результат прохождения проверки true - проверка пройдена, false - нет
     */
    private static boolean sendToCounter(String message, BigInteger signedMessage) {
/*        try (FileInputStream reader = new FileInputStream(messagePath)) {
            message = new String(reader.readAllBytes());
        } catch (Exception ex) {
            System.out.println("sendToCounter: " + ex);
        }*/
        return Counter.verifyCurs(message, signedMessage, module, e);
    }

    /**
     * Записываем голос в файл
     *
     * @param message голос
     *
     * @return путь к файлу с записанным голосом
     */
    private static String writeMessage(String message) {
        String messagePath = "F:\\OpenSSLLabs\\Labs\\DefenseLabs\\Lab4\\res\\message.txt";
        try (FileWriter writer = new FileWriter(messagePath)) {
            writer.write(message);
        } catch (Exception e){
            System.out.println("writeMessage: " + e);
        }
        return messagePath;
    }

    /**
     * Генерируем приватный ключ
     */
    private static void generatePrivateKey() {
        ProcessBuilder pb = new ProcessBuilder();
        String outFilePath = "F:\\OpenSSLLabs\\Labs\\DefenseLabs\\Lab4\\res\\privateKey.pem";
        ArrayList<String> com = new ArrayList<>();
        com.add("E:\\Git\\usr\\bin\\openssl.exe");
        com.add("genpkey");
        com.add("-algorithm");
        com.add("RSA");
        com.add("-out");
        com.add(outFilePath);
        com.add("-pkeyopt");
        com.add("rsa_keygen_bits:1024");
        pb.command(com);
        try {
            Process process = pb.start();
            BufferedReader reader = process.inputReader();
            //privateKey = reader.readLine();
            System.out.println(reader.readLine());
            reader.close();
            privateKeyPath = outFilePath;
        } catch (Exception ex) {
            System.out.println("generatePrivateKey: " + ex);
        }
    }

    /**
     * Генерируем публичный ключ
     */
    private static void generatePublicKey() {
        ProcessBuilder pb = new ProcessBuilder();
        String privateKeyFilePath = "F:\\OpenSSLLabs\\Labs\\DefenseLabs\\Lab4\\res\\privateKey.pem";
        //String publicKeyFilePath = "F:\\OpenSSLLabs\\Labs\\DefenseLabs\\Lab4\\res\\publicKey.pem";
        publicKeyPath = "F:\\OpenSSLLabs\\Labs\\DefenseLabs\\Lab4\\res\\publicKey.pem";
        ArrayList<String> com = new ArrayList<>();
        com.add("E:\\Git\\usr\\bin\\openssl.exe");
        com.add("rsa");
        com.add("-pubout");
        com.add("-in");
        com.add(privateKeyFilePath);
        com.add("-out");
        //com.add(publicKeyFilePath);
        com.add(publicKeyPath);
        pb.command(com);
        try {
            Process process = pb.start();
            BufferedReader reader = process.inputReader();
            //publicKey = reader.readLine();
            System.out.println(reader.readLine());
            //publicKey = publicKeyFilePath;
            reader.close();
        } catch (Exception ex) {
            System.out.println("generatePublicKey: " + ex);
        }
    }

    /**
     * Шифруем файл сообщения
     *
     * @param filePath путь к файлу с сообщением
     * @param publicKeyFilePath путь к файлу с публичным ключом
     * @return путь к файлу с зашифрованным сообщением
     */
    private static String encryptFile(String filePath, String publicKeyFilePath) {
        ProcessBuilder pb = new ProcessBuilder();
        String out = "F:\\OpenSSLLabs\\Labs\\DefenseLabs\\Lab4\\res\\messageEnc.enc";
        ArrayList<String> com = new ArrayList<>();
        com.add("E:\\Git\\usr\\bin\\openssl.exe");
        com.add("rsautl");
        com.add("-encrypt");
        com.add("-inkey");
        com.add(publicKeyFilePath);
        com.add("-pubin");
        com.add("-in");
        com.add(filePath);
        com.add("-out");
        com.add(out);
        pb.command(com);
        try {
            Process process = pb.start();
            BufferedReader reader = process.inputReader();
            String line = reader.readLine();
            if (line != null && line.isEmpty())
                return null;
            reader.close();
        } catch (Exception ex) {
            System.out.println("encryptFile: " + ex);
        }
        return out;
    }

    /**
     * Получаем модуль приватного ключа
     *
     * @return модуль в формате {@link BigInteger}. Размера обычного {@link Integer int} недостаточно
     */
    private static BigInteger getModule() {
        ProcessBuilder pb = new ProcessBuilder();
        ArrayList<String> com = new ArrayList<>();
        com.add("E:\\Git\\usr\\bin\\openssl.exe");
        com.add("rsa");
        //com.add("-text");
        com.add("-noout");
        com.add("-modulus");
        com.add("-in");
        com.add(privateKeyPath);
        pb.command(com);
        try {
            Process process = pb.start();
            BufferedReader reader = process.inputReader();
            String line = reader.readLine();
            line = line.split("=")[1];
            //System.out.println(reader.readLine());
            reader.close();
            //System.out.println(line);
            return toBigInt(line);
        } catch (Exception ex) {
            System.out.println("getModule: " + ex);
        }
        return null;
    }

    /**
     * Получаем публичную экспоненту приватного ключа
     *
     * @return значение экспоненты в формате {@link BigInteger}
     */
    private static BigInteger getExp() {
        ProcessBuilder pb = new ProcessBuilder();
        ArrayList<String> com = new ArrayList<>();
        com.add("E:\\Git\\usr\\bin\\openssl.exe");
        com.add("rsa");
        //com.add("-text");
        com.add("-in");
        com.add(privateKeyPath);
        com.add("-text");
        com.add("-noout");

        pb.command(com);
        try {
            Process process = pb.start();
            BufferedReader reader = process.inputReader();
            String line = "";
            String exp = "";
            while ((line = reader.readLine()) != null) {
                if (line.contains("publicExponent"))
                    exp = line.split("publicExponent:")[1].trim();
                //System.out.println(line);
            }
            System.out.println(exp);
            //System.out.println(reader.readLine());
            reader.close();
            //return Integer.parseInt(exp.split(" ")[0]);
            return new BigInteger(exp.split(" ")[0]);
        } catch (Exception ex) {
            System.out.println("getExp: " + ex);
        }
        return BigInteger.ZERO;
    }

    /**
     * Получение приватной экспоненты
     * @return приватная экспонента
     */
    public static BigInteger getPrivateEx() {
        String privateExpHexString = "";
        ProcessBuilder pb = new ProcessBuilder();
        ArrayList<String> com = new ArrayList<>();
        com.add("E:\\Git\\usr\\bin\\openssl.exe");
        com.add("rsa");
        com.add("-text");
        com.add("-noout");
        com.add("-in");
        com.add(privateKeyPath);
        pb.command(com);
        String res = "";
        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                res += line;
            }

        } catch (Exception ex) {
            System.out.println("setPrivateEx: " + ex);
        }
        privateExpHexString = res.replaceAll(" ", "").replaceAll("\n", "");
        privateExpHexString = privateExpHexString.substring(privateExpHexString.indexOf("privateExponent:") + 16,
                privateExpHexString.indexOf("prime1:")).replaceAll(":", "");
        return new BigInteger(1, Utils.hexStringToBytes(privateExpHexString));
    }

    /**
     * Перевод из 16-ой системы в 10-ую систему счисления
     *
     * @param hex шестнадцатеричное число
     *
     * @return десятичное число
     */
    private static BigInteger toBigInt(String hex) {
        return new BigInteger(hex.trim(), 16);
    }
}
