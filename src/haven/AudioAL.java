package haven;

import com.jogamp.openal.AL;
import com.jogamp.openal.ALConstants;
import com.jogamp.openal.ALFactory;
import com.jogamp.openal.util.ALut;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;


public class AudioAL {
	public static final AL al;
	static {
		ALut.alutInit();
		al = ALFactory.getAL();
		al.alGetError();
	}
	private static final Cleaner cleaner = Cleaner.create();

	private boolean started = false;
	private float pitch = 1.0f, gain = 1.0f;

	public final int[] source = new int[1];
	private final int[] buffer = new int[1];

	public AudioAL(InputStream is, boolean mono) {
		al.alGenSources(1, source, 0);
		al.alGenBuffers(1, buffer, 0);

		al.alSourcef(source[0], ALConstants.AL_ROLLOFF_FACTOR, 1/5f);
		al.alSourcef(source[0], ALConstants.AL_REFERENCE_DISTANCE, 11);
		try {
			if(mono) {
				byte[] buf = is.readAllBytes();
				ByteBuffer bb = ByteBuffer.allocate(buf.length/2);
				for(int i = 0; i < buf.length / 4; ++i) {
					int HI = 1; int LO = 0;
					short left = (short)((buf[i * 4 + HI] << 8) | (buf[i * 4 + LO] & 0xff));
					short right = (short)((buf[i * 4 + 2 + HI] << 8) | (buf[i * 4 + 2 + LO] & 0xff));
					int avg = (left + right) / 4;

					bb.put(((byte)((avg & 0xff))));
					bb.put((byte)((avg >> 8) & 0xff));
				}
				bb.rewind();
				al.alBufferData(buffer[0], ALConstants.AL_FORMAT_MONO16, bb, bb.remaining(), 44100);
			} else {
				ByteBuffer bb = ByteBuffer.wrap(is.readAllBytes());
				al.alBufferData(buffer[0], ALConstants.AL_FORMAT_STEREO16, bb, bb.remaining(), 44100);
			}
		} catch(IOException e) {
			System.err.println("Error while loading audio clip");
			e.printStackTrace();
		}
		al.alSourcei(source[0], ALConstants.AL_BUFFER, buffer[0]);
		cleaner.register(this, new Cleanup(buffer, source));
	}

	private static class Cleanup implements Runnable {
		private final int[] buffer, source;
		public Cleanup(int[] buffer, int[] source) {
			this.buffer = buffer;
			this.source = source;
		}
		@Override
		public void run() {
			al.alDeleteSources(1, source, 0);
			al.alDeleteBuffers(1, buffer, 0);
		}
	}

	void setGain(float gain) {
		if(this.gain != gain) {
			this.gain = gain;
			al.alSourcef(source[0], ALConstants.AL_GAIN, gain);
		}
	}

	void setPitch(float pitch) {
		if(this.pitch != pitch) {
			this.pitch = pitch;
			al.alSourcef(source[0], ALConstants.AL_PITCH, pitch);
		}
	}

	void setPos(float x, float y, float z) {
		al.alSource3f(source[0], ALConstants.AL_POSITION, x, y, z);
	}

	void start() {
		if(started) {
			return;
		}
		started = true;
		al.alSourcePlay(source[0]);
	}

	boolean over() {
		int[] state = new int[1];
		al.alGetSourcei(source[0], ALConstants.AL_SOURCE_STATE, state, 0);
		return (state[0] == ALConstants.AL_STOPPED);
	}
}
