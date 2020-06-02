package podrida.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import podrida.model.Instruccion;

public class Utils {

    private final static Pattern REGEX_PATTERN = Pattern.compile("^[a-zA-Z0-9_$%&()=!+@#]*$");
    private final static Random _random = new Random();
    
    public static JsonObject createJsonReply(final boolean status, final Instruccion instruccion, final String content) {
        final JsonObject jo = new JsonObject();
        jo.addProperty("status", status);
        jo.addProperty("code", instruccion.getCode());
        jo.addProperty("content", content);
        return jo;
    }

    public static JsonObject createJsonReply(final boolean status, final Instruccion instruccion, final JsonElement content) {
        return createJsonReply(status,instruccion,content.toString());
    }
    
    public static JsonObject copyJsonReply(final JsonObject jsonObject) {
        final JsonObject jo = new JsonObject();
        jo.add("status", jsonObject.get("status"));
        jo.add("code", jsonObject.get("code"));
        jo.add("content", jsonObject.get("content"));
        return jo;
    }

    public static int parsearNumero(final String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public static String readFile(final String filePath) {
        final StringBuilder sb = new StringBuilder();
        final List<String> lines = readFileLines(filePath);
        for(final String line : lines){
            sb.append(line).append(System.lineSeparator());
        }
        return sb.toString();
    }
    
    public static List<String> readFileLines(final String filePath) {
        final File file = new File(filePath);
        return readFileLines(file);
    }

    public static List<String> readFileLines(final File file) {
        final List list = new ArrayList<>();
        try {
            final FileInputStream fis = new FileInputStream(file);
            final InputStreamReader isr = new InputStreamReader(fis);
            final BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                list.add(line);
            }
            br.close();
            isr.close();
            fis.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean containsStrangeCharacters(final String parametro) {
        return !REGEX_PATTERN.matcher(parametro).find();
    }
    
    public static JsonObject encodeSound(final String filePath) {
        final File file = new File(filePath);
        final JsonObject jo = new JsonObject();
        if (file.isFile()) {
            jo.addProperty("soundfile",addSoundTag(encodeFile(file)));
        } else {
            System.out.println(filePath + " not found.");
        }
        return jo;
    }

    public static JsonObject encodeImages(final String folderPath) {
        final File folder = new File(folderPath);
        final JsonObject jo = new JsonObject();
        if (folder.isDirectory()) {
            final File[] files = folder.listFiles();
            for (final File file : files) {
                jo.addProperty(file.getName(), addImageTag(file.getName(), encodeFile(file)));
            }
        }
        return jo;
    }

    private static String encodeFile(final File file) {
        String base64File = "";
        try (final FileInputStream fileContent = new FileInputStream(file)) {
            final byte fileData[] = new byte[(int) file.length()];
            fileContent.read(fileData);
            base64File = Base64.getEncoder().encodeToString(fileData);
        } catch (final FileNotFoundException e) {
            System.out.println("File not found");
            e.printStackTrace();
        } catch (final IOException ioe) {
            System.out.println("Exception while reading the File ");
            ioe.printStackTrace();
        }
        return base64File;
    }
    
    private static String addSoundTag(final String encodedFile) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<audio id='buzzer'><source type='audio/ogg' src='data:audio/mpeg;base64,");
        sb.append(encodedFile);
        sb.append("'></audio>");
        return sb.toString();
    }

    private static String addImageTag(final String name, final String encodedFile) {
        final StringBuilder sb = new StringBuilder();
        sb.append("<img alt='");
        sb.append(name);
        sb.append("' src='data:image/png;base64, ");
        sb.append(encodedFile);
        sb.append("' />");
        return sb.toString();
    }

    public static int getRandomInt(final int notIncludedMax) {
        return abs(_random.nextInt() % notIncludedMax);
    }
    
    public static String readInputStream(final InputStream is, final String encoding) {
        int read;
        final StringBuilder sb = new StringBuilder();
        try {
            final InputStreamReader isr = new InputStreamReader(is, encoding);
            while ((read = isr.read()) != -1) {
                sb.append((char) read);
            }
            isr.close();
            is.close();
        } catch (final IOException e) {
            e.printStackTrace();
            return null;
        }
        return sb.toString();
    }

    public static void mergeJsonObjects(final JsonObject target, final JsonObject dataToBeMerged) {
        for(final Map.Entry<String, JsonElement> entry : dataToBeMerged.entrySet()){
            target.add(entry.getKey(),entry.getValue());
        }
    }

}
