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
    DECIR_AGUITA(7),
    DECIR_JUGARON_RE_MAL(8),
    DECIR_SI_JUGAMOS_BIEN(9),
    CONFIGURAR_JUEGO(10),
    ENVIAR_MENSAJE(80),
    INFORMAR_CAMBIO_CONEXION(90),
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
                case 7: return DECIR_AGUITA;
                case 8: return DECIR_JUGARON_RE_MAL;
                case 9: return DECIR_SI_JUGAMOS_BIEN;
                case 10: return CONFIGURAR_JUEGO;
                case 80: return ENVIAR_MENSAJE;
                case 90: return INFORMAR_CAMBIO_CONEXION;
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