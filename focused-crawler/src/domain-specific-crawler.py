from bs4 import BeautifulSoup
import urllib.request
from vocab import *
import numpy as np
import re
from unidecode import unidecode
import requests
import heapq
import nltk 
import math


'''
Normaloze terms
'''

CLEAN_CHARS = ['+', '=', '^', '*', '~', '#', '_', '\\']
CLEAN_CHARS += ['(', ')', '[', ']', '{', '}', '<', '>']
CLEAN_CHARS += ['\'', '"', '`', '%']
clean_rx = '[' + re.escape(''.join(CLEAN_CHARS)) + ']'
def clean(s):
	'''
	Clean string by removing unwanted characters.
	'''
	s = re.sub(clean_rx, u'', s)
	s = u' '.join(s.split())
	return s

NORM_CHARS = ['.', ',', ':', '?', '!', ';', '-', '/', '|', '&']
norm_rx = '[' + re.escape(''.join(NORM_CHARS)) + ']'
def normalize(s):
	lemmatizer = WordNetLemmatizer()
	s = lemmatizer.lemmatize(s)
	
	'''
	Normalize string by removing punctuation, capitalization, diacritics.
	'''
	# Replace diactritics
	s = unidecode(s)
	# Remove unwanted characters
	s = clean(s)
	# Remove capitalization
	s = s.lower()
	# Replace regular punctuation by spaces
	s = re.sub(norm_rx, u' ', s)
	s = u' '.join(s.split())
	# Remove double consonants
	if len(s) >= 2 and s[-1] == s[-2]:
		s = s[:-1]
	return s



def getTermFrequency(block, term, wordList):
	count = 0
	#wordList = block.get_text().split()
	for word in wordList:
	
		# normalize
		word = normalize(word)
	
		if(word == term):
			count += 1
	
	return count
	
def make_unit_vector(block_list, block, doc_vocabulary):	
	wordList = block.get_text().split()
	unitVector = {}
	
	lemmatizer = WordNetLemmatizer()
	
	for word in wordList:
		'''Calculate word weight in this contentblock'''
		#print('Calculating unit vector for word = {}'.format(word))
		'''
		word = unidecode.unidecode(word)
		word = word.lower()
		word = lemmatizer.lemmatize(word)
		'''
		
		word = normalize(word)
		
		ftu = getTermFrequency(block, word, wordList)
		nt = determineWordBlockFrequency(block_list, word)
		N = len(block_list)
		M = sum(doc_vocabulary.values())
		numerator = getNumerator(ftu, N, nt)
		denominator = getDenom(doc_vocabulary, N, nt)
		unitVector[word] = numerator/denominator
		
	#print('Unit vector = {}'.format(unitVector))
	return unitVector 


def getNumerator(ftu, N, nt):
	numerator = (ftu * math.log10(N/nt))
	#print('Numerator = {}'.format(numerator))
	return numerator
	
def getDenom(doc_vocabulary, N, nt):
	denom = 0
	'''Returns the value of the key word which is its frequency in the document vocab'''
	for word in doc_vocabulary.keys():
		fru = doc_vocabulary.get(word) 
		denom += math.pow((fru * math.log10(N/nt)), 2)
		
	denom = math.sqrt(denom)
	#print('Denominator = {}'.format(denom))
	return denom
	
def determineWordBlockFrequency(block_list, term):
	count = 0
	for b in block_list:
		if(checkBlockForWord(b, term)):
			count += 1
			
	return count
		

def checkBlockForWord(block, term):
	wordList = block.get_text().split()
	for word in wordList:
		
		# normalize
		word = normalize(word)
		
		if(word == term):
			return True
			
	return False
def lpe(url_queue, html, topic_vector, doc_vocabulary, threshold):
	#block_list = cbp(web_page)
	block_list = retrieve_content_blocks(html)
	for block in block_list:
		block_vector = make_unit_vector(block, doc_vocabulary)
		s = similarity_cbp(block_vector, topic_vector)
		
		# relavant block (above threshold)
		if s > threshold:
			link_list = extract_links(block)
			for link in link_list:
				heapq.heappush(url_queue, (s, link))
		
		# not enough relavance (below threshold)
		else:
			temp_queue = extract_AT_and_LC(html)
			for link in temp_queue:
				AT_vector = make_AT_vector(link)
				LC_vector = make_LC_vector(link)
				s = similarity_jfe(AT_vector, LC_vector)
				
				if s > threshold:
					heapq.heappush(url_queue, (s, link))
	
	return url_queue
		
				
'''
Function: visit the URL, and download HTML
Parameters:
	- url
Returns: html page
'''
def download_html(url):
	response = urllib.request.urlopen(url)
	html = response.read()	
	return html

	
	
'''
Function: to perfor the core crawling
Parameters:
	url_queue -- type: priority queue

'''
def crawl(url_queue):

	# Step 1:  Dequeues the next URL
	pair = heapq.heappop(url_queue)
	priority, url = pair[0], pair[1]
	
	# Step 2: Fetch the HTML of the url
	html = download_html(url)

	vocabulary = get_vocab(html)
	
	topic_vector = ?
	threshold = ?
	
	url_queue = lpe(url_queue, html, topic_vector, vocabulary, threshold)
	
	
	
	#print("paragraphs")
	block_list = retrieve_content_blocks(html)
	unitVectorList = []
	for index, b in enumerate(block_list):
		#print(b.get_text())
		'''print(b.get_text().split())'''
		#print(b.getText())
		#print("-----------")
		unitVectorList.append(make_unit_vector(block_list, b, vocabulary))
		'''unitVectorList should have the same exact size as block_list and contain the unit vector for each block so unitVectorList[3] will be the unit vector for block_list[3]'''
	print(unitVectorList)
	
	'''
	# TODO -- unknown
	topic_vector = ?
	threshold = ?
	
	url_queue = lpe(url_queue, html, topic_vector, vocabulary, threshold)
	crawl(url_queue)
	'''


def main():
	# initialize empty priority queue
	url_queue = []
	heapq.heapify(url_queue)
	
	topic = input("Please enter a domain/topic to search:\n")
	print("Searching for webpages pertaining to {}.".format(topic))
	# add seed with highest priority
	seed = url = "http://kite.com"
	seed = "https://en.wikipedia.org/wiki/Pok%C3%A9mon"
	heapq.heappush(url_queue, (1.0, seed))
	
	
	crawl(url_queue)
	
main()