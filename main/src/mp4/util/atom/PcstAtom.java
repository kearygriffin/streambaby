package mp4.util.atom;

public class PcstAtom extends AppleMetaAtom {

	public PcstAtom(PcstAtom old) {
		super(old);
	}

	public PcstAtom() {
		super(new byte[] { 'p', 'c', 's', 't' } );
	}
}
