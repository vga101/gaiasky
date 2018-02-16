package gaia.cu9.ari.gaiaorbit.util.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

class ByteBufferOutputStream extends OutputStream {
    ByteBuffer buf;

    public ByteBufferOutputStream(ByteBuffer buf) {
        this.buf = buf;
    }

    public synchronized void write(int b) throws IOException {
        buf.put((byte) b);
    }

    public synchronized void write(byte[] bytes, int off, int len) throws IOException {
        buf.put(bytes, off, len);
    }

}
