package mp4.util.atom;

public class CprtgrpAtom extends AppleMetaAtom {

	public CprtgrpAtom(CprtgrpAtom old) {
		super(old);
	}

	public CprtgrpAtom() {
		super(new byte[] { Atom.COPYRIGHT_BYTE_VALUE, 'g', 'r', 'p' } );
	}
	
	public String getGrouping() {
		return getStringMetadata();
	}

}
