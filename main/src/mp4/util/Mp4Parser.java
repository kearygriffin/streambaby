package mp4.util;

import java.io.DataInputStream;
import java.io.IOException;

import mp4.util.atom.Atom;
import mp4.util.atom.AtomException;
import mp4.util.atom.ContainerAtom;
import mp4.util.atom.DefaultAtomVisitor;
import mp4.util.atom.FtypAtom;
import mp4.util.atom.MdatAtom;
import mp4.util.atom.MoovAtom;
import mp4.util.atom.UnknownAtom;

public class Mp4Parser extends DefaultAtomVisitor {
	protected FtypAtom ftyp;
	protected MoovAtom moov;
	protected MdatAtom mdat;
	protected DataInputStream mp4file;
	boolean lastAtom = false;
	private long lastAtomOffset = 0;

	protected Mp4Parser() {
		
	}
	public Mp4Parser(DataInputStream mp4file) {
		this.mp4file = mp4file;
	}
	  
	  @Override
	  protected void defaultAction(Atom atom) throws AtomException {
	    
		// Even some (hybrid) containers have data, so read it
		atom.readData(mp4file);
		if (atom.isContainer()) {
	      long bytesRead = 0;
	      long bytesToRead = atom.dataSize() - atom.pureDataSize();
	      while (bytesRead < bytesToRead) {
	        Atom child = parseAtom();
	        ((ContainerAtom)atom).addChild(child);
	        bytesRead += child.size();
	      }
	    }
	  }

	  /**
	   * Don't the the mdat atom since that's the biggest segment of the 
	   * file.  It contains the video and sound data.  Plus, we'll just
	   * skip over the beginning when we cut the movie.
	   */
	  @Override
	  public void visit(MdatAtom atom) throws AtomException {
	    atom.setInputStream(mp4file);
	    try {
	    	if (atom.dataSize() != 0 && mp4file.markSupported())
	    		mp4file.skip(atom.dataSize());
	    	else
	    		lastAtom = true;
	    } catch(IOException e) {
	    	throw new AtomException(e.getMessage());
	    }
	  }
	  
		/**
		 * Parse an atom from the mpeg4 file.
		 * 
		 * @return the number of bytes read
		 * @throws AtomException
		 */
		private Atom parseAtom() throws AtomException {
			// get the atom size
			if (lastAtom)
				throw new AtomException("Already parsed last atom!");
			byte[] word = new byte[Atom.ATOM_WORD];
			int num;
			try {
				num = mp4file.read(word);
			} catch (IOException e1) {
				throw new AtomException("IOException while reading file");
			}
			// check for end of file
			if (num == -1) {
				return null;
			}
			if (num != Atom.ATOM_WORD) {
				throw new AtomException("Unable to read enough bytes for atom");
			}
			long size = Atom.byteArrayToUnsignedInt(word, 0);
			// get the atom type
			try {
				num = mp4file.read(word);
			} catch (IOException e1) {
				throw new AtomException("IOException while reading file");
			}
			if (num != Atom.ATOM_WORD) {
				throw new AtomException("Unable to read enough bytes for atom");
			}
			try {
				Atom atom;
				MP4Log.log("Reading atom at offset: " + lastAtomOffset);
				try {
					Class<?> cls = Class.forName(Atom.typeToClassName(word));
					atom = (Atom) cls.newInstance();
					MP4Log.log("AtomClass: " + cls + " (size:" + size + ")");
				} catch (ClassNotFoundException e) {
					MP4Log.log("UnknownAtom(" + ((int)word[0]&0xff) + "," + ((int)word[1]&0xff) + "," + ((int)word[2]&0xff) + "," + ((int)word[3]&0xff) + "): " + Atom.typeToClassName(word) +  " (size:" + size + ")");
					//if (word[2] == 'h' && word[3] == 'd')
						//atom = new UnkHdAtom(word);
					//else
						atom = new UnknownAtom(word);
				}
				lastAtomOffset += size;
				
				atom.setSize(size);
				atom.accept(this);
				return atom;
			} catch (InstantiationException e) {
				throw new AtomException("Unable to instantiate atom");
			} catch (IllegalAccessException e) {
				throw new AtomException("Unabel to access atom object");
			}
		}
		
		public long parseMp4() throws AtomException, IOException {
			long mdatOffset = 0;
			long offset = 0;
			while (ftyp == null || moov == null || mdat == null) {
				Atom atom = parseAtom();
				if (atom == null) {
					throw new IOException("Couldn't find all required MP4 atoms");
				}
				if (atom instanceof FtypAtom)
					ftyp = (FtypAtom) atom;
				else if (atom instanceof MoovAtom)
					moov = (MoovAtom) atom;
				else if (atom instanceof MdatAtom) {
					mdatOffset = offset;
					mdat = (MdatAtom) atom;
				}
				offset += atom.size();
			}
			return mdatOffset;
		}
		
		public MoovAtom getMoov() {
			return moov;
		}


}
