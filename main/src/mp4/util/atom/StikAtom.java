package mp4.util.atom;

public class StikAtom extends AppleMetaAtom {

	public StikAtom(StikAtom old) {
		super(old);
	}

	public StikAtom() {
		super(new byte[] { 's', 't', 'i', 'k' } );
	}
}
