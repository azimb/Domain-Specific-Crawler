package edu.carleton.comp4601.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class COMP4601Document implements Serializable {
	private static final long serialVersionUID = 1L;

	int docid;
	float score;
	String url;
	ArrayList<String> links;
	String indexedBy;
	Date date;
	String content;
	Map<String, String> metadata;
	String type;
	String parent;

	public String getParent() {
		return parent;
	}

	@Override
	public String toString() {
		return "COMP4601Document [docid=" + docid + ", url=" + url + "]";
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public COMP4601Document() {
	}

	public COMP4601Document(int docid, String url) {
		this.docid = docid;
		this.url = url;
		this.metadata = new HashMap<String, String>();
	}

	public void addToMetaData(String s1, String s2) {
		metadata.put(s1, s2);
	}

	public int getDocid() {
		return docid;
	}

	public void setDocid(int docid) {
		this.docid = docid;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getIndexedBy() {
		return indexedBy;
	}

	public void setIndexedBy(String indexedBy) {
		this.indexedBy = indexedBy;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ArrayList<String> getLinks() {
		return links;
	}

	public void setLinks(ArrayList<String> links) {
		this.links = links;
	}
}
