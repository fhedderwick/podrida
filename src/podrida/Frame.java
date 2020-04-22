package podrida;

import java.util.ArrayList;
import java.util.List;

public class Frame {

    boolean fin;
    private boolean rsv1;
    private boolean rsv2;
    private boolean rsv3;
    private byte opcode;
    
    boolean masked;
    long payloadLength = -1;
    int[] payloadLengthBuffer;
    int[] maskingKey = new int[4];
    int[] payload = null;
    private String payloadAsString = null;
    Frame nextFrame = null;
    int payloadLengthReadBytes = 0;
    int maskReadBytes = 0;
    int payloadReadBytes = 0;
    
    public boolean continues() {
        return !fin;
    }
    
    public boolean isFinished(){
        return payload != null && payloadLength == payloadReadBytes;
    }
    
    public Frame(final int firstByte){
        fin = ((firstByte & 0x80) != 0);
        rsv1 = ((firstByte & 0x40) != 0);
        rsv2 = ((firstByte & 0x20) != 0);
        rsv3 = ((firstByte & 0x10) != 0);
        opcode = (byte)(firstByte & 0x0F);
    }
    
    public void addByte(final int read){
        if(payloadLength < 0){
            processSecondByte(read);
            return;
        }
        if(payloadLengthReadBytes < payloadLengthBuffer.length){
            payloadLengthBuffer[payloadLengthReadBytes] = read;
            payloadLengthReadBytes++;
            return;
        }
        if(payloadLengthBuffer.length > 0){
            payloadLength = getExtendedPayloadLength(payloadLengthBuffer);
            payloadLengthBuffer = new int[0];
        }
        if(maskReadBytes < 4){
            maskingKey[maskReadBytes] = read;
            maskReadBytes++;
            return;
        }
        if(payload == null){
            payload = new int[(int) payloadLength];
        }
        payload[payloadReadBytes] = read;
        payloadReadBytes++;
    }
    
    private void processSecondByte(final int secondByte){
        masked = ((secondByte & 0x80) != 0);
        payloadLength = (byte)(secondByte & 0x7F);
        if(payloadLength < 126){
            payloadLengthBuffer = new int[0];
        } else if(payloadLength == 126){
            payloadLengthBuffer = new int[2];
        } else {
            payloadLengthBuffer = new int[8];
        }
    }
    
    public void addFrame(final Frame frame){
        nextFrame = frame;
    }
    
    public void mergePayloads() {
        //TODO
        //verificar que los opcodes salvo el maestro sean 0x0 (todo esto valido solo para 0x1 o 0x2)
    }
    
    public void heartbeat(){
        //TODO
    }
    
    public String getPayload(){
        if(payloadAsString == null){
            final StringBuilder sb = new StringBuilder();
            int i=0;
            for(final int a : payload){
                sb.append((char) (a ^ maskingKey[i % 4]));
                i++;
            }
            payloadAsString = sb.toString();
        }
        return payloadAsString;
    }

    public boolean isRsv1() {
        return rsv1;
    }

    public boolean isRsv2() {
        return rsv2;
    }

    public boolean isRsv3() {
        return rsv3;
    }

    public byte getOpcode() {
        return opcode;
    }

    public static List<byte[]> framize(final String string) {
        final List<byte[]> frames = new ArrayList<>();
        final String[] stringChunks = calcularCantidadFrames(string);
        for(int i = 0; i < stringChunks.length ; i++){
            final String stringChunk = stringChunks[i];
            final long stringChunkLength = (long) stringChunk.length();
            final byte[] buffer;
            byte firstByte = (byte) ((i == stringChunks.length-1) ? 0x80 : 0x00);
            if(i == 0){
                firstByte += 0x01;
            }
            byte secondByte;
            int j = 2;
            if(stringChunkLength < 126){
                secondByte = (byte) stringChunkLength;
                buffer = new byte[2 + (int) stringChunkLength];
            } else if(stringChunkLength <= 0xFFFF){
                secondByte = 126;
                buffer = new byte[4 + (int) stringChunkLength];
                buffer[2] = (byte)(( stringChunkLength >> 8 ) & (byte)255);
                buffer[3] = (byte)(( stringChunkLength      ) & (byte)255);
                j+=2;
            } else {
                secondByte = 127;
                buffer = new byte[10 + (int) stringChunkLength];
                buffer[2] = (byte)(( stringChunkLength >> 56 ) & (byte)255);
                buffer[3] = (byte)(( stringChunkLength >> 48 ) & (byte)255);
                buffer[4] = (byte)(( stringChunkLength >> 40 ) & (byte)255);
                buffer[5] = (byte)(( stringChunkLength >> 32 ) & (byte)255);
                buffer[6] = (byte)(( stringChunkLength >> 24 ) & (byte)255);
                buffer[7] = (byte)(( stringChunkLength >> 16 ) & (byte)255);
                buffer[8] = (byte)(( stringChunkLength >>  8 ) & (byte)255);
                buffer[9] = (byte)(( stringChunkLength       ) & (byte)255);
                j+=8;
            }
//            aca iria la masking key que no va
            buffer[0] = firstByte;
            buffer[1] = secondByte;
            for(final byte b : stringChunks[i].getBytes()){
                buffer[j] = b;
                j++;
            }
            frames.add(buffer);
        }
        return frames;
    }
    
    private static String[] calcularCantidadFrames(final String string) {
        //TODO
        return new String[]{string};
    }

    private long getExtendedPayloadLength(final int[] payloadLengthBuffer) {
        long val = 0;
        for(int i=0;i<payloadLengthBuffer.length;i++){
            val += payloadLengthBuffer[payloadLengthBuffer.length-1-i] << (i*8);
        }
        return val;
    }
    
}
