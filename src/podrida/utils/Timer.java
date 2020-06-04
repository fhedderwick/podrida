package podrida.utils;

public class Timer {

    private Long _startTime;
    
    public void restart() {
        _startTime = System.currentTimeMillis();
    }
    
    public Long getElapsedMillis(){
        return System.currentTimeMillis() - _startTime;
    }

}
