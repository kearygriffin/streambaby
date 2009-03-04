package mp4.util.atom;

public class TrknAtom extends AppleMetaAtom {

	public TrknAtom(TrknAtom old) {
		super(old);
	}

	public TrknAtom() {
		super(new byte[] { 't', 'r', 'k', 'n' } );
	}
}
