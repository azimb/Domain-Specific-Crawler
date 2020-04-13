from bs4 import BeautifulSoup
import urllib.request
from vocab import *
from utilities import * 
import numpy as np
import re
from unidecode import unidecode
import requests
import heapq
import nltk 
import math
import matplotlib.pyplot as plt	


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



def getTermFrequency(term, wordList):
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
		
		ftu = getTermFrequency(word, wordList)
		nt = determineWordBlockFrequency(block_list, word)
		N = len(block_list)
		
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
	
def similarity_cbp(unitVector, topicVector):
	#(u*v)/|u|*|v|
	return (np.matmul(unitVector, topicVector) / (unitVector.size * topicVector.size))
	
	
def lpe(url_queue, html, topic_vector, doc_vocabulary, threshold, url):
	#block_list = cbp(web_page)
	block_list = retrieve_content_blocks(html)
	
	sim = []
	
	most_relavant = []
	heapq.heapify(most_relavant)
	
	for block in block_list:
		# hashmap
		block_vector = make_unit_vector(block_list, block, doc_vocabulary)
		if "pokemon" in block_vector: print( "weight of pokemon: ",  block_vector["pokemon"])
		# actual vector
		block_vector = convertToVector(block_vector, doc_vocabulary)
		
		s = similarity_cbp(block_vector, topic_vector)
		
		sim.append(s)
		
		print("Similarity score: {}".format(s))
		print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
		# relavant block (above threshold)
		
		
		link_list = extract_links(url, block)
		
		heapq.heappush(most_relavant, (-s, link_list))
		
		
		'''
		if s > threshold:
			link_list = extract_links(url, block)
			for link in link_list:
				heapq.heappush(url_queue, (-s, link))
		
		
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
				
				
					heapq.heappush(url_queue, (s, link))'''
	#x = [ i for i in range(len(sim)) ]
	
	#plt.plot[x,sim]
	#plt.show()
	
	
	# CHECK
	# grab the best 5 paragraphs
	count = 1
	while most_relavant:
		pair = heapq.heappop(most_relavant)
		score, links = pair[0], pair[1]
		for link in link_list:
			heapq.heappush(url_queue, (score, link))
		count += 1
		if count == 5: break
	
	
	print("url queue: ", url_queue)
	
	#return url_queue
		
				
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

def convertToVector(unitMap, vocab):
	unitVector = []
	for word in vocab: 
		if word not in unitMap: unitVector.append(0)
		else: unitVector.append( unitMap[word] )
	return np.array(unitVector)
	
	
def make_topic_vector(vocabulary, topic):
	topic_vector = []
	topic_array = topic.split()
	for v in vocabulary:
		if v in topic_array: topic_vector.append(1)
		else: topic_vector.append(0)
	
	return np.array(topic_vector)

'''
Function: to perfor the core crawling
Parameters:
	url_queue -- type: priority queue

'''
def crawl(url_queue, topic):

	# Step 1:  Dequeues the next URL
	pair = heapq.heappop(url_queue)
	priority, url = -(pair[0]), pair[1]
	
	# Step 2: Fetch the HTML of the url
	html = download_html(url)

	vocabulary = get_vocab(html)
	M = sum(vocabulary.values())
	topic_vector = make_topic_vector(vocabulary, topic)
	threshold = 0
	
	#print("topic vector: ")
	#print(topic_vector)
	#print("Topic vector size: {}".format(len(topic_vector)))
	#print("Vocabulary size: {}".format(len(vocabulary)))
	#url_queue = lpe(url_queue, html, topic_vector, vocabulary, threshold)
	lpe(url_queue, html, topic_vector, vocabulary, threshold, url)
	
	
	
	
	
	'''
	crawl(url_queue)
	'''


def main():
	# initialize empty priority queue
	url_queue = []
	heapq.heapify(url_queue)
	
	topic = input("Please enter a domain/topic to search:\n")
	print("Searching for webpages pertaining to {}.".format(topic))
	# add seed with highest priority
	#z = “In the basket are %s and %s” % (x,y)
	seed = url = "http://kite.com"
	seed = "https://en.wikipedia.org/wiki/Pok%C3%A9mon"
	heapq.heappush(url_queue, (1.0, seed))
	
	
	crawl(url_queue, topic)
	
main()