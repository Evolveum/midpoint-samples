#!/bin/bash
midpointURL="${1:-http://localhost:8080}"
ls lookuptables | while read line
do
	echo -n "lookuptables/${line} .:. "
	grep "<name>" "lookuptables/${line}" | head -1 | cut -d ">" -f 2 | cut -d "<" -f 1
	curl --user "administrator:5ecr3t" -H "Content-Type: application/xml" -X POST ${midpointURL}/midpoint/ws/rest/lookupTables --data-binary @lookuptables/${line} -s -D /dev/tty >/dev/null
done
ls resources | while read line
do
	echo -n "resources/${line} .:. "
	grep "<name>" "resources/${line}" | head -1 | cut -d ">" -f 2 | cut -d "<" -f 1
	curl --user "administrator:5ecr3t" -H "Content-Type: application/xml" -X POST ${midpointURL}/midpoint/ws/rest/resources?options=raw --data-binary @resources/${line} -s -D /dev/tty >/dev/null
done
ls archetypes | while read line
do
	echo -n "archetypes/${line} .:. "
	grep "<name>" "archetypes/${line}" | head -1 | cut -d ">" -f 2 | cut -d "<" -f 1
	curl --user "administrator:5ecr3t" -H "Content-Type: application/xml" -X POST ${midpointURL}/midpoint/ws/rest/archetypes --data-binary @archetypes/${line} -s -D /dev/tty >/dev/null
done
ls roles | while read line
do
	echo -n "roles/${line} .:. "
	grep "<name>" "roles/${line}" | head -1 | cut -d ">" -f 2 | cut -d "<" -f 1
	curl --user "administrator:5ecr3t" -H "Content-Type: application/xml" -X POST ${midpointURL}/midpoint/ws/rest/roles --data-binary @roles/${line} -s -D /dev/tty >/dev/null
done
ls tasks | while read line
do
	echo -n "tasks/${line} .:. "
	grep "<name>" "tasks/${line}" | head -1 | cut -d ">" -f 2 | cut -d "<" -f 1
	curl --user "administrator:5ecr3t" -H "Content-Type: application/xml" -X POST ${midpointURL}/midpoint/ws/rest/tasks --data-binary @tasks/${line} -s -D /dev/tty >/dev/null
done
