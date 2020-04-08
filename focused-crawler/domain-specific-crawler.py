import heapq
import urllib.request
import requests

import nltk   

import math

from bs4 import BeautifulSoup

from vocab import *


'''

# function from the paper (wtu)
def get_term_weight(unit, doc_vocabulary, ftu):
	weight = 0.0 # this will be the output
	 
	 N = ? # number of feature collections
	 nt = ? # number of units where term occurs
	 
	
	num = ftu * math.log(N/nt)
	denom = 0.0
	for (term, freq) in doc_vocabulary.items():
		fru = ? # number of times term occurs in the unit/para
		denom += ((fru* math.log(N/nt))**2)
	
	denom = math.sqrt(denom)
	
	return num/denom
	


# input: para as a string
def make_unit_vector(unit, doc_vocabulary):
	
	
	ftu = ? # freq of term in the unit
	
	
	get_term_weight(unit, doc_vocabulary, ftu)

'''	
	

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

	
	print("paragraphs")
	block_list = retrieve_content_blocks(html)
	for b in block_list:
		print(b.get_text())
		print(b.get_text().split())
		
		#print(b.getText())
		print("-----------")
	
	
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
	
	# add seed with highest priority
	seed = url = "http://kite.com"
	seed = "https://en.wikipedia.org/wiki/Pok%C3%A9mon"
	heapq.heappush(url_queue, (1.0, seed))
	
	
	crawl(url_queue)
	
main()