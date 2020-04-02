import heapq
import urllib.request
import requests

import nltk   

from bs4 import BeautifulSoup



class HtmlComponenets:
	title = ""
	header = ""
	paragraphs = []
	
	def __init__(title, header, paragraphs):
		self.title = title
		self.header
		self.paragraphs = paragraphs


def lre(url_queue, web_page, topic_vector, threshold):
	block_list = cbp(web_page)
	for block in block_list:
		block_vector = make_block_vector(block)
		s = similarity_cbp(block_vector, topic_vector)
		
		# relavant block (above threshold)
		if s > threshold:
			link_list = extract_links(block)
			for link in link_list:
				heapq.heappush(url_queue, (s, link))
		
		# not enough relavance (below threshold)
		else:
			temp_queue = extract_AT_and_LC(web_page)
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
	pass
	'''
	
	res = requests.get(url)
	html_page = res.content
	
	
	'''

	
'''
Function: parse HTML to get title, header, and paragraphs
Returns: HtmlComponenets
'''	
def parse_html(html_page): pass
	
	
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
	
	# Step 2.1: Parse the HTML
	html_components = parse_html(html)
	
	# TODO -- unknown
	topic_vector = ?
	threshold = ?
	
	
	url_queue = lre(url_queue, html, topic_vector, threshold)
	crawl(url_queue)
	




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