package mp4.util.atom;

public class PurlAtom extends AppleMetaAtom {

	public PurlAtom(PurlAtom old) {
		super(old);
	}

	public PurlAtom() {
		super(new byte[] { 'p', 'u', 'r', 'l' } );
	}
}
