package mp4.util.atom;

public class CcmtAtom extends AppleMetaAtom {

	public CcmtAtom(CcmtAtom old) {
		super(old);
	}

	public CcmtAtom() {
		super(new byte[] { 'C', 'c', 'm', 't' } );
	}
	
	public String getComment() {
		return getStringMetadata();
	}

}
