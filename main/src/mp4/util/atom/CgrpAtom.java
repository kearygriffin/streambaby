package mp4.util.atom;

public class CgrpAtom extends AppleMetaAtom {

	public CgrpAtom(CgrpAtom old) {
		super(old);
	}

	public CgrpAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 'g', 'r', 'p' } );
	}
	
	public String getGrouping() {
		return getStringMetadata();
	}

}
