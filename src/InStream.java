import java.io.*;

public class InStream extends InputStream {
    private String data = "";
    private int pos = 0;

    public synchronized int available() throws IOException {
        return data.length();
    }

    public synchronized int read() throws IOException {

        if (pos < data.length()) {
            int x = data.charAt(pos++);
            return x;
        }

        try {
            this.wait();
            return read();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public synchronized void push(String new_data) {
        data = data.substring(pos) + new_data;
        pos = 0;
        this.notify();
    }
};