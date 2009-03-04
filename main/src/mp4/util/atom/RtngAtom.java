package mp4.util.atom;

public class RtngAtom extends AppleMetaAtom {

	public RtngAtom(RtngAtom old) {
		super(old);
	}

	public RtngAtom() {
		super(new byte[] { 'r', 't', 'n', 'g' } );
	}
}
