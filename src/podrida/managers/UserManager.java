package podrida.managers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import podrida.model.User;
import podrida.utils.Utils;

public class UserManager {

    private final List<User> _loggedUsers;
    private final File _userFolder;

    public UserManager(final String userFolderPath) {
        _loggedUsers = new ArrayList<>();
        _userFolder = new File(userFolderPath);
    }
    
    public boolean checkUserFolder(){
        return _userFolder.isDirectory();
    }
    
    public String login(final String username, final String pass) {
        final User user = getUser(username,pass);
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

    public User addGuest(final String name) {
        final User user = new User(name);
        register(UUID.randomUUID().toString(), user);
        return user;
    }

    public boolean existsName(final String name) {
        final String[] filenames = _userFolder.list();
        for(final String filename : filenames){
            if(filename.equals(name)){
                return true;
            }
        }
        return false;
    }
    
    private User getUser(final String name, final String pass){
        final File[] files = _userFolder.listFiles();
        for(final File file : files){
            if(file.getName().equals(name)){
                final List<String> fileLines = Utils.readFileLines(file);
                if(fileLines.isEmpty()){
                    return null;
                }
                if(fileLines.remove(0).equals(pass)){
                    return new User(name,fileLines);
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
        if(username.startsWith("_")){
            return "El nombre de usuario no puede comenzar con \"_\"";
        }
        if(pass.length() == 0 || pass.length() > 20 || Utils.containsStrangeCharacters(pass)){
            return "Clave invalida";
        }
        final File file = new File(_userFolder,username);
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
        if(user == null){
            return "Usuario o clave invalidos";
        }
        if(_loggedUsers.contains(user)){
            return "Debe cerrar la sesion antes";
        }
        final File file = new File(_userFolder,username);
        if(!file.delete()){
            System.out.println("No pudo eliminarse el archivo de usuario");
            return "No se pudo quitar el usuario";
        }
        return null;
    }
    
}