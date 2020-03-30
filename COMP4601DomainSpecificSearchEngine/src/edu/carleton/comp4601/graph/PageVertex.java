package edu.carleton.comp4601.graph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import edu.carleton.comp4601.model.COMP4601Document;

@XmlRootElement
public class PageVertex implements Serializable {
	private static final long serialVersionUID = 1L;

	COMP4601Document doc;

	public PageVertex(COMP4601Document d) {
		this.doc = d;
	}

	@Override
	public String toString() {
		return "PageVertex [doc=" + doc + "]";
	}

	public COMP4601Document getDoc() {
		return doc;
	}

	public void setDoc(COMP4601Document doc) {
		this.doc = doc;
	}

}
