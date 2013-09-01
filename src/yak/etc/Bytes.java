package yak.etc;

public class Bytes extends Yak {
	public byte[] arr;
	public int off;
	public int len;
	
	static final int INIT_CAP = 64;
	public Bytes() {
		this(0, INIT_CAP);
	}
	
	int roundUp(int n) {
		int z = INIT_CAP;
		while (z < n) {
			z += z;
		}
		return z;
	}

	public Bytes(int len, int cap) {
		this.arr = new byte[cap];
		this.off = 0;
		this.len = len;
	}

	public Bytes(byte[] a, int off, int len) {
		this.arr = a;
		this.off = off;
		this.len = len;
	}
	
	public Bytes(Bytes a) {
		this(a.arr, a.off, a.len);
	}
	
	void growCapBy(int n) {
		growCapTo(len + n);
	}
	
	void growCapTo(int n) {
		if (arr.length - off < n) {
			int cap = roundUp(n);
			byte[] a = new byte[cap];
			System.arraycopy(arr, off, a, 0, len);
			arr = a;
			off = 0;
		}
	}
	
	int measureInt(int x) {
		int z = 1;
		long ceiling = 128;
		while (x >= ceiling) {
			++z;
			ceiling += ceiling;
		}
		return z;
	}
	
	void appendVarInt(int x) {
		System.err.println(Fmt("appendVarInt<<< %d", x));
		while (x >= 128) {
			byte z = (byte) ((x&127) | 128);
			appendByte(z);
			x >>>= 7;
		}
		appendByte((byte)x);
	}
	
	void appendByte(byte x) {
		System.err.println(Fmt("appendByte<<< %02x  >>> (len=%d)", x, len+1));
		growCapBy(1);
		arr[off + len] = x;
		++len;
	}

	public void appendProtoString(int tag, String s) {
		byte[] a = StringToBytes(s);
		System.err.println(Fmt("appendProtoString %d: <<< [%d] %s", tag, s.length(), CurlyEncode(s)));
		appendProtoBytes(tag, a);

		System.err.println(Fmt("  >>> (len=%d) .... %s ", len, this));
	}

	public void appendProtoBytes(int tag, byte[] x) {
		appendProtoBytes(tag, new Bytes(x, 0, x.length));
	}

	public void appendProtoBytes(int tag, Bytes a) {
		System.err.println(Fmt("appendProtoString %d: <<< [%d] %s", tag, a.len, HexEncode(a.cleanArray())));
		int code = (tag << 3) | 2;
		appendVarInt(code);
		appendVarInt(a.len);
		growCapBy(a.len);
		System.arraycopy(a.arr, a.off, arr, off+len, a.len);
		len += a.len;
	}

	public void appendProtoInt(int tag, int x) {
		int code = (tag << 3) | 0;
		appendVarInt(code);
		appendVarInt(x);
	}

	public int popVarInt() {
		byte b = popByte();
		int z = b & 127;
		int shift = 7;
		while ((b & 128) == 128) {
			b = popByte();
			z |= ((b & 127) << shift);
			shift += 7;
		}
		return z;
	}

	public String popVarString() {
		Bytes b = popVarBytes();
		byte[] z = Bytes.makeArraySliceLen(b.arr, b.off, b.len);
		return Utf8ToString(z);
	}
	public Bytes popVarBytes() {
		int n = popVarInt();
		if (len < n) {
			throw Bad("Not enough to pop bytes: " + n);
		}
		Bytes z = new Bytes(arr, off, n);
		off += n;
		len -= n;
		return z;
	}
	
	public byte popByte() {
		byte z = arr[off];
		++off;
		--len;
		if (len < 0) {
			throw Bad("Popped a byte too many");
		}
		return z;
	}
	
	@Override
	public String toString() {
		byte[] z = makeArraySliceLen(arr, off, len);
		return "Bytes[" + len + "](" + off + "," + arr.length + ")" + CurlyEncode(BytesToString(z));
	}
	
	public byte[] cleanArray() {
		return makeArraySliceLen(arr, off, len);
	}
	
	public String showProto() {
		Bytes a = new Bytes(this);
		StringBuffer sb = new StringBuffer();
		try {
			while (a.len > 0) {
				sb.append("..." + CurlyEncode(BytesToString(cleanArray())) + "...\n");
				int t = popVarInt();
				int tag = t >>> 3;
				int type = t & 7;
				sb.append(Fmt("T(%d)tag(%d)type(%d)=", t, tag, type));
				switch (type) {
					case 0: {
						int x = popVarInt();
						sb.append("int=" + x);
					}
					break;
					case 2: {
						String x = popVarString();
						sb.append("str=" + CurlyEncode(x));
					}
				}
				sb.append("\n");
			}
		} catch (Exception ex) {
			sb.append("#Exception#" + ex);
		}
		return sb.toString();
	}
	
	public static byte[] makeArraySliceLen(byte[] a, int begin, int n) {
		byte[] z = new byte[n];
		System.arraycopy(a, begin, z, 0, n);
		return z;
	}
}
