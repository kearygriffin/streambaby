package mp4.util.atom;

public class DiskAtom extends AppleMetaAtom {

	public DiskAtom(DiskAtom old) {
		super(old);
	}

	public DiskAtom() {
		super(new byte[] { 'd', 'i', 's', 'k' } );
	}
}
