from collections import Counter
from bs4.element import Comment
from bs4 import BeautifulSoup
import urllib.request
import requests
import unidecode
import nltk
from nltk.stem import WordNetLemmatizer
import re
from collections import OrderedDict

def tag_visible(element):
    if element.parent.name in ['style', 'script', 'head', 'title', 'meta', '[document]']:
        return False
    if isinstance(element, Comment):
        return False
    return True

def text_from_html(body):
    soup = BeautifulSoup(body, 'html.parser')
    texts = soup.findAll(text=True)
    visible_texts = filter(tag_visible, texts)  
    return u" ".join(t.strip() for t in visible_texts)

def get_pairs(html):
	lemmatizer = WordNetLemmatizer()
	visible_text_string = text_from_html(html)
	lst = re.findall(r'\b\w+', visible_text_string)
	
	occs = {}
	for elem in lst:
		elem = unidecode.unidecode(elem)
		elem = elem.lower()
		elem = lemmatizer.lemmatize(elem)
		if elem not in occs: occs[elem] = 1
		else: occs[elem] = occs[elem] + 1
	orderedDict = OrderedDict(occs)
	return orderedDict

def retrieve_content_blocks(html): 
	paragraphs = []
	soup = BeautifulSoup(html, "html.parser")
	for p in soup.find_all('p'): paragraphs.append(p)
	return paragraphs
	
def make_vocab(html):
	vocab = set([])
	
	for p in retrieve_content_blocks(html):
		for w in p.getText().split(): 
			
			vocab.add(w)
	
	return list(vocab)

def get_vocab(html):
	hashmap = get_pairs(html)
	return hashmap

