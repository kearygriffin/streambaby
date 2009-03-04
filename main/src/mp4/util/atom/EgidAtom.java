package mp4.util.atom;

public class EgidAtom extends AppleMetaAtom {

	public EgidAtom(EgidAtom old) {
		super(old);
	}

	public EgidAtom() {
		super(new byte[] { 'e', 'g', 'i', 'd' } );
	}
}
