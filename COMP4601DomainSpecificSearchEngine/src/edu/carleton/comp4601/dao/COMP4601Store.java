package edu.carleton.comp4601.dao;

import java.util.List;

import edu.carleton.comp4601.model.COMP4601Document;

public interface COMP4601Store {
	void create(COMP4601Document v);
	COMP4601Document find(int docid);
	List<COMP4601Document> findAll();
}
