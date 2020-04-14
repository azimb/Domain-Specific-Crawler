from bs4 import BeautifulSoup
import urllib.request
from vocab import *
from utilities import * 
from createhtml import *
import numpy as np
import re
from unidecode import unidecode
import requests
import heapq
import nltk 
import math

'''
Normalize terms
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
	
	
from collections import Counter
def make_unit_vector(block_list, block, doc_vocabulary):	
	wordList = block.get_text().split()
	unitVector = {}
	
	lemmatizer = WordNetLemmatizer()
	
	wordListMap = Counter(wordList)
	
	for word in wordList:
		'''Calculate word weight in this contentblock'''
		
		word = normalize(word)
		
		ftu = getTermFrequency(word, wordList)
		nt = determineWordBlockFrequency(block_list, word)
		N = len(block_list)
		
		numerator = getNumerator(ftu, N, nt)
		denominator = getDenom(wordListMap, N, nt)
		unitVector[word] = numerator/denominator
		
	#print('Unit vector = {}'.format(unitVector))
	return unitVector 


def getNumerator(ftu, N, nt):
	numerator = (ftu * math.log10(N/nt))
	#print('Numerator = {}'.format(numerator))
	return numerator
	
def getDenom(wordListMap, N, nt):
	denom = 0
	'''Returns the value of the key word which is its frequency in the document vocab'''
	for word in wordListMap.keys():
		fru = wordListMap.get(word) 
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
	
	
def lpe(url_queue, html, topic_vector, doc_vocabulary, url):
	#print("Running LPE algorithm on ", url)
	block_list = retrieve_content_blocks(html)
	sim = []
	average_document_score = 0
	most_relavant = []
	most_relavant_p = []
	heapq.heapify(most_relavant)
	heapq.heapify(most_relavant_p)
	
	for block in block_list:
		#print("Handling content block...")
		# hashmap
		block_vector = make_unit_vector(block_list, block, doc_vocabulary)
		#if "pokemon" in block_vector: print( "weight of pokemon: ",  block_vector["pokemon"])
		# actual vector
		block_vector = convertToVector(block_vector, doc_vocabulary)
		
		s = similarity_cbp(block_vector, topic_vector)
		average_document_score += s
		sim.append(s)
		
		#print("Paragraph: ", block.get_text())
		#print("Similarity score: {}".format(s))
		#print("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~")
		
		
		link_list = extract_links(url, block)
		
		heapq.heappush(most_relavant, (-s, link_list))
		heapq.heappush(most_relavant_p, (-s, block.get_text()))
		
		#print("Done, moving onto next content block...")
		
	average_document_score = average_document_score/len(sim)
	#print("Average document score = ", average_document_score)
	if(average_document_score <= 0):
		#print("Document at url {} found to not be relevant.".format(url))
		return url_queue
	else: 
		indexed_crawled_urls.append(url)
		
	# grab the best 5 paragraphs
	count = 1
	while most_relavant:
		pair = heapq.heappop(most_relavant)
		score, links = pair[0], pair[1]
		for link in links:
			if "cite_note" not in link: heapq.heappush(url_queue, (score, link))
		count += 1		
		if count == 5: break
		
	#print("most relavant para")
	#print(heapq.heappop(most_relavant_p)[1])

	#print("Done running LPE algorithm on", url)
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
def crawl(url_queue, topic, num_pages):

	# check if we have crawled enough pages
	if  len(indexed_crawled_urls) == num_pages or not url_queue:
		print("Crawl complete.")
		return
			
			
			
	# Step 1:  Dequeues the next URL
	pair = heapq.heappop(url_queue)
	priority, url = -(pair[0]), pair[1]
	print("Crawling ", url)
	
	
	# Step 2: Fetch the HTML of the url
	html = download_html(url)

	vocabulary = get_vocab(html)
	M = sum(vocabulary.values())
	topic_vector = make_topic_vector(vocabulary, topic)
	
	#print("topic vector: ")
	#print(topic_vector)
	#print("Topic vector size: {}".format(len(topic_vector)))
	#print("Vocabulary size: {}".format(len(vocabulary)))
	url_queue = lpe(url_queue, html, topic_vector, vocabulary, url)
	#print("url queue: ", url_queue)
	
	crawl(url_queue, topic, num_pages)
	

def checkIfSiteExists(url):
	req = urllib.request.Request(url)
	try: 
		urllib.request.urlopen(req)
	except urllib.error.HTTPError as e:
		print(e.code)
		return False
	return True

def main():
	# initialize empty priority queue
	url_queue = []
	heapq.heapify(url_queue)
	
	global topic
	topic = input("Please enter a domain/topic to search:\n")
	if(len(topic.split()) > 1):
		print("The search domain should be a single term!")
		main()
		
	num_pages = int(input("How many pages would you like to crawl?: "))
	if(num_pages < 1 or num_pages > 100):
		print("Invalid number of pages to crawl, must be > 0 and < 100")
		main()
	
	# add seed with highest priority
	#z = “In the basket are %s and %s” % (x,y)
	seed = "https://en.wikipedia.org/wiki/"
	seed = "".join((seed, topic))
	alternativeSeed = "www." + topic + ".com"
	valid=checkIfSiteExists(seed) #need to make a function that tries to connect, if a connection is possible continue.
	if valid==True:
		print("Searching for webpages pertaining to {}.".format(topic))
		heapq.heappush(url_queue, (1.0, seed))
		crawl(url_queue, topic, num_pages)
	else:
		print("Invalid url, please enter a valid domain.")
		main()

topic = ""
indexed_crawled_urls = []
main()
print("crawled urls: ")
for cu in indexed_crawled_urls: print(cu)
open_browser_with_urls(topic, indexed_crawled_urls)
