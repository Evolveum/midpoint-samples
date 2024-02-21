#!/bin/bash
midpointURL="${1:-http://localhost:8080}"
ls resources | while read line
do
	echo -n "resources/${line} .:. "
	grep "<name>" "resources/${line}" | head -1 | cut -d ">" -f 2 | cut -d "<" -f 1
	curl --user "administrator:5ecr3t" -H "Content-Type: application/xml" -X POST ${midpointURL}/midpoint/ws/rest/resources?options=raw --data-binary @resources/${line} -s -D /dev/tty >/dev/null
done
ls tasks | while read line
do
	echo -n "tasks/${line} .:. "
	grep "<name>" "tasks/${line}" | head -1 | cut -d ">" -f 2 | cut -d "<" -f 1
	curl --user "administrator:5ecr3t" -H "Content-Type: application/xml" -X POST ${midpointURL}/midpoint/ws/rest/tasks --data-binary @tasks/${line} -s -D /dev/tty >/dev/null
done
