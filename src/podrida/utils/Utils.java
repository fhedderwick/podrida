package podrida.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import podrida.model.Instruccion;

public class Utils {

    private final static Pattern REGEX_PATTERN = Pattern.compile("^[a-zA-Z0-9_$%&()=!+@#]*$");

    public static JsonObject createJsonReply(final boolean status, final String msg) {
        final JsonObject jo = new JsonObject();
        jo.addProperty("status", status);
        jo.addProperty("code", Instruccion.INSTRUCCION_DESCONOCIDA.getCode());
        jo.addProperty("content", msg);
        return jo;
    }

    public static JsonObject createJsonReply(final boolean status, final Instruccion instruccion, final String content) {
        final JsonObject jo = new JsonObject();
        jo.addProperty("status", status);
        jo.addProperty("code", instruccion.getCode());
        jo.addProperty("content", content);
        return jo;
    }

    public static JsonObject createJsonReply(final boolean status, final Instruccion instruccion, final JsonElement content) {
        final JsonObject jo = new JsonObject();
        jo.addProperty("status", status);
        jo.addProperty("code", instruccion.getCode());
        jo.add("content", content);
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
        final List list = new ArrayList<>();
        try {
            final File file = new File(filePath);
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
        return abs(new Random(System.currentTimeMillis()).nextInt() % notIncludedMax);
    }

}
