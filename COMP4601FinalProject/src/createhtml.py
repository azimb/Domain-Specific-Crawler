import webbrowser
import os

def make_html(topic, urls):
	html_output = "<!DOCTYPE html>"  + "<html>"   + "<body>" + "<h2>Links relevant to " + topic + "</h2>"
	html_output += "<ul>"
	
	for url in urls:
		html_output += "<li><a href=\"{}\">{}</a></li>".format(url, url)

	html_output += "</ul>"

	html_output += "</body></html>"
		
	f = open("crawl-output.html", "w+")
	f.write(html_output)
	f.close()
	
def open_browser_with_urls(topic, urls):
	make_html(topic, urls)
	webbrowser.open('file://' + os.path.abspath("crawl-output.html"))
	