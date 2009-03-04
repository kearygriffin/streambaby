package mp4.util.atom;

public class PurdAtom extends AppleMetaAtom {

	public PurdAtom(PurdAtom old) {
		super(old);
	}

	public PurdAtom() {
		super(new byte[] { 'p', 'u', 'r', 'd' } );
	}
	
	public String getPurchaseData() {
		return getStringMetadata();
	}

}
