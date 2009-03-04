package mp4.util.atom;

public class CpilAtom extends AppleMetaAtom {

	public CpilAtom(CpilAtom old) {
		super(old);
	}

	public CpilAtom() {
		super(new byte[] { 'c', 'p', 'i', 'l' } );
	}
}
