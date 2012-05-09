.phony: compile deploy

deploy:
	ant && \cp -f build/lib/tweetmycity.war /opt/wm/apache-tomcat-5.5.17/webapps/

compile:
	ant

