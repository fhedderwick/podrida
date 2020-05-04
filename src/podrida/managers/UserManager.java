package podrida.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import podrida.model.User;
import podrida.model.estadistica.EstadisticaJugadorPartido;
import podrida.model.estadistica.EstadisticasUsuario;
import static podrida.utils.Constants.BLOCKED_STRING;
import static podrida.utils.Constants.MAX_LOGIN_ATTEMPTS;
import podrida.utils.Utils;

public class UserManager {

    private final List<User> _loggedUsers;
    private final Set<String> _blockedUsernames;
    private final File _userFolder;

    public UserManager(final String userFolderPath) {
        _loggedUsers = new ArrayList<>();
        _blockedUsernames = new HashSet<>();
        _userFolder = new File(userFolderPath);
    }
    
    public boolean checkUserFolder(){
        return _userFolder.isDirectory();
    }
    
    public String login(final String username, final String pass) {
        final User user = getUser(username,pass);
        if(_blockedUsernames.contains(username)){
            return BLOCKED_STRING;
        }
        if(user!= null){
            final String token = UUID.randomUUID().toString();
            register(token,user);
            return token;
        }
        return null;
    }
    
    private void register(final String token, final User user){
        user.setToken(token);
        _loggedUsers.add(user);
    }
    
    public User getLoggedUserByToken(final String token){
        for(final User user : _loggedUsers){
            if(user.getToken().equals(token)){
                return user;
            }
        }
        return null;
    }

    public boolean existsName(final String name) {
        for(final User user : _loggedUsers){
            if(name.equals(user.getUsername())){
                return true;
            }
        }
        return false;
    }

    public boolean nameIsRegistered(final String name) {
        return getUserFile(name).exists();
    }
    
    private User getUser(final String name, final String pass){
        final File file = getUserFile(name);
        if(file.exists()){
            final String filename = file.getName();
            final String fileUsername = filename.substring(1);
            final int retries = Integer.valueOf(filename.substring(0,1));
            if(fileUsername.equals(name)){
                final List<String> fileLines = Utils.readFileLines(file);
                if(fileLines.isEmpty()){
                    return null;
                }
                final String savedPass = fileLines.remove(0);
                final User user;
                if(retries == MAX_LOGIN_ATTEMPTS){
                    _blockedUsernames.add(name);
                    System.out.println("El usuario " + name + " esta bloqueado");
                    return null;
                } else {
                    if(savedPass.equals(pass)){
                        if(retries == MAX_LOGIN_ATTEMPTS){
                            _blockedUsernames.add(name);
                            return null;
                        }
                        user = new User(name,fileLines);
                        _blockedUsernames.remove(name);
                        changeFilePrefix(file,0+name);
                        return user;
                    } else {
                        System.out.println("Clave incorrecta (intento " + retries + ") para usuario " + name);
                        changeFilePrefix(file,(retries + 1) + name);
                    }
                }
            }
        }
        return null;
    }
    
    public String createNewUser(final String username, final String pass) {
        if(existsName(username)){
            return "Nombre de usuario en uso";
        }
        if(username.length() == 0 || username.length() > 20 || Utils.containsStrangeCharacters(username)){
            return "Nombre de usuario invalido";
        }
        if(pass.length() == 0 || pass.length() > 20 || Utils.containsStrangeCharacters(pass)){
            return "Clave invalida";
        }
        File file = getUserFile(username);
        if(file.exists()){
            return "Nombre de usuario en uso";
        }
        try {
            if(!file.createNewFile()){
                System.out.println("No pudo crearse el archivo de usuario");
                return "Error al crear el usuario";
            }
            final FileWriter fw = new FileWriter(file);
            fw.write(pass);
            fw.write(System.lineSeparator());
            fw.close();
        } catch (final IOException ex) {
            ex.printStackTrace();
            file.delete();
            return "Error al crear el usuario";
        }
        return null;
    }

    public String deleteUser(final String username, final String pass) {
        final User user = getUser(username, pass);
        if(_blockedUsernames.contains(username)){
            return "El usuario esta bloqueado";
        }
        if(user == null){
            return "Usuario o clave invalidos";
        }
        if(_loggedUsers.contains(user)){
            return "Debe cerrar la sesion antes";
        }
        final File file = getUserFile(username);
        if(!file.delete()){
            System.out.println("No pudo eliminarse el archivo de usuario");
            return "No se pudo quitar el usuario";
        }
        return null;
    }
    
    
    public JsonArray getEstadisticasUsuariosAsHtmlTable(final String usernames) {
        final JsonArray ja = new JsonArray();
        try{
            final JsonArray usernamesAsJsonArray = JsonParser.parseString(usernames).getAsJsonArray();
            final Set<String> usernamesAsList = new HashSet<>();
            for(final JsonElement je : usernamesAsJsonArray){
                usernamesAsList.add(je.getAsString());
            }
            ja.add("<tr class='bordered padded'><td class='bordered padded'>Jugador</td>");
            ja.add("<tr class='bordered padded'><td class='bordered padded'>Partidos jugados</td>");
            ja.add("<tr class='bordered padded'><td class='bordered padded'>Triunfos</td>");
            ja.add("<tr class='bordered padded'><td class='bordered padded'>Segundo puestos</td>");
            ja.add("<tr class='bordered padded'><td class='bordered padded'>Ultimo lugar</td>");
            ja.add("<tr class='bordered padded'><td class='bordered padded'>Invictos</td>");
            ja.add("<tr class='bordered padded'><td class='bordered padded'>Abandonos</td>");
//            ja.add("<tr class='bordered padded'><td class='bordered padded'>Promedio puntuacion total</td>");
//            ja.add("<tr class='bordered padded'><td class='bordered padded'>Promedio puntuacion maximo</td>");
//            ja.add("<tr class='bordered padded'><td class='bordered padded'>Puntos totales</td>");
//            ja.add("<tr class='bordered padded'><td class='bordered padded'>Ranking</td>");
            final List<EstadisticasUsuario> estadisticasUsuarios = getEstadisticasUsuarios(usernamesAsList);
            for(final EstadisticasUsuario estadisticasUsuario : estadisticasUsuarios){
                ja.add(ja.remove(0).getAsString() + "<td class='bordered padded'>"+estadisticasUsuario.getUsername()+"</td>");
                ja.add(ja.remove(0).getAsString() + "<td class='bordered padded'>"+estadisticasUsuario.getPartidosTotales()+"</td>");
                ja.add(ja.remove(0).getAsString() + "<td class='bordered padded'>"+estadisticasUsuario.getPartidosPrimero()+"</td>");
                ja.add(ja.remove(0).getAsString() + "<td class='bordered padded'>"+estadisticasUsuario.getPartidosSegundo()+"</td>");
                ja.add(ja.remove(0).getAsString() + "<td class='bordered padded'>"+estadisticasUsuario.getPartidosUltimo()+"</td>");
                ja.add(ja.remove(0).getAsString() + "<td class='bordered padded'>"+estadisticasUsuario.getPartidosInvicto()+"</td>");
                ja.add(ja.remove(0).getAsString() + "<td class='bordered padded'>"+estadisticasUsuario.getPartidosAbandonados()+"</td>");
//                ja.add(ja.get(i++).getAsString() + "<td class='bordered padded'>"+estadisticasUsuario.getPromedioPuntuacion()+"</td>");
//                ja.add(ja.get(i++).getAsString() + "<td class='bordered padded'>"+estadisticasUsuario.getPromedioPuntuacionMaximo()+"</td>");
//                ja.add(ja.get(i++).getAsString() + "<td class='bordered padded'>"+estadisticasUsuario.getPuntosTotales()+"</td>");
//                ja.add(ja.get(i++).getAsString() + "<td class='bordered padded'>"+estadisticasUsuario.getRanking()+"</td>");
            }
            final int jaSize = ja.size();
            for(int i=0; i<jaSize ; i++){
                ja.add(ja.remove(0).getAsString() + "</tr>");
            }
        }catch(final Exception e){
            e.printStackTrace();
        }
        return ja;
    }

    private List<EstadisticasUsuario> getEstadisticasUsuarios(final Set<String> usernames) {
        final List<EstadisticasUsuario> lista = new ArrayList<>();
        for(final String username : usernames){
            final User user = getLoggedUserByUsername(username);
            if(user == null){
                continue;
            }
            lista.add(user.getEstadistica());
        }
        return lista;
    }
    
    private User getLoggedUserByUsername(final String username){
        for(final User user : _loggedUsers){
            if(user.getUsername().equals(username)){
                return user;
            }
        }
        return null;
    }
    
    public boolean writeStats(final Map<String,EstadisticaJugadorPartido> estadisticaJugadorPartido){
        final Map<User,EstadisticaJugadorPartido> map = new HashMap<>();
        for(final Map.Entry<String, EstadisticaJugadorPartido> entry : estadisticaJugadorPartido.entrySet()){
            final String username = entry.getKey();
            final EstadisticaJugadorPartido stat = entry.getValue();
            if(username == null || "".equals(username)){
                return false;
            }
            final User user = getLoggedUserByUsername(username);
            if(user == null){
                return false;
            }
            map.put(user,stat);
        }
        for(final Map.Entry<User, EstadisticaJugadorPartido> entry : map.entrySet()){
            if(!writeUserStats(entry.getKey(),entry.getValue())){
                return false;
            }
        }
        return true;
    }

    private boolean writeUserStats(final User user, final EstadisticaJugadorPartido stats) {
        final String statsAsString = stats.getAsWritableString();
        final String username = user.getUsername();
        final File file = getUserFile(username);
        if(!file.exists()){
            System.out.println("No se encuentra el archivo de datos del usuario " + username);
            return false;
        }
        try {
            final FileWriter fw = new FileWriter(file,true);
            fw.write(statsAsString);
            fw.write(System.lineSeparator());
            fw.close();
        } catch (final IOException ex) {
            ex.printStackTrace();
            System.out.println("Error al escribir estadisticas en el archivo de datos del usuario: " + statsAsString);
            return false;
        }
        return true;
    }

    private void changeFilePrefix(final File file, final String newName) {
        try{
            file.renameTo(new File(_userFolder,newName));
        } catch(final Exception e){
            e.printStackTrace();
            System.out.println("Error al renombrar el archivo de usuario");
        }
    }

    private File getUserFile(final String username) {
        final File[] files = _userFolder.listFiles();
        for(final File file : files){
            final String filename = file.getName();
            final String fileUsername = filename.substring(1);
            if(fileUsername.equals(username)){
                try{
                    final int retries = Integer.valueOf(filename.substring(0,1));
                } catch(final NumberFormatException nfe){
                    nfe.printStackTrace();
                    System.out.println("Archivo de usuario invalido: " + filename);
                    continue;
                }
                return file;
            }
        }
        return new File(_userFolder,0+username);
    }

}