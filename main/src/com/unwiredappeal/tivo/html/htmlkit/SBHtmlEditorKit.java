package com.unwiredappeal.tivo.html.htmlkit;

import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.text.html.HTMLEditorKit.Parser;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;

@SuppressWarnings("serial")
public class SBHtmlEditorKit extends HTMLEditorKit {
	public String baseUrl = null;
	public SBHtmlEditorKit(String baseUrl) {
		this.baseUrl = baseUrl;
	}
    /**
     * Create an uninitialized text storage model
     * that is appropriate for this type of editor.
     *
     * @return the model
     */
	@Override
    public Document createDefaultDocument() {
	StyleSheet styles = getStyleSheet();
	StyleSheet ss = new StyleSheet();

	ss.addStyleSheet(styles);

	HTMLDocument doc = new HTMLDocument(ss);
	resetBase(doc);
	doc.setParser(getParser());
	doc.setAsynchronousLoadPriority(-1);
	//doc.setAsynchronousLoadPriority(4);
	doc.setTokenThreshold(100);
	return doc;
    }
	
	@Override
    /**
     * Inserts content from the given stream. If <code>doc</code> is
     * an instance of HTMLDocument, this will read
     * HTML 3.2 text. Inserting HTML into a non-empty document must be inside
     * the body Element, if you do not insert into the body an exception will
     * be thrown. When inserting into a non-empty document all tags outside
     * of the body (head, title) will be dropped.
     * 
     * @param in  the stream to read from
     * @param doc the destination for the insertion
     * @param pos the location in the document to place the
     *   content
     * @exception IOException on any I/O error
     * @exception BadLocationException if pos represents an invalid
     *   location within the document
     * @exception RuntimeException (will eventually be a BadLocationException)
     *            if pos is invalid
     */
    public void read(Reader in, Document doc, int pos) throws IOException, BadLocationException {

	if (doc instanceof HTMLDocument) {
	    HTMLDocument hdoc = (HTMLDocument) doc;
	    Parser p = getParser();
	    if (p == null) {
		throw new IOException("Can't load parser");
	    }
	    if (pos > doc.getLength()) {
		throw new BadLocationException("Invalid location", pos);
	    }

	    ParserCallback receiver = hdoc.getReader(pos);
	    Boolean ignoreCharset = (Boolean)doc.getProperty("IgnoreCharsetDirective");
	    p.parse(in, receiver, (ignoreCharset == null) ? false : ignoreCharset.booleanValue());
	    receiver.flush();
	    resetBase(hdoc);
	} else {
	    super.read(in, doc, pos);
	}
    }

	public void resetBase(HTMLDocument doc) {
		if (baseUrl != null)
			try {
				doc.setBase(new URL(baseUrl));
			} catch (MalformedURLException e) {
			}
	}

}
