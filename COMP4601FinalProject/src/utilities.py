from bs4 import BeautifulSoup

import urllib.request

import re
from urllib.parse import urlparse

def construct_url(parent_url, incomplete_url):
	
	parsed_uri = urlparse(parent_url)
	result = '{uri.scheme}://{uri.netloc}/'.format(uri=parsed_uri)
	result = result[:-1]
	result += incomplete_url
	return result
	

def extract_links(parent_url, block):
	
	link_list = []
	soup = BeautifulSoup(str(block), "html.parser")
	for link in soup.findAll('a', attrs={'href': re.compile("^")}):
	
		link = link.get('href')
		if not link.startswith("https://") and not link.startswith("http://"):
			link = construct_url(parent_url, link)
		link_list.append(link)
	
	return link_list
	
