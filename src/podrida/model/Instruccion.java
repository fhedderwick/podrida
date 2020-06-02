package podrida.model;

import podrida.utils.Utils;

public enum Instruccion {
    ENTRAR(0),
    GET_JUGADORES(1),
    EMPEZAR(2), 
    VER_TABLA(3),
    TIRAR_CARTA(4),
    ELEGIR_NUMERO(5),
    REPARTIR(6),
    MENSAJE_ESTANDAR(7),
    VER_ESTADISTICAS(8),
    VER_ESTADISTICAS_USUARIO(9),
    CONFIGURAR_JUEGO(10),
    REGISTRAR_ESPECTADOR(11),
    ENVIAR_MENSAJE(80),
    INFORMAR_CAMBIO_CONEXION(90),
    EXPULSAR_JUGADOR(91),
    PROPONER_REINICIAR(92),
    FORZAR_JUGADOR(93),
    HEARTBEAT(95),
    INSTRUCCION_DESCONOCIDA(99);

    public static Instruccion getInstruccion(final String param) {
        try{
            final int val = Utils.parsearNumero(param);
            switch(val){
                case 0: return ENTRAR;
                case 1: return GET_JUGADORES;
                case 2: return EMPEZAR;
                case 3: return VER_TABLA;
                case 4: return TIRAR_CARTA;
                case 5: return ELEGIR_NUMERO;
                case 6: return REPARTIR;
                case 7: return MENSAJE_ESTANDAR;
                case 8: return VER_ESTADISTICAS;
                case 9: return VER_ESTADISTICAS_USUARIO;
                case 10: return CONFIGURAR_JUEGO;
                case 11: return REGISTRAR_ESPECTADOR;
                case 80: return ENVIAR_MENSAJE;
                case 90: return INFORMAR_CAMBIO_CONEXION;
                case 91: return EXPULSAR_JUGADOR;
                case 92: return PROPONER_REINICIAR;
                case 93: return FORZAR_JUGADOR;
                case 95: return HEARTBEAT;
                default:
            }
        }catch(final Exception e){
            e.printStackTrace();
        }
        return INSTRUCCION_DESCONOCIDA;
    }
    
    final int _code;
    
    Instruccion(final int code){
        _code = code;
    }
    
    public int getCode() {
        return _code;
    }
}