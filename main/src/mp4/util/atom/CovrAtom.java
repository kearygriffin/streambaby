package mp4.util.atom;

public class CovrAtom extends AppleMetaAtom {

	public CovrAtom(CovrAtom old) {
		super(old);
	}

	public CovrAtom() {
		super(new byte[] { 'c', 'o', 'v', 'r' } );
	}
}
